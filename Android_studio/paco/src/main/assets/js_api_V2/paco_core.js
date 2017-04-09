/**
 * This javascript contains all the core functions of Paco. The functions on grouped on the following 
 * categories
 * a) experimentService
 * b) notificationService
 * c) db
 * d) events
 * e) executor
 * f) photoService
 */

var paco2 = (function (init) {
  var obj = init || {};
  var environment = obj["environment"] || "test";

  /*
   * This function is responsible for creating a response object from an input
   * @parameters - input 
   * @return - returns a response object for the input provided. 
   */
  obj.createResponseForInput = function(input) {
    return { "name" : input.name, 
             "prompt" : input.text,
             "isMultiselect" : input.isMultiselect,
             "answer" : input.answer, 
             "responseType" : input.responseType
           };
  };

  /*
   * This function is responsible for creating a response object array from an array of inputs
   * @parameters - input array 
   * @return - returns a response object array contains the responses for the inputs provided. 
   */
  obj.createResponsesForInputs = function(inputs) {
    var responses = [];
    if (!inputs) {
      return responses;
    }
    for (var experimentalInput in inputs) {
      responses.push(obj.createResponseForInput(inputs[experimentalInput]));
    }
    return responses;
  };

  /*
   * This function is responsible for creating a response event array object for an experiment with responses
   * @parameters - experiment, experimentGroup, responses, scheduledTime
   * @return - returns a array of response event objects
   */
  obj.createResponseEventForExperimentWithResponses = function(experiment, experimentGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, responses, scheduledTime) {
    var egName = experimentGroup != null ? experimentGroup.name : "";
    return  {
      "experimentId" : experiment.id,
      "experimentVersion" : experiment.version,
      "experimentGroupName" : egName,
      "actionTriggerId" : actionTriggerId,
      "actionId" : actionId, 
      "actionTriggerSpecId" : actionTriggerSpecId, 
      "responseTime" : null, 
      "scheduledTime" : scheduledTime,
      "responses" : responses
    };
  };

  /*
   * This function is responsible for creating a response event object for an experimentGroup
   * @parameters - experiment, experimentGroup, actionTriggerId, actionId, actionTriggerSpecId, scheduledTime
   * @return - returns a response event object
   */
  obj.createResponseEventForExperimentGroup = function(experiment, experimentGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, scheduledTime) {
    var inputs = experimentGroup != null ? experimentGroup.inputs : null;
    var responses = obj.createResponsesForInputs(inputs);
    
    return obj.createResponseEventForExperimentWithResponses(experiment, experimentGroup, 
        actionTriggerId, actionId, actionTriggerSpecId, responses, scheduledTime);
  };
  
  /*
   * This function is responsible for creating a response event object for an experiment
   * @parameters - experiment, experimentGroup, scheduledTime
   * @return - returns a response event object
   */
  obj.createResponseEventForExperiment = function(experiment, scheduledTime) {
    return obj.createResponseEventForExperimentGroup(experiment, null, null, null, null, scheduledTime);
  };

  obj.answerHas = function(answer, value) {
    return answer.indexOf(value) != -1;
  };
  
  function isNumeric(num) {
    return (num >=0 || num < 0);
  };

  function validNumber(val) {
    if (!isNumeric(val)) {
      return false;
    }
    try {
      // is this necessary given the isNumeric test?
      parseFloat(val);
      return true;
    } catch (e) {
      return false;
    }     
  };

  function validValueForResponseType(response) {
    if (response.responseType === "number") {
      return validNumber(response.answer);
    } else {
      return true;
    }
  };

  
//--------------------------------------------------------DB FUNCTIONS----------------------------------------------------------//
//The following functions allow you to manipulate the pacoDB, there are two kinds of DB, testDb and realDb
    
  obj.db = (function() {
	  
    //The functions below operate on realDb
    var db = function() { 
      var events = [];
      var loaded = false;

      
      /*
       * This function saves an event in the DB
       * @parameter - event
       * @return - status of the operation
       */
      function saveEvent(event) {
        event.responseTime = new Date();
        window.db.saveEvent(JSON.stringify(event));
        events.unshift(event);
        return {"status" : "success"};
      };

      
      /*
       * This function gets all the events stored in the DB for the current experiment
       * @return - an array of events
       */
      function getAllEvents() {
        if (!loaded) {
          events = JSON.parse(window.db.getAllEvents());
          loaded = true;
        }
        return events;
      }

      
      /*
       * Returns the last event stored in the DB
       */
      function getLastEvent() {
        return JSON.parse(window.db.getLastEvent());
      };

      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent
      };
    };
    

    /*
     * This function saves an event in the DB
     * @parameter - event and callback
     * @return - status of the operation
     */
    var saveEvent = function(event, callback) {
        var status = db.saveEvent(event);
        if (callback) {
          callback(status);
        }
    };
      
    /*
     * This function gets all the events stored in the DB for the current experiment
     * @return - an array of events
     */
    var getAllEvents = function() {
        // shallow cloning of the events array
    	var newarray = new Array();
        var eventData = db.getAllEvents();
        for(var i=0; i < eventData.length; i++) {
          newarray[i] = eventData[i];
        }
        return newarray;
    };

    
    /*
     * Given a list of responses and an item to be searched, it finds the response with item as its name
     * @parameter - a list of responses and an item to search for
     * @return - response associated with the item if not found, return null
     */
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
    
    
    /*
     * Gets responses for an event N times ago
     * @parameter - N, number of times before the current event
     * @return - responses for an event that occurred N times ago, null if event does not exist
     */
    var getResponsesForEventNTimesAgo = function (nBack) {
        var experimentData = db.getAllEvents();
        if (nBack > experimentData.length) {
          return null; // todo decide whether to throw an exception instead?
        } 
        var event = experimentData[nBack - 1]; 
        return event.responses;
    };

    
    /*
     * Get answer for an item from responses N times ago
     * @parameter - item and nBack, number of times before the current event
     * @return - response for an item for an event that occurred N times ago, null if event does not exist
     */
    var getAnswerForEventNTimesAgoFor = function (item, nBack) {
        var responses = getResponsesForEventNTimesAgo(nBack);        
        return getResponseForItem(responses, item);
    };

    return {
      saveEvent : saveEvent,
      getAllEvents : getAllEvents,

      /*
       * Returns the last event stored in the DB
       */
      getLastEvent : function() {
        return db.getLastEvent();
      },

      /*
       * Returns the last N events stored in the DB
       */
      getLastNEvents : function(n) {
        var events = db.getAllEvents();
        return events.slice(0..n);
      },

      getResponsesForEventNTimesAgo : getResponsesForEventNTimesAgo,

      getAnswerForEventNTimesAgoFor : getAnswerForEventNTimesAgoFor,
      
      /*
       * This function returns the most recent answer for an item
       * @parameter - key (item name)
       * @return - Finds the last event which contains the key and returns its answer
       */
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

      
      /*
       * This function returns the most recent answer for an item from events stored today
       * @parameter - key (item name)
       * @return - Finds the last event which contains the key and returns its answer
       */
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
      }
    };
  })();

  
  //--------------------------------------------------EXPERIMENT SERVICE FUNCTIONS-------------------------------------------------//
  //All the functions on this category are related to experiment and experimentService
  
  //Functions below are related to experimentService
  obj.experimentService = (function() {
    
    /*
     * Returns the current experiment group
     */
    var getExperimentGroup = function() {
      if (!window.experimentLoader) {
        return null;
      } else {
        return JSON.parse(window.experimentLoader.getExperimentGroup());
      }
    };
    
    /*
     * Returns the End of Day experiment group
     */
    var getEndOfDayReferredExperimentGroup = function() {
      if (!window.experimentLoader) {
        return null;
      } else {
        return JSON.parse(window.experimentLoader.getEndOfDayReferredExperimentGroup());	//THERE WAS TWO GET IN THIS FUNCTION NAME
      }
    };


    /*
     * Saves the experiment
     */
    var saveExperiment = function(experimentString) {
      if (!window.experimentLoader) {
        return saveTestExperiment();	//THERE IS NO saveTestExperiment function
      } else {
        return window.experimentLoader.saveExperiment(experimentString);
      }
    };

    return {
      getExperimentGroup : getExperimentGroup,
      getEndOfDayReferredExperimentGroup : getEndOfDayReferredExperimentGroup,
      saveExperiment : function(experiment, callback) {
        var result = saveExperiment(JSON.stringify(experiment), callback);
        if (callback) {
          callback(result);
        }
      }
    };
  })(); 
	  
