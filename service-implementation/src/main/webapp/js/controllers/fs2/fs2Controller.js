'use strict'

var fs2Controller = myApp.controller('FS2Controller', ['$scope', '$routeParams', '$http', '$timeout', function ($scope, $routeParams, $http, $timeout) {


        // notification queue

        $scope.notifications = [];

        // TODO this needs to be reworked....
        // TODO timer is not a very elegant solution here...
        // but need to clean up the model (the directive marks processed when noty is called)
        var clearNotifications = function () {

            for (var i=0; i < $scope.notifications.length; i++) {
                if($scope.notifications[i].processed == true) {
                    //console.log($scope.notifications[i].processed);
                    $scope.notifications = $scope.notifications.splice(i, 0);
                    i = i + 1;
                    continue;
                }
            }

            //console.log("Number of notifications:  " + $scope.notifications.length);

            $timeout(clearNotifications, 5000);
        }

        clearNotifications();

        /*
         * Display a notification message to user
         */
        var notify = function(type, text) {
            $scope.notifications.push({"type": type, "text": text});
            console.log("Added notification.  Notifications: " + $scope.notifications['processed']);
        }


        // TODO FS2 Todo...
        // Remove mock meta in favor of form fields with add/delete, bound to meta model
        // Move into own controller and directive file
        // Change console log to "quick view"
        // enumerate types... on error show longer than default of 3 seconds
        // Put actions items in table (nggrid) with icons/hover for actions.
        // Contrast REST api with S3

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
                    console.log(data);
                    $scope.fs2Objects = data;
                })
                .error(function (data, status, headers, config) {
                    console.log(data);
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
                    console.log(data);
                    $scope.fs2Object = data;
                })
                .error(function (data, status, headers, config) {
                    console.log(data);
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
                    notify('success', data);
                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    $scope.updateFS2RepoListing();
                    notify('error', data);
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
                     notify('error', data);
                });
        }

        // FOR FS2 upload
        $scope.fs2ObjectURI = '/foo/bar';
        $scope.fs2ObjectFile = null;

        $scope.upload = function (file, fs2ObjectURI) {

            if (file == null) {
                notify('error', 'Please select a file for upload.');
                return;
            }

            // build form data
            var formData = new FormData();
            formData.append("file", file[0]);

            // build http request headers
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
                    notify('success', data);

                })
                .error(function (data, status, headers, config) {
                    $scope.updateFS2RepoListing();
                    $scope.response = data;
                    notify('error', data);
                });
        }

    }])
    ;

