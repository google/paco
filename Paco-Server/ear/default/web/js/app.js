var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'ui.ace'
]);


pacoApp.config(['$routeProvider','$locationProvider',
  function($routeProvider, $locationProvider) {
    $routeProvider.
    when('/edit/:editId', {
      templateUrl: 'partials/edit.html',
      reloadOnSearch: false,
    }).
    when('/experiment/:experimentId', {
      templateUrl: 'partials/experiment.html',
    }).
    when('/data/:csvExperimentId', {
      templateUrl: 'partials/data.html',
    }).
    when('/data/:csvExperimentId/:who', {
      templateUrl: 'partials/data.html',
    }).
    when('/stats/:experimentId', {
      templateUrl: 'partials/stats.html',
    }).
    when('/stats/:experimentId/:who', {
      templateUrl: 'partials/stats.html',
    }).
    when('/respond/:respondExperimentId', {
      templateUrl: 'partials/respond.html',
    }).
    when('/experiments', {
      templateUrl: 'partials/list.html',
    }).
    when('/copy/:copyId', {
      templateUrl: 'partials/edit.html',
      reloadOnSearch: false,
    }).
    otherwise({
      templateUrl: 'partials/welcome.html',
    });

    //$locationProvider.html5Mode(true);
  }
]);


pacoApp.config(['$compileProvider',
  function ($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|file|blob):/);
}]);

