pacoApp.controller('HomeCtrl', ['$scope', '$http', '$location', 'experimentService',
  function ($scope, $http, $location, experimentService) {
    $scope.newExperiment = false;
    $scope.experimentId = false;
    $scope.tabIndex = -1;
    $scope.loaded = false;
    $scope.edit = false;

    $scope.home = function () {
      document.location = '/';
    };

    $scope.scrolling = function (flag) {
      if (flag) {
        angular.element(document.body).removeClass('no-scroll');
      } else {
        angular.element(document.body).addClass('no-scroll');
      }
    };

    $scope.forceHttps = function () {
      var devMode = ($location.host() === 'localhost' || $location.host() === '127.0.0.1');
      var insecure = ($location.protocol() === 'http');

      if (!devMode && insecure) {
        var URL = document.location;
        document.location = URL.replace('http://', 'https://');
      }
    };

    $scope.forceHttps();

    $scope.$on('$viewContentLoaded', function (event) {
      $scope.scrolling(true);
    });

    //updated http get call to suit the new angular version 1.6.5
    $http.get('/userinfo').then(function onSuccess(response) {
      $scope.loaded = true;

      // Make sure email isn't the dev email address
      if (response.data.user && response.data.user !== 'bobevans999@gmail.com') {
        $scope.user = response.data.user;
      } else {
        $scope.loginURL = response.data.login;
      }
      $scope.logoutURL = response.data.logout;

    }, function onError(response) {
      console.log(response.data);
    });
  }]);

