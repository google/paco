var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'pacoControllers'
  ]);

pacoApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/create', {
      templateUrl: 'partials/create.html',
      controller: 'CreateCtrl'
    }).
    when('/experiment/:experimentIdx', {
      templateUrl: 'partials/experiment.html',
      controller: 'ExperimentCtrl',
    }).
    when('/experiment/', {
      redirectTo: '/experiment/1'
    }).
    when('/experiments', {
      templateUrl: 'partials/experiments.html',
      controller: 'ExperimentsCtrl'
    }).
    otherwise({
      redirectTo: '/create'
    });
  }]);
