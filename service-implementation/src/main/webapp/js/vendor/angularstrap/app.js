var app = angular.module('strap', ['$strap.directives']);

app.controller('StrapCtrl', function ($scope) {
    $scope.dropdown = [
        {text:'Another action', href:'#anotherAction'},
        {text:'Something else here', href:'#', click:'modal.saved=true'},
        {divider:true},
        {text:'Separated link', href:'#',
            submenu:[
                {text:'Second level link', href:'#'},
                {text:'Second level link 2', href:'#'}
            ]
        }
    ];
    $scope.formattedDropdown = "[\n  {text: 'Another action', href:'#anotherAction'},\n  {text: 'Something else here', href:'#'},\n  {divider: true},\n  {text: 'Separated link', href:'#', submenu: [\n    {text: 'Second level link', href: '#'},\n    {text: 'Second level link 2', href: '#'}\n  ]}\n]";
    $scope.modal = {content:'Hello Modal', saved:false};
    $scope.tooltip = {title:"Hello Tooltip<br />This is a multiline message!", checked:false};
    $scope.popover = {content:"Hello Popover<br />This is a multiline message!", saved:false};
    $scope.alerts = [
        {type:'success', title:'Holy guacamole!', content:'Best check yo self, you\'re not looking too good.<br><br><pre>2 + 3 = {{ 2 + 3 }}</pre>'}
    ];
    $scope.addAlert = function () {
        $scope.alerts.push({type:'info', title:'Heads up!', content:'To prevent databinding issues, <em>"the rule of thumb is, if youï»¿ use <code>ng-model</code> there has to be a dot somewhere." MiÅ¡ko Hevery</em>'});
    };
    $scope.button = {active:true};
    $scope.buttonSelect = {price:'89,99', currency:'â‚¬'};
    $scope.checkbox = {left:false, middle:true, right:false};
    $scope.radio = {left:false, middle:true, right:false};
    $scope.radioValue = 'middle';
    $scope.typeahead = ["Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Dakota", "North Carolina", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"];
    $scope.datepicker = {date:''};
    $scope.timepicker = {time:''};

    $scope.prettyPrint = function () {
        window.prettyPrint && window.prettyPrint();
    }

});