var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'ui.ace'
]);


pacoApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/experiment/:experimentId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
      reloadOnSearch: false,
    }).
    when('/data/:csvExperimentId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
    }).    
    otherwise({
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
    });
  }
]);


pacoApp.config(['$compileProvider',
  function ($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|file|blob):/);
}]);
