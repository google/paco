/**
 * Created by muthuk on 6/28/17.
 */

app.controller('MainController',['$scope','experimentsFactory', function($scope,experimentsFactory){

    $scope.mainModel = {
        responseTypes:
            ["number","open text","likert"]
    };
}]);

app.controller('LineChartController', [ '$scope', function($scope) {

    $scope.lineChartData = [{

        "who": "pacotest100@gmail.com",
        "value": "42"
    },{

        "who": "ces100@gmail.com",
        "value": "30"
    },{

        "who": "paco100@gmail.com",
        "value": "20"
    },{

        "who": "pacotest100@gmail.com",
        "value": "42"
    },{

        "who": "paco.muthu@gmail.com",
        "value": "15"
    },{

        "who": "p100@gmail.com",
        "value": "10"
    },{

        "who": "test100@gmail.com",
        "value": "20"
    }, {

        "who": "st100@gmail.com",
        "value": "5"
    },{

        "who": "t100@gmail.com",
        "value": "100"
    },{

        "who": "pct100@gmail.com",
        "value": "27"
    },{

        "who": "pacot100@gmail.com",
        "value": "50"
    },{

        "who": "pat100@gmail.com",
        "value": "42"
    },{
        "who": "ots100@gmail.com",
        "value": "40"
    }];
}]);

app.controller('BubbleChartController',['$scope', function($scope) {
    $scope.bubbleData = [{
        "number": "42",
        "frequency": "2"
    },{

        "number": "30",
        "frequency": "1"
    },{

        "number": "50",
        "frequency": "4"
    },{

        "number": "12",
        "frequency": "1"
    },{

        "number": "10",
        "frequency": "3"
    },{

        "number": "45",
        "frequency": "1"
    },{

        "number": "31",
        "frequency": "1"
    }, {

        "number": "23",
        "frequency": "2"
    },{

        "number": "29",
        "frequency": "1"
    },{

        "number": "41",
        "frequency": "1"
    },{

        "number": "37",
        "frequency": "1"
    },{

        "number": "8",
        "frequency": "1"
    },{
        "number": "54",
        "frequency": "2"
    }];
 }]);

// app.controller('BubbleChartController',['$scope', function($scope){
//     $scope.bubbleData = [{
//        key:'Data',
//        values: {
//            "CA": 170, "US": 393, "BB": 12, "CU": 9, "BR": 89, "MX": 192, "PY": 32, "UY": 9, "VE": 25, "BG": 42, "CZ": 12, "HU": 7, "RU": 184,
//            "FI": 42, "GB": 162, "IT": 87, "ES": 65, "FR": 42, "DE": 102, "NL": 12, "CN": 92, "JP": 65, "KR": 87, "TW": 9, "IN": 98, "SG": 32,
//            "ID": 4, "MY": 7, "VN": 8, "AU": 129, "NZ": 65, "GU": 11, "EG": 18, "LY": 4, "ZA": 76, "A1": 2, "Other": 254
//        }
//
//     }];

app.controller('BarChartController',['$scope', function($scope){

    $scope.barChartData = [{

        "who": "pacotest100@gmail.com",
        "value": "42"
    },{

        "who": "ces100@gmail.com",
        "value": "30"
    },{

        "who": "paco100@gmail.com",
        "value": "20"
    },{

        "who": "pacotest100@gmail.com",
        "value": "42"
    },{

        "who": "paco.muthu@gmail.com",
        "value": "15"
    },{

        "who": "p100@gmail.com",
        "value": "10"
    },{

        "who": "test100@gmail.com",
        "value": "20"
    }, {

        "who": "st100@gmail.com",
        "value": "5"
    },{

        "who": "t100@gmail.com",
        "value": "53"
    },{

        "who": "pct100@gmail.com",
        "value": "27"
    },{

        "who": "pacot100@gmail.com",
        "value": "50"
    },{

        "who": "pat100@gmail.com",
        "value": "42"
    },{
        "who": "ots100@gmail.com",
        "value": "40"
    }];
}]);