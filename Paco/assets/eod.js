var paco = (function (init) {
  var obj = init || {};
  var environment = obj["environment"] || "test";

  obj.createDisplay = function() {
    var output = $("<div id='output'></div>");
    output.appendTo("body");
    return {
      title : function(titlemsg) {
        output.html("<h1>"+titlemsg+"</h1>\n" + output.html());
      },
      add : function(msg) {
        output.html(output.html() + "\n<p style=\"font-size: large;\">"+msg+"</p>");
      }
    };
  };

  obj.createResponseForInput = function(input) {
    return { "name" : input.name, 
             "prompt" : input.text,
             "isMultiselect" : input.multiselect,
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
  obj.addEodResponses = function(responses, referredGroupName, currentEvent) {
    responses.push({"name" : "eodResponseTime", 
                    "answer": currentEvent.responseTime
    });
    responses.push({"name" : "referred_group", 
                    "answer" : referredGroupName 
    });
  }
//
  obj.createResponseEventForExperimentGroup = function(experiment, experimentGroup, 
      actionTriggerId, actionId, actionTriggerSpecId,
            scheduledTime) {
    var responses = obj.createResponsesForInputs(experimentGroup.inputs);
    return obj.createResponseEventForExperimentWithResponses(experiment, experimentGroup, 
        actionTriggerId, actionId, actionTriggerSpecId, responses, scheduledTime);
  };

  obj.answerHas = function(answer, value) {
    return answer.indexOf(value) != -1;
  };
  
  function isNumeric(num) {
    return (num >=0 || num < 0);
  };

  function validNumber(val) {
   if (!val) {
      return true;
    }
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
  
  // TODO i18n
  valid = function(input, inputHtml, response) {
    var displayProperty = !inputHtml.element.hasClass("hide");
    if ((input.required && displayProperty) && (!response.answer || response.answer.length === 0)) {
    	// TODO i18n
      return { "succeeded" : false, "error" : "Response required for " + input.name, "name" : input.name};    
    } else if (!validValueForResponseType(response)) {
      return { "succeeded" : false, "error" : "Response required for " + name, "name" : name};    
    } else {
      return { "succeeded" : true, "name" : input.name};
    }
  };
  
  
  obj.validate = function(experimentGroup, responseEvent, inputHtmls, errorMarkingCallback) {
    var hasErrors = false;
    var all = [];
    for (var i in experimentGroup.inputs) {
      var input = experimentGroup.inputs[i];
      var response = responseEvent.responses[i];
      var visualElement = inputHtmls[i];
      var validity = valid(input, visualElement, response);
      if (!validity.succeeded) {
        hasErrors = true;
      } 
      all.push(validity);
    }
    if (hasErrors) {
      errorMarkingCallback.invalid(all);
    } else {
      errorMarkingCallback.valid(responseEvent);
    }
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
        window.db.saveEvent(JSON.stringify(event));
        events.unshift(event);
        return {"status" : "success"};
      };

      function getAllEvents() {
        if (!loaded) {
          events = JSON.parse(window.db.getAllEvents());
          loaded = true;
        }
        return events;
      };
      
      function getEventsForExperimentGroup() {
        if (!loaded) {
          events = JSON.parse(window.db.getEventsForExperimentGroup());
          loaded = true;
        }
        return events;
      };

      function getLastEvent() {
        return JSON.parse(window.db.getLastNEvents(1));
      };
      
      function getLastNEvents(num) {
        return JSON.parse(window.db.getLastNEvents(num));
      };
      
      function getEventsByQuery(queryJson) {
        return JSON.parse(window.db.getEventsByQuery(queryJson));
      };

      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent,
        getLastNEvents : getLastNEvents,
        getEventsByQuery : getEventsByQuery,        
        getEventsForExperimentGroup : getEventsForExperimentGroup
      };
    };

    var db; 
    if (!window.db) {
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
        $.each(db.getAllEvents(), function(index, value) { newarray[index] = value });
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
     * query->limit: Integer Number of records to limit the result set 
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

    var getAnswerNTimesAgoFor = function (item, nBack) {
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
    	  return db.getLastNEvents(n);
      },
      getResponseForItem  : getResponseForItem,
      
      getEventsForExperimentGroup : function() {
        return db.getEventsForExperimentGroup();
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
      if (!window.experimentLoader) {
        return getTestExperiment();
      } else {
        return JSON.parse(window.experimentLoader.getExperiment());
      }
    };
    
    var getExperimentGroup = function() {
      if (!window.experimentLoader) {
        return null;
      } else {
        return JSON.parse(window.experimentLoader.getExperimentGroup());
      }
    };
    
    var getEndOfDayReferredExperimentGroup = function() {
      if (!window.experimentLoader) {
        return null;
      } else {
        return JSON.parse(window.experimentLoader.getEndOfDayReferredExperimentGroup());
      }
    };


    var saveExperiment = function(experimentString) {
      if (!window.experimentLoader) {
        return saveTestExperiment();
      } else {
        return window.experimentLoader.saveExperiment(experimentString);
      }
    };

    return {
      getExperiment : getExperiment,
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
	  
	  
  obj.experiment = function() {
	  
    if (!window.experimentLoader) {
      return getTestExperiment();
    } else {
      return JSON.parse(window.experimentLoader.getExperiment());
    } 
  };

  obj.executor = (function() {
    if (!window.executor) {
    	// TODO i18n
      window.executor = { done : function() { alert("done"); } };
    }

    return {
      done : function() {
        window.executor.done();
      }
    };
  })();

  obj.photoService = (function() {
    var callback;

    if (!window.photoService) {
      window.photoService = { 
        launch : function(callback) { 
        	// TODO i18n
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
  
  obj.notificationService = (function() {
	    if (!window.notificationService) {
	      window.notificationService = { 
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
	        window.notificationService.createNotification(message);
	      }, 
	      createNotificationWithTimeout : function(message, timeout) {
          notificationService.createNotificationWithTimeout(message, timeout);
        },
        removeNotification : function(message) {
	    	  window.notificationService.removeNotification(message);
	      },
        removeAllNotifications : function() {
          window.notificationService.removeAllNotifications();
        }
	    };
	  })();

  obj.stringService = (function() {
	    if (!window.strings) {
	      window.strings = { 
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
	        return window.strings.getString(stringId);
	      }, 
	      getStringFormatted : function(stringId, formatArgs) {
		    return window.strings.getString(stringId, formatArgs);
		  }
	    };
	  })();

  obj.calendarService = (function() {
    if (!window.calendar) {
      window.calendar = { 
        listEventInstances : function(startMillis, endMillis) { 
          // TODO i18n
          alert("No calendar support"); 
        }
      };
    }

    return {
      listEventInstances : function(startMillis, endMillis) {
        return window.calendar.listEventInstances(startMillis, endMillis);
      }
    };
  })();
  
  return obj;
})();

paco.renderer = (function() {

  function escapeHtml(text) {
    var map = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    };

    return text.replace(/[&<>"']/g, function(m) { return map[m]; });
  }
  
  renderPrompt = function(input) {
    var element = $(document.createElement("h6"));
    element.addClass("left light");
    element.text(input.text);    
    return element;    
  };

  shortTextVisualRender = function(input, response) {
    var rawElement = document.createElement("input");
    var element = $(rawElement);
    element.attr("type", "text");
    element.attr("name", input.name);

    if (response.answer) {
      element.attr("value", response.answer);
    }

    var myElement = element;

    var obj = {
      getValue : function() {
        return myElement.val();
      },
      setValue : function(val) {
        myElement.attr("value", val);
      }
    };
    
    return obj;
  };

  renderElement = function(input, response, parent, renderVisualCallback, conditionalListener) {
    var element = renderVisualCallback(input, response);

    element.change(function() {
      response.answer = element.val();
      conditionalListener.inputChanged();
      
    });
    parent.append(element);
    
    return element;
  };

  renderShortTextExperiment = function(input, response, conditionalListener, parent) {
    return renderElement(input, response, parent, shortTextVisualRender, conditionalListener);
  };

  renderTextShort = function(input, response, parent, conditionalListener) {
    var rawElement = document.createElement("input");
    var element = $(rawElement);
    element.addClass("light");
    element.attr("type", "text");
    element.attr("name", input.name);
    if (response.answer) {
      element.attr("value", response.answer);
    }
    parent.append(element);

    element.change(function() {
      response.answer = element.val();

      conditionalListener.inputChanged();
    });

    return element;
  };

  renderNumber = function(input, response, parent, conditionalListener) {
    var rawElement = document.createElement("input");
    var element = $(rawElement);
    element.addClass("light");
    element.attr("type", "number");
    element.attr("name", input.name);
    if (response.answer) {
      element.attr("value", parseInt(response.answer) - 1);
    }
    element.blur(function() {
      try {
        response.answer = element.val();
        element.removeClass("outlineElement");
      } catch (e) {
        element.addClass("outlineElement");
    	// TODO i18n
        alert("bad value: " + e);            
      }
      conditionalListener.inputChanged();
    });
    parent.append(element);

    return element;
  };


  renderScale = function(input, response, parent, conditionalListener) {
    var left = input.leftSideLabel || "";
    if (left) {
      var element = $(document.createElement("span"));
      element.html(left);
//      element.addClass("radio-label");
      element.addClass("light");
      parent.append(element);
    }

    var selected;
    if (response.answer) {
      selected = parseInt(response.answer) - 1;
    }
    var steps = input.likertSteps;
    for(var i = 0; i < steps; i++) {
      var rawElement = document.createElement("input");
      var element = $(rawElement);
//      element.addClass("light"); // radio-input
      element.addClass("yui3-cssreset");
      element.attr("type","radio");
      element.attr("name", input.name);
      element.attr("id", input.name + "_" + i);
      if (selected && selected === i) {
        element.attr("checked", true);
      } 
      parent.append(element);
      var label = $("<label>");
      label.attr("for", input.name + "_" + i);
      label.attr("value", "");
      label.addClass("light");      
      parent.append(label);
      
      element.change(function(index) {
        return function() { 
          response.answer = index + 1;
          conditionalListener.inputChanged();
        };        
      }(i));        
    }
    
    var right = input.rightSideLabel || "";
    if (right) {
      var element = $(document.createElement("span"));
      element.text(right);
//      element.addClass("radio-label");
      element.addClass("light");
      parent.append(element);
    }

    return element;
  };

  renderList = function(input, response, root, conditionalListener) {
    
    
    var steps = input.listChoices;
    if (input.multiselect) {
      var selected;
      if (response.answer) {
        var listAnswer = parseInt(response.answer) - 1;
        selected = listAnswer.split(",");        
      } else {
        selected = [];
      }
      root.addClass("left-align");
      
      var parent = $('<div>');
      root.append(parent);
      parent.attr("name", input.name);
      
      for (var step = 0; step < steps.length; step++) {
        var currentStep = steps[step];
        var p = $('<div>'); // didn't work
        p.css("line-height", "1"); // didnt work        
        p.addClass("input-field col s12 left-align");
        parent.append(p);
        
        var lbl = $("<label>");
        lbl.addClass("grey-text text-darken-2");
        lbl.css("line-height", "1");
        lbl.attr("for", input.name + "_" + step)
        lbl.text(currentStep);
        
        
        var chk = $("<input>");
        chk.css("line-height", "1");
        chk.attr("id", input.name + "_" + step);
        chk.attr("type", "checkbox");
//        chk.attr("value", step);
        chk.attr("checked", (selected.indexOf(step + 1) != -1));
        chk.addClass("filled-in ");
        p.append(chk);
        p.append(lbl);
        //p.append($("<br>"));
        
        chk.change(function() { 
            var values = [];
            var i = 0;
            var list = $('input:checkbox[id^="' + input.name  + '_"]').each(function() {
              if (this.checked) {
                values.push(i + 1);
              }
              i++;
            });
            
            var valueString = values.join(",");
            response.answer = valueString;
//            alert("Values: " + valueString);
            conditionalListener.inputChanged();        
        });
        
                
      }
      
    } else {
      var selected;
      if (response.answer) {
        selected = parseInt(response.answer) - 1;
      }
      $('select').material_select('destroy');
      var s = $('<select id="' + input.name + '" name="' + input.name + '" />');
      s.attr("name", input.name);
      var startIndex = 0;
          $("<option />", {value: 0, text: "Please select"}).appendTo(s);
          startIndex = 1;
      for(var i = 0; i < steps.length; i++) {
        $("<option />", {value: (i + 1), text: steps[i]}).appendTo(s);
      }
      s.addClass("light");
      
      root.append(s)
      
      var label = $("<label>");
      label.attr("for", input.name);
      label.attr("text", "");
      label.addClass("light");      
      root.append(label);
   
      $('select').material_select();
      s.css("display", "block");
      
      s.change(function() {
        var val = this.selectedIndex; 
        response.answer = val;
        conditionalListener.inputChanged();
      });
  
      return s;
    }
  };

  renderPhotoButton = function(input, response, parent, conditionalListener) {
    var rawElement = document.createElement("input");
    var element = $(rawElement);
    element.addClass("light");
    element.attr("type", "button");
    element.attr("name", input.name);
    // TODO i18n
    element.attr("value", "Add Picture");
    element.css({"margin-right" : "1em"});
    
    
    var imgElement = $("<img/>", { src : "file:///android_asset/paco_sil.png"});    
    imgElement.attr("height", "50");
    imgElement.css({"border" : "1px solid #021a40"});
    element.click(function() {
      function cameraCallback(cameraData) {
        if (cameraData && cameraData.length > 0) {          
          imgElement.attr("src", "data:image/png;base64," + cameraData);          
          response.answer = cameraData;
          conditionalListener.inputChanged();        
        }
      };
      paco.photoService.launch(cameraCallback);
      
    });
    
    element.css("vertical-align", "bottom").css("margin-bottom", "0");
    imgElement.css("vertical-align", "bottom");
    parent.append(element);
    parent.append(imgElement);

    return element;
  };

  renderInput = function(input, response, conditionalListener) {
    var panelDiv = $("<div>");
    
//    var divPromptRow = $("<div>");
//    divPromptRow.addClass("row");
//    divPromptRow.css({"margin-bottom" : "0px"});
//    panelDiv.add(divPromptRow);
//    
//    var divPrompt = $("<div>");
//    divPrompt.addClass("input-field col s12 left-align");
//    
//    divPrompt.append(renderPrompt(input));
//    divPromptRow.append(divPrompt);
//    
    panelDiv.append(renderPrompt(input));
    
    
    
//    div.append(renderBreak());
    var divInputRow = $("<div>");
    divInputRow.addClass("row");
    divInputRow.css({"margin-bottom" : "0px"});
    
    var divInput = $("<div>").addClass("input-field col s12");
    divInputRow.append(divInput);
    panelDiv.append(divInputRow);
    if (input.responseType === "open text") {
      renderTextShort(input, response, divInput, conditionalListener);
    } else if (input.responseType === "likert") {
      renderScale(input, response, divInput, conditionalListener);
    } else if (input.responseType === "number") {
      renderNumber(input, response, divInput, conditionalListener);
    } else if (input.responseType === "list") {
      renderList(input, response, divInput, conditionalListener);
    } else if (input.responseType === "photo") {
      renderPhotoButton(input, response, divInput, conditionalListener);
    }
    conditionalListener.addInput(input, response, panelDiv);

    panelDiv.append(renderBreak());
    return { "element" : panelDiv, "response" : response };
  };

  renderInputs = function(experimentGroup, responseEvent, conditionalListener) {
    var inputHtmls = [];
    for (var i in  experimentGroup.inputs) {
      var input = experimentGroup.inputs[i];
      var response = responseEvent.responses[i]; // TODO kind of gross, but these are pair-wise matches with inputs.
      inputHtmls.push(renderInput(input, response, conditionalListener))
    }
    return inputHtmls;
  };
  
  renderBreak = function() {
    return $("<br>");
  };

  renderExperimentTitle = function(experiment) {
    var element = $(document.createElement("div"));
    element.text(experiment.title);
    element.addClass("title");
    return element;
  };

  renderSaveButton = function() {
    var saveButton = $(document.createElement("input"));
    saveButton.attr("type", "submit");
    // TODO i18n
    saveButton.attr("value", "Save Response");
    saveButton.css({"margin-top":".5em", "margin-bottom" : "0.5em", "width" : "90%"});
    return saveButton;
  };
  
  renderDoneButton = function(experiment) {
    var doneButton = document.createElement("input");
    doneButton.type="submit";
    // TODO i18n
    doneButton.value = "Done";
    return doneButton;
  };

  removeErrors = function(outputs) {
    for (var i in outputs) {
      var name = outputs[i].name
      var elem = $("[name=" + name + "]")
      //elem.removeClass("outlineElement");
      elem.css({"outline": ""});
    }

    // var str = JSON.stringify(json);
    // $("p").text("SUCCESS. Data" + str);
  };

  showErrors = function(json) {
    var errors = [];
    for (var i in json) {
      var event = json[i];
      if (event.error) {
        errors.push(event.error);
      }
    }
    alert("Error:\n\n" + errors.join("\n"));
  }
  
  updateErrors = function(json) {    
    //alert("json = " + JSON.stringify(json, null, 2));
    for (var i in json) {      
      var name = json[i].name
      var elem = $("[name=" + name + "]"); 
      
      if (!json[i].succeeded) {
      //elem.addClass("outlineElement");
        elem.css({"outline": "2px solid #F00"});
      } else {
        elem.css({"outline": ""});
      }
    }
    showErrors(json);
  };

  registerValidationErrorMarkingCallback = function(experimentGroup, responseEvent, inputHtmls, saveButton, mainValidationCallback) {

    var validResponse = function(event) {
      removeErrors(event.responses);      
      if (mainValidationCallback) {
        mainValidationCallback(event);
        saveButton.show();        
      }        
    };

    var invalidResponse = function(all) {
      updateErrors(all);
      saveButton.show();
    };

    var errorMarkingCallback = {
      "invalid" : invalidResponse,
      "valid" : validResponse
    };

    saveButton.off("click");
    saveButton.click(function(event) { 
      saveButton.hide(); 
      paco.validate(experimentGroup, responseEvent, inputHtmls, errorMarkingCallback);
      event.preventDefault();
    });
  };

  registerDoneButtonCallback = function(doneButton) {
    doneButton.click(function() { 
      if (window.executor) {
        window.executor.done();
      } else {
    	  // TODO i18n 
        alert("All Done!");
      }
    });
  };

  renderForm = function(experiment, experimentGroup, responseEvent, 
      rootPanel, saveCallback, conditionalListener) {
    rootPanel.html("");
    var inputHtmls = renderInputs(experimentGroup, responseEvent, conditionalListener);
    for (var i in inputHtmls) {
      var ihtml = inputHtmls[i];
      rootPanel.append(ihtml.element);      
    }

    var saveButton = $("#submit-button");
    registerValidationErrorMarkingCallback(experimentGroup, responseEvent, inputHtmls, saveButton, saveCallback);
    // run this once to hide the hidden ones
    conditionalListener.inputChanged();
  };

  renderCustomExperimentForm = function(experiment, experimentGroup, responseEvent, rootPanel, saveCallback, conditionalListener) {    
    var additionsDivId = $(document.createElement("div"));

    var customRenderingCode = experimentGroup.customRenderingCode;
    var scriptElement = document.createElement("script");
    scriptElement.type = 'text/javascript';
    
    var strippedCode = scriptBody(customRenderingCode);
    scriptElement.text = strippedCode;    
    additionsDivId.append(scriptElement);

    var newSpan = $(document.createElement('span'));
    
    var html = htmlBody(customRenderingCode);
    newSpan.html(html);    
    additionsDivId.append(newSpan);

    // var doneButton = renderDoneButton();
    // additionsDivId.append(doneButton);
    // registerDoneButtonCallback(experiment, doneButton);

    rootPanel.append(additionsDivId);
  };

  loadCustomExperiment = function(experimentGroup, rootPanel) {    
    var additionsDivId = $(document.createElement("div"));
    
    var customRenderingCode = experimentGroup.customRenderingCode;
    var newHtml = $(document.createElement('div'));
    newHtml.html(customRenderingCode);
    additionsDivId.append(newHtml)
    rootPanel.append(additionsDivId);
  };

  
  renderOutput = function(output) {
    var element = renderPlainText(output.prompt + ": " + output.answer);
    element.addClass("output");
    element.append(document.createElement("br"));
    return element;
  };

  renderOutputs = function(outputs) {
    var outputHtmls = [];
    for (var i in  outputs) {
      var output = outputs[i];
      outputHtmls.push(renderOutput(output));
    }
    return outputHtmls;
  };

  renderPlainText = function(value)  {
    var element = $(document.createElement("span"));
    element.text(value);
    return element;
  };

  renderDefaultFeedback = function(experimentGroup, db, element) {
    var subElement = $(document.createElement("div"));
    // TODO i18n
    subElement.text("Thank you for participating!");
    subElement.addClass("title");
    element.append(subElement);

    var lastEvent = db.getLastEvent();
    // TODO i18n
    element.append(renderPlainText("Scheduled Time: " + lastEvent.scheduledTime));
    element.append(renderBreak());
    element.append(renderPlainText("Response Time: " + lastEvent.responseTime));
    element.append(renderBreak());
    var outputHtmls = renderOutputs(lastEvent.responses);
    for (var i in outputHtmls) {
      var ohtml = outputHtmls[i];
      element.append(ohtml);
      element.append(renderBreak());
    }
    // render done button that listens and calls some native function wrapper that exits
    var doneButton = renderDoneButton();
    element.append(doneButton);
    registerDoneButtonCallback(doneButton);
  };


  function scriptBody(customFeedback) {
    var scriptStartIndex = customFeedback.indexOf("<script>");
    var scriptEndIndex = customFeedback.indexOf("</"+"script>");
    if (scriptStartIndex != -1 && scriptEndIndex != -1) {
      return customFeedback.substring(scriptStartIndex + 8, scriptEndIndex);
    } 
    return "";
  }
  
  function htmlBody(customFeedback) {
    var scriptEndIndex = customFeedback.indexOf("</"+"script>");
    if (scriptEndIndex != -1) {
      return customFeedback.substring(scriptEndIndex+9);
    } else {
      return customFeedback;
    }
  }
  

  renderCustomFeedback = function(experimentGroup, db, element) {
    var additionsDivId = $(document.createElement("div"));

    var feedbackText = experimentGroup.feedback.text;
    var scriptElement = document.createElement("script");
    scriptElement.type = 'text/javascript';
    scriptElement.text = scriptBody(feedbackText); 
    additionsDivId.append(scriptElement);
//    additionsDivId.append($("<script />", { html: scriptBody(feedbackText)}));
    
    var newSpan = $(document.createElement('span'));
    newSpan.html(htmlBody(feedbackText));
    additionsDivId.append(newSpan);

    var doneButton = renderDoneButton();
    additionsDivId.append(doneButton);
    registerDoneButtonCallback(doneButton);

    element.append(additionsDivId);

  };

  renderFeedback = function(experimentGroup, db, element) {
    if (!experimentGroup.feedback) {
      renderDefaultFeedback(experimentGroup, db, element);
    } else {
      renderCustomFeedback(experimentGroup, db, element);
    }
  };
  
  var mapInputs = function(inputs) {
    var inputsByName = [];
    for (var i = 0; i< inputs.length; i++) {
      var input = inputs[i];
      inputsByName[input.name] = input;
    }
    return inputsByName;
  };
  
  var renderNiceOutputs = function(responses, inputsByName, element) {
    var responsesHtml = "";
    for ( var i = 0; i < responses.length; i++) {
      var response = responses[i];
      if (response.answer == null || response.answer.length == 0) {
        continue;
        //response.answer = "";
      }
      var input = inputsByName[response.name];
      if (!input) {
        continue;
      }
      
      responsesHtml += "<div class=\"row\" style=\"margin-bottom: 8px;\">";
      responsesHtml += "<h6 class=\"left indigo-text\">";      
      responsesHtml += input.text;      
      responsesHtml += "</h6><br>";
      responsesHtml += "<p class=\"black-text\">";
      responsesHtml += "&nbsp;&nbsp;&nbsp;"
      if (input.responseType === "photo" && response["answer"].length > 0) {
        responsesHtml += "<img src='data:image/jpg;base64," + response["answer"] + "' width=150>";
      } else if (input.responseType === "location") {
        responsesHtml += response["answer"];
        // TODO i18n
        responsesHtml += "&nbsp;&nbsp;&nbsp;<a href='file:///android_asset/map.html?inputId=" + response["name"] + "'>Maps</a>";
      } else if (input.responseType === "list") {
        
        var answer = response["answer"];
        var listChoiceName = "";
        if (answer) {
          if (!input.multiselect) {
            answer = parseInt(answer);
            var index = answer;
            listChoiceName = input.listChoices[index - 1];
          } else {
            var indices = answer.split(",");
            for (var j = 0; j < indices.length; j++) {
              if (j > 0) {
                listChoiceName += ", ";
              }
              var index = indices[j]; 
              index -= 1;
              if (index < 0) {
                index = 0;
              }
              listChoiceName += input.listChoices[index];
            }
          } 
          
        }       
        responsesHtml += listChoiceName;
      } else {
        var escapedResponse = escapeHtml(response["answer"]);
        //confirm("escaped response = |" + escapedResponse +"|");
        responsesHtml += escapedResponse;
      }
      responsesHtml += "</p></div>";
    }

    element.html(responsesHtml);
  };

  // TODO i18n
  var days = ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
  
  renderDailyPingResponsesPanel = function(referredExperimentGroupInputs, 
      currentEvent, currentPingIndex, pingCount) {
    var responseDateTime = new Date(currentEvent.responseTime);
    $("#date-dow-banner").html(days[responseDateTime.getDay()]);
    $("#date-mdy-field").html(responseDateTime.toLocaleDateString());
    $("#response-n-of").html(currentPingIndex + 1);
    $("#response-count").html(pingCount);
    $("#responsetime-field").html(responseDateTime.toLocaleTimeString());
    
    var dailyResponsesDiv = $("#daily-responses");
    renderNiceOutputs(currentEvent.responses, 
                                        mapInputs(referredExperimentGroupInputs),
                                        dailyResponsesDiv);
  };


  var obj = {};
  obj.renderPrompt = renderPrompt;
  obj.renderTextShort = renderTextShort;
  obj.renderScale = renderScale;
  obj.renderPhotoButton = renderPhotoButton;
  obj.renderPlainText = renderPlainText;
  obj.renderList = renderList;
  obj.renderInput = renderInput;
  obj.renderInputs = renderInputs;
  obj.renderBreak = renderBreak;
  obj.renderForm = renderForm;
  obj.renderCustomExperimentForm = renderCustomExperimentForm;
  obj.loadCustomExperiment = loadCustomExperiment;
  obj.renderSaveButton = renderSaveButton;
  obj.registerValidationErrorMarkingCallback = registerValidationErrorMarkingCallback;
  obj.renderFeedback = renderFeedback;
  obj.renderDailyPingResponsesPanel = renderDailyPingResponsesPanel;
  return obj;

})();




paco.executeEod = (function() {

  return function(experiment, experimentGroup, referredExperimentGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, form_root) {
    

    
    var mapInputs = function(inputs) {
      var inputsByName = [];
      for (var i = 0; i< inputs.length; i++) {
        var input = inputs[i];
        inputsByName[input.name] = input;
      }
      return inputsByName;
    };
    
    var getEodResponseTimeFrom = function(event) {
      return getResponseForItem(event.responses, "eodResponseTime");
    };
    
    var getResponseTimeFrom = function(event) {
      return event.responseTime;
    };
    
    var getResponseForItem = function(responses, item) {
      return paco.db.getResponseForItem(responses, item);
    }
    
    function isActive(eventDate, now, triggerTime, timeout) { 
      var nt = now.getTime(); 
			var eventDateMidnight = new Date(eventDate.getFullYear(), 
                                           eventDate.getMonth(), 
                                           eventDate.getDate()).getTime();
      var ft = eventDateMidnight + triggerTime.fixedTimeMillisFromMidnight + timeout;
      //var active = nt <= ft;
      //alert("nt = " + nt + ", edt = " + edt + ", ft = " + ft + ", active = " + active );
      
      return eventDate.getDate() == now.getDate() || 
             eventDate.getDate() == (now.getDate() - 1)  && 
                 now.getTime() <= ft; 
    };
    
    var getActiveEventsWithoutEod = function(referredExperimentGroup, experimentGroup, db) {      
      var dailyEvents = []; // responseTime, event
      var eodEvents = {}; // eodResponseTime, event
      var trigger = experimentGroup.actionTriggers[0];
      var triggerTime = trigger.schedules[0].signalTimes[0];
      var timeout = trigger.actions[0].timeout * 60 * 1000; // in millis
      
      var now = new Date();
      
      var allEvents = db.getAllEvents();
      for (var i = 0; i < allEvents.length; i++) {
        var event = allEvents[i];
        if (!event.responseTime) {
          continue;
        } 
        var eventDateTime = new Date(event.responseTime);
        if (!isActive(eventDateTime, now, triggerTime, timeout)) {
          // maybe build the list of already expired events to show as well.
          continue;
        }
        var eventGroupName = event.experimentGroupName;
        if (!eventGroupName) {
          continue;
        }
        if (eventGroupName === experimentGroup.name) { // eod
          var eventEodResponseTimeObj = getEodResponseTimeFrom(event);
          if (eventEodResponseTimeObj) {
            eodEvents[eventEodResponseTimeObj + ""] = event;
          }
        } else if (eventGroupName === referredExperimentGroup.name) { // daily
          var eventResponseTime = getResponseTimeFrom(event);
          dailyEvents.push(event);
        }
      }
    
      var unfinished = [];
      var finishedTimes = [];
      $.each(dailyEvents, function(i, event) {
        var eventResponseTime = getResponseTimeFrom(event);
        if (eventResponseTime) {
          var alreadyAnswered = eodEvents[eventResponseTime];
          if (!alreadyAnswered) {
            unfinished.push(event); // this reverses the order to responseTime ascending.
          } else {
            finishedTimes.push(eventResponseTime);
          }
        }
      });

      return { "unfinished" : dailyEvents, "finishedTimes" : finishedTimes};
    };
    

    var dailyEvents = getActiveEventsWithoutEod(referredExperimentGroup, experimentGroup, paco.db);
    var unfinishedDailyEvents = dailyEvents.unfinished;
    var submitted = dailyEvents.finishedTimes;
    //var inputsByName = mapInputs(referredExperimentGroup.inputs);
    var eodInputsByName = mapInputs(experimentGroup.inputs);
    var pingCount = unfinishedDailyEvents.length;
    var currentPingIndex = 0;
    var unsavedEdits = false;
    

   
    var dbSaveOutcomeCallback = function(status) {
      if (status["status"] === "success") {
        var justSaved = unfinishedDailyEvents[currentPingIndex]; 
        submitted.push(justSaved.responseTime);
        if (submitted.length == unfinishedDailyEvents.length) {
          // TODO render a congratulations that they have completed k of n days or all done.
          paco.executor.done();
        } else {
//          history.pushState({ "id" : currentPingIndex}, '', '');
          currentPingIndex++;
          if (currentPingIndex == pingCount) {
            currentPingIndex = currentPingIndex - 2;
          }
          $('html, body').animate({ scrollTop: 0 }, 0);          
          renderEvent();
        }
      } else {
    	  // TODO i18n
        alert("Could not store data. You might try again. Or contact the researcher running the study. Error: " + status["error"]);
      }   
    };

    var saveDataCallback = function(event) {
      var responses = event.responses;
      var atLeastOneAnswer = false;
      for (var i = 0; i < event.responses.length; i++) {
        var name = event.responses[i].name;
        if (!eodInputsByName[name]) {
          continue;
        }
        var answer = event.responses[i].answer; 
        if (answer && answer.length > 0) {
          atLeastOneAnswer = true;
        } 
      }
      if (atLeastOneAnswer) {
        paco.db.saveEvent(event, dbSaveOutcomeCallback);
      } else {
    	  //TODO i18n
        alert("You cannot submit an empty response.");
      } 
    };
    
    var scheduledTime;
    if (window.env) {
      scheduledTime = window.env.getValue("scheduledTime");
    }
    
    var renderEvent = function() {
      unsavedEdits = false;
      var currentEvent = unfinishedDailyEvents[currentPingIndex];
      
      form_root.append(paco.renderer.renderDailyPingResponsesPanel(referredExperimentGroup.inputs, 
          currentEvent, currentPingIndex, pingCount));
      if (submitted.indexOf(currentEvent.responseTime) != -1) {
    	  // TODO i18n
        $("#eod-questions").html("<b>Already replied</b>");
      } else {
       // prepare response collector
        
        var responseEvent = paco.createResponseEventForExperimentGroup(experiment, experimentGroup, 
            actionTriggerId, actionId, actionTriggerSpecId, scheduledTime);
        paco.addEodResponses(responseEvent.responses, referredExperimentGroup.name, currentEvent);
                
        
        var conditionalListener = (function() {
          var inputs = [];
          var obj = {};

          obj.addInput = function(input, responseHolder, visualElement) {
            inputs.push({
              "input" : input,
              "responseHolder" : responseHolder,
              "viz" : visualElement
            });
          };

          obj.inputChanged = function() {
            var values = {};
            unsavedEdits = false;

            for ( var inputIdx in inputs) {
              var inputPair = inputs[inputIdx];
              var input = inputPair.input;
              var value = inputPair.responseHolder.answer;
              values[input.name] = value;
              
              if (value && value.length > 0) {
                unsavedEdits = true;
              }
            }

            for ( var inputIdx in inputs) {
              var inputPair = inputs[inputIdx];
              var input = inputPair.input;
              
              if (input.conditional) {
                var validity = parser.parse(input.conditionExpression, values);
                if (!validity) {
                  //inputPair.viz[0].style.display = "none";
                  inputPair.viz.addClass("hide");
                } else {
                  //inputPair.viz[0].style.display = "";
                  inputPair.viz.removeClass("hide");
                }
              }
            }

          };

          return obj;
        })();

        paco.renderer.renderForm(experiment, experimentGroup, responseEvent, 
            $("#eod-questions"), saveDataCallback, conditionalListener);
      }
      if (currentPingIndex == 0) {
        $("#ping-left-nav").hide();
      } else {      
        $("#ping-left-nav").show();
        $("#ping-left-nav").off("click");
        $("#ping-left-nav").on("click", function() {
          if (unsavedEdits) {
        	  // TODO i18n
            var r = confirm("You have not submitted the work on this page. Are you sure to leave this page?");
            if (r == true) {
              currentPingIndex -= 1;
              renderEvent();
            }
          } else {
            currentPingIndex -= 1;
            renderEvent();
          }
        });

      }
      if (currentPingIndex == (pingCount - 1)) {
        $("#ping-right-nav").hide();
      } else {
        $("#ping-right-nav").show();
        $("#ping-right-nav").off("click");
        $("#ping-right-nav").on("click", function() {
          if (unsavedEdits) {
        	  // TODO i18n
            var r = confirm("You have not submitted the work on this page. Are you sure to leave this page?");
            if (r == true) {
              currentPingIndex += 1;
              renderEvent();
            }
          } else {
            currentPingIndex += 1;
            renderEvent();
          }          
        });

      }
      
    };
    
    function exit() {
      if (unsavedEdits || submitted.length < unfinishedDailyEvents.length) {
    	  // TODO i18n
        var answer = confirm("You have not submitted responses for all daily events. Are you sure you want to leave?");
        if (answer) {
            paco.executor.done();
        }
      } else {
        paco.executor.done();
      }
    };
      
    $("#exit-button").click(exit);
    
    if (unfinishedDailyEvents.length > 0 && submitted.length < unfinishedDailyEvents.length) {      
      renderEvent();      
    } else {
    	// TODO i18n
      $("#response-banner").html(paco.renderer.renderPlainText("No active daily responses"));
      $("#submit-button").prop("disabled", true);
    }
  };
})();


function leftPing(currentIndex) {
}

function rightPing(currentIndex) {
}


function runEodExperiment() {
  var form_root = $(document.body);
  
  var experiment = paco.experimentService.getExperiment();
  //var experimentGroup = paco.experimentService.getExperimentGroup();
  var experimentGroupName = window.env.getValue("experimentGroupName");
  
  var experimentGroup = null;
  for (var j = 0; j < experiment.groups.length; j++) {
    var grp = experiment.groups[j];
    if (grp.name === experimentGroupName) {
      experimentGroup = grp;
      break;
    }
  }
  if (experimentGroup == null) {
    //alert("Did not find eod group");
    experimentGroup = paco.experimentService.getExperimentGroup();
  }
  
  var referredGroupName = experimentGroup.endOfDayReferredGroupName;
  var referredGroup = null;
  for (var i = 0; i < experiment.groups.length; i++) {
    var grp = experiment.groups[i];
    if (grp.name === referredGroupName) {
      referredGroup = grp;
      break;
    }
  }
  if (referredGroup == null) {
    //alert("did not find referred group");
    referredGroup = paco.experimentService.getEndOfDayReferredExperimentGroup();
  }
  var actionTriggerId = window.env.getValue("actionTriggerId");
  var actionTriggerSpecId = window.env.getValue("actionTriggerSpecId");
  var actionId = window.env.getValue("actionId");
  
  paco.executeEod(experiment, experimentGroup, referredGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, form_root);
};

