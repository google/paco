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
      return { "succeeded" : false , "error" : "Response required for " + input.name, "name" : input.name};    
    } else if (!validValueForResponseType(response)) {
      return { "succeeded" : false , "error" : "Response required for " + name, "name" : name};    
    } else {
      return { "succeeded" : true };
    }
  };
  
  
  obj.validate = function(experimentGroup, responseEvent, inputHtmls, errorMarkingCallback) {
    var errors = [];
    for (var i in experimentGroup.inputs) {
      var input = experimentGroup.inputs[i];
      var response = responseEvent.responses[i];
      var visualElement = inputHtmls[i];
      var validity = valid(input, visualElement, response);
      if (!validity.succeeded) {
        errors.push(validity);
      } 
    }
    if (errors.length > 0) {
      errorMarkingCallback.invalid(errors);
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
      }

      function getLastEvent() {
        return JSON.parse(window.db.getLastEvent());
      };

      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent
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
        return JSON.parse(window.experimentLoader.getgetEndOfDayReferredExperimentGroup());
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
	          alert("No notification support"); 
	        },
	        removeNotification : function(message) { 
		          alert("No notification support"); 
		    }
	      };
	    }

	    return {
	      createNotification : function(message) {
	        window.notificationService.createNotification(message);
	      }, 
	      removeNotification : function() {
	    	  window.notificationService.removeNotification();
	      }
	    };
	  })();


  return obj;
})();

paco.renderer = (function() {

  renderPrompt = function(input) {
    var element = $(document.createElement("span"));
    element.text(input.text);
    element.addClass("prompt");
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
    
    conditionalListener.addInput(input, response, parent);
    return element;
  };

  renderShortTextExperiment = function(input, response, conditionalListener, parent) {
    return renderElement(input, response, parent, shortTextVisualRender, conditionalListener);
  };

  renderTextShort = function(input, response, parent, conditionalListener) {
    var rawElement = document.createElement("input");
    var element = $(rawElement);
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


    conditionalListener.addInput(input, response, parent);
    
    return element;
  };

  renderNumber = function(input, response, parent, conditionalListener) {
    var rawElement = document.createElement("input");
    var element = $(rawElement);

    element.attr("type", "text");
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
        alert("bad value: " + e);            
      }
      conditionalListener.inputChanged();
    });
    parent.append(element);
    
    conditionalListener.addInput(input, response, parent);

    return element;
  };


  renderScale = function(input, response, parent, conditionalListener) {
    var left = input.leftSideLabel || "";
    if (left) {
      var element = $(document.createElement("span"));
      element.html(left);
      element.addClass("radioLabel");
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
      element.attr("type","radio");
      element.attr("name", input.name);
      if (selected && selected === i) {
        element.attr("checked", true);
      } 
      parent.append(element);
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
      element.addClass("radioLabel");
      parent.append(element);
    }

    conditionalListener.addInput(input, response, parent);

    return element;
  };

  renderList = function(input, response, parent, conditionalListener) {
    var selected;
    if (response.answer) {
      selected = parseInt(response.answer) - 1;
    }
    var steps = input.listChoices;


    var s = $('<select name="' + input.name + '" ' + (input.multiselect ? 'multiple' : '') + '/>');
    var startIndex = 0;
    if (!input.multiselect) {
      $("<option />", {value: 0, text: "Please select"}).appendTo(s);
      startIndex = 1;
    }
    for(var i = 0; i < steps.length; i++) {
      $("<option />", {value: (i + 1), text: steps[i]}).appendTo(s);
    }
    s.change(function() {
      if (!input.multiselect) {
        var val = this.selectedIndex; 
        response.answer = val;
      } else {
        var values = [];
        var list = $("select[name=" + input.name + "]");
        var listOptions = list.val();
        for( x = 0; x < listOptions.length; x++) {
          values.push(parseInt(x) + 1);
        }
        var valueString = values.join(",");
        response.answer = valueString;
      }
      conditionalListener.inputChanged();
    });
    parent.append(s)

    conditionalListener.addInput(input, response, parent);

    return s;
  };

  renderPhotoButton = function(input, response, parent, conditionalListener) {
    var rawElement = document.createElement("input");
    var element = $(rawElement);

    element.attr("type", "button");
    element.attr("name", input.name);
    element.attr("value", "Click");
    
    
    var imgElement = $("<img/>", { src : "file:///android_asset/paco_sil.png"});    
    imgElement.attr("height", "100");
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
    
    
    conditionalListener.addInput(input, response, parent);

    return element;
  };

  renderInput = function(input, response, conditionalListener) {
    var rawElement = document.createElement("div");    
    var div = $(rawElement);
    div.css({"margin-top":".5em", "margin-bottom" : "0.5em"});
    div.append(renderPrompt(input));
    div.append(renderBreak());
    
    if (input.responseType === "open text") {
      renderTextShort(input, response, div, conditionalListener);
    } else if (input.responseType === "likert") {
      renderScale(input, response, div, conditionalListener);
    } else if (input.responseType === "number") {
      renderNumber(input, response, div, conditionalListener);
    } else if (input.responseType === "list") {
      renderList(input, response, div, conditionalListener);
    } else if (input.responseType === "photo") {
      renderPhotoButton(input, response, div, conditionalListener);
    } 
    div.append(renderBreak());
    return { "element" : div, "response" : response };
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
    var br = $(document.createElement("br"));
    return br;
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
    saveButton.attr("value", "Save Response");
    saveButton.css({"margin-top":".5em", "margin-bottom" : "0.5em"});
    return saveButton;
  };

  renderDoneButton = function(experiment) {
    var doneButton = document.createElement("input");
    doneButton.type="submit";
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

  addErrors = function(json) {
    for (var i in json) {
      var name = json[i].name
      $("input[name=" + name + "]").addClass("outlineElement");
    }
  };

  registerValidationErrorMarkingCallback = function(experimentGroup, responseEvent, inputHtmls, saveButton, mainValidationCallback) {

    var validResponse = function(event) {
      removeErrors(event.responses);      
      if (mainValidationCallback) {
        mainValidationCallback(event);
      }        
    };

    var invalidResponse = function(event) {
      addErrors(event);
    };

    var errorMarkingCallback = {
      "invalid" : invalidResponse,
      "valid" : validResponse
    };

    saveButton.click(function() { paco.validate(experimentGroup, responseEvent, inputHtmls, errorMarkingCallback) });
  };

  registerDoneButtonCallback = function(doneButton) {
    doneButton.click(function() { 
      if (window.executor) {
        window.executor.done();
      } else {
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
    subElement.text("Thank you for participating!");
    subElement.addClass("title");
    element.append(subElement);

    var lastEvent = db.getLastEvent();
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
        form_root.html("Feedback");
        paco.renderer.renderFeedback(experiment, experimentGroup, paco.db, form_root);
      } else {
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
  $(document.body).append(form_root);
  var experiment = paco.experimentService.getExperiment();
  var experimentGroup = paco.experimentService.getExperimentGroup();
  
  paco.renderer.loadCustomExperiment(experimentGroup, form_root);
  if (main) {
    main(paco.experiment(), experimentGroup, form_root);
  } else {
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
