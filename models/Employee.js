const mongoose = require("mongoose");
const bcrypt = require("bcryptjs");

const employeeSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: [true, "Name is required"],
      trim: true,
      minlength: [2, "Name must be at least 2 characters long"],
    },
    email: {
      type: String,
      required: [true, "Email is required"],
      unique: true,
      lowercase: true,
      match: [
        /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/,
        "Please enter a valid email",
      ],
    },
    password: {
      type: String,
      required: [true, "Password is required"],
      minlength: [6, "Password must be at least 6 characters long"],
      select: false,
    },
    role: {
      type: String,
      enum: ["Employee", "Manager", "HR", "Admin"],
      default: "Employee",
    },
    department: {
      type: String,
      required: [true, "Department is required"],
      enum: [
        "Engineering",
        "Human Resources",
        "Marketing",
        "Sales",
        "Finance",
        "Operations",
        "Design",
        "Product Management",
      ],
    },
    joiningDate: {
      type: Date,
      required: [true, "Joining date is required"],
      validate: {
        validator: function (date) {
          return date <= new Date();
        },
        message: "Joining date cannot be in the future",
      },
    },
    leaveBalance: {
      type: Number,
      default: 20,
      min: [0, "Leave balance cannot be negative"],
    },
    isActive: {
      type: Boolean,
      default: true,
    },
  },
  {
    timestamps: true,
  }
);

employeeSchema.pre("save", async function (next) {
  if (!this.isModified("password")) return next();

  this.password = await bcrypt.hash(this.password, 12);
});

employeeSchema.methods.comparePassword = async function (candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

employeeSchema.index({ department: 1 });

module.exports = mongoose.model("Employee", employeeSchema);
