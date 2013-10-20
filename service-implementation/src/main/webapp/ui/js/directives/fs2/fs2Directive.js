'use strict';

directives.directive('fileUpload', function () {
    return function( scope, element, attrs) {
        element.bind('change', function(event) {
            scope.$apply(function() {
                scope[attrs.name] = event.target.files;
                console.log(scope[attrs.name]);

            });
        });
    };
});