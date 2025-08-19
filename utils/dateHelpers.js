const moment = require('moment');

const calculateBusinessDays = (startDate, endDate) => {
  const start = moment(startDate);
  const end = moment(endDate);
  let businessDays = 0;
  
  const current = start.clone();
  while (current.isSameOrBefore(end)) {
    if (current.day() !== 0 && current.day() !== 6) { 
      businessDays++;
    }
    current.add(1, 'day');
  }
  
  return businessDays;
};

const datesOverlap = (start1, end1, start2, end2) => {
  return moment(start1).isSameOrBefore(moment(end2)) && 
         moment(end1).isSameOrAfter(moment(start2));
};

const isValidDateRange = (startDate, endDate) => {
  return moment(endDate).isSameOrAfter(moment(startDate));
};

module.exports = {
  calculateBusinessDays,
  datesOverlap,
  isValidDateRange
};
