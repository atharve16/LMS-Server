const mongoose = require('mongoose');

const leaveSchema = new mongoose.Schema({
  employeeId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Employee',
    required: [true, 'Employee ID is required']
  },
  startDate: {
    type: Date,
    required: [true, 'Start date is required']
  },
  endDate: {
    type: Date,
    required: [true, 'End date is required'],
    validate: {
      validator: function(endDate) {
        return endDate >= this.startDate;
      },
      message: 'End date must be after or equal to start date'
    }
  },
  reason: {
    type: String,
    required: [true, 'Reason is required'],
    minlength: [3, 'Reason must be at least 3 characters long']
  },
  status: {
    type: String,
    enum: ['pending', 'approved', 'rejected'],
    default: 'pending'
  },
  reviewedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Employee'
  },
  reviewedAt: {
    type: Date
  },
  reviewComments: {
    type: String
  },
  daysRequested: {
    type: Number,
    required: true
  }
}, {
  timestamps: true
});

leaveSchema.index({ employeeId: 1, startDate: 1, endDate: 1 });
leaveSchema.index({ status: 1 });

module.exports = mongoose.model('Leave', leaveSchema);
