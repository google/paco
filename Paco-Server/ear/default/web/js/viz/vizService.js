/**
 * Created by muthuk on 6/28/17.
 */
pacoApp.factory('experimentsVizService', ['$http', 'experimentService', function ($http, experimentService) {

    var expObject = '';

    function getExperimentObj(id) {

        var getExp = experimentService.getExperiment(id).then(function (experimentValue) {
            return experimentValue.data;
        }, function (error) {
            console.log(error);
        });

        expObject = getExp.then(function (experimentObj) {
            return experimentObj.results[0];
        }, function (error) {
            console.log(error);
        });
        return expObject;
    }

    function getEventsResponseDetails(experimentObject) {

        var responseDetails = [];
        var inputs = experimentObject.groups[0].inputs;
        inputs.forEach(function (input) {
            if (input.responseType == "likert") {
                responseDetails.push({
                    "name": input.name,
                    "responseType": input.responseType,
                    "text": input.text,
                    "leftsidelabel": input.leftSideLabel,
                    "rightsidelabel": input.rightSideLabel
                });
            } else if (input.responseType == "list") {
                responseDetails.push({
                    "name": input.name,
                    "responseType": input.responseType,
                    "text": input.text,
                    "listChoices": input.listChoices
                });
            } else {
                responseDetails.push({"name": input.name, "responseType": input.responseType, "text": input.text});
            }
        });
        return responseDetails;
    }

    function getExperimentEvents(id) {

        var fetchEvents = $http({
            method: 'GET',
            url: '/events?q=experimentId=' + id + '&json'
        }).then(function successCallback(response) {
            return response.data.events;
        }, function errorCallback(error) {
            console.log(error);
        });

        //TODO
        // var message = '{ query : { criteria : "experiment_id = ? and group_name = ?", values : [5770237022568448, "New Group"]}, order : response_time}';

        // var message = '{ query : { criteria : "experiment_id = ? and group_name = ?", values : [' + id + ', "' + group_name + '"]}}';
        // var fetchEvents = $http({
        //     method: 'POST',
        //     url: '/csSearch',
        //     data: {'message': message}
        // }).then(function successCallback(response) {
        //     console.log(response);
        //     return response;
        // }, function errorCallback(error) {
        //     console.log(error);
        // });

        var events = [];
        var answers = {};
        var outputObject = [];

        var getEventResponses = fetchEvents.then(function (eventObject) {
            eventObject.forEach(function (event) {
                answers.who = event.who;
                answers.when = event.when;
                answers.responseTime = event.responseTime;
                events.push(event.responses);
                event.responses.forEach(function (e) {
                    answers.name = e.name;
                    answers.answer = e.answer;
                    if (answers.name != "joined") {
                        outputObject.push({
                            "who": answers.who,
                            "when": answers.when,
                            "responseTime": answers.responseTime,
                            "name": answers.name,
                            "answer": answers.answer
                        });
                    }
                });

            });
            return outputObject;
        });

        var groupEventResponses = getEventResponses.then(function (responses) {
            var groupByNames = d3.nest()
                .key(function (d) {
                    return d.name;
                })
                .entries(responses);
            return groupByNames;
        });
        return groupEventResponses;
    }

    return {
        getExperimentEvents: getExperimentEvents,
        getEventsResponseDetails: getEventsResponseDetails,
        getExperimentObj: getExperimentObj
    }
}]);
