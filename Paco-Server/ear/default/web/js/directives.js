
pacoApp.directive('pacoGroup', function () {

  var controller = ['$scope', '$http', '$location', '$mdDialog', '$anchorScroll', 'util',
    function($scope, $http, $location, $mdDialog, $anchorScroll, util) {

    $scope.mask = {};
    $scope.responses = $scope.responses || {};

    $scope.post = {
      appId: 'webform',
      pacoVersion: 1,
    };

    $scope.$watch('group', function(newValue, oldValue) {
      if (angular.isDefined($scope.experiment)) {
        $scope.post.experimentId = $scope.experiment.id;
      }
    });

    $scope.$watchCollection('responses', function(newValue, oldValue) {

        if (angular.isDefined(newValue) &&
            angular.isDefined($scope.group)) {

          for ( var inputIdx in $scope.group.inputs) {
            var input = $scope.group.inputs[inputIdx];
            if (input.conditional) {
              var validity = parser.parse(input.conditionExpression, $scope.responses);
              $scope.mask[inputIdx] = !validity;
            }
          }
        }
    });

    $scope.respond = function() {

      var post = {};

      post.experimentGroupName = $scope.group.name;
      post.experimentName = $scope.experiment.title;
      post.experimentId = $scope.$parent.experiment.id;
      post.experimentVersion = $scope.experiment.version;
      post.pacoVersion = 4;
      post.appId = 'webform';
      post.responses = [];
      post.responseTime = util.formatDate(new Date());

      for (var name in $scope.responses) {
        var pair = {
          name: name,
          answer: $scope.responses[name]
        };
        post.responses.push(pair);
      }

      if ($scope.events) {

        var event = $scope.events[$scope.activeIdx];
        var responseTime = util.formatDate(new Date(event.responseTime));

        var eodPair = {
          'name': 'eodResponseTime',
          'answer':  responseTime
        }
        var referPair = {
          'name': 'referred_group',
          'answer': event.experimentGroupName
        }
        post.responses.push(eodPair);
        post.responses.push(referPair);
      }

    $http.post('/events', post).success(function(data) {

        if (data[0].status === true) {

          if ($scope.events) {
            $scope.activeIdx++;
            $scope.responses = {};
          }

          $anchorScroll('');

          if (!$scope.events || !$scope.events[$scope.activeIdx]) {
            $mdDialog.show(
              $mdDialog.alert()
              .title('Respond Status')
              .content('Success!')
              .ariaLabel('Success')
              .ok('OK')
            ).then(function() {
              $location.path('/experiments');
            });
          }
        }

      }).error(function(data, status, headers, config) {
        console.error(data);
      });
    };

    $scope.range = function(start, end) {
      var arr = [];
      for (var i = start; i <= end; i++) {
        arr.push(i);
      }
      return arr;
    }

    $scope.inListString = function(item, responseName) {
      if (!$scope.responses) {
        return false;
      }
      var listString = $scope.responses[responseName];
      if (listString === undefined || listString === '') {
        return false;
      }
      var list = listString.split(',');
      if (list.indexOf(item + '') !== -1) {
        return true;
      }
      return false;
    }

    $scope.toggleStringItem = function(item, responseName) {

      var listString = $scope.responses[responseName];
      var list = [];

      if (listString === undefined || listString === '') {
        $scope.responses[responseName] = [];
      } else {
        list = listString.split(',');
      }

      var find = list.indexOf('' + item);

      if (find === -1) {
        list.push(item + '');
      } else {
        list.splice(find, 1);
      }

      $scope.responses[responseName] = list.join();
    };

  }];

  return {
    restrict: 'E',
    scope: {  'group': '=data',
              'responses': '=',
              'preview': '=',
              'readonly': '=',
              'events': '=',
              'experiment': '=',
              'activeIdx': '='},

    controller: controller,
    templateUrl: 'partials/group.html'
  };
});


pacoApp.directive('noIos', function() {
  return {
    restrict: 'E',
    transclude: true,
    template: '<div class="no-ios"><img src="img/no_ios.png"><span class="tip-text" ng-transclude></span></div>',
    link: function(scope, element) {
    }
  };
});

pacoApp.directive('pacoHelp', function() {
  return {
    restrict: 'E',
    transclude: true,
    scope: {  'tip': '@tip' },
//    template: '<a class="paco-help" href="#/help/{{tip}}" target="_new"><img src="img/ic_help_outline_black_24px.svg"></a>',
    template: '<a class="paco-help" href="#/help/{{tip}}" target="_new"><img src="img/ic_help_outline_black_24px.svg"><span class="tip-text" ng-transclude></span></a>',
    link: function(scope, element) {}
  };
});

//

/**
 * This directive uses two-way data filtering to convert between the time
 * returned by an HTML time input (a timestamp relative to midnight, 12/31/1969) 
 * and the the format that PACO uses, millisSinceMidnight.
 */

