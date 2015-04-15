pacoApp.controller('HomeCtrl', ['$scope', '$http', '$routeParams', '$location',
  function($scope, $http, $routeParams, $location) {
    $scope.newExperiment = false;
    $scope.experimentId = false;
    $scope.tabIndex = -1;

    $http.get('/userinfo').success(function(data) {

      // For now, make sure email isn't bobevans999@gmail for local dev testing
      if (data.user && data.user !== 'yourGoogleEmail@here.com') {
        $scope.user = data.user;

        $http.get('/experiments?mine').success(function(data) {
          $scope.experiments = data;
        });
      } else {
        $scope.loginURL = data.login;
      }

    }).error(function(data) {
      console.log(data);
    });


    if (angular.isDefined($routeParams.experimentId)) {
      if ($routeParams.experimentId == 'new') {
        $scope.newExperiment = true;
        $scope.experimentId = -1;
      } else {
        $scope.experimentId = parseInt($routeParams.experimentId);
      }
    }

    $scope.addExperiment = function() {
      $location.path('/experiment/new');
    };
  }
]);


pacoApp.controller('ExperimentCtrl', ['$scope', '$http',
  '$mdDialog', '$filter', 'config', 'template', '$location',
  function($scope, $http, $mdDialog, $filter, config, template, 
      $location) {
    $scope.ace = {};
    $scope.feedbackTypes = config.feedbackTypes;
    $scope.tabs = config.tabs;
    $scope.state = {tabId: 0};

    if ($location.hash()) {
      var newTabId = config.tabs.indexOf($location.hash());
      if (newTabId != -1) {
        $scope.state.tabId = newTabId;
      }
    }

    if ($scope.experimentId == -1) {
      $scope.experiment = angular.copy(template.experiment);

      if ($scope.user) {
        $scope.experiment.creator = $scope.user;
        $scope.experiment.contactEmail = $scope.user;
        $scope.experiment.admins.push($scope.user);
      }
    } else if ($scope.experimentId) {
      $http.get('/experiments?id=' + $scope.experimentId).success(
        function(data) {
          $scope.experiment = data[0];
          //
          $scope.prepareAce();
        });
    }

    $scope.$watch('user', function(newValue, oldValue) {
      if ($scope.newExperiment) {
        $scope.experiment.creator = $scope.user;
        $scope.experiment.contactEmail = $scope.user;
        $scope.experiment.admins = [$scope.user];
      }
    });

    // TODO(ispiro): figure out a way to disabled the default # scrolling 
    $scope.$watch('state.tabId', function(newValue, oldValue) {
      if ($scope.state.tabId == 0) {
        $location.hash('');
      } else if ($scope.state.tabId > 0) {
        $location.hash(config.tabs[$scope.state.tabId]);
      }
    });

    // Ace is loaded when the Source tab is selected so get pretty JSON here
    $scope.prepareAce = function(editor) {
      if (editor) {
        editor.$blockScrolling = 'Infinity';
      }

      $scope.ace = {
        JSON: JSON.stringify($scope.experiment, null, '  '),
        error: false
      };
    };

    $scope.$watch('ace.JSON', function(newValue, oldValue) {
      try {
        var exp = JSON.parse(newValue);
      } catch (e) {
        $scope.ace.error = true;
        return false;
      }
      $scope.ace.error = false;
      $scope.experiment = exp;
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

            if ($scope.newExperiment) {
              $location.path('/experiment/' + data[0].experimentId);
            }

          } else {
            console.dir(data);
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
      });
    };

    $scope.addGroup = function() {
      $scope.experiment.groups.push(angular.copy(template.group));
    };

    $scope.remove = function(arr, idx) {
      arr.splice(idx, 1);
    };

    $scope.convertBack = function(event) {
      var json = event.target.value;
      $scope.experiment = JSON.parse(json);
    };
  }
]);


pacoApp.controller('GroupCtrl', ['$scope', 'template',
  function($scope, template) {

    $scope.addInput = function(event, expandFn) {
      $scope.group.inputs.push({});
      expandFn(true);
      event.stopPropagation();
    };

    $scope.addScheduleTrigger = function(event, expandFn) {
      $scope.group.actionTriggers.push(angular.copy(template.scheduleTrigger));
      expandFn(true);
      event.stopPropagation();
    };

    $scope.addEventTrigger = function(event, expandFn) {
      $scope.group.actionTriggers.push(angular.copy(template.eventTrigger));
      expandFn(true);
      event.stopPropagation();
    };

    $scope.aceLoaded = function(editor) {
      editor.$blockScrolling = 'Infinity';
    };


    $scope.$watchCollection('group.actionTriggers', function() {
      for (var i = 0; i < $scope.group.actionTriggers.length; i++) {
        if ($scope.group.actionTriggers[i].type == 'scheduleTrigger') {
          $scope.hasScheduleTrigger = true;
          return;
        }
      }
      $scope.hasScheduleTrigger = false;
    });

  }
]);


