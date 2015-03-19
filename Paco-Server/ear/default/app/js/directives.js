/**
 * This directive uses two-way data filtering to convert between the time
 * returned by an HTML time input (a timestamp relative to midnight, 12/31/1969) 
 * and the the format that PACO uses, millisSinceMidnight.
 */

app.directive('milli', function() {
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
