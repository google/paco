pacoApp.controller('EodCtrl', ['$scope', '$http', '$mdDialog', '$timeout',
  function($scope,
    $http, $mdDialog, $timeout) {

    var endpoint = '/events?q=\'experimentId=' + $scope.exp.id + ':who=' +
      $scope.user + '\&json';

    $scope.showEod = function(event) {
      $http.get(endpoint, {
          cache: true
        })
        .success(function(data) {
          window.env.events = data;

          $mdDialog.show({
            template: '<md-dialog class="eod"><md-dialog-content><iframe src="/eod/eod_skeleton.html"></iframe></md-dialog-content></md-dialog>',
            clickOutsideToClose: true,
          });
        });
    };

    window.env = window.env || {};
    window.env.experimentId = $scope.exp.id;

    window.env.getValue = function(id) {
      console.dir($scope.exp);
      if ($scope.exp[id]) {
        return $scope.exp[id];
      }
      return null;
    };


    window.db = {
      // saveEvent: 
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
        return $scope.exp.groups[0];
      },
      getEndOfDayReferredExperimentGroup: function() {
        return $scope.exp.groups[1];
      }
    };

  }
]);
