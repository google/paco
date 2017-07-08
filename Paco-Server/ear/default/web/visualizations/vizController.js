/**
 * Created by muthuk on 6/28/17.
 */

app.controller('MainController',['$scope','experimentsFactory', function($scope,experimentsFactory){

    //Response map for storing meta data of the response types
    var responsesMap = new Map();
    experimentsFactory.getExperimentObject.then(function(experimentInfo){
        //console.log(experimentInfo);
        var responseTypes = [];
        experimentInfo.groups[0].inputs.forEach(function(response){
          if(response.responseType == "likert"){
              responsesMap.set(response.name,{"name":response.name, "responseType":response.responseType,"rightSideLabel":response.rightSideLabel,"leftSideLabel":response.leftSideLabel});
          } else if(response.responseType == "list"){
              responsesMap.set(response.name, {"name":response.name,"responseType":response.responseType,"listChoice":response.listChoices});
          }else{
              responsesMap.set(response.name,{"name":response.name,"responseType":response.responseType});
          }
          responseTypes.push(response.responseType);
        });
        $scope.experimentModel = {
            id: experimentInfo.id,
            title: experimentInfo.title,
            creator: experimentInfo.creator,
            date: experimentInfo.modifyDate,
            responseTypes: responseTypes
        }
    });
    console.log(responsesMap);

    //TODO - Create data model for visualization based on response types
    var allResponses = [];
    experimentsFactory.groupResponses.then(function(responses){
        var mapIter = responsesMap.values();
        //console.log(responses);
        responses.forEach(function(res){
            //console.log(res);
            if(responsesMap.has(res.key)){
                var value = mapIter.next().value;
                if(res.key === value.name){
                    //console.log(value.responseType);
                }

            }
        });
    });
}]);
//Sample controller and data set
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