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
    if (!inputs) {
      return responses;
    }
    for (var experimentalInput in inputs) {
      responses.push(obj.createResponseForInput(inputs[experimentalInput]));
    }
    return responses;
  };

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

//  
//
  obj.createResponseEventForExperimentGroup = function(experiment, experimentGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, scheduledTime) {
    var inputs = experimentGroup != null ? experimentGroup.inputs : null;
    var responses = obj.createResponsesForInputs(inputs);
    return obj.createResponseEventForExperimentWithResponses(experiment, experimentGroup, 
        actionTriggerId, actionId, actionTriggerSpecId, responses, scheduledTime);
  };
  
  obj.createResponseEventForExperiment = function(experiment, scheduledTime) {
    return obj.createResponseEventForExperimentGroup(experiment, null, null, null, null, scheduledTime);
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
      
      function getEventsForExperimentGroup() {
        alert("not implemented!");
      };
      
      function getLastEvent() {
        getAllEvents();
        return events[events.length - 1];
      };

      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent,
        getEventsForExperimentGroup : getEventsForExperimentGroup
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
          events = JSON.parse(pacodb.getAllEvents());
          loaded = true;
        }
        return events;
      };
      
      function getEventsForExperimentGroup() {
        if (!loaded) {
          events = JSON.parse(pacodb.getEventsForExperimentGroup());
          loaded = true;
        }
        return events;
      };

      function getLastEvent() {
        return JSON.parse(pacodb.getLastEvent());
      };

      function getEventsByQuery(queryJson) {
        return JSON.parse(pacodb.getEventsByQuery(queryJson));
      };


      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent,
        getEventsByQuery : getEventsByQuery,
        getEventsForExperimentGroup : getEventsForExperimentGroup
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

   /*
     * The query JSON should have the following format Example
     * {query:{criteria: " (group_name in(?,?) and (answer=?)) ",values:["New
     * Group","Exp Group", "ven"]},limit: 100,group: "group_name",order:
     * "response_time" ,select: ["group_name","response_time",
     * "experiment_name", "text", "answer"]}
     * The above JSON represents the following
     * query->criteria: String with where clause conditions and the values replaced by '?'
     * query->values: An array of String representing the values of the '?' expressed in query->criteria (in order).
     * query->limit: Integer Number of records to limit the result set . This will apply only if we have valid value in 'order' clause
     * query->group: String which holds the group by column
     * query->order: String which holds the order by columns separated by commas
     * query->select: An array of String which holds the column names and executes the following query
     * Since the query requires columns from both Events and Outputs table, we do the
     * inner join. If the query requires columns from just Events table, it will
     * be a plain select ......from Events
     * SELECT group_name, response_time,
     * experiment_name, text, answer FROM events INNER JOIN outputs ON
     * events._id = event_id WHERE ( (group_name in(?,?) and (answer=?)) ) GROUP
     * BY group_name ORDER BY response_time limit 100
     *
     */
    var getEventsByQuery = function(queryJson) {
      return db.getEventsByQuery(queryJson);
    };

    var getResponsesForEventNTimesAgo = function (nBack) {
        var experimentData = db.getAllEvents();
        if (nBack > experimentData.length) {
          return null; // todo decide whether to throw an exception instead?
        } 
        var event = experimentData[nBack - 1]; 
        return event.responses;
    };

    var getAnswerNTimesAgoFor_ = function (item, nBack) {
        var responses = getResponsesForEventNTimesAgo(nBack);
        return getResponseForItem(responses, item);
    };

    return {
      saveEvent : saveEvent,
      getAllEvents : getAllEvents,
      getEventsByQuery : getEventsByQuery,
      getLastEvent : function() {
        return db.getLastEvent();
      },

      getLastNEvents : function(n) {
        var events = db.getAllEvents();
        return events.slice(0..n);
      },
      
      getEventsForExperimentGroup : function() {
        return db.getEventsForExperimentGroup();
      },

      getResponsesForEventNTimesAgo : getResponsesForEventNTimesAgo,

      getAnswerNTimesAgoFor : getAnswerNTimesAgoFor_,
      getLastAnswerFor : function (item) {
        return getAnswerNTimesAgoFor_(item, 1);
      },

      getAnswerBeforeLastFor : function (item) {
        return getAnswerNTimesAgoFor_(item, 2);
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
	        	// TODO i18n
	          alert("No notification support"); 
	        },
	        createNotificationWithTimeout : function(message, timeout) { 
            // TODO i18n
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
	      createNotificationWithTimeout : function(message, timeout) {
          notificationService.createNotificationWithTimeout(message, timeout);
        },
        removeNotification : function(message) {
	    	  notificationService.removeNotification(message);
	      },
        removeAllNotifications : function() {
          notificationService.removeAllNotifications();
        }
	    };
	  })();

  obj.stringService = (function() {
	    if (!strings) {
	      strings = { 
	        getString: function(stringId) { 
	        	// TODO i18n
	          alert("No strings support"); 
	        },
	        getString : function(stringId, formatArgs) { 
		          alert("No strings support"); 
		      }
	        };
	    }

	    return {
	      getString : function(stringId) {
	        return strings.getString(stringId);
	      }, 
	      getStringFormatted : function(stringId, formatArgs) {
		    return strings.getString(stringId, formatArgs);
		  }
	    };
	  })();

  obj.calendarService = (function() {
    if (!calendar) {
      calendar = { 
        listEventInstances : function(startMillis, endMillis) { 
          // TODO i18n
          alert("No calendar support"); 
        }
      };
    }

    return {
      listEventInstances : function(startMillis, endMillis) {
        return calendar.listEventInstances(startMillis, endMillis);
      }
    };
  })();
  
  obj.locationService = (function() {
    if (!locationService) {
      locationService = { 
        getLastKnownLocation: function() { 
          // TODO i18n
          alert("No locationService support"); 
        },
        getDistanceFrom : function(location) { 
          alert("No locationService support"); 
        }
      };
    }

    return {
      getLastKnownLocation: function() { 
        return JSON.parse(locationService.getLastKnownLocation());
         
      },
      getDistanceFrom : function(location) { 
        return JSON.parse(locationService.getDistanceFrom(location)); 
      }
    };
  })();

  return obj;
})();
