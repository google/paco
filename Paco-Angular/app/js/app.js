var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial'
]);

pacoApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/experiment/:experimentId', {
      templateUrl: 'partials/experiment.html',
      controller: 'ExperimentCtrl',
    }).
    otherwise({
      templateUrl: 'partials/experiment.html',
      controller: 'ExperimentCtrl',
    });
  }
]);
