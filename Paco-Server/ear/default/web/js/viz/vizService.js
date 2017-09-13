pacoApp.factory('experimentsVizService', ['$http', 'experimentService', '$filter', function ($http, experimentService, $filter) {

  var experiment = '';

  function getExperiment(id) {
    var getExperiment = experimentService.getExperiment(id).then(function successCallback(experimentData) {
      return experimentData.data;
    }, function errorCallback(error) {
      return error;
    });
    experiment = getExperiment.then(function successCallback(experiment) {
      return experiment;
    });
    return experiment;
  }

  function getParticipants(experimentId) {
    if (experimentId != undefined) {
      var message = '{ "select":["who"], "query" : { "criteria" : "experiment_id = ?", "values" : [' + experimentId + ']},"group":"who"}';
      var participants = httpPostBody(message);
    }
    return participants;
  }

  function getDateRange(experimentId) {
    var dateRange = [];
    var format = 'MM/dd/yyyy';
    if (experimentId != undefined) {
      var endDateQuery = '{ "select":["response_time"], "query" : { "criteria" : "experiment_id = ? and response_time is not null", "values" : [' + experimentId + ']},"order":"response_time desc","limit":"1"}';
      var startDateQuery = '{ "select":["response_time"], "query" : { "criteria" : "experiment_id = ? and response_time is not null", "values" : [' + experimentId + ']},"order":"response_time asc","limit":"1"}';

      var startDate = httpPostBody(startDateQuery);
      var endDate = httpPostBody(endDateQuery);

      startDate.then(function (data) {
        if(data.data.customResponse !== undefined){
          if(data.data.customResponse.length > 0){
            var format_startDate = $filter('date')(new Date(data.data.customResponse[0].response_time), format);
            dateRange[0] = format_startDate;
          }
        }
      });
      endDate.then(function (data) {
        if(data.data.customResponse !== undefined){
          if(data.data.customResponse.length > 0){
            var format_endDate = $filter('date')(new Date(data.data.customResponse[0].response_time), format);
            dateRange[1] = format_endDate;
          }
        }
      });
      return dateRange;
    }
  }

  function getEventsCounts(id) {
    var eventsCount = [];
    var getCount = $http({
      method: 'GET',
      url: '/participantStats?experimentId=' + id + '&reportType=totalEventCounts&statv2=1'
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
    getCount.then(function (data) {
      if(data.data[0] !== undefined){
        eventsCount.push(data.data[0].schedR, data.data[0].missedR, data.data[0].selfR);
      }
    });
    return eventsCount;
  }

  function getEvents(experimentId, group, input, participants, startDateTime, endDateTime) {
    var message = "";
    if (experimentId != undefined && group != undefined) {
      message = '{"select":["who","when","response_time","text","answer","client_timezone"], "query" : { "criteria" : "experiment_id = ? and group_name = ? and text=?", "values" : [' + experimentId + ', "' + group + '","' + input + '"]},"order":"who","order":"text"}';
    }
    if (experimentId != undefined && participants != undefined && participants.length > 0 && input != undefined) {
      var questionMarks = [];
      var participantsList = [];
      for (var i = 0; i < participants.length; i++) {
        questionMarks.push("?");
        participantsList.push('"' + participants[i] + '"');
      }
      message = '{ "select":["who","when","response_time","text","answer","client_timezone"], "query" : { "criteria" : "experiment_id = ? and group_name = ? and text=? and who in (' + questionMarks + ')", "values" : [' + experimentId + ', "' + group + '" ,"' + input + '" ,' + participantsList + ']},"order":"who","order":"text"}';
    }
    //filter data based on start and end date/timestamp values
    if (experimentId != undefined && participants != undefined && participants.length > 0 && input != undefined && group != undefined && startDateTime != undefined && endDateTime != undefined) {
      var questionMarks = [];
      var participantsList = [];
      for (var i = 0; i < participants.length; i++) {
        questionMarks.push("?");
        participantsList.push('"' + participants[i] + '"');
      }
    message = '{ "select":["who","when","response_time","text","answer","client_timezone"], "query" : { "criteria" : "experiment_id = ? and response_time>? and response_time<? and group_name = ? and text=? and who in (' + questionMarks + ')", "values" : [' + experimentId + ', "' + startDateTime + '", "' + endDateTime + '","' + group + '" ,"' + input + '" ,' + participantsList + ']},"order":"who,text"}';
    }
    console.log(message);
    var events = httpPostBody(message);
    return events;
  }

  function httpPostBody(message) {
    var response = $http({
      method: 'POST',
      url: '/csSearch',
      data: angular.fromJson(message),
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
    return response;
  }

  function saveVisualizations(experiment) {
    var saveVizs = $http.post('/experiments?id=' + experiment.id, JSON.stringify(experiment))
        .then(function successCallback(response) {
          return response;
        }, function errorCallback(error) {
          console.log(error);
        });
    return saveVizs;
  }

  return {
    getExperiment: getExperiment,
    getEvents: getEvents,
    getParticipants: getParticipants,
    getEventsCounts: getEventsCounts,
    getDateRange: getDateRange,
    saveVisualizations: saveVisualizations
  }
}]);
