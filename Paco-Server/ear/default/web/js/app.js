var pacoApp = angular.module('pacoApp', [
  'ngRoute',
  'ngMaterial',
  'ui.ace',
  'nvd3',
  'ngSanitize'
]);

// 1.5 to 1.6 update need to have bindings or use $onInit function in controllers
// https://docs.angularjs.org/guide/migration#-compile-
pacoApp.config(['$compileProvider', function($compileProvider) {
  $compileProvider.preAssignBindingsEnabled(true);
}]);

pacoApp.config(['$sceDelegateProvider', function($sceDelegateProvider) {
    // Angular doesn't deal well with audio src=data: this lets us load local data. 
    // TODO remove when port to Google Cloud Storage for Audio and Image data is complete
	$sceDelegateProvider.resourceUrlWhitelist(['data:audio/mpeg;base64,**','self']);
}]);

//Code update: Since the new angular version 1.6.5 has changed empty string to '!'
//for example - mydomain.com/#/a/b/c is now mydomain.com/#!/a/b/c
//the following config block is required to restore the previous behavior.
pacoApp.config(['$locationProvider', function($locationProvider) {
  $locationProvider.hashPrefix('');
}]);

pacoApp.factory('PacoConstantsService', function() {
  return {
      useOldColumns : false
  };
});

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
    when( '/viz/:experimentId', {
      templateUrl: 'partials/viz/viz.html',
    }).
    when('/stats/:experimentId/:filter', {
      templateUrl: 'partials/stats.html',
    }).
    when('/respond/:respondExperimentId', {
      templateUrl: 'partials/respond.html',
    }).
    when('/hack',{
      templateUrl: 'partials/hack.html',
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
    when('/survey/:pubRespondExperimentId',{
      templateUrl: 'partials/respondpub.html',
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

