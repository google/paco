pacoApp.directive('multipleInputsDropDown', [function () {

  return {
    restrict: 'E',
    templateUrl: 'partials/viz/multipleInputs.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {
    }
  }
}]);

pacoApp.directive('singleInputDropDown', [function () {

  return {
    restrict: 'E',
    templateUrl: 'partials/viz/singleInput.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {
    }
  }
}]);

pacoApp.directive('participantsDropDown', [function () {

  return {
    restrict: 'E',
    templateUrl: 'partials/viz/participantsControl.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

pacoApp.directive('typesDropDown', [function () {

  return {
    restrict: 'E',
    template: '<md-input-container class="typesDropDown">' +
    '<label class="typesLabel">Types</label>' +
    '<md-select ng-model="currentVisualization.type"  md-on-close="toggleDrawButton(true)">' +
    '<md-optgroup label="Viz Types">' +
    '<md-option ng-repeat="type in vizTypes" ng-value="type">{{type}}</md-option>' +
    '</md-optgroup></md-select></md-input-container>',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

pacoApp.directive('createButton', [function () {
  return {
    restrict: 'E',
    template: '<md-input-container class="vizCreateButton">' +
    '<md-button class="md-raised" ng-disabled="!drawButton" ng-click="createViz()">Draw</md-button>' +
    '</md-input-container>',
    controller: 'VizCtrl', //Embed a custom controller in the directive
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

pacoApp.directive('clearButton', [function () {
  return {
    restrict: 'E',
    template: '<md-input-container>' +
    '<md-button class="md-primary vizClearButton" ng-click="clearViz()">Clear</md-button>' +
    '</md-input-container>',
    controller: 'VizCtrl', //Embed a custom controller in the directive
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

pacoApp.directive('dateRange', [function () {
  return {
    restrict: 'E',
    templateUrl: 'partials/viz/dateTimeControl.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {

    }
  }
}]);


pacoApp.directive('historyDropDown', [function () {
  return {
    restrict: 'E',
    templateUrl: 'partials/viz/historyDropDown.html',
    controller: 'VizCtrl',
    link: function (scope, element, attrs, controller) {

    }
  }
}]);

