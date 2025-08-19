const express = require("express");
const mongoose = require("mongoose");
const Leave = require("../models/Leave");
const Employee = require("../models/Employee");
const { authenticateToken, authorize } = require("../middleware/auth");
const {
  calculateBusinessDays,
  datesOverlap,
  isValidDateRange,
} = require("../utils/dateHelpers");
const router = express.Router();

router.post("/", authenticateToken, async (req, res) => {
  try {
    const employeeId = req.user._id;
    const { startDate, endDate, reason } = req.body;

    if (!startDate || !endDate || !reason) {
      return res.status(400).json({
        success: false,
        message: "All fields are required: startDate, endDate, reason",
      });
    }

    const employee = await Employee.findById(employeeId);
    if (!employee) {
      return res.status(404).json({
        success: false,
        message: "Employee not found",
      });
    }

    if (!isValidDateRange(startDate, endDate)) {
      return res.status(400).json({
        success: false,
        message: "End date must be after or equal to start date",
      });
    }

    if (new Date(startDate) < employee.joiningDate) {
      return res.status(400).json({
        success: false,
        message: "Cannot apply for leave before joining date",
      });
    }

    const daysRequested = calculateBusinessDays(startDate, endDate);

    if (daysRequested > employee.leaveBalance) {
      return res.status(400).json({
        success: false,
        message: `Insufficient leave balance. Requested: ${daysRequested} days, Available: ${employee.leaveBalance} days`,
      });
    }

    const overlappingLeaves = await Leave.find({
      employeeId,
      status: { $in: ["pending", "approved"] },
      $or: [
        {
          startDate: { $lte: new Date(endDate) },
          endDate: { $gte: new Date(startDate) },
        },
      ],
    });

    if (overlappingLeaves.length > 0) {
      return res.status(400).json({
        success: false,
        message: "Leave request overlaps with existing pending/approved leave",
        conflictingLeaves: overlappingLeaves.map((leave) => ({
          id: leave._id,
          startDate: leave.startDate,
          endDate: leave.endDate,
          status: leave.status,
        })),
      });
    }

    const leave = new Leave({
      employeeId,
      startDate: new Date(startDate),
      endDate: new Date(endDate),
      reason,
      daysRequested,
    });

    await leave.save();
    await leave.populate("employeeId", "name email department leaveBalance");

    res.status(201).json({
      success: true,
      message: "Leave request submitted successfully",
      data: {
        leaveId: leave._id,
        employeeName: leave.employeeId.name,
        startDate: leave.startDate,
        endDate: leave.endDate,
        daysRequested: leave.daysRequested,
        reason: leave.reason,
        status: leave.status,
        appliedAt: leave.createdAt,
        currentBalance: leave.employeeId.leaveBalance,
      },
    });
  } catch (error) {
    console.error("Leave application error:", error);
    res.status(400).json({
      success: false,
      message: error.message || "Failed to submit leave request",
    });
  }
});

router.patch(
  "/:leaveId",
  authenticateToken,
  authorize("HR", "Admin"),
  async (req, res) => {
    const session = await mongoose.startSession();

    try {
      await session.startTransaction();

      const { leaveId } = req.params;
      const { status, reviewComments } = req.body;
      const reviewerId = req.user._id;

      if (!["approved", "rejected"].includes(status)) {
        await session.abortTransaction();
        return res.status(400).json({
          success: false,
          message: 'Status must be either "approved" or "rejected"',
        });
      }

      const leave = await Leave.findById(leaveId)
        .populate("employeeId")
        .session(session);

      if (!leave) {
        await session.abortTransaction();
        return res.status(404).json({
          success: false,
          message: "Leave request not found",
        });
      }

      if (reviewerId.toString() === leave.employeeId._id.toString()) {
        await session.abortTransaction();
        return res.status(400).json({
          success: false,
          message: "Employees cannot approve their own leave requests",
        });
      }

      if (leave.status !== "pending") {
        await session.abortTransaction();
        return res.status(400).json({
          success: false,
          message: `Leave request is already ${leave.status}`,
        });
      }

      leave.status = status;
      leave.reviewedBy = reviewerId;
      leave.reviewedAt = new Date();
      leave.reviewComments = reviewComments || "";

      let updatedEmployee = leave.employeeId;

      if (status === "approved") {
        updatedEmployee = await Employee.findOneAndUpdate(
          {
            _id: leave.employeeId._id,
            leaveBalance: { $gte: leave.daysRequested },
          },
          {
            $inc: { leaveBalance: -leave.daysRequested },
          },
          {
            new: true,
            session: session,
          }
        );

        if (!updatedEmployee) {
          await session.abortTransaction();
          return res.status(400).json({
            success: false,
            message: `Insufficient leave balance. Employee may have used leaves in another request.`,
          });
        }
      }

      await leave.save({ session });
      await session.commitTransaction();

      await leave.populate("employeeId", "name email department leaveBalance");

      res.json({
        success: true,
        message: `Leave request ${status} successfully`,
        data: {
          leaveId: leave._id,
          employeeName: leave.employeeId.name,
          status: leave.status,
          daysRequested: leave.daysRequested,
          reviewedAt: leave.reviewedAt,
          reviewComments: leave.reviewComments,
          remainingBalance:
            status === "approved"
              ? updatedEmployee.leaveBalance
              : leave.employeeId.leaveBalance,
        },
      });
    } catch (error) {
      await session.abortTransaction();
      console.error("Leave approval error:", error);
      res.status(400).json({
        success: false,
        message: error.message || "Failed to update leave request",
      });
    } finally {
      await session.endSession();
    }
  }
);

router.get("/", authenticateToken, async (req, res) => {
  try {
    const { status, page = 1, limit = 10, employeeId } = req.query;

    let filter = {};

    if (req.user.role === "Employee") {
      filter.employeeId = req.user._id;
    } else if (employeeId) {
      filter.employeeId = employeeId;
    }

    if (status) filter.status = status;

    const skip = (parseInt(page) - 1) * parseInt(limit);

    const leaves = await Leave.find(filter)
      .populate("employeeId", "name email department leaveBalance")
      .populate("reviewedBy", "name email")
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Leave.countDocuments(filter);

    res.json({
      success: true,
      data: leaves,
      pagination: {
        current: parseInt(page),
        pages: Math.ceil(total / parseInt(limit)),
        total,
        limit: parseInt(limit),
      },
    });
  } catch (error) {
    console.error("Fetch leaves error:", error);
    res.status(500).json({
      success: false,
      message: error.message || "Failed to fetch leave requests",
    });
  }
});

router.get("/:leaveId", authenticateToken, async (req, res) => {
  try {
    const leave = await Leave.findById(req.params.leaveId)
      .populate("employeeId", "name email department leaveBalance")
      .populate("reviewedBy", "name email");

    if (!leave) {
      return res.status(404).json({
        success: false,
        message: "Leave request not found",
      });
    }

    if (
      req.user.role === "Employee" &&
      leave.employeeId._id.toString() !== req.user._id.toString()
    ) {
      return res.status(403).json({
        success: false,
        message: "Access denied. You can only view your own leave requests.",
      });
    }

    res.json({
      success: true,
      data: leave,
    });
  } catch (error) {
    console.error("Fetch single leave error:", error);
    res.status(500).json({
      success: false,
      message: error.message || "Failed to fetch leave request",
    });
  }
});

module.exports = router;
