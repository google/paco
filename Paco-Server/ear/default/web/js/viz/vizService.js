pacoApp.factory('experimentsVizService', ['$http', 'experimentService', '$filter', function ($http, experimentService, $filter) {

  var experiment = '';
  var events = ' ';

  function getExperiment(id) {

    var getExperiment = experimentService.getExperiment(id).then(function successCallback(experimentData) {
      console.log(experimentData);
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
    }
    var participants = httpPostBody(message);
    return participants;
  }

  function getDateRange(experimentId) {

    var dateRange = [];
    var format = 'MM/dd/yyyy';
    if (experimentId != undefined) {
      var endDateQuery = '{ "select":["response_time"], "query" : { "criteria" : "experiment_id = ?", "values" : [' + experimentId + ']},"order":"response_time desc","limit":"1"}';
      var startDateQuery = '{ "select":["response_time"], "query" : { "criteria" : "experiment_id = ?", "values" : [' + experimentId + ']},"order":"response_time asc","limit":"1"}';
    }
    var startDate = httpPostBody(startDateQuery);
    var endDate = httpPostBody(endDateQuery);

    startDate.then(function (data) {

      var format_startDate = $filter('date')(new Date(data.data.customResponse[0].response_time), format);
      dateRange.push(format_startDate)
    });
    endDate.then(function (data) {
      var format_endDate = $filter('date')(new Date(data.data.customResponse[0].response_time), format);
      dateRange.push(format_endDate)
    });
    return dateRange;
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
      eventsCount.push(data.data[0].schedR, data.data[0].missedR, data.data[0].selfR);
    });
    return eventsCount;
  }

  function getEvents(experimentId, group, participants, startDateTime, endDateTime) {

    console.log(experimentId + " " + group + " " +participants+ " " +startDateTime+" "+ endDateTime);
    var message = "";
    if (experimentId != undefined && group != undefined) {
      message = '{ "query" : { "criteria" : "experiment_id = ? and group_name = ?", "values" : [' + experimentId + ', "' + group + '"]},"limit":"50"}';
    }
    if (experimentId != undefined && participants != undefined && participants.length > 0) {
      var questionMarks = [];
      var participantsList = [];
      for (var i = 0; i < participants.length; i++) {
        questionMarks.push("?");
        participantsList.push('"' + participants[i] + '"');
      }
      message = '{ "query" : { "criteria" : "experiment_id = ? and group_name = ? and who in (' + questionMarks + ')", "values" : [' + experimentId + ', "' + group + '" , ' + participantsList + ']},"limit":"50"}';
    }
    if (experimentId != undefined && group != undefined && startDateTime != undefined && endDateTime != undefined) {
      message = '{"query": {"criteria":" experiment_id=?  and response_time>? and response_time<? and group_name=? ) ","values" : [' + experimentId + ', "' + startDateTime + '", "' + endDateTime + '","' + group + '"]}}';
    }
    console.log(message);
    var events = httpPostBody(message);
    return events;
  }

  function getResponses(events, input) {

    var answers = {};
    var responseResults = [];
    var inputText = "";
    var responses = [];

    events.forEach(function (response) {
      answers.who = response.who;
      answers.when = response.when;
      answers.responseTime = response.responseTime;
      answers.timezone = response.timezone;
      response.responses.forEach(function (e) {
        answers.name = e.name;
        answers.answer = e.answer;
        if (answers.name != "joined") {
          responses.push({
            "who": answers.who,
            "when": answers.when,
            "responseTime": answers.responseTime,
            "name": answers.name,
            "answer": answers.answer,
            "timezone": answers.timezone
          });
        }
      });
    });
    var groupByInputs = d3.nest()
        .key(function (d) {
          return d.name;
        })
        .entries(responses);
    inputText = input;
    responseResults = [];
    groupByInputs.forEach(function (inputs) {
      if (inputText === inputs.key) {
        responseResults = inputs.values;
      }
    });
    return responseResults;
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
    getResponses: getResponses,
    getParticipants: getParticipants,
    getEventsCounts: getEventsCounts,
    getDateRange: getDateRange,
    saveVisualizations: saveVisualizations
  }
}]);
