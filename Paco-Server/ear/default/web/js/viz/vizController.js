"use strict";

pacoApp.controller('VizCtrl', ['$scope', 'experimentsVizService', '$timeout', '$routeParams', function ($scope, experimentsVizService, $timeout, $routeParams) {


    $scope.responseTypeDetails = [];
    $scope.vizResponseData = [];
    var responseTypeList = [];
    var experimentResponses = [];

    //experiment json objects are retrieved from the 'experimentsVizService'
    // to create a scope variable for response type meta data.
    $scope.getExperimentDetails = function () {
        experimentsVizService.getExperiment($scope.experimentId).then(
            function (experiment) {
                console.log(experiment);
                if (experiment.status === 404) {
                    displayErrorMessage("Experiments ", experiment);
                }
                $scope.experimentDataModel = {
                    id: experiment.results[0].id,
                    title: experiment.results[0].title,
                    creator: experiment.results[0].creator,
                    date: experiment.results[0].modifyDate
                };
                experimentsVizService.getInputs(experiment.results[0]).forEach(function (inputType) {
                    responseTypeList.push(inputType);
                });
            });
    };
    $scope.responseTypeDetails = responseTypeList;

    //events json objects are retrieved from the 'experimentsVizService'
    // to create a scope variable for participants' responses.
    $scope.getExperimentEvents = function () {
        experimentsVizService.getEvents($scope.experimentId).then(
            function (events) {
                if (events.status === 404) {
                    displayErrorMessage("Events ", events);
                }
                events.forEach(function (response) {
                    experimentResponses.push(response);
                })
            }
        )
    };
    $scope.vizResponseData = experimentResponses;

    if (angular.isDefined($routeParams.experimentId)) {
        $scope.experimentId = parseInt($routeParams.experimentId, 10);
        $scope.getExperimentDetails();
        $scope.getExperimentEvents();
    }

    function displayErrorMessage(data, error) {
        $scope.error = {
            data: data,
            code: error.status,
            message: error.statusText
        };
    }
}]);