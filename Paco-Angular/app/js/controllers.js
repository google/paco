var app = angular.module('pacoControllers', []);


//TODO(ispiro):Move these into a factory or service
var groupTemplate = {
  actionTriggers: [],
  inputs: []
};
var scheduleTriggerTemplate = {
  type: 'scheduleTrigger',
  actions: [{}],
  schedules: [{}]
};
var eventTriggerTemplate = {
  type: 'interruptTrigger',
  actions: [{}]
};
var scheduleTypes = ['Daily', 'Weekdays', 'Weekly', 'Monthly',
  'Random sampling (ESM)', 'Self Report'
];

var actionTypes = ['Create notification to participate',
  'Create notification message',
  'Log data',
  'Execute script'
];

app.controller('ExperimentCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {

  $scope.experimentIdx = parseInt($routeParams.experimentIdx);
  $scope.selectedIndex = 1;
  $scope.loaded = false;
http://localhost:8080/experiments?id=5629499534213120

  $http.get('/experiments?id=5629499534213120').success(function(data) {
  //$http.get('js/experiment.json').success(function(data) {
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

  $scope.save = function() {
    var json = JSON.stringify($scope.experiment);
    var result = $http.post('/save', json);
    result.success(function(data, status, headers, config) {
      alert("success");
    });
    result.error(function(data, status, headers, config) {
      alert( "failure message: " + JSON.stringify({data: data}));
    });
  }

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

  $scope.scheduleTypes = scheduleTypes;

  $scope.getType = function(idx) {
    return $scope.scheduleTypes[idx];
  };

  $scope.showSchedule = function(event, schedule) {
    $mdDialog.show({
      targetEvent: event,
      templateUrl: 'partials/schedule.html',
      locals: {
        schedule: schedule
      },
      controller: 'ScheduleCtrl'
    });
  };

  $scope.showAction = function(event, action) {
    $mdDialog.show({
      targetEvent: event,
      templateUrl: 'partials/action.html',
      locals: {
        action: action
      },
      controller: 'ActionCtrl'
    });
  };


}]);




app.controller('ActionCtrl', ['$scope', '$mdDialog', 'action', function($scope, $mdDialog, action) {

  $scope.action = action;
  $scope.actionTypes = actionTypes;

  $scope.hide = function() {
    $mdDialog.hide();
  };

}]);




app.controller('ScheduleCtrl', ['$scope', '$mdDialog', 'schedule', function($scope, $mdDialog, schedule) {

  $scope.schedule = schedule;
  $scope.scheduleTypes = scheduleTypes;
  $scope.weeksOfMonth = ["First", "Second", "Third", "Fourth", "Fifth"];
  $scope.esmPeriods = ["Day", "Week", "Month"];
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
    times.splice(idx + 1, 0, {
      'fixedTimeMillisFromMidnight': 0
    });
  };

  $scope.remove = function(arr, idx) {
    arr.splice(idx, 1);
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


app.controller('SummaryCtrl', ['$scope', function($scope) {

  $scope.getActionSummary = function() {
    if ($scope.action.actionCode !== undefined) {
      return actionTypes[$scope.action.actionCode];
    } else {
      return 'Undefined';
    }
  };

  $scope.getScheduleSummary = function() {
    var sched = $scope.schedule;
    var str = '';

    //ispiro:using === for these comparisons breaks on schedule edit
    if (sched.scheduleType == 0) {
      if (sched.repeatRate == 1) {
        str += 'Every day';
      } else {
        str += 'Every ' + sched.repeatRate + ' days'
      }
    } else if (sched.scheduleType == 1) {
      str += 'Every weekday';
    } else if (sched.scheduleType == 2) {
      if (sched.repeatRate == 1) {
        str += 'Every week';
      } else {
        str += 'Every ' + sched.repeatRate + ' weeks'
      }
    } else if (sched.scheduleType == 3) {
      if (sched.repeatRate == 1) {
        str += 'Every month';
      } else {
        str += 'Every ' + sched.repeatRate + ' months'
      }
    } else if (sched.scheduleType == 4) {
      str += scheduleTypes[4] + ', ' + sched.esmFrequency + ' time';
      if (sched.esmFrequency > 1) {
        str += 's per day';
      } else {
        str += ' per day';
      }
      //TODO(ispiro):Use period when model supports it
    } else if (sched.scheduleType == 5) {
      str = 'Self report only';
    } else {
      str = 'Undefined';
    }

    if (sched.scheduleType >= 0 && sched.scheduleType <= 3) {
      str += ', ' + sched.signalTimes.length;
      if (sched.signalTimes.length == 1) {
        str += ' time each';
      } else {
        str += ' times each';
      }
    }

    return str;
  };

}]);
