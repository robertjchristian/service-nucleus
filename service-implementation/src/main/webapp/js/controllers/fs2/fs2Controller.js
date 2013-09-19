'use strict'

var fs2Controller = myApp.controller('FS2Controller', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

        // TODO FS2 Todo...
        // Remove mock meta in favor of form fields with add/delete, bound to meta model
        // Move into own controller and directive file
        // Change console log to "quick view"
        // Show error as alert
        // Show success as alert
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

// form validation and binding
        $scope.master = "";

        $scope.saveForm = function (user) {
            console.log("User..." + $scope.user);
            $scope.master = user;
        }

// carousel
        $scope.slides = [
            {"image": "http://cdn-static.zdnet.com/i/r/story/70/00/004209/original/raspberry-pi-supercomputer-1-620x465.jpg?hash=AQx4MwRjZG", "text": "foo", "active": true},
            {"image": "http://www.rubberrepublic.com/wp-content/uploads/2011/09/lolcat-funny-picture-moderator1.jpg", "text": "bar", "active": false},
            {"image": "http://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Composition_of_38th_Parliament.png/220px-Composition_of_38th_Parliament.png", "text": "moo", "active": false}
        ]


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
                    console.log(data);

                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    $scope.updateFS2RepoListing();
                    alert("Error:  " + data);
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
                    alert("Error:  " + data);
                });
        }

// FOR FS2 upload
        $scope.fs2ObjectURI = '/foo/bar';
        $scope.fs2ObjectFile = null;

        $scope.upload = function (file, fs2ObjectURI) {

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

                })
                .error(function (data, status, headers, config) {
                    $scope.updateFS2RepoListing();
                    $scope.response = data;
                });
        }

    }])
    ;
