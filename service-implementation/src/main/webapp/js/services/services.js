'use strict';

// simple stub that could use a lot of work...
myApp.factory('RESTService',
    function ($http) {
        return {
            get:function (url, callback) {
                return $http({method:'GET', url:url}).
                    success(function (data, status, headers, config) {
                        callback(data);
                        //console.log(data.json);
                    }).
                    error(function (data, status, headers, config) {
                        console.log("failed to retrieve data");
                    });
            }
        };
    }
);


// simple auth service that can use a lot of work... 
myApp.factory('AuthService',
    function () {
        var currentUser = null;
        var authorized = false;

        // initMaybe it wasn't meant to work for mpm?ial state says we haven't logged in or out yet...
        // this tells us we are in public browsing
        var initialState = true;

        return {
            initialState:function () {
                return initialState;
            },
            login:function (name, password) {
                currentUser = name;
                authorized = true;
                //console.log("Logged in as " + name);
                initialState = false;
            },
            logout:function () {
                currentUser = null;
                authorized = false;
            },
            isLoggedIn:function () {
                return authorized;
            },
            currentUser:function () {
                return currentUser;
            },
            authorized:function () {
                return authorized;
            }
        };
    }
);

// TODO use websockets instead
// TODO this quick polling blocks the rest of the app and causes funny
// behavior, ie select file dialog and meta modal hang/cancel...
myApp.factory('FS2ObjectPollerService', function($http, $timeout) {

    var url = 'rest/v1/fs2';

    var data = { response: {}, calls: 0 };
    var poller = function() {
        $http.get(url).then(function(r) {
            data.response = r.data;
            console.log("Poll response:  " + r.data);
            data.calls++;
            // call explicitly instead of timing out   (due to bug explained above)
            //$timeout(poller, 5000);
        });
    };
    poller();

    return {
        data: data
    };
});