/**
 * This JavaScript file contains all the utility functions that are needed for Paco
 * The category of functions include time and date functions, string functions, json functions etc
 */

var paco2 = (function (init) {
	var obj = init || {};
	var environment = obj["environment"] || "test";
	
	/*
	 * This function checks if a given date is from today
	 * @return - true if it is else null
	 */
	var isFromToday = function(dateStr) {
	  if (dateStr) {
	    var eventDate = new Date(dateStr);
	    var nowDate = new Date();
	    return eventDate.getYear() === nowDate.getYear() &&
	      eventDate.getMonth() === nowDate.getMonth() &&
	      eventDate.getDate() === nowDate.getDate();
	  }
	  return null;
	};
	
	/*
	 * Function to generate a random no, provided a limit
	 */
	obj.random = function(limit) {
	  return Math.floor(Math.random() * limit);
	};
    
	/*
	 * Gets the experiment EndDate
	 */
	function getExperimentEndDate() {
		var experimentGroup = paco2.experimentService.getExperimentGroup();
		var endDateStr = experimentGroup["endDate"];
		var endDate = new Date(Date.parse(endDateStr));
		return endDate;
	}

	/*
	 * Checks if current date is experiment EndDate
	 */
	function isEndDate(today, experimentEndDate) {
		return today.getDate() === experimentEndDate.getDate() &&
		today.getMonth() === experimentEndDate.getMonth() &&
		today.getFullYear() === experimentEndDate.getFullYear();
	}
})();
  