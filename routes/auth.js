const express = require('express');
const Employee = require('../models/Employee');
const { generateToken } = require('../middleware/auth');
const bcrypt = require('bcryptjs');
const router = express.Router();

router.post('/register', async (req, res) => {
  try {
    const { name, email, password, department, role, joiningDate } = req.body;

    const existingEmployee = await Employee.findOne({ email });
    if (existingEmployee) {
      return res.status(400).json({
        success: false,
        message: 'Employee with this email already exists'
      });
    }

    const employee = new Employee({
      name,
      email,
      password,
      department,
      role: role || 'Employee',
      joiningDate
    });

    await employee.save();

    const token = generateToken(employee._id);

    res.status(201).json({
      success: true,
      message: 'Employee registered successfully',
      data: {
        employeeId: employee._id,
        name: employee.name,
        email: employee.email,
        department: employee.department,
        role: employee.role,
        joiningDate: employee.joiningDate,
        leaveBalance: employee.leaveBalance
      },
      token
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message || 'Failed to register employee'
    });
  }
});

router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email and password are required'
      });
    }

    const employee = await Employee.findOne({ email, isActive: true }).select('+password');
    
    if (!employee) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password'
      });
    }

    const isPasswordValid = await employee.comparePassword(password);
    
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password'
      });
    }

    const token = generateToken(employee._id);

    res.json({
      success: true,
      message: 'Login successful',
      data: {
        employeeId: employee._id,
        name: employee.name,
        email: employee.email,
        department: employee.department,
        role: employee.role,
        leaveBalance: employee.leaveBalance
      },
      token
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Login failed'
    });
  }
});

router.get('/profile', require('../middleware/auth').authenticateToken, (req, res) => {
  res.json({
    success: true,
    data: req.user
  });
});

module.exports = router;