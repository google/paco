/**
 * Created by muthuk on 6/28/17.
 */
app.factory('experimentsFactory', function($http){

    var getExperiments = $http.get('data/experiments.json').then(function(response){
        return response.data;
    },function(error){
        console.log(error);
    });

    var getEvents = $http.get('data/events.json').then(function(response){
        return response.data;
    },function(error){
        console.log(error);
    });

    var experimentObject = getExperiments.then(function(response){
        return response;
    }).then(function(data){
        return data.experiments[0];
    });

    var eventsObject = getEvents.then(function(response){
        return response.events;
    });

    getResponsesPerExperiment(experimentObject,eventsObject);

    function getResponsesPerExperiment(experimentObject,eventsObject){

        console.log(experimentObject, eventsObject);

    }

    return {
        getExperiments: getExperiments,
        getEvents: getEvents
        // getExperimentsData: getExperimentsData,
        // getResponsePerExperiment: getResponsesPerExperiment
    }
});
