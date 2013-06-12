'use strict';

/* Directives */

var directives = angular.module('myApp.directives', []);

// simple fade in animation for a touch of class...
directives.directive('fadeIn', function () {
    return {
        compile:function (elm) {
            $(elm).css('opacity', 0.0);
            return function (scope, elm, attrs) {
                $(elm).animate({ opacity:1.0 }, 1500);
            };
        }
    };
});

// this is the angular way to stop even propagation 
directives.directive('stopEvent', function () {
    return {
        restrict:'A',
        link:function (scope, element, attr) {
            element.bind(attr.stopEvent, function (e) {
                e.stopPropagation();
            });
        }
    }
});