//------------------------------------------------------------OTHER CORE FUNCTIONS---------------------------------------------------//
//This category includes executor, photoService, notificationService

  //Executor function
  obj.executor = (function() {
    if (!window.executor) {
      window.executor = { done : function() { alert("done"); } };
    }
    return {
      done : function() {
        window.executor.done();
      }
    };
  })();

  
  //PhotoService function which provides support for photo
  obj.photoService = (function() {
    var callback;

    if (!window.photoService) {
      window.photoService = { 
        launch : function(callback) { 
          alert("No photo support"); 
        } 
      };
    }

    return {
      launch : function(callback2) {
        callback = callback2;
        window.photoService.launch();
      },
      photoResult : function(base64BitmapEncoding) {
        //alert("Got it!");
        if (callback) {
          callback(base64BitmapEncoding);
        }
      } 
    };
  })();
  
  
  //NotificationService function that enables to create or remove notification
  obj.notificationService = (function() {
	    if (!window.notificationService) {
	      window.notificationService = { 
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
	        window.notificationService.createNotification(message);
	      }, 
	      removeNotification : function(message) {
	    	  notificationService.removeNotification(message);
	      },
          removeAllNotifications : function() {
        	  notificationService.removeAllNotifications();
          }
	    };
	  })();


  return obj;
})();