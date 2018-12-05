pacoApp.controller('EodCtrl', ['$scope', '$http', '$mdDialog', '$timeout',
  function($scope, $http, $mdDialog, $timeout) {

    var endpoint = '/events?q=\'experimentId=' + $scope.experiment.id + ':who=' +
      $scope.user + '\'&json&includePhotos=true';

    $scope.activeIdx = 0;

    $scope.getExperimentGroup = function() {
      for (var i = 0; i < $scope.experiment.groups.length; i++) {
         if ($scope.experiment.groups[i].endOfDayGroup === true) {
            $scope.eodGroup = $scope.experiment.groups[i];
            return;
         }
      }
      $scope.eodGroup = null;
    };

    $scope.getEndOfDayReferredExperimentGroup = function() {
      if ($scope.eodGroup !== null) {
        var referringGroupName = $scope.eodGroup.endOfDayReferredGroupName;
        for (var i = 0; i < $scope.experiment.groups.length; i++) {
          if ($scope.experiment.groups[i].name === referringGroupName) {
            $scope.referredGroup = $scope.experiment.groups[i];
            return;
          }
        }
      }
      $scope.referredGroup = null;
    };

    $scope.loadAllEvents = function() {

      $scope.getExperimentGroup();
      $scope.getEndOfDayReferredExperimentGroup();

      $http.get(endpoint, {
        cache: false
      })
      .success(function(data) {
        $scope.allEvents = data;
        $scope.getActiveEventsWithoutEod();
      });
    };

    $scope.loadAllEvents();


    $scope.getActiveEventsWithoutEod = function() {

      var dailyEvents = [];
      var eodEvents = {};
      var timeout = $scope.eodGroup.actionTriggers[0].actions[0].timeout * 60 * 1000; // in millis
      var now = new Date().getTime();
      var cutoffDateTimeMs = now - timeout;

      var eventCount = 0;
      if ($scope.allEvents.events) {
        eventCount = $scope.allEvents.events.length;
      }
      for (var i = 0; i < eventCount; i++) {
        var event = $scope.allEvents.events[i];

        event.responsePairs = {};
        for (var j = 0; j < event.responses.length; j++) {
          var response = event.responses[j];
          event.responsePairs[response.name] = response.answer;
        }

        if (!event.responseTime) {
          continue;
        } 
        if (new Date(event.responseTime).getTime() < cutoffDateTimeMs) {
          break;
        }
        var eventGroupName = event.experimentGroupName;
        if (!eventGroupName) {
          continue;
        }
        if (eventGroupName === $scope.eodGroup.name) {
          
          var eventEodResponseTime = new Date(event.responsePairs['eodResponseTime']).getTime();
          if (eventEodResponseTime) {
            eodEvents[eventEodResponseTime + ""] = event;
          }
        } else if (eventGroupName === $scope.referredGroup.name) {
          var alreadyAnswered = eodEvents[event.responseTime];
          if (!alreadyAnswered) {
            dailyEvents.push(event);
          }
        }
      }
      $scope.activeEvents = dailyEvents;
    };
  }
]);
