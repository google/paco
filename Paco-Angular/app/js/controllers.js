var pacoControllers = angular.module('pacoControllers', []);

pacoControllers.controller('CreateCtrl', function ($scope, $http){
  $scope.questions = [{}];
  $scope.removeQuestion = function(index) {
    $scope.questions.splice(index, 1);
  }
  $scope.debug = function() {
    console.dir($scope);
  }
 });

pacoControllers.controller('QuestionCtrl', function ($scope, $http){
  $scope.questionType = 'likert';
  $scope.choices = [{}];
  $scope.multiChoice = false;
  $scope.removeChoice = function(index) {
    $scope.choices.splice(index, 1);
  }
 });

pacoControllers.controller('ExperimentsCtrl', function ($scope, $http){
  $http.get('js/experiments.json').success(function(data) {
    $scope.experiments = data;
  });

  $scope.sortField = 'title';
  $scope.reverse = true;
});