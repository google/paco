pacoApp.factory('experimentsVizService', ['$http', 'experimentService', function ($http, experimentService) {

  var experiment = '';
  var events = ' ';

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
    }
    var participants = $http({
      method: 'POST',
      url: '/csSearch',
      data: angular.fromJson(message),
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
    return participants;

  }

  function getEventCounts(id) {
    var getCount = $http({
      method: 'GET',
      url: '/participantStats?experimentId=' + id + '&reportType=totalEventCounts&statv2=1'
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
    return getCount;
  }

  function getEvents(experimentId, group, participants, startDateTime, endDateTime) {

    var message = "";
    var startDateTimeStamp = "";
    var endDateTimeStamp = "";
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
    //filter data based on start and end date/timestamp values
    if (experimentId != undefined && group != undefined && startDateTime != undefined && endDateTime != undefined) {
      message = '{"query": {"criteria":" experiment_id=?  and response_time>? and response_time<? and group_name=? ) ","values" : [' + experimentId + ', "' + startDateTime + '", "' + endDateTime + '","' + group + '"]}}';
    }
    events = $http({
      method: 'POST',
      url: '/csSearch',
      data: angular.fromJson(message),
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
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

  return {
    getExperiment: getExperiment,
    getEvents: getEvents,
    getResponses: getResponses,
    getParticipants: getParticipants,
    getEventCounts: getEventCounts
  }
}]);
