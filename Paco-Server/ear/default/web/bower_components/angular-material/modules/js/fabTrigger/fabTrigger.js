/*!
 * Angular Material Design
 * https://github.com/angular/material
 * @license MIT
 * v0.11.0-rc1-master-d74f93a
 */
(function( window, angular, undefined ){
"use strict";

(function() {
  'use strict';

  /**
   * @ngdoc module
   * @name material.components.fabTrigger
   */
  angular
    .module('material.components.fabTrigger', ['material.core'])
    .directive('mdFabTrigger', MdFabTriggerDirective);

  /**
   * @ngdoc directive
   * @name mdFabTrigger
   * @module material.components.fabSpeedDial
   *
   * @restrict E
   *
   * @description
   * The `<md-fab-trigger>` directive is used inside of a `<md-fab-speed-dial>` or
   * `<md-fab-toolbar>` directive to mark the an element (or elements) as the trigger and setup the
   * proper event listeners.
   *
   * @usage
   * See the `<md-fab-speed-dial>` or `<md-fab-toolbar>` directives for example usage.
   */
  function MdFabTriggerDirective() {
    // TODO: Remove this completely?
    return {
      restrict: 'E',

      require: ['^?mdFabSpeedDial', '^?mdFabToolbar']
    };
  }
})();


})(window, window.angular);