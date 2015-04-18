var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'ui.ace'
]).value('$anchorScroll', angular.noop);


pacoApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/experiment/:experimentId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
      reloadOnSearch: false,
    }).
    otherwise({
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
    });
  }
]);

// pacoApp.config(['$anchorScrollProvider',
//   function($anchorScrollProvider) {
//     $anchorScrollProvider.disableAutoScrolling();
//   }
// ]);
