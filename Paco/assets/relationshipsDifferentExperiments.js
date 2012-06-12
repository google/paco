function getResponseType(event, chosenVarId) {
  var responses = event.responses;
  for ( var i = 0; i < responses.length; i++) {
    var response = responses[i];
    if (response["inputId"] == chosenVarId) {
      return response["responseType"];
    }
  }
}

function isPossibleNumber(answer) {
  return answer == null || answer == "" || !isNaN(answer);
}

function isNumericalResponse(response) {
  var responseType = response["responseType"];
  return responseType != "photo" && responseType != "location" && 
    (responseType != "open text" || (responseType == "open text" && isPossibleNumber(response["answer"])));  
}

function sanityCheck(experimentData, chosenVarId) {
  var foundMatch = false;
  for ( var j = 0; j < experimentData.length; j++) {
    if (foundMatch) {
      break;
    }
    // single var version of two var version sanity checker in relationships.js
    for ( var i = 0; i < experimentData[j].responses.length; i++) {
      var response = experimentData[j].responses[i];
      if (response.inputId == chosenVarId) {
        foundMatch = true;
        if (!isNumericalResponse(response)) {
          alert("Sorry, we only show relationships between numerical values.");
          return false;
        }
      }
    }
  }
  return foundMatch;
}

function getData(experimentData, chosenVarId) {
  experimentData = experimentData.sort(function(a, b) {
    return a.responseTime - b.responseTime;
  });

  var result = { data : [], label : null};
  
  for ( var j = 0; j < experimentData.length; j++) {
    var responses = experimentData[j].responses;
    for (var r = 0; r < responses.length; r++) {
      if (responses[r].inputId == chosenVarId) {
        if (result.label == null && responses[r].inputName != null) {
          result.label = responses[r].inputName;
        }
        if (responses[r].answer) {
          result.data.push([experimentData[j].responseTime, responses[r].answer]);
        }
      }
    }
  }
  return result;
}

function parseJson(jsonVarName) {
  var jsondata = window.env.getValue(jsonVarName);
  var experimentData = $.parseJSON(jsondata);
  if (!experimentData) {
    // hack for samsung tmobile phones
    experimentData = eval('(' + jsondata + ')');
  }
  return experimentData;
}


function main() {
  var xAxisExperimentData = parseJson("xAxisData");
  var yAxisExperimentData = parseJson("yAxisData");


  if (!xAxisExperimentData) {
    alert("The first experiment has no data.");
    return;
  }
  if (!yAxisExperimentData) {
    alert("The second experiment has no data.");
    return;
  }

  var xAxisInputId = parseInt(window.env.getValue("xAxisInputId"));
  var yAxisInputId = parseInt(window.env.getValue("yAxisInputId"));

  if (!sanityCheck(xAxisExperimentData, xAxisInputId) || !sanityCheck(yAxisExperimentData, yAxisInputId)) {
    return;
  } 
  var xAxisData = getData(xAxisExperimentData, xAxisInputId);
  var yAxisData = getData(yAxisExperimentData, yAxisInputId);

  $("#charttitle").html("<h2>" + yAxisData.label + " - " + xAxisData.label + "</h2>");

  $.plot($("#placeholder"), [ {
    label : xAxisData.label,
    data : xAxisData.data,
    lines : {
      show : true
    }
  }, {
    label : yAxisData.label,
    data : yAxisData.data,
    lines : {
      show : true
    }
  } ], {
    legend : {
      container : null,
      backgroundOpacity : 0.5
    },
    xaxis : {
      mode : "time"
    }
  });
}