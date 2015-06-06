var paco = (function (init) {
  var obj = init || {};
  var environment = obj["environment"] || "test";
  var context = parent;

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
    if ((input.mandatory && inputHtml.element[0].style.display != "none") && (!response.answer || response.answer.length === 0)) {
      return { "succeeded" : false , "error" : "Response mandatory for " + input.name, "name" : input.name};    
    } else if (!validValueForResponseType(response)) {
      return { "succeeded" : false , "error" : "Response mandatory for " + name, "name" : name};    
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
            events = eventsString;
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
        context.db.saveEvent(JSON.stringify(event));
        events.unshift(event);
        return {"status" : "success"};
      };

      function getAllEvents() {
        if (!loaded) {
          events = context.db.getAllEvents();
          loaded = true;
        }
        return events;
      }

      function getLastEvent() {
        return context.db.getLastEvent();
      };

      return {
        saveEvent : saveEvent,
        getAllEvents: getAllEvents,
        getLastEvent : getLastEvent
      };
    };

    var db; 
    if (!context.db) {
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
      getResponseForItem  : getResponseForItem,
      
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
      if (!context.experimentLoader) {
        return getTestExperiment();
      } else {
        return context.experimentLoader.getExperiment();
      }
    };
    
    var getExperimentGroup = function() {
      if (!context.experimentLoader) {
        return null;
      } else {
        return context.experimentLoader.getExperimentGroup();
      }
    };
    
    var getEndOfDayReferredExperimentGroup = function() {
      if (!context.experimentLoader) {
        return null;
      } else {
        return context.experimentLoader.getEndOfDayReferredExperimentGroup();
      }
    };


    var saveExperiment = function(experimentString) {
      if (!context.experimentLoader) {
        return saveTestExperiment();
      } else {
        return context.experimentLoader.saveExperiment(experimentString);
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
    
    if (!context.experimentLoader) {
      return getTestExperiment();
    } else {
      return context.experimentLoader.getExperiment();
    } 
  };

  obj.executor = (function() {
    if (!context.executor) {
      context.executor = { done : function() { alert("done"); } };
    }

    return {
      done : function() {
        context.executor.done();
      }
    };
  })();

  obj.photoService = (function() {
    var callback;

    if (!context.photoService) {
      context.photoService = { 
        launch : function(callback) { 
          alert("No photo support"); 
        } 
      };
    }

    return {
      launch : function(callback2) {
        callback = callback2;
        context.photoService.launch();
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
      if (!context.notificationService) {
        context.notificationService = { 
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
          context.notificationService.createNotification(message);
        }, 
        removeNotification : function() {
          context.notificationService.removeNotification();
        }
      };
    })();


  return obj;
})();

paco.renderer = (function() {

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

  renderList = function(input, response, parent, conditionalListener) {
    
    
    var steps = input.listChoices;
    if (input.multiselect) {
      var selected;
      if (response.answer) {
        var listAnswer = parseInt(response.answer) - 1;
        selected = listAnswer.split(",");        
      } else {
        selected = [];
      }
      parent.addClass("left-align");
      
      for (var step = 0; step < steps.length; step++) {
        var currentStep = steps[step];
        var p = $('<p>');
        p.addClass("input-field col s12 left-align");
        parent.append(p);
        
        var lbl = $("<label>");
        lbl.attr("for", input.name + "_" + step)
        lbl.text(currentStep);
        
        
        var chk = $("<input>");        
        chk.attr("id", input.name + "_" + step);
        chk.attr("type", "checkbox");
//        chk.attr("value", step);
        chk.attr("checked", (selected.indexOf(step) != -1));
        chk.addClass("filled-in ");
        p.append(chk);
        p.append(lbl);
        p.append($("<br>"));
        
        chk.change(function() { 
            var values = [];
            var i = 0;
            var list = $('input:checkbox[id^="' + input.name  + '_"]').each(function() {
              if (this.checked) {
                values.push(i);
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
      var startIndex = 0;
          $("<option />", {value: 0, text: "Please select"}).appendTo(s);
          startIndex = 1;
      for(var i = 0; i < steps.length; i++) {
        $("<option />", {value: (i + 1), text: steps[i]}).appendTo(s);
      }
      s.addClass("light");
      
      parent.append(s)
      
      var label = $("<label>");
      label.attr("for", input.name);
      label.attr("text", "");
      label.addClass("light");      
      parent.append(label);
   
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
        saveButton.removeClass("disabled");
      }        
    };

    var invalidResponse = function(event) {
      addErrors(event);
    };

    var errorMarkingCallback = {
      "invalid" : invalidResponse,
      "valid" : validResponse
    };

    saveButton.off("click");
    saveButton.click(function() { 
      saveButton.addClass("disabled"); 
      paco.validate(experimentGroup, responseEvent, inputHtmls, errorMarkingCallback) 
    });
  };

  registerDoneButtonCallback = function(doneButton) {
    doneButton.click(function() { 
      if (context.executor) {
        context.executor.done();
      } else {
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
      
      responsesHtml += "<div class=\"row\" style=\"margin-bottom: 0px;\">";
      responsesHtml += "<h6 class=\"left\">";
      responsesHtml += input.text;
      responsesHtml += "</h6><br>";
      responsesHtml += "<p class=\"light grey-text\">";
      if (input.responseType === "photo" && response["answer"].length > 0) {
        responsesHtml += "<img src='data:image/jpg;base64," + response["answer"] + "' width=150>";
      } else if (input.responseType === "location") {
        responsesHtml += response["answer"];
        responsesHtml += "&nbsp;&nbsp;&nbsp;<a href='file:///android_asset/map.html?inputId=" + response["name"] + "'>Maps</a>";
      } else if (input.responseType === "list") {
        
        var answer = response["answer"];
        var listChoiceName = answer;
        if (answer) {
          answer = parseInt(answer);
          var index = answer - 1;
          listChoiceName = input.listChoices[index];
        }        
        responsesHtml += listChoiceName;
      } else {
        responsesHtml += response["answer"];
      }
      responsesHtml += "</p></div>";
    }
    element.html(responsesHtml);
  };

  var days = ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
  
  renderDailyPingResponsesPanel = function(referredExperimentGroupInputs, 
      currentEvent, currentPingIndex, pingCount) {
    var responseDateTime = new Date(currentEvent.responseTime);
    $("#date-dow-banner").html(days[responseDateTime.getDay()]);
    $("#date-mdy-field").html(responseDateTime.toLocaleDateString());
    $("#response-n-of").html(currentPingIndex);
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
    var getEodResponseTimeFrom = function(event) {
      return getResponseForItem(event.responses, "eodResponseTime");
    };
    
    var getResponseTimeFrom = function(event) {
      return event.responseTime;
    };
    
    var getResponseForItem = function(responses, item) {
      return paco.db.getResponseForItem(responses, item);
    }
    
    var getActiveEventsWithoutEod = function(referredExperimentGroup, experimentGroup, db) {      
      var dailyEvents = []; // responseTime, event
      var eodEvents = {}; // eodResponseTime, event
      var timeout = experimentGroup.actionTriggers[0].actions[0].timeout * 60 * 1000; // in millis
      var now = new Date().getTime();
      var cutoffDateTimeMs = now - timeout;
      var allEvents = db.getAllEvents();
      for (var i = 0; i < allEvents.length; i++) {
        var event = allEvents[i];
        if (!event.responseTime) {
          continue;
        } 
        if (new Date(event.responseTime).getTime() < cutoffDateTimeMs) {
          break;
        }
        var eventGroupName = event.experimentGroupName;
        if (!eventGroupName) {
          continue;
        }
        if (eventGroupName === experimentGroup.name) {
          var eventEodResponseTimeObj = getEodResponseTimeFrom(event);
          if (eventEodResponseTimeObj) {
            eodEvents[eventEodResponseTimeObj + ""] = event;
          }
        } else if (eventGroupName === referredExperimentGroup.name) {
          var eventResponseTime = getResponseTimeFrom(event);
          var alreadyAnswered = eodEvents[eventResponseTime];
          if (!alreadyAnswered) {
            dailyEvents.push(event);
          }
        }
      }
    
      var result = [];
      $.each(dailyEvents, function(i, event) {
        var eventResponseTime = getResponseTimeFrom(event);
        if (eventResponseTime) {
          var alreadyAnswered = eodEvents[eventResponseTime];
          if (!alreadyAnswered) {
            result.push(event);
          } else {
          }
        }
      });

      return result;
    };
    

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

        for ( var inputIdx in inputs) {
          var inputPair = inputs[inputIdx];
          var input = inputPair.input;
          var value = inputPair.responseHolder.answer;
          values[input.name] = value;
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

    var unfinishedDailyEvents = getActiveEventsWithoutEod(referredExperimentGroup, experimentGroup, paco.db);
    var pingCount = unfinishedDailyEvents.length;
    var currentPingIndex = 1;
    
    var dbSaveOutcomeCallback = function(status) {
      if (status["status"] === "success") {
//        alert("Success in dbSaveOutcomeCallback");
        var currentEvent = unfinishedDailyEvents.pop();
//        alert("dbSave curr = " + JSON.stringify(currentEvent, null, 2));
        if (!currentEvent) {
          paco.executor.done();
        } else {
          currentPingIndex++;          
          renderEvent(currentEvent, currentPingIndex, pingCount);
        }
      } else {
        alert("Could not store data. You might try again. Or contact the researcher running the study. Error: " + status["error"]);
      }   
    };

    var saveDataCallback = function(event) {
//      alert("Saving event: " + JSON.stringify(event, null, 2));
      paco.db.saveEvent(event, dbSaveOutcomeCallback);
    };
    
    var scheduledTime;
    var context = parent;
    if (context.env) {
      scheduledTime = context.env.getValue("scheduledTime");
    }
    
    var renderEvent = function(currentEvent, currentPingIndex, pingCount) {
      var responseEvent = paco.createResponseEventForExperimentGroup(experiment, experimentGroup, 
          actionTriggerId, actionId, actionTriggerSpecId, scheduledTime);
      paco.addEodResponses(responseEvent.responses, referredExperimentGroup.name, currentEvent);
      
      form_root.append(paco.renderer.renderDailyPingResponsesPanel(referredExperimentGroup.inputs, 
          currentEvent, currentPingIndex, pingCount));
      paco.renderer.renderForm(experiment, experimentGroup, responseEvent, 
          $("#eod-questions"), saveDataCallback, conditionalListener);
    }
        
    if (unfinishedDailyEvents.length > 0) {      
      var currentEvent = unfinishedDailyEvents.pop();
//      alert("main curr = " + JSON.stringify(currentEvent, null, 2));
      renderEvent(currentEvent, currentPingIndex, pingCount);
    } else {
      $("#response-banner").html(paco.renderer.renderPlainText("No active daily responses"));
      $("#submit-button").prop("disabled", true);
    }

    
    
    
  };
})();
    
function runEodExperiment() {
  var form_root = $(document.body);
  
  var experiment = paco.experimentService.getExperiment();
  var experimentGroup = paco.experimentService.getExperimentGroup();
  var referredGroup = paco.experimentService.getEndOfDayReferredExperimentGroup();
  var actionTriggerId = null;//context.env.getValue("actionTriggerId");
  var actionTriggerSpecId = null;//context.env.getValue("actionTriggerSpecId");
  var actionId = null;//context.env.getValue("actionId");
  
  paco.executeEod(experiment, experimentGroup, referredGroup, 
      actionTriggerId, actionId, actionTriggerSpecId, form_root);
};

