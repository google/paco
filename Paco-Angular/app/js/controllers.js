var app = angular.module('pacoControllers', []);

var groupTemplate = {actionTriggers:[],inputs:[]};
var scheduleTriggerTemplate = {type:'scheduleTrigger', actions:[{}], schedules:[{}]};
var eventTriggerTemplate = {type:'interruptTrigger', actions:[{}]};


app.controller('ExperimentCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {

  $scope.experimentIdx = parseInt($routeParams.experimentIdx);
  $scope.selectedIndex = 1;
  $scope.loaded = false;

  $http.get('js/experiment.json').success(function(data) {
    $scope.experiment = data[$scope.experimentIdx];    
    $scope.loaded = true;
    $scope.$broadcast('experimentChange');
  });

  $scope.addGroup = function() {
    $scope.experiment.groups.push(groupTemplate);
  }

  $scope.addInput = function(inputs, event, expandFn) {
    inputs.push({});
    expandFn(true);
    event.stopPropagation();
  }

  $scope.addScheduleTrigger = function(triggers, event, expandFn) {
    triggers.push(scheduleTriggerTemplate);
    expandFn(true);
    event.stopPropagation();
  }

  $scope.addEventTrigger = function(triggers, event, expandFn) {
    triggers.push(eventTriggerTemplate);
    expandFn(true);
    event.stopPropagation();
  }

  $scope.remove = function(arr, idx) {
    arr.splice(idx, 1);
  };
}]);




app.controller('InputsCtrl', ['$scope', function($scope) {

  $scope.responseTypes = ["likert", "likert_smileys", "open text", "list", "photo", "location"];

  $scope.addChoice = function(input) {
    if (input.listChoices === undefined) {
      input.listChoices = [];
    }
    input.listChoices.push('');
  }
}]);




app.controller('ExpandCtrl', ['$scope', function($scope) {
  $scope.expand = false;

  $scope.toggleExpand = function(flag) {
    if (flag === undefined) {
      $scope.expand = !$scope.expand;
    } else {
      $scope.expand = flag;
    }
  }

  $scope.$on('experimentChange', function(event, args) {
    $scope.expand = true;
  });
}]);






app.controller('TriggerCtrl', ['$scope', '$mdDialog', function($scope, $mdDialog) {

  $scope.scheduleTypes = ["Daily", "Weekdays", "Weekly", "Monthly", "Random sampling (ESM)", "Self Report"];
  
  $scope.getType = function(idx) {
    return $scope.scheduleTypes[idx];
  };

  $scope.getDescription = function(trigger) {

    var str = '';

    if (trigger.scheduleType === 0) {
      if (trigger.repeatRate === 1) {
        str = 'Every day';
      } else {
        str = 'Every ' + trigger.repeatRate + ' days'
      }
    }

    if (trigger.scheduleType === 1) {
      str = 'Every weekday';
    }

    if (trigger.scheduleType === 2) {
      if (trigger.repeatRate === 1) {
        str = 'Every week';
      } else {
        str = 'Every ' + trigger.repeatRate + ' weeks'
      }
    }




    if (trigger.scheduleType === 4) {
      str = $scope.scheduleTypes[trigger.scheduleType];
    }
    return str;
  };

  $scope.showSchedule = function(event,sc) {
    $mdDialog.show({
      parent: angular.element(document.body),
      targetEvent: event,
      templateUrl: 'partials/schedule.html',
      locals: {
        schedule: sc
      },
      controller: 'ScheduleCtrl'
    })
    .then(function(answer) {
      $scope.alert = 'You said the information was "' + answer + '".';
    }, function() {
      $scope.alert = 'You cancelled the dialog.';
    });
  };
}]);


app.controller('ScheduleCtrl', ['$scope', '$mdDialog', 'schedule', function($scope, $mdDialog, schedule) {
  
  $scope.schedule = schedule;
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

  $scope.addTime = function(times, idx) {
    times.splice(idx + 1, 0, {'fixedTimeMillisFromMidnight': 0});
  };

  $scope.hide = function() {
    $mdDialog.hide();
  };

  $scope.$watchCollection('schedule.days', function(days) {
    var sum = 0;
    if (days) {
      for (var i = 0; i < 7; i++) {
        if ($scope.schedule.days[i]) {
          sum += Math.pow(2, i);
        }
      }
      $scope.schedule.weekDaysScheduled = sum;
    }
  });
  
  $scope.$watch('schedule.scheduleType', function(times) {
    if (times) {
      $scope.schedule.signalTimes = [{}]; 
    }
  });
}]);