pacoApp.controller('InputCtrl', ['$scope', 'config', function($scope, config) {

  $scope.responseTypes = config.responseTypes;

  $scope.$watch('input.responseType', function(newValue, oldValue) {
    if ($scope.input.responseType === 'list' &&
      $scope.input.listChoices === undefined) {
      $scope.input.listChoices = [''];
    }
  });

  $scope.addChoice = function() {
    $scope.input.listChoices.push('');
  }
}]);


pacoApp.controller('TriggerCtrl', ['$scope', '$mdDialog', 'config', 'template',
  function($scope, $mdDialog, config, template) {

    $scope.scheduleTypes = config.scheduleTypes;

    $scope.addAction = function(event) {
      var action = angular.copy(template.action);
      //action.id = $scope.trigger.actions.length;
      $scope.trigger.actions.push(action);
    }

    $scope.addSchedule = function(event) {
      $scope.trigger.schedules.push(angular.copy(template.schedule));
    }

   $scope.addCue = function(event) {
      $scope.trigger.cues.push(angular.copy(template.cue));
    }

    $scope.showSchedule = function(event, schedule) {
      $mdDialog.show({
        templateUrl: 'partials/schedule.html',
        locals: {
          schedule: schedule
        },
        controller: 'ScheduleCtrl'
      });
    };

    $scope.showAction = function(event, action) {
      $mdDialog.show({
        templateUrl: 'partials/action.html',
        locals: {
          action: action
        },
        controller: 'ActionCtrl'
      });
    };

    $scope.showCue = function(event, cue) {
      $mdDialog.show({
        templateUrl: 'partials/cue.html',
        locals: {
          cue: cue
        },
        controller: 'CueCtrl'
      });
    };
  }
]);


pacoApp.controller('ActionCtrl', ['$scope', '$mdDialog', 'config', 'action',
  function($scope, $mdDialog, config, action) {

    $scope.action = action;

    if ($scope.action.actionCode == undefined) {
      $scope.action.actionCode = '';
    } else {
      $scope.action.actionCode += '';
    }

    $scope.actionTypes = config.actionTypes;

    $scope.hide = function() {
      $mdDialog.hide();
    };

    $scope.aceLoaded = function(editor) {
      editor.$blockScrolling = 'Infinity';
    };
  }
]);


pacoApp.controller('CueCtrl', ['$scope', '$mdDialog', 'config', 'cue',
  function($scope, $mdDialog, config, cue) {

    $scope.cue = cue;

    if ($scope.cue.cueCode == undefined) {
      $scope.cue.cueCode = '';
    } else {
      $scope.cue.cueCode += '';
    }

    $scope.cueTypes = config.cueTypes;

    $scope.hide = function() {
      $mdDialog.hide();
    };
  }
]);


pacoApp.controller('ErrorCtrl', ['$scope', '$mdDialog', 'config',
  'errorMessage',
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
  }
]);


pacoApp.controller('ScheduleCtrl', ['$scope', '$mdDialog', 'config', 'template',
  'schedule',
  function($scope, $mdDialog, config, template, schedule) {

    $scope.schedule = schedule;

    if ($scope.schedule.repeatRate != undefined) {
      $scope.schedule.repeatRate += '';
    }
    
    if ($scope.schedule.esmPeriodInDays == undefined) {
      $scope.schedule.esmPeriodInDays = '';
    } else {
      $scope.schedule.esmPeriodInDays += '';
    }

    // Force scheduleType to be a string
    $scope.schedule.scheduleType += '';

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
      times.splice(idx + 1, 0, angular.copy(template.signalTime));
    };

    $scope.remove = function(arr, idx) {
      arr.splice(idx, 1);
    };

    $scope.hide = function() {
      $mdDialog.hide();
    };

    $scope.parseInt = function(number) {
      return parseInt(number, 10);
    }

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
        if ($scope.schedule.signalTimes == undefined) {
          $scope.schedule.signalTimes = [angular.copy(template.signalTime)];
          $scope.schedule.repeatRate = '';
        }
      }
    });
  }
]);


pacoApp.controller('SummaryCtrl', ['$scope', 'config', function($scope, config) {

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
      str += config.scheduleTypes[4] + ', ' + sched.esmFrequency +
        ' time';
      if (sched.esmFrequency > 1) {
        str += 's per ';
      } else {
        str += ' per ';
      }
      str += config.esmPeriods[sched.esmPeriodInDays];

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
