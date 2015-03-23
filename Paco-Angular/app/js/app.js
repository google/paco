var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'pacoControllers'
  ]);

pacoApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/experiment/:experimentIdx', {
      templateUrl: 'partials/experiment.html',
      controller: 'ExperimentCtrl',
    }).
    otherwise({
      templateUrl: 'partials/experiment.html',
      controller: 'ExperimentCtrl',
    });
  }]);

// Code based on this tutorial
// http://buildinternet.com/2013/08/drag-and-drop-file-upload-with-angularjs/
pacoApp.directive('fileDropzone', function() {
    return {
      restrict: 'A',
      scope: {
        file: '=',
        fileName: '=',
        dragging: '='
      },
      link: function(scope, element, attrs) {
        var checkSize, isTypeValid, processDragOver, processDragEnd, validMimeTypes, processDragEnter;
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
          if (event != null) {
            event.preventDefault();
          }
          return false;
        };
        processDragEnd = function(event) {
          console.log(event);
          scope.$apply(function() {
            scope.dragging = false;
          });
          if (event != null) {
            event.stopPropagation();
          }
          // return false;
        };
        validMimeTypes = attrs.fileDropzone;
        checkSize = function(size) {
          var _ref;
          if (((_ref = attrs.maxFileSize) === (void 0) || _ref === '') || (size / 1024) / 1024 < attrs.maxFileSize) {
            return true;
          } else {
            alert("File must be smaller than " + attrs.maxFileSize + " MB");
            return false;
          }
        };
        isTypeValid = function(type) {
          if ((validMimeTypes === (void 0) || validMimeTypes === '') || validMimeTypes.indexOf(type) > -1) {
            return true;
          } else {
            alert("Invalid file type.  File must be one of following types " + validMimeTypes);
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