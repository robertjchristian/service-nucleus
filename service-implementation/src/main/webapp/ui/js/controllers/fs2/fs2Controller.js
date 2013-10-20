'use strict'

var fs2Controller = myApp.controller('FS2Controller', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

        // instantiate and initialize a notification manager
        var notifier = new NotificationManager($scope);

        $scope.fs2Objects = {};

        $scope.fs2RepoIsEmpty = function () {
            for(var prop in $scope.fs2Objects) {
                if($scope.fs2Objects.hasOwnProperty(prop))
                    return false;
            }
            return true;
        }

        $scope.updateFS2RepoListing = function () {
            var url = 'rest/v1/fs2/';
            console.log("calling fs2 list");
            return $http({method: 'GET', url: url})
                .success(function (data, status, headers, config) {
                    //console.log(data);
                    $scope.fs2Objects = data;
                })
                .error(function (data, status, headers, config) {
                    //console.log(data);
                    $scope.fs2Objects = data;
                });
        }


        // NOTE:  update on page load, and after create and delete activities
        // note that updates from other clients will not show until this client
        // refreshes or performs create/delete.  For auto update, we can use the
        // polling service defined in services.js.  This is problematic however as
        // polling comes with its own set of issues.  Ideally we' use websockets.
        // Putting websockets on the roadmap.  For now, this should suffice.
        $scope.updateFS2RepoListing();

        $scope.fs2Object = null;

        $scope.fs2Fetch = function (uri) {
            var url = 'rest/v1/fs2/';
            console.log("calling fs2 fetch");
            var d = '{' + '\"uri\"' + ':' + "\'" + uri + "\'" + '}';
            return $http({method: 'POST', url: url, data: d})
                .success(function (data, status, headers, config) {
                    //console.log(data);
                    $scope.fs2Object = data;
                })
                .error(function (data, status, headers, config) {
                    //console.log(data);
                    $scope.fs2Object = data;
                });
        }

        // FS2 service base url
        $scope.fs2ServiceUrl = "rest/v1/fs2/";

        // FOR FS2 repo browsing
        $scope.fs2ExistingObjects = null;

        // FOR FS2 Delete Object
        $scope.deleteFS2Object = function (fs2Uri) {

            var requestBody = {"uri": fs2Uri};

            // this is required to map to the Jersey request...
            // TODO can set this as default header for all DELETE calls
            var headers = {
                'Content-Type': 'application/json;charset=utf-8'
            }

            return $http({
                method: 'DELETE',
                url: $scope.fs2ServiceUrl,
                headers: headers,
                data: requestBody}).
                success(function (data, status, headers, config) {
                    // this callback will be called asynchronously
                    // when the response is available
                    $scope.updateFS2RepoListing();
                    notifier.notify('success', data);
                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    $scope.updateFS2RepoListing();
                    notifier.notify('error', data);
                });
        }


        // FOR FS2 Fetch Object
        // Note this is ajax only so not available for file download via browser
        $scope.fetchFS2Object = function (fs2Uri) {

            var requestBody = {"uri": fs2Uri};

            $http.post($scope.fs2ServiceUrl, requestBody).
                success(function (data, status, headers, config) {
                    // this callback will be called asynchronously
                    // when the response is available
                    console.log(data);
                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                     notifier.notify('error', data);
                });
        }

        // FOR FS2 upload
        $scope.fs2ObjectURI = '/foo/bar';
        $scope.fs2ObjectFile = null;

        $scope.upload = function (file, fs2ObjectURI) {

            if (file == null) {
                notifier.notify('error', 'Please select a file for upload.');
                return;
            }

            // build form data
            var formData = new FormData();
            formData.append("file", file[0]);

            // build http request headers
            // TODO create a meta model and bind to form...
            // TODO and use this instead of hard coding meta below
            var headers = {
                "Content-Type": undefined,
                "fs2-uri": fs2ObjectURI,
                "fs2-meta1": "foo",
                "fs2-meta2": "bar"
            };

            return $http({
                method: 'POST',
                url: $scope.fs2ServiceUrl,
                headers: headers,
                data: formData,
                transformRequest: function (data) {
                    return data;
                }
            })
                .success(function (data, status, headers, config) {
                    $scope.updateFS2RepoListing();
                    $scope.response = data;
                    notifier.notify('success', data);

                })
                .error(function (data, status, headers, config) {
                    $scope.updateFS2RepoListing();
                    $scope.response = data;
                    notifier.notify('error', data);
                });
        }

    }])
    ;

