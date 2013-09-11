'use strict'

// TODO move to Playground and tie to fs2 (this is just an example)

// TODO make controller scoped

var url = "rest/v1/fs2/";

myApp.controller('DemoFileUploadController', [
    '$scope', '$http', '$filter', '$window',
    function ($scope, $http) {
        $scope.options = {
            url: url,
            limitMultiFileUploads: 1
        };

    }
])

myApp.controller('FileDestroyController', [
    '$scope', '$http',
    function ($scope, $http) {
        var file = $scope.file,
            state;
        if (file.url) {
            file.$state = function () {
                return state;
            };
            file.$destroy = function () {
                state = 'pending';
                return $http({
                    url: file.deleteUrl,
                    method: file.deleteType
                }).then(
                    function () {
                        state = 'resolved';
                        $scope.clear(file);
                    },
                    function () {
                        state = 'rejected';
                    }
                );
            };
        } else if (!file.$cancel && !file._index) {
            file.$cancel = function () {
                $scope.clear(file);
            };
        }
    }
]);