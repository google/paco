var app = angular.module('pacoControllers', []);




app.controller('ExperimentCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {

  $scope.experimentIdx = parseInt($routeParams.experimentIdx);
  $scope.selectedIndex = 1;
  $scope.loaded = false;

  $http.get('js/experiment.json').success(function(data) {
    $scope.experiment = data[$scope.experimentIdx];
    //$scope.db = data;
    
    // if ($scope.experimentIdx < data.length - 1) {
    //   $scope.nextIdx = $scope.experimentIdx + 1;
    // }

    // if ($scope.experimentIdx > 0) {
    //   $scope.previousIdx = $scope.experimentIdx - 1;
    // }
    
    $scope.loaded = true;
    $scope.$broadcast('experimentChange');
  });

  $scope.addInput = function(inputs,event) {
    inputs.push({});
    event.stopPropagation();
  }


  $scope.removeGroup = function(groups, idx) {
    groups.splice(idx, 1);
  };
}]);


app.controller('InputsCtrl', ['$scope', function($scope) {

  $scope.responseTypes = ["likert", "likert_smileys", "open text", "list", "photo", "location"];

  $scope.removeInput = function(input, idx) {
    input.splice(idx, 1);
  };

  $scope.removeChoice = function(input, idx) {
    input.splice(idx, 1);
  };

  $scope.addChoice = function(input) {
    if (input.listChoices === undefined) {
      input.listChoices = [];
    }
    input.listChoices.push('');
  }
}]);


app.controller('ExpandCtrl', ['$scope', function($scope) {
  $scope.expand = false;

  $scope.toggleExpand = function() {
    $scope.expand = !$scope.expand;
  }

  $scope.$on('experimentChange', function(event, args) {
    $scope.expand = true;
  });
}]);


app.controller('ScheduleCtrl', ['$scope', function($scope) {

  $scope.scheduleTypes = ["Daily", "Weekdays", "Weekly", "Monthly", "Random sampling (ESM)", "Self Report"];
  $scope.weeksOfMonth = ["First", "Second", "Third", "Fourth", "Fifth"];
  $scope.esmPeriods = ["Day","Week","Month"];
  $scope.repeatRates = range(1, 30);
  $scope.daysOfMonth = range(1, 31);

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

  $scope.$watchCollection('experiment.schedule.days', function(days) {

    var sum = 0;

    if (days) {
      for (var i = 0; i < 7; i++) {
        if ($scope.experiment.schedule.days[i]) {
          sum += Math.pow(2, i);
        }
      }
      $scope.experiment.schedule.weekDaysScheduled = sum;
    }
  });
  
  $scope.$watch('experiment.schedule.scheduleType', function(times) {
    if (times) {
      $scope.experiment.schedule.signalTimes = [{}]; 
    }
  });
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
