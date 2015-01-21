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
    when('/experiments', {
      templateUrl: 'partials/experiments.html',
      controller: 'ExperimentsCtrl'
    }).
    otherwise({
      redirectTo: '/create'
    });
  }]);
