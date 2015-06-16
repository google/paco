var paco = (function (init) {
  var obj = init || {};
  var environment = obj["environment"] || "test";
  
  obj.createResponseForInput = function(input) {
    return { "name" : input.name, 
             "prompt" : input.text,
             "isMultiselect" : input.isMultiselect,
             "answer" : input.answer, 
             "responseType" : input.responseType
           };
  };
  
  obj.createResponsesForInputs = function(inputs) {
    var responses = [];
    for (var experimentalInput in inputs) {
      responses.push(obj.createResponseForInput(inputs[experimentalInput]));
    }
    return responses;
  };

  obj.createResponseEventForExperimentWithResponses = function(experiment, experimentGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, responses, scheduledTime) {
    return  {
      "experimentId" : experiment.id,
      "experimentVersion" : experiment.version,
      "experimentGroupName" : experimentGroup.name,
      "actionTriggerId" : actionTriggerId,
      "actionId" : actionId, 
      "actionTriggerSpecId" : actionTriggerSpecId, 
      "responseTime" : null, 
      "scheduledTime" : scheduledTime,
      "responses" : responses
    };
  };

//  
//
  obj.createResponseEventForExperimentGroup = function(experiment, experimentGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, scheduledTime) {
    var responses = obj.createResponsesForInputs(experimentGroup.inputs);
    return obj.createResponseEventForExperimentWithResponses(experiment, experimentGroup, 
        actionTriggerId, actionId, actionTriggerSpecId, responses, scheduledTime);
  };

  obj.answerHas = function(answer, value) {
    return answer.indexOf(value) != -1;
  };
  
  obj.random = function(limit) {
    return Math.floor(Math.random() * limit);
  };


  obj.db = (function() {
    var testDb = function() { 
      var events = [];
      var hasLocalStorage = typeof(Storage) !== "undefined";
      var loaded = false;

      function saveEvent(event) {
        event.responseTime = new Date();
        getAllEvents();
        events.unshift(event);
        if (hasLocalStorage) {            
          sessionStorage.events = JSON.stringify(events);
        } 
        return {"status" : "success"};
      };

      function getAllEvents() {
        if (!loaded && hasLocalStorage) {
          var eventsString = sessionStorage.events;
          if (eventsString) {
            events = JSON.parse(eventsString);
          }
          loaded = true;
        }
        return events;
      };
      
      function getLastEvent() {
        getAllEvents();
        return events[events.length - 1];
      };

      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent
      };
    };

    var realDb = function() { 
      var events = [];
      var loaded = false;

      function saveEvent(event) {
        event.responseTime = new Date();
        pacodb.saveEvent(JSON.stringify(event));
        events.unshift(event);
        return {"status" : "success"};
      };

      function getAllEvents() {
        if (!loaded) {
          events = JSON.parse(db.getAllEvents());
          loaded = true;
        }
        return events;
      }

      function getLastEvent() {
        return JSON.parse(db.getLastEvent());
      };

      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent
      };
    };

    var db; 
    if (!pacodb) {
      db = testDb();
    } else {
      db = realDb();
    } 

    var isFromToday = function(dateStr) {
      if (dateStr) {
        var eventDate = new Date(dateStr);
        var nowDate = new Date();
        return eventDate.getYear() === nowDate.getYear() &&
          eventDate.getMonth() === nowDate.getMonth() &&
          eventDate.getDate() === nowDate.getDate();
      }
      return null;
    };
    
    var getResponseForItem = function (responses, item) {
      if (responses == null) {
        return null;
      }
      
      for (var j =0 ; j < responses.length; j++) {
        if (responses[j]["name"] === item) {
          return responses[j]["answer"];
        }
      }
      return null;
    };


    var saveEvent = function(event, callback) {
        var status = db.saveEvent(event);
        if (callback) {
          callback(status);
        }
    };
      
    var getAllEvents = function() {
        // shallow cloning of the events array
        var newarray = new Array();
        var eventData = db.getAllEvents();
        for(var i=0; i < eventData.length; i++) {
          newarray[i] = eventData[i];
        }
        return newarray;
    };

    var getResponsesForEventNTimesAgo = function (nBack) {
        var experimentData = db.getAllEvents();
        if (nBack > experimentData.length) {
          return null; // todo decide whether to throw an exception instead?
        } 
        var event = experimentData[nBack - 1]; 
        return event.responses;
    };

    var getAnswerNTimesAgoFor = function (item, nBack) {
        var responses = getResponsesForEventNTimesAgo(nBack);
        return getResponseForItem(responses, item);
    };

    return {
      saveEvent : saveEvent,
      getAllEvents : getAllEvents,

      getLastEvent : function() {
        return db.getLastEvent();
      },

      getLastNEvents : function(n) {
        var events = db.getAllEvents();
        return events.slice(0..n);
      },

      getResponsesForEventNTimesAgo : getResponsesForEventNTimesAgo,

      getAnswerNTimesAgoFor : getAnswerNTimesAgoFor,
      getLastAnswerFor : function (item) {
        return getAnswerNTimesAgoFor(item, 1);
      },

      getAnswerBeforeLastFor : function (item) {
        return getAnswerNTimesAgoFor(item, 2);
      },

      getMostRecentAnswerFor : function(key) {
        var experimentData = db.getAllEvents();
        for(var i=0; i < experimentData.length; i++) {
          var modelResponse = getResponseForItem(experimentData[i].responses, key);
          if (modelResponse) {
            return modelResponse;
          }
        }
        return null;
      },

      getMostRecentAnswerTodayFor : function(key) {
        var experimentData = db.getAllEvents();
        for(var i=0; i < experimentData.length; i++) {
          if (!isFromToday(experimentData[i].responseTime)) {
            return null;
          }

          var modelResponse = getResponseForItem(experimentData[i].responses, key);
          if (modelResponse) {
            return modelResponse;
          }
        }
        return null;
      },

      recordEvent : function recordEvent(responses) {
        db.saveEvent({ "responses" : responses });
      }

    };
  })();

  obj.experimentService = (function() {
    var getExperiment = function() {
      if (!experimentLoader) {
        return getTestExperiment();
      } else {
        return JSON.parse(experimentLoader.getExperiment());
      }
    };

    var getExperimentGroup = function() {
      if (!experimentLoader) {
        return null;
      } else {
        return JSON.parse(experimentLoader.getExperimentGroup());
      }
    };
    

    var saveExperiment = function(experimentString) {
      if (!experimentLoader) {
        return saveTestExperiment();
      } else {
        return experimentLoader.saveExperiment(experimentString);
      }
    };

    return {
      getExperiment : getExperiment,
      getExperimentGroup : getExperimentGroup,
      saveExperiment : function(experiment, callback) {
        var result = saveExperiment(JSON.stringify(experiment), callback);
        if (callback) {
          callback(result);
        }
      }
    };
  })(); 
	  
	  
  obj.notificationService = (function() {
	    if (!notificationService) {
	      notificationService = { 
	        createNotification : function(message) { 
	          alert("No notification support"); 
	        },
	        removeNotification : function(message) { 
		          alert("No notification support"); 
		    },
		      removeAllNotifications : function() {
		        alert("No notification support");
		      }
	      };
	    }

	    return {
	      createNotification : function(message) {
	        notificationService.createNotification(message);
	      }, 
	      removeNotification : function() {
	    	  notificationService.removeNotification();
	      },
        removeAllNotifications : function() {
          notificationService.removeAllNotifications();
        }
	    };
	  })();


  return obj;
})();
