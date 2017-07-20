/**
 * Created by muthuk on 6/28/17.
 */
"use strict";

pacoApp.controller('VizCtrl', ['$scope', 'experimentsVizService', '$timeout', '$routeParams', function ($scope, experimentsVizService, $timeout, $routeParams) {


    $scope.responseTypeDetails = [];
    $scope.vizResponseData = [];
    var responseTypeList = [];
    var experimentResponses = [];

    //experiment json objects are retrieved from the 'experimentsVozService'
    // to create a scope variable for response type meta data.
    $scope.getExperimentDetails = function () {
        experimentsVizService.getExperimentObj($scope.experimentId).then(
            function (experimentObject) {
                $scope.experimentDataModel = {
                    id: experimentObject.id,
                    title: experimentObject.title,
                    creator: experimentObject.creator,
                    date: experimentObject.modifyDate
                };
                experimentsVizService.getEventsResponseDetails(experimentObject).forEach(function (resType) {
                    responseTypeList.push(resType);
                });
            }, function (error) {
                console.log(error);
            });
    };
    $scope.responseTypeDetails = responseTypeList;

    //experiment events json objects are retrieved from the 'experimentsVozService'
    // to create a scope variable for participants' responses.
    $scope.getExperimentEvents = function () {
        experimentsVizService.getExperimentEvents($scope.experimentId).then(
            function (eventsObject) {
                eventsObject.forEach(function (response) {
                    experimentResponses.push(response);
                })
            },
            function (error) {
                console.log(error);
            }
        )
    };
    $scope.vizResponseData = experimentResponses;

    if (angular.isDefined($routeParams.experimentId)) {
        $scope.experimentId = parseInt($routeParams.experimentId, 10);
        $scope.getExperimentDetails();
        $scope.getExperimentEvents();
    }
}]);