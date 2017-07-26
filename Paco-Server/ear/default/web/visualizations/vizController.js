/**
 * Created by muthuk on 6/28/17.
 */
"use strict";

app.controller('MainController', ['$scope', 'experimentsFactory', function ($scope, experimentsFactory) {

    experimentsFactory.getExperimentObject.then(function (experiment) {

        $scope.experimentDataModel = {
            id: experiment.id,
            title: experiment.title,
            creator: experiment.creator,
            date: experiment.modifyDate
        }
    });

    var responseTypeInfoMap = new Map();

    experimentsFactory.getResponseDetails.then(function (responseType) {
        responseType.forEach(function (resType) {
            responseTypeInfoMap.set(resType.name, resType);
        });
    });

    experimentsFactory.groupResponses.then(function (responses) {
        responses.forEach(function (response) {
            if (responseTypeInfoMap.has(response.key)) {
                createVizData(responseTypeInfoMap.get(response.key), response.values);
            }
        });
    });
    var responseTypes = [];

    function createVizData(responseTypeInfo, responses) {
        responseTypes.push(responseTypeInfo);
        if (responseTypeInfo.responseType === "number") {
            $scope.responseTypeNumber = responseTypeInfo;
            $scope.numberData = responses;
        } else if (responseTypeInfo.responseType === "open text") {
            $scope.responseTypeOpenText = responseTypeInfo;
            $scope.openTextData = responses;
        } else if (responseTypeInfo.responseType === "likert") {
            $scope.responseTypeLikert = responseTypeInfo;
            $scope.likertData = responses;
        } else if (responseTypeInfo.responseType === "list") {
            $scope.responseTypeList = responseTypeInfo;
            $scope.listData = responses;
        }
    }

    $scope.responseTypeInfo = responseTypes;
}]);