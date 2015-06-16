function truncString(str, len) {
  if (str.length > len) {
    return str.substr(0, len);
  } else {
    return str;
  }
}

function map(fn, xs) {
  var result = [];
  for ( var i = 0; i < xs.length; i++) {
    result.push(fn(xs[i]));
  }
  return result;
}

function reduce(fn, xs, init) {
  var result = init;
  for ( var i = 0; i < xs.length; i++) {
    result = fn(result, xs[i]);
  }
  return result;
}

function sum(xs) {
  return reduce(function(sum, x) {
    return sum + x;
  }, xs, 0);
}

function getXs(xs) {
  return map(function(x) {
    return x[0];
  }, xs);
}

function getYs(xs) {
  return map(function(x) {
    return x[1];
  }, xs);
}

function slopeIntercept(dataPoints) {
  var xs = getXs(dataPoints);
  var sumX = sum(xs);
  var sumY = sum(getYs(dataPoints));
  var sumXSquared = reduce(function(sum, x) {
    return sum + Math.pow(x, 2);
  }, xs, 0);
  var sumXYProduct = sum(map(function(x) {
    return x[0] * x[1];
  }, dataPoints));
  var n = dataPoints.length;

  var slope = ((n * sumXYProduct) - (sumX * sumY)) / ((n * sumXSquared) - Math.pow(sumX, 2));
  var intercept = (sumY - (slope * sumX)) / n;
  return [slope, intercept];
}

function computeRegressionLine(dataPoints) {
  var slopeInterceptResult = slopeIntercept(dataPoints);

  var slope = slopeInterceptResult[0];
  var intercept = slopeInterceptResult[1];

  var minX = Math.min.apply(this, getXs(dataPoints));
  var maxX = Math.max.apply(this, getXs(dataPoints));

  var regressionLine = [];
  regressionLine.push([ minX, (slope * minX) + intercept ]);
  regressionLine.push([ maxX, (slope * maxX) + intercept ]);
  return regressionLine;
}

function yourChartHere() {
  $("#titleOne").html("<h2>Nothing to Display</h2>");
  $("#titleTwo").html("<p></p>");
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
      return { match : true, 
        errmsg : null };
    }
    // single var version of two var version sanity checker in relationships.js
    for ( var i = 0; i < experimentData[j].responses.length; i++) {
      var response = experimentData[j].responses[i];
      if (response.inputId == chosenVarId) {
        foundMatch = true;
        if (!isNumericalResponse(response)) {
          return { match : false, errmsg : "Sorry, we only show relationships between numerical values." };
        }
      }
    }
  }
  return { match : false, 
    errmsg : "missing data." };
}

function getData(experimentData, axisInputId) {
  experimentData = experimentData.sort(function(a, b) {
    return a.responseTime - b.responseTime;
  });

  var result = { data : [], label : null, responseType : null, axisLabels  : []};
  
  for ( var j = 0; j < experimentData.length; j++) {
    var responseTime = experimentData[j].responseTime;
    var responses = experimentData[j].responses;
    for ( var r = 0; r < responses.length; r++) {
      var answer = responses[r].answer;
      var inputId = responses[r].inputId;      
      if (!answer) {
        continue;
      } 

      if (inputId == axisInputId) {
        if (result.label == null) {
          result.label = responses[r].inputName;
          result.responseType = responses[r]["responseType"];
        }
        if (result.responseType == "list") {
          result.data.push([responseTime, responses[r].answerOrder]);
          result.axisLabels.push([responses[r].answerOrder, truncString(answer, 6)]);
        } else {
          result.data.push([responseTime, answer]);
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
    alert("The first input has no data.");
    return;
  }
  if (!yAxisExperimentData) {
    alert("The second input has no data.");
    return;
  }

  var xAxisInputId = parseInt(window.env.getValue("xAxisInputId"));
  var yAxisInputId = parseInt(window.env.getValue("yAxisInputId"));

  var xAxisSanity = sanityCheck(xAxisExperimentData, xAxisInputId);
  if (xAxisSanity.match == null) {
    alert("xAxis: " + xAxisSanity.errmsg);
    yourChartHere();
    return;
  }
  var yAxisSanity = sanityCheck(yAxisExperimentData, yAxisInputId);
  if (!yAxisSanity.match) {
    alert("yAxis: " + yAxisSanity.errmsg);
    yourChartHere();
    return;
  }
  
  var xAxisData = getData(xAxisExperimentData, xAxisInputId);
  var yAxisData = getData(yAxisExperimentData, yAxisInputId);

  $("#titleTwo").html("<h2>Time Series</h2>");
  $.plot($("#placeholderTwo"), [ {
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

  
  var scatterPlotData = [];
  if (xAxisData.data.length == yAxisData.data.length) {
    for ( var j = 0; j < xAxisData.data.length; j++) {
      scatterPlotData.push([xAxisData.data[j][1], yAxisData.data[j][1]])
    }
  }

  var regressionLine = computeRegressionLine(scatterPlotData);

  $("#titleOne").html("<h2>X-Y Plot</h2>");
  var options = {
    legend : {
      container : null,
      backgroundOpacity : 0.0
    }
  };
  
  if (xAxisData.responseType == "list") {
    options.xaxis = {
        ticks : xAxisData.axisLabels
      };
  } else {
    options.legend.backgroundOpacity = 0.5;
  }
  if (yAxisData.responseType == "list") {
    options.yaxis = {
        ticks : yAxisData.axisLabels
      };
  } else {
    options.legend.backgroundOpacity = 0.5;
  }
  
  $.plot($("#placeholderOne"), [ {
    label : yAxisData.label + " - " + xAxisData.label,
    data : scatterPlotData,
    points : {
      show : true
    },
    lines : { show : false }
  } // ,
  // {
  // data: regressionLine,
  // lines: { show: true }
  // }
  ], options);

}