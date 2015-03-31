var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial'
]);

pacoApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/experiment/:experimentId', {
      templateUrl: 'partials/home.html',
      controller: 'HomeCtrl',
    }).
    otherwise({
      templateUrl: 'partials/home.html',
      controller: 'HomeCtrl',
    });
  }
]);
