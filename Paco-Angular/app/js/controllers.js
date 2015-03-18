var app = angular.module('pacoControllers', []);

app.controller('ExperimentCtrl', ['$scope', '$http', '$routeParams', '$mdDialog', 'config', 
  function($scope, $http, $routeParams, $mdDialog, config) {

  $scope.experimentIdx = parseInt($routeParams.experimentIdx);
  $scope.selectedIndex = 0;
  $scope.loaded = false;

  if ($scope.experimentIdx !== 0) {
    $http.get('/experiments?id=' + $scope.experimentIdx).success(function(data) {
      $scope.experiment = data[0];
      $scope.loaded = true;
      $scope.$broadcast('experimentChange');
    });
    $scope.selectedIndex = 1;
  }

  $http.get('/experiments?mine').success(function(data) {
    $scope.experiments = data;
  });


  $scope.saveExperiment = function() {
    $http.post('/experiments', $scope.experiment).success(function(data) {
      if (data.length > 0) {
        if (data[0].status === true) {
          $mdDialog.show(
            $mdDialog.alert()
            .title('Save Status')
            .content('Success!')
            .ariaLabel('Success')
            .ok('OK')
          );

        } else {
          var errorMessage = data[0].errorMessage;
          $mdDialog.show({
            templateUrl: 'partials/error.html',
            locals: {
              errorMessage: errorMessage
            },
            controller: 'ErrorCtrl'
          });
        }
      }
    }).error(function(data, status, headers, config) {
      console.log(data);
      console.log(status);
      console.log(headers);
      console.log(config);
    });
  };

  $scope.addGroup = function() {
    $scope.experiment.groups.push(config.groupTemplate);
  }

  $scope.addInput = function(inputs, event, expandFn) {
    inputs.push({});
    expandFn(true);
    event.stopPropagation();
  }

  $scope.addScheduleTrigger = function(triggers, event, expandFn) {
    triggers.push(config.scheduleTriggerTemplate);
    expandFn(true);
    event.stopPropagation();
  }

  $scope.addEventTrigger = function(triggers, event, expandFn) {
    triggers.push(config.eventTriggerTemplate);
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
      alert('success');
    });
    result.error(function(data, status, headers, config) {
      alert( 'failure message: ' + JSON.stringify({data: data}));
    });
  }

}]);




app.controller('InputsCtrl', ['$scope', 'config', function($scope, config) {

  $scope.responseTypes = config.responseTypes;

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


app.controller('TriggerCtrl', ['$scope', '$mdDialog', 'config', 
  function($scope, $mdDialog, config) {

  $scope.scheduleTypes = config.scheduleTypes;

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

  $scope.showCue = function(event, cue) {
    $mdDialog.show({
      targetEvent: event,
      templateUrl: 'partials/cue.html',
      locals: {
        cue: cue
      },
      controller: 'CueCtrl'
    });
  };
}]);


app.controller('ActionCtrl', ['$scope', '$mdDialog', 'config', 'action', 
  function($scope, $mdDialog, config, action) {

  $scope.action = action;
  $scope.actionTypes = config.actionTypes;

  $scope.hide = function() {
    $mdDialog.hide();
  };

  $scope.$watch('action.actionCode', function(newValue, oldValue) {
    if (newValue) {
      action.actionCode = parseInt(action.actionCode);
    }
  });
}]);


app.controller('CueCtrl', ['$scope', '$mdDialog', 'config', 'cue', 
  function($scope, $mdDialog, config, cue) {

  $scope.cue = cue;
  $scope.cueTypes = config.cueTypes;

  $scope.hide = function() {
    $mdDialog.hide();
  };

  $scope.$watch('cue.cueCode', function(newValue, oldValue) {
    if (newValue) {
      cue.cueCode = parseInt(cue.cueCode);
    }
  });
}]);


app.controller('ErrorCtrl', ['$scope', '$mdDialog', 'config', 'errorMessage', 
  function($scope, $mdDialog, config, errorMessage) {

  $scope.errorMessage = errorMessage;
  var lines = errorMessage.split('\n');
  var errors = [];
  for (var i = 0; i < lines.length; i++) {
    if (lines[i].indexOf("ERROR:") === 0) {
      errors.push(lines[i].substr(7));
    }
  }
  $scope.errors = errors;

  $scope.hide = function() {
    $mdDialog.hide();
  };
}]);


app.controller('ScheduleCtrl', ['$scope', '$mdDialog', 'config', 'schedule', 
  function($scope, $mdDialog, config, schedule) {

  $scope.schedule = schedule;
  $scope.scheduleTypes = config.scheduleTypes;
  $scope.weeksOfMonth = config.weeksOfMonth;
  $scope.esmPeriods = config.esmPeriods;
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

  $scope.$watch('schedule.scheduleType', function(newValue, oldValue) {
    if (newValue) {
      schedule.scheduleType = parseInt(schedule.scheduleType);
      $scope.schedule.signalTimes = [{}];
    }
  });
}]);


app.controller('SummaryCtrl', ['$scope', 'config', function($scope, config) {

  $scope.getActionSummary = function() {
    if ($scope.action.actionCode !== undefined) {
      return config.actionTypes[$scope.action.actionCode];
    } else {
      return 'Undefined';
    }
  };

  $scope.getCueSummary = function() {
    if ($scope.cue.cueCode !== undefined) {
      return config.cueTypes[$scope.cue.cueCode];
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
      } else if (sched.repeatRate != undefined) {
        str += 'Every ' + sched.repeatRate + ' days'
      }
    } else if (sched.scheduleType == 1) {
      str += 'Every weekday';
    } else if (sched.scheduleType == 2) {
      if (sched.repeatRate == 1) {
        str += 'Every week';
      } else if (sched.repeatRate != undefined) {
        str += 'Every ' + sched.repeatRate + ' weeks'
      }
    } else if (sched.scheduleType == 3) {
      if (sched.repeatRate == 1) {
        str += 'Every month';
      } else if (sched.repeatRate != undefined) {
        str += 'Every ' + sched.repeatRate + ' months'
      }
    } else if (sched.scheduleType == 4) {
      str += config.scheduleTypes[4] + ', ' + sched.esmFrequency + ' time';
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
      if (sched.signalTimes) {
        str += ', ' + sched.signalTimes.length;
        if (sched.signalTimes.length == 1) {
          str += ' time each';
        } else {
          str += ' times each';
        }
      }
    }

    return str;
  };
}]);