pacoApp.controller('ExperimentCtrl', [
  '$scope',
  '$mdDialog',
  '$filter',
  'config',
  'template',
  '$routeParams',
  '$location',
  'experimentService',
  'PacoConstantsService',
  function ($scope, $mdDialog, $filter, config, template, $routeParams, $location, experimentService, PacoConstantsService) {
    $scope.ace = {};
    $scope.useOldColumns = PacoConstantsService.useOldColumns;
    $scope.feedbackTypes = config.feedbackTypes;
    $scope.ringtones = config.ringtones;
    $scope.tabs = config.editTabs;
    $scope.dataDeclarations = config.dataDeclarations;
    $scope.declared = [];

    $scope.state = {
      tabId: 0,
      groupIndex: null
    };

    // temporarily comment this because it prevents loading experiments
    // directly
    // fix for bug https://github.com/google/paco/issues/1448
    // regresses bug https://github.com/google/paco/issues/1272
    // if ($scope.user === undefined) {
    // $location.path('/');
    // }

    if ($location.hash()) {
      var newTabId = config.editTabs.indexOf($location.hash());
      if (newTabId !== -1) {
        $scope.state.tabId = newTabId;
      }
    }

    if (angular.isDefined($routeParams.editId)) {
      if ($routeParams.editId === 'new') {
        $scope.newExperiment = true;
        $scope.experimentId = -1;
      } else {
        $scope.experimentId = parseInt($routeParams.editId, 10);
      }
      $scope.edit = true;
    }

    if (angular.isDefined($routeParams.copyId)) {
      $scope.newExperiment = true;
      $scope.edit = true;
      $scope.experimentId = parseInt($routeParams.copyId);
    }

    if (angular.isDefined($routeParams.csvExperimentId)) {
      $scope.csvExperimentId = parseInt($routeParams.csvExperimentId, 10);
    }

    if (angular.isDefined($routeParams.respondExperimentId)) {
      $scope.respondExperimentId = parseInt($routeParams.respondExperimentId, 10);
      $scope.experimentId = $scope.respondExperimentId;
    }

    if (angular.isDefined($routeParams.experimentId)) {
      $scope.experimentId = parseInt($routeParams.experimentId, 10);
    }

    if ($scope.experimentId === -1) {
      $scope.experiment = angular.copy(template.experiment);
      // TODO
//      $scope.experiment.groups = angular.copy(template.group);
      var grObject = angular.copy(template.group);
      var matchingInputObj = [];
      matchingInputObj = template.inputsForPredefinedGroupSystem;
      grObject.fixedDuration = false;
      grObject.groupType = 'SYSTEM';
      grObject.name = 'SYSTEM';
      
      angular.forEach(matchingInputObj, function(value, key){
        grObject.inputs.push(value); 
      })
      $scope.experiment.groups.push(grObject);
      
      if ($scope.user) {
        $scope.experiment.creator = $scope.user;
        $scope.experiment.contactEmail = $scope.user;
        $scope.experiment.admins = [$scope.user];
      }
    } else if ($scope.experimentId) {
      experimentService.getExperiment($scope.experimentId).then(function (response) {
        var data = response.data.results;
        $scope.experiment = data[0];
        $scope.experiment0 = angular.copy(data[0]);
        $scope.prepareSourceAce();

        if ($scope.newExperiment) {
          $scope.experiment.title = 'Copy of ' + $scope.experiment.title;
          $scope.experiment.id = null;
          $scope.experiment.version = 1;

          if ($scope.user) {
            $scope.experiment.creator = $scope.user;
            $scope.experiment.contactEmail = $scope.user;
            $scope.experiment.admins = [$scope.user];
            $scope.experiment.publishedUsers = [];
            $scope.experiment.published = false;
          }
        }
      });
    }

    $scope.$watch('user', function (newValue, oldValue) {
      if (newValue && $scope.newExperiment && $scope.experiment) {
        $scope.experiment.creator = $scope.user;
        $scope.experiment.contactEmail = $scope.user;
        $scope.experiment.admins = [$scope.user];
      }
    });

    $scope.$watch('state.tabId', function (newValue, oldValue) {
      if (config.editTabs[$scope.state.tabId] === 'source') {
        $scope.prepareSourceAce();
      }

      if ($scope.state.tabId === 0) {
        $location.hash('');
      } else if ($scope.state.tabId > 0) {
        $location.hash(config.editTabs[$scope.state.tabId]);
      }
    });

    $scope.lineCount = function (str) {
      var lines = str.split('\n');
      return lines.length;
    }

    $scope.$watch('experiment.groups', function (newValue, oldValue) {
      var groups = [];
      if (newValue) {
        $scope.admin = ($scope.experiment.admins.indexOf($scope.user) !== -1);
       
        for (var groupId in $scope.experiment.groups) {
          var group = $scope.experiment.groups[groupId];
          if (group.inputs.length > 0 && (group.groupType === "SURVEY" || ($scope.useOldColumns && !group.groupType))) {
            groups.push(group);
          } 
        }
        $scope.respondableGroups = groups;
        if ($scope.respondableGroups.length === 1) {
          $scope.state.groupIndex = 0;
        }
      }
    });

    // Need to set blockScrolling to Infinity to supress Ace deprecation
    // warning
    $scope.aceInfinity = function (editor) {
      if (editor) {
        editor.$blockScrolling = 'Infinity';
      }
    };

    // Ace is loaded when the Source tab is selected so get pretty JSON here
    $scope.prepareSourceAce = function (editor) {
      $scope.aceInfinity(editor);

      $scope.ace = {
        JSON: angular.toJson($scope.experiment, true),
        error: false
      };
    };

    $scope.$watch('ace.JSON', function (newValue, oldValue) {
      if (!oldValue || oldValue == newValue) {
        return;
      }
      try {
        var exp = JSON.parse(newValue);
      } catch (e) {
        $scope.ace.error = true;
        return false;
      }
      $scope.experiment = exp;
      $scope.ace.error = false;
      $scope.ace.height = $scope.lineCount(newValue) * 16;
    });

    $scope.discardChanges = function () {
      if ($scope.newExperiment) {
        $location.path('/experiments/');
      } else {
        $scope.experiment = angular.copy($scope.experiment0);
      }
    }

    $scope.saveExperiment = function () {
      $scope.cancelSave = false;
      $mdDialog.show(
          $mdDialog.alert().title('Save Status').content('Saving to remote PACO server').ariaLabel('Save Status').ok(
              'Cancel')).then(function () {
        $scope.cancelSave = true;
      });

      experimentService.saveExperiment($scope.experiment).then(function (response) {

        var data = response.data;

        // Save may succeed if post request was in flight when canceled
        if ($scope.cancelSave) {
          return;
        }
        $mdDialog.cancel();
        if (data.length > 0) {
          if (data[0].status === true) {

            $scope.experiment.version++;

            // Need to manually update the Ace content after version change
            // in case the user modified the experiment via source view.
            // Otherwise, Ace will retain a stale version number and any
            // subsequent saves will fail.
            $scope.prepareSourceAce();

            $scope.experiment0 = angular.copy($scope.experiment);

            if ($scope.newExperiment) {
              $location.path('/edit/' + data[0].experimentId);
            }
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
      });
    };
    $scope.closeDialog = function() { 
      $mdDialog.cancel();
    };
    
    $scope.addGroup = function (groupType) {
      var grObject = angular.copy(template.group);
      var crGrpsInExperiment = $scope.experiment.groups;
      for (var groupId in crGrpsInExperiment) {
        var crGroup = $scope.experiment.groups[groupId];
        $scope.hiding=true;
      }
      grObject.groupType = groupType;
      $scope.experiment.groups.push(grObject); 
      
    };

    $scope.showSensorGroupPopup = function () {
      $mdDialog.show({
        controller: function () {
        return self;
      },
      controllerAs: 'ExperimentCtrl',
      templateUrl: 'partials/sensorgroup.html',
      scope: $scope,
      preserveScope: true,
      parent: angular.element(document.body),
      clickOutsideToClose: true
      });
    };
    $scope.dateToString = function (d) {
      var s = d.getUTCFullYear() + '/' + (d.getMonth() + 1) + '/' + d.getDate();
      return s;
    };
   
    $scope.addSensorGroup = function (groupType) {
      experiment = $scope.experiment;
      var grObject = angular.copy(template.group);
      var matchingInputObj = [];
      if (groupType === 'APPUSAGE_ANDROID') {
        matchingInputObj = template.inputsForPredefinedGroupAppUsageAndroid;
        grObject.fixedDuration = true;
        grObject.logActions = true;
      } else if (groupType === 'PHONESTATUS') {
        matchingInputObj = template.inputsForPredefinedGroupPhoneStatus;
        grObject.fixedDuration = true;
        grObject.logShutdown = true;
      } else if (groupType === 'APPUSAGE_DESKTOP') {
         matchingInputObj = template.inputsForPredefinedGroupAppUsageDesktop;
         grObject.fixedDuration = true;
         grObject.logActions = true;
      } else if (groupType === 'APPUSAGE_SHELL') {
        matchingInputObj = template.inputsForPredefinedGroupAppUsageShell;
        grObject.fixedDuration = true;
        grObject.logActions = true;
      }  else if (groupType === 'IDE_IDEA_USAGE') {
         matchingInputObj = template.inputsForPredefinedGroupIdeIdeaUsage;
         grObject.fixedDuration = true;
         grObject.logActions = true;
      }

      if (grObject.fixedDuration) {
        $scope.startDate = new Date();
        $scope.endDate = new Date($scope.startDate.getTime() + (24 * 60 * 60 * 1000));
        grObject.startDate = $scope.dateToString($scope.startDate);      
        grObject.endDate = $scope.dateToString($scope.endDate);
      }
      grObject.groupType = groupType;
      grObject.name = groupType;
      
      angular.forEach(matchingInputObj, function(value, key){
        grObject.inputs.push(value); 
      })
      $scope.experiment.groups.push(grObject);
      $scope.closeDialog();
    }
   
    $scope.remove = function (arr, index) {
      arr.splice(index, 1);
    };

    $scope.swap = function (arr, index1, index2) {
      var temp = arr[index2];
      arr[index2] = arr[index1];
      arr[index1] = temp;
    };

    $scope.convertBack = function (event) {
      var json = event.target.value;
      $scope.experiment = JSON.parse(json);
    };

    $scope.inList = function (item, list) {
      if ($scope.experiment && $scope.experiment.extraDataCollectionDeclarations) {
        var id = parseInt(item);
        if ($scope.experiment.extraDataCollectionDeclarations.indexOf(id) !== -1) {
          return true;
        }
      }
      return false;
    }
  

    $scope.toggleDeclaration = function (item, list) {
      var id = parseInt(item);
      var find = $scope.experiment.extraDataCollectionDeclarations.indexOf(id);

      if (find === -1) {
        $scope.experiment.extraDataCollectionDeclarations.push(id);
      } else {
        $scope.experiment.extraDataCollectionDeclarations.splice(find, 1);
      }
    };
  }]);

pacoApp.controller('ListCtrl', [
  '$scope',
  '$mdDialog',
  '$location',
  'experimentService',
  'config',
  function ($scope, $mdDialog, $location, experimentService, config) {

    $scope.cursor = {};
    $scope.list = {
      'admin': [],
      'joined': [],
      'mine': [],
      'popular': [],
      'new': [],
      'public': []
    };
    $scope.currentAdminSortOrder = 'title_asc';
    $scope.loading = {};
    $scope.state = {};

    if ($location.hash()) {
      var newTabId = config.listTabs.indexOf($location.hash());
      if (newTabId !== -1) {
        $scope.state.listTab = newTabId;
      }
    }

    $scope.$watch('state.listTab', function (newValue, oldValue) {
      if ($scope.state.listTab === 0) {
        $location.hash('');
      } else if ($scope.state.listTab > 0) {
        $location.url('/experiments');
        $location.hash(config.listTabs[$scope.state.listTab]);
      }
    });

    $scope.$watch('user', function (newValue, oldValue) {
      if ($scope.user) {
        $scope.loadLists(true);
      }
    });

    $scope.loadLists = function (reset) {
      $scope.loadAdminList(reset);
      $scope.loadJoinedList(reset);
      $scope.loadJoinableList(reset);
      $scope.loadPopularList(reset);
      $scope.loadNewList(reset);
      $scope.loadAllPublicList(reset);
    };

    $scope.loadList = function (listName, reset, sortName) {
      var cursor = $scope.cursor[listName];
      if (reset === undefined) {
        reset = false;
      } else if (reset) {
        cursor = null;
        $scope.list[listName] = [];
      }

      $scope.loading[listName] = true;
      experimentService.getExperimentList(listName, true, cursor, sortName).then(function (response) {

        if (reset) {
          $scope.cursor[listName] = null;
          $scope.list[listName] = [];
        }

        if (response.data.results.length < response.data.limit) {
          $scope.cursor[listName] = null;
        } else {
          $scope.cursor[listName] = response.data.cursor;
        }

        $scope.list[listName] = $scope.list[listName].concat(response.data.results);

        if (listName === 'joined') {
          $scope.joinedIndex = [];
          for (var i = 0; i < $scope.list['joined'].length; i++) {
            var experiment = $scope.list['joined'][i];
            $scope.joinedIndex.push(experiment.id);
          }
        }

        $scope.loading[listName] = false;
      });
    }

    $scope.loadJoinedList = function (reset) {
      $scope.loadList('joined', reset);
    };

    $scope.loadAdminList = function (reset) {
      $scope.loadList('admin', reset, this.currentAdminSortOrder);
    }

    $scope.loadJoinableList = function (reset) {
      $scope.loadList('mine', reset);
    };

    $scope.loadAllPublicList = function (reset) {
      $scope.loadList('public', reset);
    };

    $scope.loadPopularList = function (reset) {
      $scope.loadList('popular', reset);
    };

    $scope.loadNewList = function (reset) {
      $scope.loadList('new', reset);
    };

    $scope.sortAdminList = function () {
      $scope.currentAdminSortOrder = this.currentAdminSortOrder;
      $scope.loadList('admin', true, this.currentAdminSortOrder);
    }

    $scope.deleteExperiment = function (ev, exp) {
      var confirm = $mdDialog.confirm().parent(angular.element(document.body)).title('Confirm experiment deletion')
          .content('Would you like to delete experiment ' + exp.id + '? (' + exp.title + ')').ariaLabel(
              'Confirm experiment deletion').ok('Delete').cancel('Cancel').targetEvent(ev);
      $mdDialog.show(confirm).then(function () {
        exp.deleting = true;
        experimentService.deleteExperiment(exp.id).then(function (response) {
          $scope.cursor = {};
          $scope.loadLists(true);

          // If we're on the experiment or edit page, change location to home
          if ($location.path().indexOf('/experiment/') === 0 || $location.path().indexOf('/edit/') === 0) {
            $location.path('/');
          }
        });
      });
    };

    $scope.joinExperiment = function ($event, exp) {
      experimentService.joinExperiment(exp).then(function (result) {
        if (result.data && result.data[0].status === true) {
          experimentService.invalidateCachedList('joined');
          $scope.loadJoinedList();

          $mdDialog.show($mdDialog.alert().title('Join Status').content('Success!').ariaLabel('Success').ok('OK'));
        }
      });

      $event.stopPropagation();
    };
  }]);

pacoApp.controller('DataCtrl', [
  '$scope',
  '$mdDialog',
  '$location',
  '$filter',
  '$routeParams',
  'dataService',
  'experimentService',
  'config',
  function ($scope, $mdDialog, $location, $filter, $routeParams, dataService, experimentService, config) {

    $scope.sortColumn = 0;
    $scope.reverseSort = false;
    $scope.loading = null;
    $scope.table = null;
    $scope.showColumn = {};
    $scope.currentView = 'data';
    $scope.restrict = null;
    $scope.anon = false;
    $scope.eventCursor = null;
    $scope.events = null;
    $scope.screenData = null;
    $scope.photoHeader = ''; // TODO handle legacy data with header: 'data:image/jpeg;base64,';
    $scope.photoMarker = '/9j/';
    $scope.audioMarker = 'AAAAGGZ0eXBtc';
    $scope.textDiffMarker = 'SW5';
    $scope.statsDate = new Date();
    $scope.groupNames = [];
    $scope.showGroup = 'all';

    $scope.switchView = function () {
      var newPath = $scope.currentView + '/' + $scope.experimentId;
      if ($scope.userChips) {
        newPath += '/' + $scope.userChips[0];
      }
      $location.path(newPath);
    }

    $scope.setColumn = function (columnId) {
      if ($scope.sortColumn === columnId) {
        $scope.reverseSort = !$scope.reverseSort;
      } else {
        $scope.reverseSort = false;
        $scope.sortColumn = columnId;
      }
    }

    $scope.columnSort = function (row) {
      return row[$scope.sortColumn];
    };

    enableColumns = function (columns) {
      for (var id in columns) {
        $scope.showColumn[columns[id]] = true;
      }
    };

    $scope.loadEvents = function (forceReload) {
      forecReload = typeof forceReload !== 'undefined' ? forceReload : false;
      $scope.loading = true;
      var loadingMore = ($scope.eventCursor !== null && !forceReload);
      if (forceReload) {
        $scope.eventCursor = null;
      }

      dataService.getEvents($scope.experimentId, $scope.restrict, $scope.anon, $scope.showGroup, $scope.eventCursor)
          .then(function (response) {

            $scope.scrolling(false);
            if (response.data) {

              if (response.data.cursor) {
                $scope.eventCursor = response.data.cursor;
              }

              if (response.data.events.length < config.dataPageSize) {
                $scope.eventCursor = null;
              }

              if ($scope.events && !forceReload) {
                $scope.events = $scope.events.concat(response.data.events);
              } else {
                $scope.events = response.data.events;
              }

              if (!$scope.events) {
                $scope.csv = [];
                return;
              }

              var table = $filter('jsonToTable')($scope.events, true);
              $scope.table = table;

              if (table === null) {
                $scope.loading = false;
                return;
              }

              if (!loadingMore) {
                if ($scope.columnOverride) {
                  enableColumns($scope.columnOverride);
                } else {
                  enableColumns(config.dataOrder);
                  enableColumns(table.responseNames);
                }
              }

              $scope.csv = $filter('tableToCsv')(table);
              $scope.loading = false;
            }
          }, function (result) {
            $scope.loading = false;
            $scope.error = {
              code: result.status,
              message: result.message
            };
          });

      $scope.status = 'Requesting response data';
    };

    $scope.showReportOptions = function (experiment) {
      var promise = $mdDialog.show({
        templateUrl: 'partials/report.html',
        preserveScope: true,
        locals: {
          experiment: experiment,
          anonymous: $scope.anon,
        },
        clickOutsideToClose: true,
        controller: 'ReportCtrl'
      });
      promise.then(function (result) {
        $scope.reportData = result.data;
        $scope.reportType = result.type;
      });
    };

    $scope.$watch('statsDate', function (newVal, oldVal) {
      if (oldVal !== newVal) {
        dataService.updateParticipantDateStats($scope.experimentId, $scope.statsDate, $scope.stats);
      }
      ;
    });

    $scope.loadStats = function () {
      $scope.loading = true;
      $scope.stats = null;
      $scope.currentView = 'stats';

      dataService.getParticipantStats($scope.experimentId, $scope.statsDate, $scope.restrict, $scope.showGroup).then(
          function (result) {

            if (result.data) {
              $scope.stats = result.data;
              $scope.loading = false;
            }
          }, function (result) {
            $scope.loading = false;
            $scope.error = {
              code: result.status,
              message: result.statusText
            };
          });

      $scope.status = 'Sending stats request';
    }

    $scope.isPhotoData = function (data) {
      return (typeof (data) === 'string' && (data.indexOf($scope.photoMarker) === 0 || data.indexOf("iVBORw0K") === 0 || data.indexOf("mt=image") !== -1));
    }

    $scope.isAudioData = function (data) {
      return (typeof (data) === 'string' && data.indexOf($scope.audioMarker) === 0);
    }

    $scope.makeAudioSrc = function (cell) {
      return "data:audio/mpeg;base64," + cell;
    }

    $scope.isTextDiffData = function (data) {
      return (typeof (data) === 'string' && data.indexOf("mt=textdiff") !== -1); 
    }

    $scope.makeTextDiffCell = function (cell) {
      return cell; //cell.substring($scope.textDiffMarker.length);
    }
    
    $scope.isZipFileData = function (data) {
      return (typeof (data) === 'string' && data.indexOf("mt=zipfile") !== -1); 
    }


    $scope.removeUserChip = function () {
      var newPath = $scope.currentView + '/' + $scope.experimentId;
      $location.path(newPath);
    };

    if ($location.hash()) {
      $scope.columnOverride = $location.hash().split(',');
    }

    if (angular.isDefined($routeParams.filter)) {

      if ($routeParams.filter === 'anonymous') {
        $scope.anon = true;
      } else if ($routeParams.filter === 'mine') {
        $scope.restrict = $scope.user;
      } else {
        $scope.restrict = $routeParams.filter;
        $scope.userChips = [$routeParams.filter];
      }
    }

    if (angular.isDefined($routeParams.csvExperimentId)) {
      $scope.experimentId = parseInt($routeParams.csvExperimentId, 10);
      $scope.loadEvents();
    }

    if (angular.isDefined($routeParams.experimentId)) {
      $scope.experimentId = parseInt($routeParams.experimentId, 10);
      $scope.loadStats();
    }

    experimentService.getExperiment($scope.experimentId).then(function (response) {
      $scope.experiment = response.data.results[0];
      for (var i = 0; i < $scope.experiment.groups.length; i++) {
        $scope.groupNames.push($scope.experiment.groups[i].name);
      }
    });

    $scope.$watchCollection('showColumn', function (newVal, oldVal) {
      var columnString = '';
      for (var key in $scope.showColumn) {
        if ($scope.showColumn[key] && key !== 'responses') {
          if (columnString !== '') {
            columnString += ',';
          }
          columnString += key;
        }
      }
      $scope.columnString = columnString;
    });
  }]);

pacoApp.controller('HelpCtrl', ['$scope', '$routeParams', 'config', function ($scope, $routeParams, config) {

  $scope.helpLink = config.helpLinkBase;

  if (angular.isDefined($routeParams.helpId)) {
    var link = config.helpLinks[$routeParams.helpId];
    if (angular.isDefined(link)) {
      $scope.helpLink = config.helpLinkBase + '#' + link;
    }
  }

}]);

pacoApp.controller('HackCtrl', ['$scope', '$http', function ($scope, $http) {
  var allResponses = '';
  var editorReqSesssion = '';
  var editorResSession = '';
  $scope.hackRequest = '';
  $scope.errorResponse = '';
  var searchUrlWithVersion = '';
  
  $scope.submitForm = function (protocolVersion) {
    if (protocolVersion == 5) {
      searchUrlWithVersion = '/csSearch?pacoProtocol=5&fullBlobAddress=true';
    } else {
      searchUrlWithVersion = '/csSearch?pacoProtocol=4';
    }
    $http({
      method: 'POST',
      url: searchUrlWithVersion,
      data: angular.fromJson($scope.hackRequest),
      // making the header as undefined, will remove the header value for ONLY this request (which was originally added from the default header configuration)
      headers: {'pacoProtocol': undefined}
    })
    .then(function mySuccess(response) {
       allResponses = '';
       $scope.hackResponse = '';
       $scope.errorResponse = '';
         if(response.data.errorMessage == null) {
           if(response.data.events != null) {
             allResponses =  JSON.stringify(response.data.events, null, 4);
           } else if(response.data.customResponse != null) {
             allResponses = JSON.stringify(response.data.customResponse, null, 4);
           } else {
             allResponses = "Unknown response format";
           }
         } else { 
           $scope.errorResponse = response.data.errorMessage;
         }
       $scope.hackResponse = allResponses;
     }, function myError(response) {
        $scope.errorResponse = response.data;
     });
   };
   //Need to set blockScrolling to Infinity to supress Ace deprecation
   // warning
   $scope.aceInfinity = function (editor) {
     if (editor) {
       editor.$blockScrolling = 'Infinity';
     }
   };
   
   $scope.aceReqLoaded = function(editor) {
     editorReqSession = editor.getSession();
   };
   $scope.aceReqChanged = function(editor) {
     $scope.hackRequest = editorReqSession.getDocument().getValue();
   };
  
   // Ace is loaded  so get pretty JSON here
   $scope.prepareReqSourceAce = function (editor) {
     $scope.aceInfinity(editor);
     $scope.aceReq = {
       JSON : angular.toJson($scope.hackRequest, true),
       error : false
     };
   };
   
  $scope.aceResLoaded = function (editor) {
   editorResSession = editor.getSession();
  };
  $scope.aceResChanged = function (editor) {
    $scope.hackResponse = editorResSession.getDocument().getValue();
  };

  // Ace is loaded  so get pretty JSON here
  $scope.prepareResSourceAce = function (editor) {
    $scope.aceInfinity(editor);

    $scope.aceRes = {
      JSON: angular.toJson($scope.hackResponse, true),
      error: false
    };
  };


}]);

pacoApp.controller('ReportCtrl', [
  '$scope',
  '$mdDialog',
  'dataService',
  'config',
  'experiment',
  'anonymous',
  function ($scope, $mdDialog, dataService, config, experiment, anonymous) {
    $scope.reportTypes = ['csv', 'html', 'json'];
    $scope.hide = $mdDialog.hide;
    $scope.reportURL = '/events?q=\'experimentId=' + experiment.id + '\'&csv&cmdline=1';
    $scope.csvData = null;
    $scope.loading = false;
    $scope.options = {
      photos: false,
      anonymous: false,
    }

    $scope.getReport = function () {
      $scope.loading = true;
      $scope.error = null;
      dataService.getReport(experiment.id, null, $scope.reportType, $scope.options.anonymous, $scope.options.photos)
          .then(function (result) {

            if (result.error) {
              $scope.loading = false;
              $scope.error = result.error;
              return;
            }

            var blob = new Blob([result.data], {
              type: 'text/' + $scope.reportType
            });
            var csvData = (window.URL || window.webkitURL).createObjectURL(blob);
            $mdDialog.hide({
              data: csvData,
              type: $scope.reportType
            });
          });
    }
  }]);

pacoApp.controller('GroupsCtrl', ['$scope', 'template', 'PacoConstantsService',  function ($scope, template, PacoConstantsService) {
  $scope.hiding = false;
  $scope.useOldColumns = PacoConstantsService.useOldColumns;
  $scope.defaultFeedback = 'Thanks for Participating!';
  
  if ($scope.group.startDate) {
    $scope.startDate = $scope.group.startDate
  } else {
    $scope.startDate = null;
  }
  if ($scope.group.endDate) {
    $scope.endDate = $scope.group.endDate
  } else {
    $scope.endDate = null;
  }


  $scope.moreThanOneSurveyGroupPresent = function(experiment) {
    var expGroups = experiment.groups;
    var surveyGrpCt = 0;
    if (expGroups.length > 1) {
      for (var i = 0; i < expGroups.length; i++) {
        if (expGroups[i].groupType === 'SURVEY') {
          surveyGrpCt++;
          if (surveyGrpCt > 1) {
            return true;
          }
        }
      }
    }
    return false;
  };
  $scope.dateToString = function (d) {
    var s = d.getUTCFullYear() + '/' + (d.getMonth() + 1) + '/' + d.getDate();
    return s;
  };

  $scope.findInputName = function (findName) {
    var result = $scope.group.inputs.filter(function (obj) {
      return obj.name === findName;
    });
    return (result.length > 0);
  }

  $scope.newInputName = function () {
    var safeId = $scope.group.inputs.length + 1;
    var newName = 'input' + safeId;

    while ($scope.findInputName(newName)) {
      safeId++;
      newName = 'input' + safeId;
    }
    return 'input' + safeId;
  }

  $scope.addInput = function (event, expandFn, index) {
   
    var input = angular.copy(template.input);
    input.name = $scope.newInputName();

    if (index !== undefined) {
      $scope.group.inputs.splice(index, 0, input);
    } else {
      $scope.group.inputs.push(input);
    }
    if (expandFn) {
      expandFn(true);
    }

    event.stopPropagation();
  };

  $scope.toggleHide = function ($event) {
    $scope.hiding = !$scope.hiding;
  };

  $scope.addScheduleTrigger = function (event, expandFn) {
    $scope.group.actionTriggers.push(angular.copy(template.scheduleTrigger));
    var trigger = $scope.group.actionTriggers[$scope.group.actionTriggers.length - 1];
    expandFn(true);
    event.stopPropagation();
  };

  $scope.addEventTrigger = function (event, expandFn) {
    $scope.group.actionTriggers.push(angular.copy(template.eventTrigger));
    expandFn(true);
    event.stopPropagation();
  };
 
  $scope.$watch('group.fixedDuration', function (newVal, oldVal) {
    if (newVal && !oldVal) {
      $scope.startDate = new Date();
      $scope.endDate = new Date($scope.startDate.getTime() + (24 * 60 * 60 * 1000));
      $scope.group.startDate = $scope.dateToString($scope.startDate);      
      $scope.group.endDate = $scope.dateToString($scope.endDate);
    }

    if (newVal === false) {
      $scope.group.startDate = null;
      $scope.startDate = null;
      $scope.group.endDate = null;
      $scope.endDate = null;
    }
  });
}]);

pacoApp.controller('InputCtrl', ['$scope', 'config', 'template', function ($scope, config, template) {
  $scope.predefinedChecked = [];
  $scope.responseTypes = config.responseTypes;

  $scope.$watch('input.responseType', function (newValue, oldValue) {
    if ($scope.input.responseType === 'list' && $scope.input.listChoices === undefined) {
      $scope.input.listChoices = [''];
    }
  });
  
  $scope.isPredefinedInputForGroupType = function(groupType, inputObject) {
    var inputVarName = inputObject.name;
    var matchingInputObj = [];
    if (groupType === 'APPUSAGE_ANDROID') {
      matchingInputObj = template.inputsForPredefinedGroupAppUsageAndroid;
    } else if (groupType === 'PHONESTATUS') {
      matchingInputObj = template.inputsForPredefinedGroupPhoneStatus;
    } else if (groupType === 'SYSTEM') {
      matchingInputObj = template.inputsForPredefinedGroupSystemAndAdvanced;
    } else if (groupType === 'APPUSAGE_DESKTOP') {
      matchingInputObj = template.inputsForPredefinedGroupAppUsageDesktop;
    } else if (groupType === 'APPUSAGE_SHELL') {
      matchingInputObj = template.inputsForPredefinedGroupAppUsageShell;
    } else if (groupType === 'IDE_IDEA_USAGE') {
      matchingInputObj = template.inputsForPredefinedGroupIdeIdeaUsage;
    } else {
      return false;
    }
   // TODO This works, but there is a lot of scope to improve. This method is getting called so many times.
      for (var i = 0; i < matchingInputObj.length; i++) {
        var inputObj = matchingInputObj[i];
        if (inputObj.name === inputVarName) {
          return true;
        }
      }
    return false;
  }
  
  $scope.addChoice = function (index) {
    if (index !== undefined) {
      $scope.input.listChoices.splice(index, 0, '');
    } else {
      $scope.input.listChoices.push('');
    }
  }
}]);

pacoApp.controller('TriggerCtrl', ['$scope', '$mdDialog', 'config', 'template',
  function ($scope, $mdDialog, config, template) {

    $scope.scheduleTypes = config.scheduleTypes;
    if($scope.group.groupType !== 'SURVEY' && $scope.group.groupType !== 'undefined') {
      $scope.disabled = true;
    }
    $scope.addAction = function (event) {
      var action = angular.copy(template.defaultAction);
      $scope.trigger.actions.push(action);
    }

    $scope.addSchedule = function (event) {
      $scope.trigger.schedules.push(angular.copy(template.defaultEsmSchedule));
    }

    $scope.addCue = function (event) {
      $scope.trigger.cues.push(angular.copy(template.cue));
    }

    $scope.showSchedule = function (event, schedule) {
      $mdDialog.show({
        templateUrl: 'partials/schedule.html',
        locals: {
          schedule: schedule
        },
        clickOutsideToClose: true,
        controller: 'ScheduleCtrl'
      });
    };

    $scope.showAction = function (event, action, triggerType) {

      $mdDialog.show({
        templateUrl: 'partials/action.html',
        locals: {
          action: action,
          triggerType: triggerType
        },
        clickOutsideToClose: true,
        controller: 'ActionCtrl'
      });
    };

    $scope.showCue = function (event, cue) {
      $mdDialog.show({
        templateUrl: 'partials/cue.html',
        locals: {
          cue: cue
        },
        clickOutsideToClose: true,
        controller: 'CueCtrl'
      });
    };
  }]);

pacoApp.controller('ActionCtrl', ['$scope', '$mdDialog', 'config', 'template', 'action', 'triggerType',
  function ($scope, $mdDialog, config, template, action, triggerType) {

    $scope.action = action;
    $scope.triggerType = triggerType;
    $scope.actionTypes = config.actionTypes;
    $scope.hide = $mdDialog.hide;

    $scope.$watch('action.actionCode', function (newValue, oldValue) {
      if (newValue === oldValue) {
        return;
      }

      if (newValue <= 2) {
        angular.extend($scope.action, template.defaultAction);
        $scope.action.actionCode = newValue;
      } else if (newValue >= 3) {
        angular.extend($scope.action, template.otherAction);
      }
    });

  }]);

pacoApp.controller('CueCtrl', ['$scope', '$mdDialog', 'config', 'cue', function ($scope, $mdDialog, config, cue) {
  $scope.cue = cue;
  $scope.cueTypes = config.cueTypes;
  $scope.hide = $mdDialog.hide;
}]);

pacoApp.controller('ErrorCtrl', ['$scope', '$mdDialog', 'config', 'errorMessage',
  function ($scope, $mdDialog, config, errorMessage) {

    $scope.errorMessage = errorMessage;
    $scope.hide = $mdDialog.hide;

    // TODO(bobevans): make server error formats consistent to avoid this
    // special casing
    if (errorMessage.indexOf('Exception') === 0 || errorMessage.indexOf('Newer version') === 0) {
      $scope.errors = [errorMessage];
    } else {

      var err = JSON.parse($scope.errorMessage);
      $scope.errors = [];
      for (error in err) {
        $scope.errors.push(err[error].msg);
      }
    }
  }]);

pacoApp.controller('ScheduleCtrl', ['$scope', '$mdDialog', 'config', 'template', 'schedule',
  function ($scope, $mdDialog, config, template, schedule) {

    $scope.schedule = schedule;

    $scope.scheduleTypes = config.scheduleTypes;
    $scope.weeksOfMonth = config.weeksOfMonth;
    $scope.esmPeriods = config.esmPeriods;
    $scope.repeatRates = range(1, 30);
    $scope.daysOfMonth = range(1, 31);
    $scope.days = [];
    $scope.hide = $mdDialog.hide;

    if ($scope.schedule.weekDaysScheduled !== undefined) {
      var bits = parseInt($scope.schedule.weekDaysScheduled).toString(2);
      for (var i = 0; i < bits.length; i++) {
        var bit = bits[bits.length - i - 1];
        if (bit == '1') {
          $scope.days[i] = true;
        }
      }
    }

    function range(start, end) {
      var arr = [];
      for (var i = start; i <= end; i++) {
        arr.push(i);
      }
      return arr;
    }

    $scope.addTime = function (times, idx) {
      times.splice(idx + 1, 0, angular.copy(template.signalTime));
    };

    $scope.remove = function (arr, idx) {
      arr.splice(idx, 1);
    };

    $scope.parseInt = function (number) {
      return parseInt(number, 10);
    }

    $scope.$watchCollection('days', function (days) {
      var sum = 0;
      if (days) {
        for (var i = 0; i < 7; i++) {
          if ($scope.days[i]) {
            sum += Math.pow(2, i);
          }
        }
        $scope.schedule.weekDaysScheduled = sum;
      }
    });

    $scope.$watch('schedule.scheduleType', function (newValue, oldValue) {

      if (angular.isDefined(newValue)) {
        if ($scope.schedule.signalTimes === undefined) {
          $scope.schedule.signalTimes = [angular.copy(template.signalTime)];
        }

        if (newValue === 4 && oldValue !== 4) {

          // We can't just assign a new copy of the template to the schedule
          // variable since this orphans it from the top-level experiment.
          // Instead, we use extend to copy the properties of the template in.
          angular.extend($scope.schedule, template.defaultEsmSchedule);
        }

      }
    });
  }]);

pacoApp.controller('AdminCtrl', ['$scope', 'config', function ($scope, config) {

}]);

pacoApp.controller('SummaryCtrl', ['$scope', 'config', function ($scope, config) {

  $scope.getActionSummary = function () {
    if ($scope.action.actionCode !== undefined && $scope.action.actionCode !== '') {
      // this dumb logic is due to disabling option 3, log value, in the actions list
      if ($scope.action.actionCode === 1 || $scope.action.actionCode === 2) {
        return config.actionTypes[$scope.action.actionCode - 1].name;
      } else if ($scope.action.actionCode === 4) {
        return config.actionTypes[2].name;
      }
    } else {
      return 'Undefined';
    }
  };

  $scope.getCueSummary = function () {
    if ($scope.cue.cueCode !== undefined && $scope.cue.cueCode !== '') {
      return config.cueTypes[$scope.cue.cueCode - 1];
    } else {
      return 'Undefined';
    }
  };

  $scope.getScheduleSummary = function () {
    var sched = $scope.schedule;
    var str = '';

    if (sched.scheduleType === null) {
      return 'Undefined';
    }

    // ispiro:using === for these comparisons breaks on schedule edit
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
      str += config.scheduleTypes[4].name + ', ' + sched.esmFrequency + ' time';
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

pacoApp.controller('HubCtrl', ['$scope', '$mdDialog', '$filter', 'config', 'template', '$routeParams', '$location',
  'experimentService',
  function ($scope, $mdDialog, $filter, config, template, $routeParams, $location, experimentService) {

    $scope.tabs = config.hubTabs;

    $scope.state = {
      tabId: 0,
      groupIndex: null
    };

    // temporarily comment this because it prevents loading experiments
    // directly
    // fix for bug https://github.com/google/paco/issues/1448
    // regresses bug https://github.com/google/paco/issues/1272
    // if ($scope.user === undefined) {
    // $location.path('/');
    // }

    if ($location.hash()) {
      var newTabId = config.editTabs.indexOf($location.hash());
      if (newTabId !== -1) {
        $scope.state.tabId = newTabId;
      }
    }

    $scope.$watch('user', function (newValue, oldValue) {
      if (newValue && $scope.newExperiment && $scope.experiment) {
        $scope.experiment.creator = $scope.user;
        $scope.experiment.contactEmail = $scope.user;
        $scope.experiment.admins = [$scope.user];
      }
    });

    $scope.$watch('state.tabId', function (newValue, oldValue) {
      if ($scope.state.tabId === 0) {
        $location.hash('');
      } else if ($scope.state.tabId > 0) {
        $location.hash($scope.tabs[$scope.state.tabId]);
      }
    });
  }]);

pacoApp.controller('Experiment2Ctrl', ['$scope', '$mdDialog', '$filter', 'config', 'template', '$routeParams',
  '$location', 'pubExperimentService', 'PacoConstantsService',
  function ($scope, $mdDialog, $filter, config, template, $routeParams, $location, experimentService, PacoConstantsService) {
    $scope.state = {
      tabId: 0,
      groupIndex: null
    };

    if (angular.isDefined($routeParams.pubRespondExperimentId)) {
      $scope.respondExperimentId = parseInt($routeParams.pubRespondExperimentId, 10);
      $scope.experimentId = $scope.respondExperimentId;
    }
    if ($scope.experimentId) {
      experimentService.getExperiment($scope.experimentId).then(function (response) {
        var data = response.data.results;
        $scope.experiment = data[0];
      });
    }
    $scope.$watch('experiment.groups', function (newValue, oldValue) {
      $scope.useOldColumns = PacoConstantsService.useOldColumns;
      
      if (newValue) {
        $scope.admin = ($scope.experiment.admins.indexOf($scope.user) !== -1);

        var groups = [];
        for (var groupId in $scope.experiment.groups) {
          var group = $scope.experiment.groups[groupId];
          if (group.inputs.length > 0 && ($scope.useOldColumns || group.groupType === "SURVEY")) {
            groups.push(group);
          }
        }
        $scope.respondableGroups = groups;
        if ($scope.respondableGroups.length === 1) {
          $scope.state.groupIndex = 0;
        }
      }
    });
  }]);
