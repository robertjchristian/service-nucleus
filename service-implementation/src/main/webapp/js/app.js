'use strict';

// declare top-level module which depends on filters,and services
var myApp = angular.module('myApp',
    ['myApp.filters',
        'myApp.directives', // custom directives
        'ngGrid', // angular grid
        'ui', // angular ui
        'ngSanitize', // for html-bind in ckeditor
        'ui.bootstrap', // jquery ui bootstrap
        '$strap.directives', // angular strap
        'n3-charts.linechart' // d3 charting https://github.com/angular-d3/line-chart
    ]);

// bootstrap angular
myApp.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {

    // TODO use html5 *no hash) where possible
    // $locationProvider.html5Mode(true);

    $routeProvider.when('/', {
        templateUrl:'partials/home.html'
    });

    $routeProvider.when('/dashboard', {
        templateUrl:'partials/dashboard.html'
    });

    $routeProvider.when('/contact', {
        templateUrl:'partials/contact.html'
    });
    $routeProvider.when('/about', {
        templateUrl:'partials/about.html'
    });

    // note that to minimize playground impact on app.js, we
    // are including just this simple route with a parameterized 
    // partial value (see playground.js and playground.html)
    $routeProvider.when('/playground/:widgetName', {
        templateUrl:'playground/playground.html',
        controller:'PlaygroundCtrl'
    });

    // by default, redirect to site root
    $routeProvider.otherwise({
        redirectTo:'/'
    });

}]);

// this is run after angular is instantiated and bootstrapped
myApp.run(function ($rootScope, $location, $http, $timeout, AuthService, RESTService) {


    // CRUDE GLOBAL GRAPH DATA -   http://angular-d3.github.io/line-chart/
    // TODO MOVE

    $rootScope.d1 = [
        {x: 0, value: 4, otherValue: 14},
        {x: 1, value: 8, otherValue: 1},
        {x: 2, value: 15, otherValue: 11},
        {x: 3, value: 16, otherValue: 147},
        {x: 4, value: 23, otherValue: 87},
        {x: 5, value: 32, otherValue: 45}
    ];

    // Line
    $rootScope.o1 ={
        axes: {
            x: {type: 'linear', tooltipFormatter: function(x) {return x;}}
        },
        series: [
            {y: 'value', color: 'steelblue', type: 'area', label: 'Pouet'},
            {y: 'otherValue', axis: 'y2', color: 'lightsteelblue'}
        ],
        lineMode: 'linear'
    }

    $rootScope.o2 ={
        axes: {
            x: {type: 'cardinal', tooltipFormatter: function(x) {return x;}}
        },
        series: [
            {y: 'value', color: 'steelblue', type: 'area', label: 'Pouet'},
            {y: 'otherValue', axis: 'y2', color: 'lightsteelblue'}
        ],
        lineMode: 'cardinal'
    }

    // Area
    //$rootScope.o1 = {series: [{y: 'value', type: 'area', color: 'steelblue'}]};

    // Column
    //$rootScope.o2 = {series: [{y: 'value', type: 'column', color: 'steelblue'}]};

    // Interpolation
    //$rootScope.o3 = {lineMode: 'cardinal', series: [{y: 'value', color: 'steelblue'}]};

    // TODO move so soa dashboard controller
    $rootScope.alert1 =
        {
            "type": "success",
            "title": "Contivo",
            "content": "Successfully deployed on 5/12/13"
        }
    $rootScope.alert2 =
    {
        "type": "info",
        "title": "G2",
        "content": "Is so rock solid that support is getting bored."
    }
    $rootScope.alert3 =
    {
        "type": "success",
        "title": "FTP",
        "content": "Successfully deployed on 6/16/13"
    }

    $rootScope.alert4 =
    {
        "type": "success",
        "title": "AS2",
        "content": "Successfully deployed on 9/12/13"
    }
    $rootScope.alert5 =
    {
        "type": "error",
        "title": "3:00 pm Alert",
        "content": "Specialties just pulled cookies out of the oven"
    }
    $rootScope.alert6 =
    {
        "type": "success",
        "title": "SMTP",
        "content": "Successfully deployed on 12/01/14"
    }
    $rootScope.alert7 =
    {
        "type": "success",
        "title": "KMS",
        "content": "Successfully deployed on 11/01/13"
    }
    // end TODO move



    // *****
    // Eager load some data using simple REST client
    // *****

    $rootScope.restService = RESTService;

    // async load constants
    $rootScope.constants = [];
    $rootScope.restService.get('data/constants.json', function (data) {
            $rootScope.constants = data[0];
        }
    );

    // async load data do be used in table (playgound grid widget)
    $rootScope.listData = [];
    $rootScope.restService.get('data/generic-list.json', function (data) {
            $rootScope.listData = data;
        }
    );


    // *****
    // Initialize authentication
    // *****
    $rootScope.authService = AuthService;

    // text input for login/password (only)
    $rootScope.loginInput = 'rchristian@liaison.com';
    $rootScope.passwordInput = 'foo';

    $rootScope.$watch('authService.authorized()', function () {

        // if never logged in, do nothing (otherwise bookmarks fail)
        if ($rootScope.authService.initialState()) {
            // we are public browsing
            return;
        }

        // when user logs in, redirect to dashboard
        if ($rootScope.authService.authorized()) {
            $location.path("/dashboard");
        }

        // when user logs out, redirect to home
        if (!$rootScope.authService.authorized()) {
            $location.path("/");
        }

    }, true);

});




