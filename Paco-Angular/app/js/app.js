var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'ui.ace'
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
