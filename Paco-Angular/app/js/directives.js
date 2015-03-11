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
