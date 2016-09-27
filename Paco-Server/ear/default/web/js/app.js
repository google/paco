var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'ui.ace'
]);

pacoApp.config(['$sceDelegateProvider', function($sceDelegateProvider) {
    // Angular doesn't deal well with audio src=data: this lets us load local data. 
    // TODO remove when port to Google Cloud Storage for Audio and Image data is complete
	$sceDelegateProvider.resourceUrlWhitelist(['data:audio/mpeg;base64,**','self']);
}]);

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
    when('/data/:csvExperimentId/:filter', {
      templateUrl: 'partials/data.html',
    }).
    when('/stats/:experimentId', {
      templateUrl: 'partials/stats.html',
    }).
    when('/stats/:experimentId/:filter', {
      templateUrl: 'partials/stats.html',
    }).
    when('/respond/:respondExperimentId', {
      templateUrl: 'partials/respond.html',
    }).
    when('/help/:helpId?', {
      templateUrl: 'partials/help.html',
    }).
    when('/experiments', {
      templateUrl: 'partials/list.html',
      reloadOnSearch: false,
    }).
    when('/copy/:copyId', {
      templateUrl: 'partials/edit.html',
      reloadOnSearch: false,
    }).
    when('/hub',{
        templateUrl: 'partials/hub.html',
        reloadOnSearch: false,
    }).
    otherwise({
      templateUrl: 'partials/welcome.html',
      reloadOnSearch: false,
    });
  }
]);

// Prevents a change in the hash parameter from jumping to internal anchors
pacoApp.value('$anchorScroll', angular.noop);

pacoApp.config(['$compileProvider',
  function ($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|file|blob):/);
}]);