pacoApp.directive('milli', function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attr, ngModel) {

      /*
       * Since the time input is relative to 12/31/1969, it will never
       * include daylight savings time. So to compute the millisecond delta, we
       * make a date in January, which will be in the current system timezone,
       * then call getTimezoneOffset on that date.
       */
      var now = new Date();
      var nonDSTDate = new Date(now.getFullYear(), 0, 1);
      var UTCOffset = 60 * 1000 * nonDSTDate.getTimezoneOffset();

      function dateToMillis(text) {
        var dd = Date.parse(text);
        return dd - UTCOffset;
      }

      function millisToDate(millis) {
        return new Date(parseInt(millis) + UTCOffset);
      }
      ngModel.$parsers.push(dateToMillis);
      ngModel.$formatters.push(millisToDate);
    }
  };
});


pacoApp.filter('trusted', ['$sce', function ($sce) {
    return function(url) {
        return $sce.trustAsResourceUrl(url);
    };
}]);

/**
 * Directive for two-way filtering numbers and booleans. The various select
 * elements use stringified numbers or bools for their values, a constraint of
 * Angular. Add this directive to a select and it will properly handle
 * numbers and bools by converting back and forth to stringified versions.
 * Use the ng-selected directive on each option inside the select to get it to
 * properly show the selected value on initial load.
 */

pacoApp.directive('asString', function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attr, ngModel) {

      function numberOrBoolToString(number) {
        if (number === undefined || number === null) {
          return '';
        }
        return number + '';
      }

      function stringToNumberOrBool(string) {
        if (string === 'true') {
          return true;
        } else if (string === 'false') {
          return false;
        } 
        return parseInt(string, 10);
      }

      ngModel.$parsers.push(stringToNumberOrBool);
      ngModel.$formatters.push(numberOrBoolToString);
    }
  };
});


pacoApp.directive('pacoDate', function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, element, attr, ngModel) {

      function dateToPacoDate(dateString) {
        var date = new Date(dateString);
        dd = scope.dateToString(date);
        return dd;
      }

      function pacoDateToDate(pacoDate) {
        return new Date(pacoDate);
      }
      ngModel.$parsers.push(dateToPacoDate);
      ngModel.$formatters.push(pacoDateToDate);
    }
  };
});


pacoApp.directive('expandable', ['$timeout', function($timeout) {
  return {
    restrict: 'A',
    scope: true,
    link: function(scope, element, attributes) {
      scope.state = { expand: true };
      scope.container = element;

      scope.fixHeight = function() {
        scope.toggleExpand(!scope.state.expand, true);
        scope.toggleExpand(!scope.state.expand, true);
      };

      scope.toggleExpand = function(flag, skipAnimation) {

        if (flag === undefined) {
          scope.state.expand = !scope.state.expand;
        } else {
          scope.state.expand = flag;
        }

        if (angular.isDefined(skipAnimation) && skipAnimation === true) {
          angular.element(scope.expander).addClass('notransition');
        } else {
          angular.element(scope.expander).removeClass('notransition');
        }

        if (scope.state.expand) {
          scope.expander.style.marginBottom = 0;
          element.addClass('expand');
        } else {
          var ht = element[0].clientHeight;
          scope.expander.style.marginBottom = -ht + 'px';
          element.removeClass('expand');
        }
      };

      if (attributes['expanded'] === 'false') {
        scope.state.expand = false;
      }

      $timeout(function() {
        scope.toggleExpand(scope.state.expand, true);
      }, 0);

      $timeout(function() {
        scope.fixHeight();
      }, 1000);
    }
  }
}]);


pacoApp.directive('expander', function() {
  return {
    restrict: 'A',
    scope: false,
    link: function(scope, element, attributes) {
      scope.expander = element[0];
    }
  }
});


pacoApp.directive('hidable', [function() {
  return {
    restrict: 'A',
    scope: true,
    link: function(scope, element, attributes) {
      scope.hiding = false;
      scope.container = element;
      var lastHeight;

      scope.toggleHide = function() {
        scope.hiding = !scope.hiding;
      };
    }
  }
}]);


pacoApp.filter('percent', ['$filter', function ($filter) {
  return function (input) {
    if (isNaN(input)) {
      return '-';
    }
    return $filter('number')(input * 100, 0) + '%';
  };
}]);


