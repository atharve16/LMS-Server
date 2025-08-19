const express = require("express");
const Employee = require("../models/Employee");
const Leave = require("../models/Leave");
const router = express.Router();

router.post("/", async (req, res) => {
  try {
    const { name, email, department, joiningDate } = req.body;

    const existingEmployee = await Employee.findOne({ email });
    if (existingEmployee) {
      return res.status(400).json({
        success: false,
        message: "Employee with this email already exists",
      });
    }

    const employee = new Employee({
      name,
      email,
      department,
      joiningDate,
    });

    await employee.save();

    res.status(201).json({
      success: true,
      message: "Employee added successfully",
      data: {
        employeeId: employee._id,
        name: employee.name,
        email: employee.email,
        department: employee.department,
        joiningDate: employee.joiningDate,
        leaveBalance: employee.leaveBalance,
      },
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message || "Failed to add employee",
    });
  }
});

router.get("/", async (req, res) => {
  try {
    const employees = await Employee.find({ isActive: true })
      .select("-__v")
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      count: employees.length,
      data: employees,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || "Failed to fetch employees",
    });
  }
});

router.get("/:id", async (req, res) => {
  try {
    const employee = await Employee.findById(req.params.id);

    if (!employee) {
      return res.status(404).json({
        success: false,
        message: "Employee not found",
      });
    }

    res.json({
      success: true,
      data: employee,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || "Failed to fetch employee",
    });
  }
});

router.get("/:id/leave-balance", async (req, res) => {
  try {
    const employee = await Employee.findById(req.params.id);

    if (!employee) {
      return res.status(404).json({
        success: false,
        message: "Employee not found",
      });
    }

    const leaves = await Leave.find({ employeeId: req.params.id }).sort({
      createdAt: -1,
    });

    const leaveHistory = {
      pending: leaves.filter((leave) => leave.status === "pending").length,
      approved: leaves.filter((leave) => leave.status === "approved").length,
      rejected: leaves.filter((leave) => leave.status === "rejected").length,
    };

    res.json({
      success: true,
      data: {
        employeeId: employee._id,
        name: employee.name,
        currentBalance: employee.leaveBalance,
        leaveHistory,
        leaves: leaves.slice(0, 10),
      },
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || "Failed to fetch leave balance",
    });
  }
});

module.exports = router;
