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



pacoApp.directive('expandable', ['$timeout', function($timeout) {
  return {
    restrict: 'A',
    scope: true,
    link: function(scope, element, attributes) {
      scope.expand = true;
      scope.container = element;

      scope.toggleExpand = function(flag) {

        if (flag === undefined) {
          scope.expand = !scope.expand;
        } else {
          scope.expand = flag;
        }

        if (scope.expand) {
          scope.expander.style.marginBottom = 0;
          scope.expander.style.opacity = 1;
          element.addClass('expand');
        } else {
          var ht = element[0].clientHeight;
          scope.expander.style.marginBottom = -ht + 'px';
          scope.expander.style.opacity = 0;
          element.removeClass('expand');
        }
      };


      $timeout(function() {
        scope.toggleExpand(scope.expand)
      }, 250);
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
