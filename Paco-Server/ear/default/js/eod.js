pacoApp.controller('EodCtrl', ['$scope', '$http', '$mdDialog', '$timeout',
  function($scope,
    $http, $mdDialog, $timeout) {

    var endpoint = '/events?q=\'experimentId=' + $scope.exp.id + ':who=' +
      $scope.user + '\'&json';

    $scope.showEod = function(event) {
      $http.get(endpoint, {
          cache: true
        })
        .success(function(data) {
          window.env.events = data;

          $mdDialog.show({
            template: '<md-dialog class="eod" aria-label="End of Day"><md-dialog-content><iframe src="/eod/eod_skeleton.html"></iframe></md-dialog-content></md-dialog>',
            clickOutsideToClose: true,
          });
        });
    };

    window.env = window.env || {};
    window.env.experimentId = $scope.exp.id;

    window.env.getValue = function(id) {
      if ($scope.exp[id]) {
        return $scope.exp[id];
      }
      return null;
    };


    window.db = {
      saveEvent: function(e) {
        var obj = JSON.parse(e);

        var now = new Date();
        var iso = now.toISOString();

        // Tweak ISO string to conform to yyyy/MM/dd HH:mm:ssZ
        iso = iso.replace(/-/g, '/');
        iso = iso.replace(/T/, ' ');
        iso = iso.replace(/\.[0-9]*/, '');
        obj.responseTime = iso;

        console.dir(obj);



        $http.post('/events', obj).success(function(data) {
          console.log(data[0]);
        }).error(function(data, status, headers, config) {
          console.error(data);
        });

      },
      getAllEvents: function() {
        return window.env.events;
      },
      getLastEvent: function() {
        return window.env.events[window.env.events.length - 1];
      }
    };

    window.experimentLoader = {
      getExperiment: function() {
        return $scope.exp;
      },
      getExperimentGroup: function() {
        for (var i = 0; i < $scope.exp.groups.length; i++) {
          if ($scope.exp.groups[i].endOfDayGroup === true) {
            return $scope.exp.groups[i];
          }
        }
        return null;
      },
      getEndOfDayReferredExperimentGroup: function() {
        var eodGroup = this.getExperimentGroup();
        if (eodGroup !== null) {
          var referringGroupName = eodGroup.endOfDayReferredGroupName;
          for (var i = 0; i < $scope.exp.groups.length; i++) {
            if ($scope.exp.groups[i].name === referringGroupName) {
              return $scope.exp.groups[i];
            }
          }
        }
        return null;
      }
    };

  }
]);
