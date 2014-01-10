module.exports = function(config) {
    config.set({
        // your config
        basePath: '../',
        frameworks: ["jasmine"],
        browsers: ["Chrome"],
        files: [
            'ui/js/vendor/angular-1.2.0-rc.3/angular.js',
            'ui/js/vendor/angular-1.2.0-rc.3/angular-*.js',
            'http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js',
            'http://ajax.googleapis.com/ajax/libs/jqueryui/1.9.2/jquery-ui.min.js',
            'ui/js/vendor/angularui/*.js',
            'ui/js/vendor/ace/*.js',
            'http://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.6.0.js',
            'ui/js/vendor/bootstrap/*.js',
            'ui/js/vendor/ng-grid/*.js',
            'ui/js/vendor/angularstrap/*.js',
            'ui/js/*.js',
            'ui/js/services/*.js',
            'ui/js/controllers/**/*.js',
            'ui/js/directives/**/*.js',
            'ui/js/filters/*.js',
            'test/unit/**/*.js'
        ],
        exclude: [
            'ui/js/vendor/angular-1.2.0-rc.3/angular-scenario.js'
        ]
    });
};