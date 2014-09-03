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
             "inputId" : input.id, 
             "prompt" : input.text,
             "isMultiselect" : input.isMultiselect,
             "answer" : input.answer, 
             "answerOrder" : input.answerOrder, 
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

  obj.createResponseEventForExperimentWithResponses = function(experiment, responses, scheduledTime) {
    return  {
      "experimentId" : experiment.id,
      "responseTime" : null, 
      "scheduledTime" : scheduledTime,
      "version" : experiment.version,
      "responses" : responses
    };
  };

  obj.createResponseEventForExperiment = function(experiment, scheduledTime) {
    return obj.createResponseEventForExperimentWithResponses(experiment, obj.createResponsesForInputs(experiment.inputs), scheduledTime);
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
      return validNumber(response.answerOrder);
    } else {
      return true;
    }
  };
  
  valid = function(input, response) { 
    if (input.mandatory && (!response.answerOrder || response.answerOrder.length === 0)) {
      return { "succeeded" : false , "error" : "Response mandatory for " + input.name, "name" : input.name};    
    } else if (!validValueForResponseType(response)) {
      return { "succeeded" : false , "error" : "Response mandatory for " + name, "name" : name};    
    } else {
      return { "succeeded" : true };
    }
  };
  
  
  obj.validate = function(experiment, responseEvent, errorMarkingCallback) {
    var errors = [];
    for (var i in experiment.inputs) {
      var input = experiment.inputs[i];
      var response = responseEvent.responses[i];
      var validity = valid(input, response);
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
          return responses[j]["answerOrder"];
        }
      }
      return null;
    };

      
    return {
      saveEvent : function(event, callback) {
        var status = db.saveEvent(event);
        if (callback) {
          callback(status);
        }
      },

      getAllEvents : function() {
        // shallow cloning of the events array
        var newarray = new Array();
        $.each(db.getAllEvents(), function(index, value) { newarray[index] = value });
        return newarray;
      },

      getLastEvent : function() {
        return db.getLastEvent();
      },

      getLastNEvents : function(n) {
        var events = db.getAllEvents();
        return events.slice(0..n);
      },

      getResponsesForEventNTimesAgo : function (nBack) {
        var experimentData = db.getAllEvents();
        if (nBack > experimentData.length) {
          return null; // todo decide whether to throw an exception instead?
        } 
        var event = experimentData[nBack - 1]; 
        return event.responses;
      },

      getAnswerNTimesAgoFor : function (item, nBack) {
        var responses = getResponsesForNTimesAgo(nBack);
        return getResponseForItem(responses, item);
      },

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
        alert("Got it!");
        if (callback) {
          callback(base64BitmapEncoding);
        }
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

    if (response.answerOrder) {
      element.attr("value", response.answerOrder);
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
      response.answerOrder = element.val();
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
    if (response.answerOrder) {
      element.attr("value", response.answerOrder);
    }
    parent.append(element);

    element.change(function() {
      response.answerOrder = element.val();
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
    if (response.answerOrder) {
      element.attr("value", parseInt(response.answerOrder) - 1);
    }
    element.blur(function() {
      try {
        response.answerOrder = element.val();
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
    if (response.answerOrder) {
      selected = parseInt(response.answerOrder) - 1;
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
          response.answerOrder = index + 1;
          response.answer = index + 1 
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
    if (response.answerOrder) {
      selected = parseInt(response.answerOrder) - 1;
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
        response.answerOrder = val;
        response.answer = val;        
      } else {
        var values = [];
        var list = $("select[name=" + input.name + "]");
        var listOptions = list.val();
        for(x=0;x<listOptions.length;x++) {
          values.push(parseInt(x) + 1);
        }
        var valueString = values.join(",");
        response.answerOrder = valueString;
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
    element.click(function() {
      function cameraCallback(cameraData) {
        confirm("Got CameraData: " + (cameraData ? cameraData.substring(0, 40) : "empty"));
        if (cameraData && cameraData.length > 0) {          
          imgElement.attr("src", "data:image/png;base64," + cameraData);
          response.answer = cameraData;
          conditionalListener.inputChanged();        
        }
      };
      paco.photoService.launch(cameraCallback);
      
    });
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

  renderInputs = function(experiment, responseEvent, conditionalListener) {
    var inputHtmls = [];
    for (var i in  experiment.inputs) {
      var input = experiment.inputs[i];
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

  registerValidationErrorMarkingCallback = function(experiment, responseEvent, saveButton, mainValidationCallback) {

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

    saveButton.click(function() { paco.validate(experiment, responseEvent, errorMarkingCallback) });
  };

  registerDoneButtonCallback = function(experiment, doneButton) {
    doneButton.click(function() { 
      if (window.executor) {
        window.executor.done();
      } else {
        alert("All Done!");
      }
    });
  };

  renderForm = function(experiment, responseEvent, rootPanel, saveCallback, conditionalListener) {
    rootPanel.append(renderExperimentTitle(experiment));
    var inputHtmls = renderInputs(experiment, responseEvent, conditionalListener);
    for (var i in inputHtmls) {
      var ihtml = inputHtmls[i];
      rootPanel.append(ihtml.element);      
    }
    var saveButton = renderSaveButton();
    rootPanel.append(saveButton);
    registerValidationErrorMarkingCallback(experiment, responseEvent, saveButton, saveCallback);
    // run this once to hide the hidden ones
    conditionalListener.inputChanged();
  };

  renderCustomExperimentForm = function(experiment, responseEvent, rootPanel, saveCallback, conditionalListener) {    
    var additionsDivId = $(document.createElement("div"));

    var customRenderingCode = experiment.customRenderingCode;
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

  loadCustomExperiment = function(experiment, rootPanel) {    
    var additionsDivId = $(document.createElement("div"));

    var customRenderingCode = experiment.customRenderingCode;
    var scriptElement = document.createElement("script");
    scriptElement.type = 'text/javascript';
    
    var strippedCode = scriptBody(customRenderingCode);
    scriptElement.text = strippedCode;
    
    additionsDivId.append(scriptElement);

    var newSpan = $(document.createElement('span'));
    
    var html = htmlBody(customRenderingCode);
    newSpan.html(html);    
    additionsDivId.append(newSpan);

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

  renderDefaultFeedback = function(experiment, db, element) {
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
    registerDoneButtonCallback(experiment, doneButton);
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
  

  renderCustomFeedback = function(experiment, db, element) {
    var additionsDivId = $(document.createElement("div"));

    var feedbackText = experiment.feedback[0].text;
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
    registerDoneButtonCallback(experiment, doneButton);

    element.append(additionsDivId);

  };

  renderFeedback = function(experiment, db, element) {
    if (!experiment.feedback) {
      renderDefaultFeedback(experiment, db, element);
    } else {
      renderCustomFeedback(experiment, db, element);
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
  obj.renderFeedback = renderFeedback;
  return obj;

})();




paco.execute = (function() {

  return function(experiment, form_root) {

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
        var value = inputPair.responseHolder.answerOrder;
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
        paco.renderer.renderFeedback(experiment, paco.db, form_root);
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
    var responseEvent = paco.createResponseEventForExperiment(experiment, scheduledTime);

    if (!experiment.customRendering) {
      paco.renderer.renderForm(experiment, responseEvent, form_root, saveDataCallback, conditionalListener);    
    } else {
      paco.renderer.renderCustomExperimentForm(experiment, responseEvent, form_root, saveDataCallback, conditionalListener);
    }
  };

  
})();
    
function runCustomExperiment() {
  var form_root = $(document.createElement("div"));
  $(document.body).append(form_root);
  var experiment = paco.experiment();
  paco.renderer.loadCustomExperiment(experiment, form_root);
  if (main) {
    main(paco.experiment(), form_root);
  } else {
    form_root.html("Could not initialize the experiment");
  }
};
  

var getTestExperiment = function() {
  return {"title":"CustomHtml","description":"","informedConsentForm":"","creator":"bobevans999@gmail.com","fixedDuration":false,"id":995,"questionsChange":false,"modifyDate":"2013/11/05","inputs":[{"id":998,"questionType":"question","text":"What time is it?","mandatory":false,"responseType":"likert","likertSteps":5,"name":"q1","conditional":false,"listChoices":[],"invisibleInput":false},{"id":999,"questionType":"question","text":"How do you feel?","mandatory":false,"responseType":"open text","likertSteps":5,"name":"q2","conditional":false,"listChoices":[],"invisibleInput":false}],"feedback":[{"id":1194,"feedbackType":"display","text":"Thanks for Participating!"}],"published":false,"deleted":false,"webRecommended":false,"version":27,"signalingMechanisms":[{"type":"signalSchedule","id":996,"scheduleType":0,"esmFrequency":3,"esmPeriodInDays":0,"esmStartHour":32400000,"esmEndHour":61200000,"times":[0],"repeatRate":1,"weekDaysScheduled":0,"nthOfMonth":1,"byDayOfMonth":true,"dayOfMonth":1,"esmWeekends":false,"byDayOfWeek":false}],"schedule":{"type":"signalSchedule","id":996,"scheduleType":0,"esmFrequency":3,"esmPeriodInDays":0,"esmStartHour":32400000,"esmEndHour":61200000,"times":[0],"repeatRate":1,"weekDaysScheduled":0,"nthOfMonth":1,"byDayOfMonth":true,"dayOfMonth":1,"esmWeekends":false,"byDayOfWeek":false},"customRendering":true,"customRenderingCode":"<script>\nfunction save() {\n    var experiment = paco.experiment();\n    var inputs = experiment.inputs;\n    var responses = [];\n    for (var i in inputs) {\n        var input = inputs[i];\n        var element = $('input[name='+input.name+']');\n        var value = element.val();\n        var responseObject = paco.createResponseForInput(input);\n        responseObject.answerOrder = value;   \n        responseObject.answer = value;   \n        responses.push(responseObject);\n    }\n    var event = paco.createResponseEventForExperimentWithResponses(experiment, responses);\n    \n    var dbSaveOutcomeCallback = function(status) {\n      if (status[\"status\"] === \"success\") {    \n        alert(\"Saved. \" + JSON.stringify(event));        \n        paco.executor.done();\n        // var form_root = $('#root');\n        // form_root.html(\"\");\n        // paco.renderer.renderFeedback(experiment, paco.db, form_root);        \n      } else {\n        alert(\"Could not store data. You might try again. Error: \" + status[\"error\"]);\n      }   \n    };\n\n    paco.db.saveEvent(event, dbSaveOutcomeCallback);        \n\n}\n\n\n</script>\n<div id=\"root\">\n<h1>Please answer the following</h1>\nQ1 <input type=text name=q1></br>\nQ2 <input type=text name=q2></br>\n<input type=submit name=submit onclick=\"save()\">\n</div>"};
};

  var XgetTestExperiment = function() {
    return {"title":"Food Concierge",
"description":"Help Googlers get the best performance through functional foods.",
"informedConsentForm":"tbd","creator":"bobevans999@gmail.com","fixedDuration":false,"id":10871024,"questionsChange":false,"modifyDate":"2013/09/13","inputs":[{"id":3,"questionType":"question","text":"Which part of the day is this?","mandatory":false,"responseType":"list","likertSteps":5,"name":"time_of_day","conditional":false,"listChoices":["Right before lunchtime","Right after lunchtime","Three hours after lunchtime"],"invisibleInput":false},{"id":4,"questionType":"question","text":"What's important to you today?","mandatory":false,"responseType":"list","likertSteps":5,"name":"important_today","conditional":true,"conditionExpression":"time_of_day == 1","listChoices":["Calm","Focus & Concentration","Energy & Pep","Stimulation & Engagement","Reducing Anxiety","Reducing Distractability","Reducing Fatigue","Reducing Boredom"],"multiselect":true,"invisibleInput":false},{"id":8002,"questionType":"question","text":"Did you eat based on the recommendation?","mandatory":false,"responseType":"list","likertSteps":2,"leftSideLabel":"Yes","rightSideLabel":"No","name":"ate_recommendation","conditional":true,"conditionExpression":"time_of_day == 2","listChoices":["Yes","No","I didn't get a recommendation"],"invisibleInput":false},{"id":9,"questionType":"question","text":"I Feel","mandatory":false,"responseType":"likert","likertSteps":5,"leftSideLabel":"Anxious","rightSideLabel":"Calm","name":"mood_a_c","conditional":true,"conditionExpression":"time_of_day >= 1","listChoices":[],"invisibleInput":false},{"id":10,"questionType":"question","text":"","mandatory":false,"responseType":"likert","likertSteps":5,"leftSideLabel":"Distractable","rightSideLabel":"Focused","name":"mood_d_f","conditional":true,"conditionExpression":"time_of_day > 0","listChoices":[],"invisibleInput":false},{"id":11,"questionType":"question","text":"","mandatory":false,"responseType":"likert","likertSteps":5,"leftSideLabel":"Fatigued","rightSideLabel":"Energized","name":"mood_f_e","conditional":true,"conditionExpression":"time_of_day > 0","listChoices":[],"invisibleInput":false},{"id":12,"questionType":"question","text":"","mandatory":false,"responseType":"likert","likertSteps":5,"leftSideLabel":"Bored","rightSideLabel":"Stimulated","name":"mood_b_s","conditional":true,"conditionExpression":"time_of_day > 0","listChoices":[],"invisibleInput":false}],"feedback":[{"id":99001,"feedbackType":"display","text":"<script>function isLunchTime() { \n return paco.db.getMostRecentAnswerTodayFor(\"time_of_day\") === 1; \n }; \n\nfunction isRightAfterLunchTime() { \n \n return paco.db.getMostRecentAnswerTodayFor(\"time_of_day\") === 2; \n}; \n\n\nfunction isAfternoonTime() { \n return paco.db.getMostRecentAnswerTodayFor(\"time_of_day\") === 3; \n}; \n\n\nfunction get_foods(importantToday) { \n var suggestions = food_suggestions(importantToday); \n return suggestions; //food_mood_restrictions(data[\"anxious\"], data[\"energy\"], data[\"fatigued\"], data); \n}; \n\n\nvar DEFAULT_SCORE = 100; \n\nfunction calm_foods() { \n return [ \n { \"food\" : \n \"brown rice\", \n \"reason\": \"B-complex vitamins help calm your nervous system.\", \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"whole grain pasta\", \n \"reason\" : \"B-complex vitamins help calm your nervous system.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"cereals\", \n \"reason\" : \"B-complex vitamins help calm your nervous system.\" , \n \"score\" : DEFAULT_SCORE}, \n { \n \"food\" : \"wheat germ\", \n \"reason\" : \"B-complex vitamins help calm your nervous system.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"yeast extracts\", \n \"reason\" : \"B-complex vitamins help calm your nervous system.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"Herbal tea \(chamomile or rooibos\)\", \n \"reason\" : \"\" , \n \"score\" : DEFAULT_SCORE}, \n { \n \"food\" : \"sparking water\", \n \"reason\" : \"\" , \n \"score\" : DEFAULT_SCORE} \n ]; \n}; \n\nfunction focus_foods() { \n return [ \n { \"food\" : \"oatmeal\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"whole wheat\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \n \"leafy green and vegetables\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"dairy\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"eggs\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"berries\", \n \"reason\" : \n \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"honeydew melon\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n \n { \"food\" : \"cantaloupe\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"tomatoes\", \n \"reason\" : \"Stimulates serotonin production.\" , \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"coffee\", \n \"reason\": \"May help improve focus while you work.\", \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"chewing gum\", \n \"reason\" : \n \"May help improve focus while you work.\" , \n \"score\" : DEFAULT_SCORE}]; \n}; \n\n\nfunction energy_foods() { \n return [ \n { \"food\" : \"Yogurt and nuts\", \n \"reason\":\"The combination of amino acids in yogurt and nuts (lysine and arginine) help quell rising stress hormones.\", \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"Nuts rich in magnesium\", \"reason\" : \"Stave off fatigue by preventing excess lactic acid production.\", \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"Foods rich in tyrosine (yogurt, turkey, eggs, seaweed, to name a few)\", \"reason\" : \"Help with alertness and memory\", \n \"score\" : DEFAULT_SCORE } ]; \n}; \n\nfunction engagement_foods() { \n return [ \n { \"food\" : \"Yogurt and nuts\", \n \"reason\":\"The combination of amino acids in yogurt and nuts (lysine and arginine) help quell rising stress hormones.\", \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"Nuts rich in magnesium\", \"reason\" : \"Stave off fatigue by preventing excess lactic acid production.\", \n \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"Foods rich in tyrosine (yogurt, turkey, eggs, seaweed, to name a few)\", \"reason\" : \"Help with alertness and memory\", \n \"score\" : \n DEFAULT_SCORE } ]; \n}; \n\nfunction reduce_anxiety_foods() { \n return [ \n { \"food\" : \"Balanced macronutrient intake\", \"reason\" : \"\", \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"peppermint tea\", \n \"reason\" : \"For focus\", \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"banana\", \"reason\" : \"For concentration\" , \"score\" : DEFAULT_SCORE} ]; \n}; \n\nfunction reduce_distractability_foods() { \n return [ \n { \"food\" : \"Space out calorie consumption\", \"reason\" : \"\", \"score\" : DEFAULT_SCORE}, \n { \"food\" : \"Eat leafy greens and lots of small balanced snacks\" , \"reason\" : \"\", \n \"score\" : \n DEFAULT_SCORE} ]; \n}; \n\nfunction reduce_fatigue_foods() { \n return [ \n {\"food\" : \"Avoid high protein\", \"reason\" : \"\", \"score\" : DEFAULT_SCORE }, \n {\"food\" : \"whole grains, berries\", \n \"reason\" : \"To improve spatial memory\" , \"score\" : DEFAULT_SCORE}, \n {\"food\": \"flax\", \"reason\" : \"To sharpen your senses\", \"score\" : DEFAULT_SCORE }]; \n}; \n\nfunction \nreduce_boredom_foods() { \n return [ \n { \"food\" : \"balanced meal\" , \"reason\" : \"Consume a balanced meal prior to a stimulant like caffeine or chocolate\", \n \"score\" : DEFAULT_SCORE}, \n { \n \"food\" : \"add flax seeds to your food\", \"reason\" : \"To sharpen your senses\", \"score\" : DEFAULT_SCORE}]; \n}; \n\nvar initialModelRules = [ \n { \"id\" : 1, \"goals\" : [\"Calm\"], \n \"interventions\" : \n calm_foods(), \n \"inputs\" : [\"mood_a_c\"]}, \n \n { \"id\" : 2, \"goals\" : [\"Focus & Concentration\"], \n \"interventions\" : focus_foods(), \n \"inputs\" : [\"mood_d_f\"] }, \n \n { \"id\" : 3, \"goals\" : \n [\"Energy & Pep\"], \n \"interventions\" : energy_foods(), \n \"inputs\" : [\"mood_f_e\"] }, \n \n { \"id\" : 4, \"goals\" : [\"Stimulation & Engagement\"], \n \"interventions\" : engagement_foods(), \n \n \"inputs\" : [\"mood_b_s\"]}, \n \n { \"id\" : 5, \"goals\" : [\"Reducing Anxiety\"], \n \"interventions\" : reduce_anxiety_foods(), \n \"inputs\" : [\"mood_a_c\"]}, \n \n { \"id\" : 6, \"goals\" : [\"Reducing Distractability\"], \n \"interventions\" :reduce_distractability_foods(), \n \"inputs\" : [\"mood_d_f\"] }, \n \n { \"id\" : 7, \"goals\" : [\"Reducing Fatigue\"], \n \"interventions\" :reduce_fatigue_foods(), \n \"inputs\" : [\"mood_f_e\"] }, \n \n { \"id\" : 8, \"goals\" : [\"Reducing Boredom\"], \n \"interventions\" :reduce_boredom_foods(), \n \"inputs\" : [\"mood_b_s\"] } \n]; \n\n\nfunction \ngetPersistedRules(experimentData) { \n return JSON.parse(paco.db.getMostRecentAnswerFor(\"model\")); \n}; \n\nvar model = (function() { \n \n var modelRules; \n var loaded = false; \n \n var checkLoaded = \n function() { \n if (!loaded) { \n throw \"Models must be loaded before they can be used\" \n } \n } \n \n var goalsMatchRuleGoals = function(goals, ruleGoals) { \n checkLoaded(); \n var match = true; \n $.each(ruleGoals, function(r) { \n var ruleGoal = ruleGoals[r]; \n if (!paco.answerHas(goals, ruleGoal)) { \n match = false; \n } \n }); \n return match; \n }; \n \n // TODO Should we return all interventions for all sets of rules? \n // for now return first matching rule for goal answer \n var rulesForGoals = function(goals) { \n checkLoaded(); \n var matchingRules = []; \n var result = []; \n for \n (var i = 0; i < modelRules.length; i++) { \n var rule = modelRules[i]; \n result = goalsMatchRuleGoals(goals, rule.goals); \n if (result) { \n matchingRules.push(rule); \n } \n } \n return matchingRules; \n }; \n \n var updateInterventionScore = function(goal, intervention, inputResponses) { \n checkLoaded(); \n var matchingRules = rulesForGoals(goal); \n for(var i = 0; i < matchingRules.length; i++) { \n var currentRule = matchingRules[i]; \n for(var j = 0; j < currentRule.interventions.length; j++) { \n var currentIntervention = currentRule.interventions[j]; \n // this implies that the foods will be unique across interventions for a given goal. \n if (currentIntervention.food === intervention) { \n var scoreAdjustment = 0; \n // iterate the model inputs and pull their new value from the inputResponses to update their score; \n for (var k = 0; k < currentRule.inputs.length; k++) { \n var currentRuleInput = currentRule.inputs[k]; \n for (var l = 0; l < \n inputResponses.length; l++) { \n var currentInputResponse = inputResponses[l]; \n if (currentInputResponse.inputName === currentRuleInput) { \n var answer = currentInputResponse.answerOrder; \n var answerInt = 0; \n if (answer) { \n answerInt = parseInt(answer); \n } \n scoreAdjustment += answerInt; // todo is this model even reasonable? \n } \n } \n currentIntervention.score = Math.max(0, currentIntervention.score + scoreAdjustment); // don\'t go below 0 \n break; \n } \n } \n } \n } \n }; \n \n var persistModel = function() { \n checkLoaded(); \n return JSON.stringify(modelRules); \n }; \n \n var loadModelFrom = function(experimentData) { \n if (loaded) { \n return; \n } \n \n modelRules = getPersistedRules(experimentData) || initialModelRules; \n loaded = true; \n } \n \n var dumpModel = function() { \n checkLoaded(); \n return JSON.stringify(modelRules); \n }; \n \n \n var obj = { \n getInterventionsForGoal : rulesForGoals, \n updateGoalInterventionScoreFromResponses : updateInterventionScore, \n save : persistModel, \n printModel : dumpModel, \n loadModelFrom : \n loadModelFrom \n }; \n \n return obj; \n})(); \n\nfunction getModel(experimentData) { \n model.loadModelFrom(experimentData); \n return model; \n}; \n\nfunction \nupdateProbabilitiesBasedOnMood(experimentData) { \n var goals =  paco.experiment().inputs[1].listChoices[paco.db.getMostRecentAnswerTodayFor(\"important_today\") - 1]; \n var intervention = paco.db.getMostRecentAnswerTodayFor(\"intervention\"); \n // todo unify db\'s \n if (!intervention || intervention.default) { \n return; \n } \n var model = getModel(experimentData); \n var responses; \n if (isRightAfterLunchTime(experimentData)) { \n responses = paco.db.getResponsesForEventNTimesAgo(1); \n } else if (isAfternoonTime()) { \n responses = paco.db.getResponsesForEventNTimesAgo(3); \n}\n if (responses) { \n model.updateGoalInterventionScoreFromResponses(goals, intervention, responses); \n recordModel(model.save()); \n } \n }; \n\nfunction getWeightedRandomIntervention(rules) { \n rules = rules[0];\nvar interventions = rules.interventions;\nvar sortedInterventions = interventions.sort(function(a,b) { return b.score - a.score; /* descending */ }); \n var \n total = 0; \n for (var i = 0; i < sortedInterventions.length; i++) { \n total = total + sortedInterventions[i].score; \n } \n var rand = paco.random(total); \n var count = 0; \n for (var i = 0; i < \n sortedInterventions.length; i++) { \n var intervention = sortedInterventions[i]; \n var score = intervention.score; \n if ((count + score) > rand) { \n return intervention; \n } else { \n count = count + \n score; \n } \n } \n return sortedInterventions[sortedInterventions.length - 1]; \n}; \n\nfunction getWeightedRandomInterventionFromModel(experimentData, goals) { \n var model = getModel(experimentData); \n var interventions = model.getInterventionsForGoal(goals); \n if (interventions != null && interventions.length > 0) { \n return getWeightedRandomIntervention(interventions); \n } else { \n return [ { \"food\" : \n \"Eat anything you like.\", \"reason\" : \"I have no recommendation.\", score : DEFAULT_SCORE, \"default\" : true } ]; \n } \n}; \n\n/** \n * Test that our weighted random works. \n */ \nfunction simWeightedRandom(iterations) { \n var interventions = [ \n { \"food\" : \"b\", \"score\" : 3}, \n { \"food\" : \"a\", \"score\" : 10}, \n { \"food\" : \"c\", \"score\" : 2} \n ]; \n var counts = {}; \n for (var i = \n 0; i < iterations; i++) { \n var chosenIntervention = getWeightedRandomIntervention(interventions); \n counts[chosenIntervention.food] = (counts[chosenIntervention.food] || 0 ) + 1; \n } \n return counts; \n}; \n\nfunction getStraightRandom(choices) { \n var index = paco.random(choices.length); \n return choices[index]; \n}; \n\nfunction toIntArray(strArray) { \n return $.map(strArray, function(s) { return parseInt(s); }); \n}; \n\n\nfunction diffVariablesAsInts(v1, v2) { \n return v1 != null && v2 != null ? parseInt(v1) - parseInt(v2) : 0; \n}; \n\nfunction recordIntervention(intervention) { \n paco.db.recordEvent([ {\"inputName\" : \n \"intervention\", \"answerOrder\" : intervention[\"food\"]} ]); \n}; \n\nfunction recordModel(model) { \n paco.db.recordEvent([ {\"inputName\" : \"model\", \"answerOrder\" : model} ]); \n}; \n\nfunction displayIntervention(display, \n goal, intervention) { \n display.add(\"<b>For goal:</b> \" + goal); \n display.add(\"<b>Suggested Foods:</b> \" + intervention[\"food\"]); \n display.add(\"<b>Why they are suggested:</b> \" + \n intervention[\"reason\"]); \n}; \n\nfunction ateAsRecommended() { \n // todo - deal with missed recommendation. \n // if dealing with a custom html interface, just check to see if we need to \n // ask them at this point. \n return paco.db.getMostRecentAnswerTodayFor(\"ate_recommendation\"); \n}; \n\nfunction ateAsRecommendedEarlierToday() { \n // todo - deal with missed right after lunch response. \n // if dealing with a custom html interface, just check to see if we need to ask them at this point. \n return paco.db.getMostRecentAnswerTodayFor(\"ate_recommendation\"); \n}; \n\n\n// This is the main part of the script that creates the display, gets the data, and \n// shows conditional feedback based on analysis. \nfunction main() { \n //debugger;\nvar experimentData = paco.db.getAllEvents(); \n var display = paco.createDisplay(); \n \n if (experimentData === undefined || experimentData.length == 0) { \n display.add(\"Thanks for participating. No data collected yet.\"); \n } else { \n if \n (isLunchTime()) { \n display.title(\"Your Food Suggestions\"); \n var importantTodayAnswers = paco.db.getMostRecentAnswerTodayFor(\"important_today\"); \nfor (var goalChoice = 0; goalChoice < importantTodayAnswers.length; goalChoice++) { \n var chosenIntervention = getWeightedRandomInterventionFromModel(experimentData, paco.experiment().inputs[1].listChoices[importantTodayAnswers[goalChoice] - 1]); \n // cleaning it up on the back side. This way we can record it at least.. \n //if (!chosenIntervention.default) { \n recordIntervention(chosenIntervention); \n // } \n displayIntervention(display, paco.experiment().inputs[1].listChoices[importantTodayAnswers[goalChoice] - 1], chosenIntervention); \n \n display.add(\"<br>\"); \n } \n } else if (isRightAfterLunchTime(experimentData)) { \n var ateRecommendation = ateAsRecommended(experimentData); \n if (ateRecommendation && ateRecommendation \n === 1) { \n var intervention = paco.db.getMostRecentAnswerTodayFor(\"intervention\"); \n var goal = paco.db.getMostRecentAnswerTodayFor(\"important_today\", 3); \n \n updateProbabilitiesBasedOnMood(experimentData); \n } \n display.title(\"Thanks for participating\"); \n } else if (isAfternoonTime()) { \n var ateRecommendation = \n ateAsRecommendedEarlierToday(experimentData); \n if (ateRecommendation && ateRecommendation === 1) { \n updateProbabilitiesBasedOnMood(experimentData); \n } \n \n display.title(\"Thanks for participating\"); \n } else { \n display.title(\"Thanks for participating\"); \n } \n \n } \n }; \n \n main(); \n</script>"}],"published":true,"deleted":false,"webRecommended":false,"version":56,"signalingMechanisms":[{"type":"signalSchedule","timeout":59,"id":1,"scheduleType":1,"esmFrequency":3,"esmPeriodInDays":0,"esmStartHour":32400000,"esmEndHour":61200000,"times":[42600000,46200000,57600000],"repeatRate":1,"weekDaysScheduled":0,"nthOfMonth":1,"byDayOfMonth":true,"dayOfMonth":1,"esmWeekends":false,"byDayOfWeek":false}],"schedule":{"type":"signalSchedule","timeout":59,"id":1,"scheduleType":1,"esmFrequency":3,"esmPeriodInDays":0,"esmStartHour":32400000,"esmEndHour":61200000,"times":[42600000,46200000,57600000],"repeatRate":1,"weekDaysScheduled":0,"nthOfMonth":1,"byDayOfMonth":true,"dayOfMonth":1,"esmWeekends":false,"byDayOfWeek":false}};

    var obj = JSON.parse(s);
    return obj;
  };

