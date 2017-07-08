/**
 * Created by muthuk on 6/28/17.
 */
app.factory('experimentsFactory', function ($http) {

    var getExperiments = $http.get('data/experiments.json').then(function (response) {
        return response.data;
    }, function (error) {
        console.log(error);
    });

    var getEvents = $http.get('data/events.json').then(function (response) {
        return response.data;
    }, function (error) {
        console.log(error);
    });
    var experimentId;
    var getExperimentObject = getExperiments.then(function (response) {
        experimentId = response.experiments[0].id;
        return response;
    }).then(function (data) {
        return data.experiments[0];
    });
    var eventsObject = getEvents.then(function (response) {
        return response.events;
    });

    var getResponseDetails = getExperimentObject.then(function (response) {
        var responseDetails = [];
        var inputs = response.groups[0].inputs;
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
    });

    var events = [];
    var answers = {};
    var outputObject = [];
    var getResponses = eventsObject.then(function (eventObject) {

        eventObject.forEach(function (event) {
            if (event.experimentId === experimentId) {
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
            }
        });
        return outputObject;
    });

    var groupResponses = getResponses.then(function (output) {
        var responsesByName = d3.nest()
            .key(function (d) {
                return d.name;
            })
            .entries(output);
        return responsesByName;
    });

    return {
        getExperiments: getExperiments,
        getEvents: getEvents,
        getExperimentObject: getExperimentObject,
        getResponseDetails: getResponseDetails,
        getResponses: getResponses,
        groupResponses: groupResponses
    }
});
