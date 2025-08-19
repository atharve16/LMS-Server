const jwt = require('jsonwebtoken');
const Employee = require('../models/Employee');

const generateToken = (employeeId) => {
  return jwt.sign({ employeeId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '2d'
  });
};

const authenticateToken = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    const token = authHeader && authHeader.split(' ')[1]; 

    if (!token) {
      return res.status(401).json({
        success: false,
        message: 'Access token required. Please provide a valid token.'
      });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    
    const employee = await Employee.findById(decoded.employeeId).select('-password');
    
    if (!employee || !employee.isActive) {
      return res.status(401).json({
        success: false,
        message: 'Invalid token or employee not found'
      });
    }

    req.user = employee;
    next();
  } catch (error) {
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        success: false,
        message: 'Invalid token'
      });
    }
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        message: 'Token expired'
      });
    }
    
    res.status(500).json({
      success: false,
      message: 'Authentication error'
    });
  }
};

const authorize = (...roles) => {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        message: 'Authentication required'
      });
    }

    if (!roles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        message: `Access denied. Required roles: ${roles.join(', ')}`
      });
    }

    next();
  };
};

module.exports = {
  generateToken,
  authenticateToken,
  authorize
};
