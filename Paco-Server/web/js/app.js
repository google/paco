var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'ui.ace'
]);


pacoApp.config(['$routeProvider','$locationProvider',
  function($routeProvider, $locationProvider) {
    $routeProvider.
    when('/edit/:editId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
      reloadOnSearch: false,
    }).
    when('/experiment/:experimentId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl'
    }).
    when('/data/:csvExperimentId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
    }).
    when('/respond/:respondExperimentId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
    }).
    when('/experiments', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
    }).
    when('/copy/:copyId', {
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
      reloadOnSearch: false,
    }).
    otherwise({
      templateUrl: 'partials/main.html',
      controller: 'HomeCtrl',
    });

    //$locationProvider.html5Mode(true);
  }
]);


pacoApp.config(['$compileProvider',
  function ($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|file|blob):/);
}]);
