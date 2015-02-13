var pacoControllers = angular.module('pacoControllers', []);




pacoControllers.controller('ExperimentCtrl', ['$scope', '$http', '$routeParams', function ($scope, $http, $routeParams){

  $scope.responseTypes = ["likert", "likert_smileys", "open text", "list", "photo"];
  $scope.experimentIdx = parseInt($routeParams.experimentIdx);
  $scope.previousIdx = -1;
  $scope.nextIdx = -1;
  $scope.selectedIndex = 0;

  $scope.removeInput = function(idx) {
    $scope.experiment.inputs.splice(idx,1);
  };

  $scope.removeChoice = function(input, idx) {
    input.splice(idx,1);
  };

  $http.get('js/experiments.json').success(function(data) {
    $scope.experiment = data[$scope.experimentIdx];

    if ($scope.experimentIdx < data.length - 1) {
      $scope.nextIdx = $scope.experimentIdx + 1;
    }

    if ($scope.experimentIdx > 0) {
      $scope.previousIdx = $scope.experimentIdx - 1;
    }
  });  
}]);

pacoControllers.controller('CreateCtrl', function ($scope, $http){
  $scope.questions = [{}];

  $scope.removeQuestion = function(index) {
    $scope.questions.splice(index, 1);
  }
  $scope.debug = function() {
    console.dir(angular.toJson($scope.questions));
  }
});


