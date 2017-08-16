pacoApp.directive('inputsDropDown', [function () {

  return {
    restrict: 'E',
    templateUrl: 'partials/viz/inputsControl.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {
    }
  }
}]);

pacoApp.directive('participantsDropDown', [ function () {

  return {
    restrict: 'E',
    templateUrl:'partials/viz/participantsControl.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

pacoApp.directive('typesDropDown', [ function () {

  return {
    restrict: 'E',
    template: '<md-input-container class="typesDropDown">'+
    '<label class="typesLabel">Viz Types</label>'+
    '<md-select ng-model="selectedType" ng-change="getSelectedType()">'+
    '<md-optgroup label="Viz Types">'+
    '<md-option ng-repeat="type in vizTypes" ng-value="type" ng-selected="$first">{{type}}</md-option>'+
    '</md-optgroup></md-select></md-input-container>',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

pacoApp.directive('createButton', [function () {

  return {
    restrict: 'E',
    template: '<md-input-container class="vizCreateButton">'+
    '<md-button class="md-raised" ng-click="createViz()">Create</md-button>'+
    '</md-input-container>',
     controller: 'VizCtrl', //Embed a custom controller in the directive
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

pacoApp.directive('dateRange',[function(){
  return {
    restrict: 'E',
    templateUrl: 'partials/viz/dateTimeControl.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {

    }
  }

}]);

