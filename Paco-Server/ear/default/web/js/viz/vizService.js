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
  function getParticipants(experimentId){

   if(experimentId != undefined){
         var message = '{ "select":["who"], "query" : { "criteria" : "experiment_id = ?", "values" : ['+experimentId+']},"group":"who"}';
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

  function getEventCounts(id){
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

  function getEvents(experimentId, group, participants, startDate, startTime, endDate, endTime) {

    var message = "";
    if (experimentId != undefined && group != undefined) {
      message = '{ "query" : { "criteria" : "experiment_id = ? and group_name = ?", "values" : [' + experimentId + ', "' + group + '"]},"limit":"50"}';
    }
    if(experimentId != undefined  && participants!=undefined && participants.length > 0){
      var questionMarks = [];
      var participantsList = [];
      for(var i=0;i<participants.length;i++){
        questionMarks.push("?");
        participantsList.push('"'+participants[i]+'"');
      }
      message =  '{ "query" : { "criteria" : "experiment_id = ? and group_name = ? and who in ('+questionMarks+')", "values" : [' + experimentId + ', "' + group + '" , '+participantsList+']},"limit":"50"}';
    }
     if(experimentId != undefined && group != undefined && startDate != undefined){
      var start_date = startDate + " " + "00:00:00";
      var end_date = startDate + " " + "23:59:59";
      message = '{"query": {"criteria":" experiment_id=?  and response_time>? and response_time<? and group_name=? ) ","values" : [' + experimentId + ', "' + start_date + '", "' + end_date + '","' + group + '"]}}';
    }

    if(experimentId != undefined && group != undefined && startDate != undefined && endDate != undefined){
      var start_date = startDate + " " + "00:00:00";
      var end_date = endDate  + " " + "23:59:59";
      message = '{"query": {"criteria":" experiment_id=?  and response_time>? and response_time<? and group_name=? ) ","values" : [' + experimentId + ', "' + start_date + '", "' + end_date + '","' + group + '"]}}';
    }

    if(experimentId != undefined && group != undefined && startDate != undefined && startTime != undefined && endTime != undefined){
      var start_dateTime = startDate + " " + startTime+":00";
      var end_dateTime = startDate + " " + endTime+":00";
      message = '{"query": {"criteria":" experiment_id=?  and response_time>? and response_time<? and group_name=? ) ","values" : [' + experimentId + ', "' + start_dateTime + '", "' + end_dateTime + '", "' + group + '"]}}';
    }

    if(experimentId != undefined && group != undefined && startDate != undefined && startTime != undefined && endDate != undefined && endTime != undefined){
      var start_dateTime = startDate + " " + startTime+":00";
      var end_dateTime = endDate + " " + endTime+":00";
      message = '{"query": {"criteria":" experiment_id=?  and response_time>? and response_time<? and group_name=? ) ","values" : [' + experimentId + ', "' + start_dateTime + '", "' + end_dateTime + '","' + group + '"]}}';
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
    getParticipants:getParticipants,
    getEventCounts:getEventCounts
  }
}]);
