'use strict';

// this is the angular way to stop even propagation
angular.module('myApp.directives', []).directive('stopEvent', function () {
    return {
        restrict:'A',
        link:function (scope, element, attr) {
            element.bind(attr.stopEvent, function (e) {
                e.stopPropagation();
            });
        }
    }
});