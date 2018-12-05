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

  obj.createResponseEventForExperimentWithResponses = function(experiment, experimentGroup, responses, scheduledTime) {
    return  {
      "experimentId" : experiment.id,
      "experimentVersion" : experiment.version,
      "experimentGroupName" : experimentGroup.name,
      "responseTime" : null, 
      "scheduledTime" : scheduledTime,
      "responses" : responses
    };
  };

  obj.createResponseEventForExperiment = function(experiment, experimentGroup, scheduledTime) {
    return obj.createResponseEventForExperimentWithResponses(experiment, experimentGroup, 
        obj.createResponsesForInputs(experimentGroup.inputs), scheduledTime);
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
  
  valid = function(input, inputHtml, response) { 
    if ((input.required && inputHtml.element[0].style.display != "none") && (!response.answer || response.answer.length === 0)) {
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
    var divInputRow = $("<div>");
    divInputRow.addClass("row indigo lighten-5");
    divInputRow.css({"margin-bottom" : "0px", "padding-top" : "0.2em"});
    var element = $(document.createElement("h6"));
    element.addClass("left indigo lighten-5 prompt col s12");    
    element.text(input.text);
    
    divInputRow.append(element);
    return divInputRow;    
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
    element.addClass("title lighten-1 section no-pad-bot");
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
      $("input[name=" + name + "]").removeClass("outlineElement");
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
    for (var i in json) {
      var name = json[i].name
      var elem = $("input[name=" + name + "]");
      if (!json[i].succeeded) {
        elem.addClass("outlineElement");
      } else {
        elem.removeClass("outlineElement");
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

  renderForm = function(experiment, experimentGroup, responseEvent, rootPanel, saveCallback, conditionalListener) {
    rootPanel.append(renderExperimentTitle(experiment));
    var inputHtmls = renderInputs(experimentGroup, responseEvent, conditionalListener);
    for (var i in inputHtmls) {
      var ihtml = inputHtmls[i];
      rootPanel.append(ihtml.element);      
    }
    var saveButton = renderSaveButton();
    rootPanel.append(saveButton);
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
  return obj;

})();



// this is an example of a custom main function
paco.execute = (function() {

  return function(experiment, experimentGroup, form_root) {

  var conditionalListener = (function() {
    var inputs = [];

    var obj = {};
    obj.addInput = function(input, responseHolder, visualElement) {
      inputs.push({ "input" : input, "responseHolder" : responseHolder, "viz" : visualElement});
    };

    obj.inputChanged = function() {
      var values = {};
      
      for (var inputIdx in inputs) {
        var inputPair = inputs[inputIdx];
        var input = inputPair.input;
        var value = inputPair.responseHolder.answer;
        values[input.name] = value; 
      }

      for (var inputIdx in inputs) {
        var inputPair = inputs[inputIdx];
        var input = inputPair.input;
        if (input.conditional) {
          var valid = parser.parse(input.conditionExpression, values);
          if (!valid) {
            inputPair.viz[0].style.display = "none";
          } else {
            inputPair.viz[0].style.display = "";
          }
        }
      }
      
    };

    return obj;
  })();


    var dbSaveOutcomeCallback = function(status) {
      if (status["status"] === "success") {    
    	  paco.executor.done();
      } else {
    	// TODO i18n
        alert("Could not store data. You might try again. Error: " + status["error"]);
      }   
    };

    var saveDataCallback = function(event) {
      paco.db.saveEvent(event, dbSaveOutcomeCallback);
    };
    var scheduledTime;
    if (window.env) {
      scheduledTime = window.env.getValue("scheduledTime");
    }
    var responseEvent = paco.createResponseEventForExperiment(experiment, experimentGroup, scheduledTime);

    if (!experiment.customRendering) {
      paco.renderer.renderForm(experiment, experimentGroup, responseEvent, form_root, saveDataCallback, conditionalListener);    
    } else {
      paco.renderer.renderCustomExperimentForm(experiment, experimentGroup, responseEvent, form_root, saveDataCallback, conditionalListener);
    }
  };

  
})();
    
function runCustomExperiment(s0) {
  var form_root = $(document.createElement("div"));
  form_root.addClass("container");
  form_root.css({"width" : "95%"});
  
//  var form_root = $(document.createElement("div"));
//  form_root.addClass("section");
//  form_base.append(form_root);
//  
  
  $(document.body).append(form_root);
  var experiment = paco.experimentService.getExperiment();
  
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
    experimentGroup = paco.experimentService.getExperimentGroup();
  }
  
  var actionTriggerId = window.env.getValue("actionTriggerId");
  var actionTriggerSpecId = window.env.getValue("actionTriggerSpecId");
  var actionId = window.env.getValue("actionId");
  // loads the custom code into the webview
  paco.renderer.loadCustomExperiment(experimentGroup, form_root);
  if (main) {
    // calls the custom code's main function to render the custom experiment
    main(paco.experiment(), experimentGroup, form_root);
  } else {
	// TODO i18n
    form_root.html("Could not initialize the experiment");
  }
};


var getTestExperiment = function() {
  return {"title":"CustomHtml","description":"","informedConsentForm":"","creator":"bobevans999@gmail.com","fixedDuration":false,"id":995,"questionsChange":false,"modifyDate":"2013/11/05","inputs":[{"id":998,"questionType":"question","text":"What time is it?","mandatory":false,"responseType":"likert","likertSteps":5,"name":"q1","conditional":false,"listChoices":[],"invisibleInput":false},{"id":999,"questionType":"question","text":"How do you feel?","mandatory":false,"responseType":"open text","likertSteps":5,"name":"q2","conditional":false,"listChoices":[],"invisibleInput":false}],"feedback":[{"id":1194,"feedbackType":"display","text":"Thanks for Participating!"}],"published":false,"deleted":false,"webRecommended":false,"version":27,"signalingMechanisms":[{"type":"signalSchedule","id":996,"scheduleType":0,"esmFrequency":3,"esmPeriodInDays":0,"esmStartHour":32400000,"esmEndHour":61200000,"times":[0],"repeatRate":1,"weekDaysScheduled":0,"nthOfMonth":1,"byDayOfMonth":true,"dayOfMonth":1,"esmWeekends":false,"byDayOfWeek":false}],"schedule":{"type":"signalSchedule","id":996,"scheduleType":0,"esmFrequency":3,"esmPeriodInDays":0,"esmStartHour":32400000,"esmEndHour":61200000,"times":[0],"repeatRate":1,"weekDaysScheduled":0,"nthOfMonth":1,"byDayOfMonth":true,"dayOfMonth":1,"esmWeekends":false,"byDayOfWeek":false},"customRendering":true,"customRenderingCode":"<script>\nfunction save() {\n    var experiment = paco.experiment();\n    var inputs = experiment.inputs;\n    var responses = [];\n    for (var i in inputs) {\n        var input = inputs[i];\n        var element = $('input[name='+input.name+']');\n        var value = element.val();\n        var responseObject = paco.createResponseForInput(input);\n        responseObject.answerOrder = value;   \n        responseObject.answer = value;   \n        responses.push(responseObject);\n    }\n    var event = paco.createResponseEventForExperimentWithResponses(experiment, responses);\n    \n    var dbSaveOutcomeCallback = function(status) {\n      if (status[\"status\"] === \"success\") {    \n        alert(\"Saved. \" + JSON.stringify(event));        \n        paco.executor.done();\n        // var form_root = $('#root');\n        // form_root.html(\"\");\n        // paco.renderer.renderFeedback(experiment, paco.db, form_root);        \n      } else {\n        alert(\"Could not store data. You might try again. Error: \" + status[\"error\"]);\n      }   \n    };\n\n    paco.db.saveEvent(event, dbSaveOutcomeCallback);        \n\n}\n\n\n</script>\n<div id=\"root\">\n<h1>Please answer the following</h1>\nQ1 <input type=text name=q1></br>\nQ2 <input type=text name=q2></br>\n<input type=submit name=submit onclick=\"save()\">\n</div>"};
};

var saveTestExperiment = function() {
	return {
		"status" : "1",
		"error_message" : "not supported in test environment"
	};
};
