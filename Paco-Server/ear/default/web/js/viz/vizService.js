pacoApp.factory('experimentsVizService', ['$http', 'experimentService', function ($http, experimentService) {

    var experiment = '';

    function getExperiment(id) {

        var getExperiment = experimentService.getExperiment(id).then(function successCallback(experimentData) {
            return experimentData.data;
        }, function errorCallback(error) {
            return error;
        });

        experiment = getExperiment.then(function (experiment) {
            return experiment;
        });
        return experiment;
    }

    function getInputs(experiment) {
        var responseDetails = [];
        var inputs = experiment.groups[0].inputs;
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

    function getEvents(id) {

        var getEvents = $http({
            method: 'GET',
            url: '/events?q=experimentId=' + id + '&json'
        }).then(function successCallback(response) {
            return response.data.events;
        }, function errorCallback(error) {
            return error;
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

        var answers = {};
        var responseResults = "";
        var responses = [];

        var getResponses = getEvents.then(function (events) {
            if (events.status === 404) {
                return events;
            } else {
                events.forEach(function (event) {
                    answers.who = event.who;
                    answers.when = event.when;
                    answers.responseTime = event.responseTime;
                    events.push(event.responses);
                    event.responses.forEach(function (e) {
                        answers.name = e.name;
                        answers.answer = e.answer;
                        if (answers.name != "joined") {
                            responses.push({
                                "who": answers.who,
                                "when": answers.when,
                                "responseTime": answers.responseTime,
                                "name": answers.name,
                                "answer": answers.answer
                            });
                        }
                    });
                });
                var groupByInputs = d3.nest()
                    .key(function (d) {
                        return d.name;
                    })
                    .entries(responses);
                return groupByInputs;
            }
        });
        responseResults = getResponses;
        return responseResults;
    }
    return {
        getEvents: getEvents,
        getInputs: getInputs,
        getExperiment: getExperiment
    }
}]);
