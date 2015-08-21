/**
 * This javascript contains all the render functions of Paco. The functions on grouped on the following 
 * categories
 * a) paco createDisplay
 * b) renderer and
 * b) execute functions which include conditionalListener and dbSaveOutcomeCallback
 */

var paco2 = (function (init) {
  var obj = init || {};
  var environment = obj["environment"] || "test";

  /*
   * The following function creates a HTML display on the form
   */
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
})();
  
//-------------------------------------------------------RENDER FUNCTIONS------------------------------------------------//
// The functions below are for rendering different UI elements on the form in Paco

paco2.renderer = (function() {
  /*
   * THis function renders a prompt window
   * @parameter - input to prompt a dialog box
   * @return - HTML element for prompt
   */
  renderPrompt = function(input) {
    var element = $(document.createElement("span"));
    element.text(input.text);
    element.addClass("prompt");
    return element;    
  };

  /*
   * This function renders a short text provided as input and response
   * @parameter - input and response
   * @return - input and value in HTML format
   */
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

  /*
   * Renders an element in the form 
   */
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

  
  /*
   * Renders a short text experiment, calls render element in turn
   */
  renderShortTextExperiment = function(input, response, conditionalListener, parent) {
    return renderElement(input, response, parent, shortTextVisualRender, conditionalListener);
  };

  
  /*
   * Renders short text
   */
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

  
  /*
   * Renders a number
   */
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


  /*
   * Renders a Likert scale
   */
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

  
  /*
   * Renders a list with option to select multiple items
   */
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

  
  /*
   * Renders a photo button
   */
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

  
  /*
   * Renders an input from a list of choices
   */
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

  
  /*
   * Render multiple inputs
   */
  renderInputs = function(experimentGroup, responseEvent, conditionalListener) {
    var inputHtmls = [];
    for (var i in  experimentGroup.inputs) {
      var input = experimentGroup.inputs[i];
      var response = responseEvent.responses[i]; // TODO kind of gross, but these are pair-wise matches with inputs.
      inputHtmls.push(renderInput(input, response, conditionalListener))
    }
    return inputHtmls;
  };
  
  
  /*
   * Renders a HTML break
   */
  renderBreak = function() {
    var br = $(document.createElement("br"));
    return br;
  };

  /*
   * Renders a HTML div
   */
  renderExperimentTitle = function(experiment) {
    var element = $(document.createElement("div"));
    element.text(experiment.title);
    element.addClass("title");
    return element;
  };

  /*
   * Renders a save button 
   */
  renderSaveButton = function() {
    var saveButton = $(document.createElement("input"));
    saveButton.attr("type", "submit");
    saveButton.attr("value", "Save Response");
    saveButton.css({"margin-top":".5em", "margin-bottom" : "0.5em"});
    return saveButton;
  };

  /*
   * Renders a DONE button
   */
  renderDoneButton = function(experiment) {
    var doneButton = document.createElement("input");
    doneButton.type="submit";
    doneButton.value = "Done";
    return doneButton;
  };

  /*
   * Removes an error from the custom page
   */
  removeErrors = function(outputs) {
    for (var i in outputs) {
      var name = outputs[i].name
      $("input[name=" + name + "]").removeClass("outlineElement");
    }

    // var str = JSON.stringify(json);
    // $("p").text("SUCCESS. Data" + str);
  };

  
  /*
   * Renders an error on the custom page
   */
  addErrors = function(json) {
    for (var i in json) {
      var name = json[i].name
      $("input[name=" + name + "]").addClass("outlineElement");
    }
  };

  
  /*
   * Register to validate the content when the save button is clicked
   */
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

  
  /*
   * Register a callback when Done button is clicked
   */
  registerDoneButtonCallback = function(doneButton) {
    doneButton.click(function() { 
      if (window.executor) {
        window.executor.done();
      } else {
        alert("All Done!");
      }
    });
  };

  
  /*
   * Renders form with all the HTML elements 
   */
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

  
  /*
   * Renders the custom experiment form enabling javascript
   */
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

  
  /*
   * loads the custom experiment
   */
  loadCustomExperiment = function(experimentGroup, rootPanel) {    
    var additionsDivId = $(document.createElement("div"));
    
    var customRenderingCode = experimentGroup.customRenderingCode;
    var newHtml = $(document.createElement('div'));
    newHtml.html(customRenderingCode);
    additionsDivId.append(newHtml)
    rootPanel.append(additionsDivId);
  };

  
  /*
   * 
   */
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

  /*
   * Renders plain text on screen
   */
  renderPlainText = function(value)  {
    var element = $(document.createElement("span"));
    element.text(value);
    return element;
  };

  
  /*
   * Renders default feedback options
   */
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


  /*
   * Extracts content within javascript tags and returns it
   */
  function scriptBody(customFeedback) {
    var scriptStartIndex = customFeedback.indexOf("<script>");
    var scriptEndIndex = customFeedback.indexOf("</"+"script>");
    if (scriptStartIndex != -1 && scriptEndIndex != -1) {
      return customFeedback.substring(scriptStartIndex + 8, scriptEndIndex);
    } 
    return "";
  }
  
  /*
   * Extracts content within HTML and returns it
   */
  function htmlBody(customFeedback) {
    var scriptEndIndex = customFeedback.indexOf("</"+"script>");
    if (scriptEndIndex != -1) {
      return customFeedback.substring(scriptEndIndex+9);
    } else {
      return customFeedback;
    }
  }
  

  /*
   * Renders custom feedback form
   */
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


/*
 * Executes the code within custom page
 */
 paco2.execute = (function() {

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
  


  /*
   * DB callback function called when data is saved
   */
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