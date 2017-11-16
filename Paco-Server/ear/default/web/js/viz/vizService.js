pacoApp.factory('experimentsVizService', ['$http', 'experimentService', '$filter',
  function ($http, experimentService, $filter) {

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
      var message = '{ "select":["distinct who"], ' +
        '"query" : { "criteria" : "experiment_id = ?", ' +
        '"values" : [' + experimentId + ']}}';
      return csSearchHttpPostBody(message);
    }
    return [];
  }

  function getAdditionalInputsFromEventsData(experimentId, definedInputs){
    var questionMarks = [];
    var textsList = [];

    for (var i = 0; i < definedInputs.length; i++) {
      questionMarks.push("?");
      textsList.push('"' + definedInputs[i] + '"');
    }

    if(experimentId !== undefined && textsList !== undefined){
      var distinctTextQuery = '{ "select":["group_name","text"], ' +
        '"query" : { "criteria" : "experiment_id = ? and text not in (' + questionMarks + ')", ' +
        '"values" : ['+experimentId+',' + textsList + ']},' +
        '"order":"group_name,text","group":"group_name,text"}';
      //console.log(distinctTextQuery);
      var textQuery = csSearchHttpPostBody(distinctTextQuery);
    }
    return textQuery;
  }

  function getStartDate(experimentId){
    var startDateQuery = "";
    if(experimentId !== undefined){
      startDateQuery = '{ "select":["response_time"], ' +
        '"query" : { "criteria" : "experiment_id = ? and response_time is not null", ' +
        '"values" : [' + experimentId + ']},' +
        '"order":"response_time asc","limit":"1"}';
    }
    var startDate = csSearchHttpPostBody(startDateQuery);
    return startDate;
  }

  function getEndDate(experimentId){
    var endDateQuery = "";
    if(experimentId !== undefined){
      endDateQuery = '{ "select":["response_time"], ' +
        '"query" : { "criteria" : "experiment_id = ? and response_time is not null", ' +
        '"values" : [' + experimentId + ']},' +
        '"order":"response_time desc","limit":"1"}';
    }

    var endDate = csSearchHttpPostBody(endDateQuery);
    return endDate;
  }

  function getEventsCounts(id) {
    var eventsCount = $http({
      method: 'GET',
      url: '/participantStats?experimentId=' + id + '&reportType=totalEventCounts&statv2=1'
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
    return eventsCount;
  }

  function getEvents(experimentId, groups, texts, participants, startDateTime, endDateTime) {
    var message = "";

    if (experimentId && participants && participants.length > 0 && texts && groups && startDateTime && endDateTime) {
      var expGroups = parametersList(groups, "groups");
      var expTexts  = parametersList(texts, "texts");
      var expParticipants = parametersList(participants, "participants");

      message = '{ "select":["who","when","response_time","text","answer","client_timezone"], "query" : ' +
        '{ "criteria" : "experiment_id = ? and response_time>? and response_time<? and ' +
        'group_name in (' + expGroups.questionMarks + ') and ' +
        'text in (' + expTexts.questionMarks + ') and ' +
        'who in (' + expParticipants.questionMarks + ')", ' +
        '"values" : [' + experimentId + ', "' + startDateTime + '", "' + endDateTime + '",' + expGroups.params +
        ' ,' + expTexts.params + ' ,' + expParticipants.params + ']},' +
        '"order":"who,text"}';
    } else if (experimentId && groups && participants && participants.length > 0 && texts) {
      var expGroups = parametersList(groups, "groups");
      var expTexts  = parametersList(texts, "texts");
      var expParticipants = parametersList(participants, "participants");

      message = '{ "select":["who","when","response_time","text","answer","client_timezone"], ' +
        '"query" : { "criteria" : "experiment_id = ? and group_name in (' + expGroups.questionMarks + ') and ' +
        'text in (' + expTexts.questionMarks + ') and ' +
        'who in (' + expParticipants.questionMarks + ')", ' +
        '"values" : [' + experimentId + ',  '+ expGroups.params + ' ,' + expTexts.params + ' ,' +
        expParticipants.params + ']},' +
        '"order":"who","order":"text"}';
    } else if (experimentId && groups) {
      var expTexts = parametersList(texts,"texts");
      var expGroups = parametersList(groups,"groups");

      message = '{"select":["who","when","response_time","text","answer","client_timezone"], ' +
        '"query" : { "criteria" : "experiment_id = ? and group_name in(' + expGroups.questionMarks + ') and ' +
        'text in (' + expTexts.questionMarks + ')", "values" : [' + experimentId + ', "' + expGroups.params + '","' +
        expTexts.params + '"]},' +
        '"order":"who","order":"text"}';
    } else {
      console.log("not enough information provided to vizService.getEvents");
      return null;
    }

    //console.log(message);
    return csSearchHttpPostBody(message);
  }

  function getDataForScatterPlotTemplate3(experimentId, groups, inputs, participants, startDateTime, endDateTime){
    var message = "";
    var expTexts = {};
    var expGroups = {};
    var expParticipants = {};

    if (experimentId && participants && participants.length > 0 && inputs && groups && startDateTime && endDateTime) {
      expGroups = parametersList(groups, "groups");
      expTexts  = parametersList(inputs, "texts");
      expParticipants = parametersList(participants, "participants");
      message = '{ "select":["who","when","response_time","text","answer","client_timezone"], ' +
        '"query" : { "criteria" : "experiment_id = ? and response_time>? and response_time<? and ' +
                                    'group_name in (' + expGroups.questionMarks + ') and ' +
                                    'text in (' + expTexts.questionMarks + ') ' +
                                    'and text is not null and ' +
                                    'who in (' + expParticipants.questionMarks + ')", ' +
                     '"values" : [' + experimentId + ', "' + startDateTime + '", "' + endDateTime + '",' +
                                      expGroups.params + ' ,' + expTexts.params + ' ,' +
                                   expParticipants.params + ']},' +
        '"order":"who,response_time"}';
      //console.log(message);
      return csSearchHttpPostBody(message);
    } else {
      return {};
    }

  }

  function parametersList(parameterList, parameter){
    var questionMarks_list = [];
    var paramsList = [];

    for (var i = 0; i < parameterList.length; i++) {
      questionMarks_list.push("?");
      paramsList.push('"' + parameterList[i] + '"');
    }
    return {"questionMarks":questionMarks_list,"params":paramsList }
  }

  function csSearchHttpPostBody(message) {
    var response = $http({
      method: 'POST',
      url: '/csSearch?pacoProtocol=5',
      data: angular.fromJson(message),
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
    return response;
  }

  function phoneSessionHttpPostBody(experimentId, whoList, groupName, startTime, endTime) {
    var url = "phoneSessions?experimentId="+experimentId;
    if (whoList) {
      url = url + "&who=" + whoList;
    }
    if (groupName) {
      url = url + "&groupName=" + groupName;
    }
    if (startTime && endTime) {
      url = url + "&startTime=" + startTime + "&endTime=" + endTime;
    }

    var response = $http({
      method: 'GET',
      url: url
    }).then(function successCallback(response) {
      return response;
    }, function errorCallback(error) {
      return error;
    });
    return response;
  }


    function convertDatesToStrings(experiment) {
    if (experiment.visualizations && experiment.visualizations.length > 0) {
      experiment.visualizations.forEach(function(v) {
        v.startDatetime = $filter('date')(v.startDatetime, 'yyyy-MM-ddTHH:mm:ss');
        v.endDatetime = $filter('date')(v.endDatetime, 'yyyy-MM-ddTHH:mm:ss');
      });
    }
  }

  function saveVisualizations(experiment) {
    var experimentCopy = {};
    angular.copy(experiment, experimentCopy);
    convertDatesToStrings(experimentCopy);
    var saveVizs = $http.post('/experiments?id=' + experimentCopy.id, JSON.stringify(experimentCopy))
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
    getAdditionalInputsFromEventsData: getAdditionalInputsFromEventsData,
    getEventsCounts: getEventsCounts,
    getDataForScatterPlotTemplate3:getDataForScatterPlotTemplate3,
    phoneSessionHttpPostBody:phoneSessionHttpPostBody,
    getStartDate:getStartDate,
    getEndDate:getEndDate,
    saveVisualizations: saveVisualizations
  }
}]);
