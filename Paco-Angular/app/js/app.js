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
    when('/experiment/', {
      redirectTo: '/experiment/1'
    }).
    otherwise({
      redirectTo: '/experiment/1'
    });
  }]);

