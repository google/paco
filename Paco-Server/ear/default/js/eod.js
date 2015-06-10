pacoApp.controller('EodCtrl', ['$scope', '$http', '$mdDialog', '$timeout',
  function($scope,
    $http, $mdDialog, $timeout) {

    var endpoint = '/events?q=\'experimentId=' + $scope.experiment.id + ':who=' +
      $scope.user + '\'&json';

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

      var dailyEvents = []; // responseTime, event
      var eodEvents = {}; // eodResponseTime, event
      var timeout = $scope.eodGroup.actionTriggers[0].actions[0].timeout * 60 * 1000; // in millis
      var now = new Date().getTime();
      var cutoffDateTimeMs = now - timeout;

      console.log($scope.allEvents);

      for (var i = 0; i < $scope.allEvents.length; i++) {
        var event = $scope.allEvents[i];
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
          if (event.responseTime) {
            eodEvents[event.responseTime + ''] = event;
          }
        } else if (eventGroupName === $scope.referredGroup.name) {
          var alreadyAnswered = eodEvents[event.responseTime];
          if (!alreadyAnswered) {
            dailyEvents.push(event);
          }
        }
      }

      console.log(eodEvents);
      console.log(dailyEvents);

      for (var i = 0; i < dailyEvents.length; i++) {
        var event = dailyEvents[i];
        var responsePairs = {};
        for (var j = 0; j < event.responses.length; j++) {
          var response = event.responses[j];
          responsePairs[response.name] = response.answer;
        }
        event.responsePairs = responsePairs;
      }

      $scope.activeEvents = dailyEvents;
      $scope.activeEvent = dailyEvents[0];
    };





    // window.db = {
    //   saveEvent: function(e) {
    //     var obj = JSON.parse(e);

    //     var now = new Date();
    //     var iso = now.toISOString();

    //     // Tweak ISO string to conform to yyyy/MM/dd HH:mm:ssZ
    //     iso = iso.replace(/-/g, '/');
    //     iso = iso.replace(/T/, ' ');
    //     iso = iso.replace(/\.[0-9]*/, '');
    //     obj.responseTime = iso;

    //     console.dir(obj);



    //     $http.post('/events', obj).success(function(data) {
    //       console.log(data[0]);
    //     }).error(function(data, status, headers, config) {
    //       console.error(data);
    //     });

    //   },
    //   getAllEvents: function() {
    //     return window.env.events;
    //   },
    //   getLastEvent: function() {
    //     return window.env.events[window.env.events.length - 1];
    //   }
    // };

  //   window.experimentLoader = {
  //     getExperiment: function() {
  //       return $scope.exp;
  //     },
  //     getExperimentGroup: function() {
  //       for (var i = 0; i < $scope.exp.groups.length; i++) {
  //         if ($scope.exp.groups[i].endOfDayGroup === true) {
  //           return $scope.exp.groups[i];
  //         }
  //       }
  //       return null;
  //     },
  //     getEndOfDayReferredExperimentGroup: function() {
  //       var eodGroup = this.getExperimentGroup();
  //       if (eodGroup !== null) {
  //         var referringGroupName = eodGroup.endOfDayReferredGroupName;
  //         for (var i = 0; i < $scope.exp.groups.length; i++) {
  //           if ($scope.exp.groups[i].name === referringGroupName) {
  //             return $scope.exp.groups[i];
  //           }
  //         }
  //       }
  //       return null;
  //     }
  //   };

  // }
  }
]);
