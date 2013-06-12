'use strict'

myApp.controller('InboxCtrl', ['$scope', function ($scope) {

    // modal
    $scope.open = function () {
        $scope.shouldBeOpen = true;
    };

    $scope.close = function () {
        $scope.closeMsg = 'I was closed at: ' + new Date();
        $scope.shouldBeOpen = false;
    };
    // end modal

    $scope.recentMail = [
        { type:'unread', from:'Phil Jenkins', msg:'Your password will expire in 23 days' },
        { type:'unread', from:'Paul Terry', msg:'CRM system updated.' },
        { type:'read', from:'Matt Lundquist', msg:'Successfully deployed Oracle 11g' },
        { type:'read', from:'Lindsay Dugan', msg:'Closed support ticket #4455' },
        { type:'read', from:'Lindsay Dugan', msg:'Reminder: Offsite in San Diego (comicon)' }
    ];

    $scope.closeMail = function (index) {
        //console.log("closing mail " + index);
        $scope.recentMail.splice(index, 1);
    };

}]);


