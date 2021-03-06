/**
 * Binds a ACE Ediitor widget
 */

//TODO handle Could not load worker ace.js:1
//DOMException {message: "SECURITY_ERR: DOM Exception 18", name: "SECURITY_ERR", code: 18, stack: "Error: An attempt was made to break through the seâ€¦cloudfront.net/src-min-noconflict/ace.js:1:76296)", INDEX_SIZE_ERR: 1â€¦}

angular.module('ui.ace', [])
    .constant('uiAceConfig', {})
    .directive('uiAce', ['uiAceConfig', function (uiAceConfig) {
        if (angular.isUndefined(window.ace)) {
            throw new Error('ui-ace need ace to work... (o rly?)');
        }
        return {
            restrict: 'EA',
            require: '?ngModel',
            link: function (scope, elm, attrs, ngModel) {
                var options, opts, acee, session, onChange;

                //console.log(ngModel);

                options = uiAceConfig.ace || {};
                opts = angular.extend({}, options, scope.$eval(attrs.uiAce));

                acee = window.ace.edit(elm[0]);
                session = acee.getSession();

                // RJC adding due to issue expressed here
                // http://stackoverflow.com/questions/15599597/how-to-load-ace-editor
                session.setUseWorker(false);

                onChange = function (callback) {
                    return function (e) {
                        var newValue = session.getValue();
                        if (newValue !== scope.$eval(attrs.value) && !scope.$$phase && !scope.$root.$$phase) {
                            if (angular.isDefined(ngModel)) {
                                scope.$apply(function () {
                                    ngModel.$setViewValue(newValue);
                                });
                            }

                            /**
                             * Call the user onChange function.
                             */
                            if (angular.isDefined(callback)) {
                                scope.$apply(function () {
                                    if (angular.isFunction(callback)) {
                                        callback(e, acee);
                                    }
                                    else {
                                        throw new Error('ui-ace use a function as callback.');
                                    }
                                });
                            }
                        }
                    };
                };


                // Boolean options
                if (angular.isDefined(opts.showGutter)) {
                    acee.renderer.setShowGutter(opts.showGutter);
                }
                if (angular.isDefined(opts.useWrapMode)) {
                    session.setUseWrapMode(opts.useWrapMode);
                }

                // onLoad callback
                if (angular.isFunction(opts.onLoad)) {
                    // RJC:  Including index on callback
                    if (angular.isDefined(opts.index)) {
                        //console.log("Task index:  " + opts.index);
                        opts.onLoad(acee, opts.index);

                    } else {
                        opts.onLoad(acee);
                    }

                }

                // Basic options
                if (angular.isString(opts.theme)) {
                    acee.setTheme("ace/theme/" + opts.theme);
                }
                if (angular.isString(opts.mode)) {
                    session.setMode("ace/mode/" + opts.mode);
                }

                attrs.$observe('readonly', function (value) {
                    acee.setReadOnly(value === 'true');
                });

                // Value Blind
                if (angular.isDefined(ngModel)) {
                    ngModel.$formatters.push(function (value) {
                        if (angular.isUndefined(value) || value === null) {
                            return '';
                        }
                        else if (angular.isObject(value) || angular.isArray(value)) {
                            throw new Error('ui-ace cannot use an object or an array as a model');
                        }

                        return value;
                    });

                    ngModel.$render = function () {
                        //session.setValue(ngModel.$viewValue);

                        // TODO - Can't seem to figure out how to make the editor
                        // TODO not highlight all text upon render
                        // TODO see this SO for details:
                        // TODO http://stackoverflow.com/questions/20111668/ace-editor-loaded-script-is-highlighted-by-default
                        var value = ngModel.$viewValue;
                        session.setValue(value, -1) // moves cursor to the start
                        session.setValue(value, 1) // moves cursor to the end


                    };
                }

                // EVENTS
                session.on('change', onChange(opts.onChange));

            }
        };
    }]);