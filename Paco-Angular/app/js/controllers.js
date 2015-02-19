var app = angular.module('pacoControllers', []);




app.controller('ExperimentCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {

  $scope.responseTypes = ["likert", "likert_smileys", "open text", "list", "photo", "location"];
  $scope.experimentIdx = parseInt($routeParams.experimentIdx);
  $scope.previousIdx = -1;
  $scope.nextIdx = -1;
  $scope.selectedIndex = 1;

  $scope.removeInput = function(idx) {
    $scope.experiment.inputs.splice(idx, 1);
  };

  $scope.removeChoice = function(input, idx) {
    input.splice(idx, 1);
  };

  $http.get('js/experiments.json').success(function(data) {
    $scope.experiment = data[$scope.experimentIdx];

    if ($scope.experimentIdx < data.length - 1) {
      $scope.nextIdx = $scope.experimentIdx + 1;
    }

    if ($scope.experimentIdx > 0) {
      $scope.previousIdx = $scope.experimentIdx - 1;
    }
  });
}]);

app.controller('GroupCtrl', ['$scope', function($scope) {
  $scope.expand = true;

  $scope.toggleExpand = function() {
    $scope.expand = !$scope.expand;
  }
}]);


app.controller('ScheduleCtrl', ['$scope', function($scope) {

  $scope.scheduleTypes = ["Daily", "Weekdays", "Weekly", "Monthly", "Random sampling (ESM)", "Self Report"];
  $scope.repeatRates = range(0, 30);

  function range(start, end) {
    var arr = [];
    for (var i = start; i <= end; i++) {
      arr.push(i);
    }
    return arr;
  }

  $scope.removeTime = function(times, idx) {
    times.splice(idx, 1);
  };

  $scope.addTime = function(times, idx) {
    times.splice(idx + 1, 0, {'fixedTimeMillisFromMidnight': 0});
  };

}]);


app.directive('milli', function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attr, ngModel) {
      var UTCOffset = 60 * 1000 * (new Date()).getTimezoneOffset();

      function dateToMillis(text) {
        var dd = Date.parse(text);
        return dd - UTCOffset;
      }

      function millisToDate(text) {
        return new Date(parseInt(text) + UTCOffset);
      }
      ngModel.$parsers.push(dateToMillis);
      ngModel.$formatters.push(millisToDate);
    }
  };
});