pacoApp.filter('jsonToTable', ['util', 'config', function(util, config) {

  return function (json, unpackResponse){
    var rows = [];
    var order = json[0];
    var responseLookup = {};
    var numberResponseVariables = 0;
    var responseStartId;
    var responseEndId;
    var responseNames = [];

    if (json.length === 0) {
      return null;
    }

    var headerRow = [];
    for (var id in config.dataOrder) {
      var column = config.dataOrder[id];
      if (column === 'responses') {
        responseStartId = headerRow.length;
        responseEndId = responseStartId;
      } else {
        headerRow.push(column);
      }
    }

    for (var column in order) {
      // Omit unpacked responses column
      if (unpackResponse && column === 'responses') {
        continue;
      }

      // Add columns not already included from config data order
      if (headerRow.indexOf(column) == -1) {
        headerRow.push(column);
      }
    }


    if (unpackResponse) {

      // Iterate over all rows' responses fields to find additional columns
      for (var i = 0; i < json.length; i++) {
        var responses = json[i]['responses'];
        if (responses) {
          for (var id in responses) {
            var responseName = responses[id]['name'];
            if (responseLookup[responseName] === undefined) {
              responseNames.push(responseName);

              // Add response variables to header too
              headerRow.splice(responseEndId, 0, responseName);
              responseLookup[responseName] = numberResponseVariables;
              numberResponseVariables++;
              responseEndId = responseStartId + numberResponseVariables;

            }
          }
        }
      }

    }

    for (var i = 0; i < json.length; i++) {
      var responses;
      var newRow = [];

      for (var id in headerRow) {
        var column = headerRow[id];

        if (unpackResponse && id >= responseStartId && id < responseEndId) {
          responses = json[i]['responses'];
        } else {
          var val = json[i][column];
          if (val === undefined) {
            val = '';
          }
          if (config.timeColumns.indexOf(column) !== -1) {
            var timezone = json[i]['timezone'];
            val = util.formatDate(val, timezone);
          }
          newRow[id] = val;
        }
      }

      if (responses) {

        // Explicitly set all responses to the empty string first
        for (var j = responseStartId; j < responseEndId; j++) {
          newRow[j] = '';
        }

        for (var id in responses) {
          var responseName = responses[id]['name'];
          var responseValue = responses[id]['answer'];
          var responseId = responseLookup[responseName];
          newRow[responseStartId + responseId] = responseValue;
         }
      }
      rows.push(newRow);
    }

    return {header: headerRow, rows:rows, responseNames: responseNames};
  }
}]);


pacoApp.filter('tableToCsv', function() {

  return function (table){

    var rows = angular.copy(table.rows);
    rows.splice(0, 0, table.header);

    var string = '';

    for (var i = 0; i < rows.length; i++) {
      var row = rows[i];
      if (i > 0) {
        string += '\n';
      }
      for (var j = 0; j < row.length; j++) {
        if (j > 0) {
          string += ',';
        }
        var word = '"' + row[j] + '"';
        string += word;
      }
    }
    return string;
  }
});


pacoApp.filter('jsonToCsv', function() {
  var rows = [];

  return function (json){
    var order = json[0];
    var newRow = [];
    var headerRow = [];

    for (var i = 0; i < json.length; i++) {

      for (var column in order) {
        if (i === 0) {
          headerRow.push(column);
        }

        newRow.push(json[i][column]);
      }
      if (i === 0) {
        rows.push(headerRow);
      }
      rows.push(newRow);
    }
    return rows;
  }
});



/**
 * Code based on this tutorial
 * http://buildinternet.com/2013/08/drag-and-drop-file-upload-with-angularjs/
 */

pacoApp.directive('fileDropzone', function() {
  return {
    restrict: 'A',
    scope: {
      file: '=',
      fileName: '=',
      dragging: '='
    },
    link: function(scope, element, attrs) {
      var checkSize, isTypeValid, processDragOver, processDragEnd,
        validMimeTypes, processDragEnter;
      processDragOver = function(event) {
        if (event != null) {
          event.preventDefault();
        }
        return false;
      };
      processDragEnter = function(event) {
        scope.$apply(function() {
          scope.dragging = true;
        });
        if (event !== null) {
          event.preventDefault();
        }
        return false;
      };
      processDragEnd = function(event) {
        console.log(event);
        scope.$apply(function() {
          scope.dragging = false;
        });
        if (event !== null) {
          event.stopPropagation();
        }
        // return false;
      };
      validMimeTypes = attrs.fileDropzone;
      checkSize = function(size) {
        var _ref;
        if (((_ref = attrs.maxFileSize) === (void 0) || _ref === '') ||
          (size / 1024) / 1024 < attrs.maxFileSize) {
          return true;
        } else {
          alert("File must be smaller than " + attrs.maxFileSize +
            " MB");
          return false;
        }
      };
      isTypeValid = function(type) {
        if ((validMimeTypes === (void 0) || validMimeTypes === '') ||
          validMimeTypes.indexOf(type) > -1) {
          return true;
        } else {
          alert(
            "Invalid file type.  File must be one of following types " +
            validMimeTypes);
          return false;
        }
      };
      element.bind('dragover', processDragOver);
      element.bind('dragenter', processDragEnter);
      element.bind('dragleave', processDragEnd);
      return element.bind('drop', function(event) {
        var file, name, reader, size, type;
        if (event != null) {
          event.preventDefault();
        }
        reader = new FileReader();
        reader.onload = function(evt) {
          if (checkSize(size) && isTypeValid(type)) {
            return scope.$apply(function() {
              scope.file = 'url(' + evt.target.result + ')';
              if (angular.isString(scope.fileName)) {
                return scope.fileName = name;
              }
            });
          }
        };
        file = event.dataTransfer.files[0];
        name = file.name;
        type = file.type;
        size = file.size;
        reader.readAsDataURL(file);
        scope.$apply(function() {
          scope.dragging = false;
        });
        return false;
      });
    }
  };
});