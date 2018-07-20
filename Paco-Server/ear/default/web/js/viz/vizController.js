pacoApp.controller('VizCtrl', ['$scope', '$element', '$compile', 'experimentsVizService',
      '$timeout', '$routeParams', '$filter', '$mdDialog', '$sce',
    function ($scope, $element, $compile, experimentsVizService, $timeout,
              $routeParams, $filter, $mdDialog, $sce) {

  $scope.dateRangeControl = false;
  $scope.multipleInputs = false;
  $scope.expParticipants = false;
  $scope.vizChartTypes = false;
  $scope.createBtn = false;
  $scope.singleInput = false;
  $scope.saveDownload = false;
  $scope.editMode = true;
  $scope.editDescMode = true;
  $scope.backButtonDisabled = true;
  $scope.forwardButtonDisabled = true;
  $scope.vizHistory = [];
  $scope.drawButton = false;
  $scope.selectAllParticipants = false;
  $scope.deSelectAllParticipants = true;

  var responseTypeMap = new Map();
  var responseMetaData = [];
  var questionsMap = new Map();

  var DATE_FORMAT = 'yyyy-MM-ddTHH:mm:ssZ';
  var DATE_FORMAT_NO_TZ = 'yyyy-MM-ddTHH:mm:ss';

  $scope.currentVisualization = newVizSchema();

  $scope.questions = [{
    qno: 1,
    question: "Show the distribution of responses for the variable?",
  }, {
    qno: 2,
    question: "Compare distribution of responses for the variable by day?",
  }, {
    qno: 3,
    question: "How do the responses for input 1 relate to the responses for variable 2?",
  }
  , {
    qno: 4,
    question: "What is the value of the variable over time for each person?"
  }
  // , {
  //   qno: 5,
  //   question: "What is the value of the variable over time for each person?"
  // }
  ,
    {
    qno: 6,
    question: "Plot app sessions for user",
  }
  // , {
  //     qno: 6,
  //     question: "How many people in total and basic demographics.",
  //   }
    //, {
    //   qno: 7,
    //   question: "Stats: Spread of # of devices, average by use from high to low"
    // }, {
    //   qno: 8,
    //   question: "Stats:range of time on devices, any differences by demographics?"
    // }, {
    //   qno: 9,
    //   question: "No.of apps in total and ranges of time spent, and differences by demographics?"
    // }, {
    //   qno: 10,
    //   question: "App usage by category"
    // }, {
    //   qno: 11,
    //   question: "App usage by time of day with ESM responses"
    // }
  ];

  $scope.questions.forEach(function (ques) {
    questionsMap.set(ques.question, ques.qno);
  });

  $scope.dataSnapshot = function () {
    $scope.dateRange = [];
    $scope.responseCounts = [];
    $scope.loadResponseCounts = false;
    $scope.loadParticipantsCount = false;
    $scope.loadStartDate = false;
    $scope.loadEndDate = false;

    experimentsVizService.getEventsCounts($scope.experimentId).then(function (data) {
      if (data && data.data && data.data[0]) {
        $scope.responseCounts.push(data.data[0].schedR, data.data[0].missedR, data.data[0].selfR);
        $scope.loadResponseCounts = true;
      }
    });

    experimentsVizService.getParticipants($scope.experimentId).then(function (participants) {
      $scope.participants = [];
      if (participants.data && participants.data.customResponse) {
        participants.data.customResponse.forEach(function (participant) {
          $scope.participants.push(participant.who);
        });
      }
      $scope.participantsCount = $scope.participants.length;
      $scope.loadParticipantsCount = true;
    });

    experimentsVizService.getStartDate($scope.experimentId).then(function (data) {
      var format = 'yyyy/MM/dd';
      if (data.data.customResponse !== undefined) {
        if (data.data.customResponse.length > 0) {
          var format_startDate = $filter('date')(new Date(data.data.customResponse[0].response_time), format);
          $scope.dateRange[0] = format_startDate;
        }
      }
      $scope.loadStartDate = true;
    });

    experimentsVizService.getEndDate($scope.experimentId).then(function (data) {
      var format = 'yyyy/MM/dd';
      if (data.data.customResponse !== undefined) {
        if (data.data.customResponse.length > 0) {
          var format_endDate = $filter('date')(new Date(data.data.customResponse[0].response_time), format);
          $scope.dateRange[1] = format_endDate;
        }
      }
      $scope.loadEndDate = true;
      $scope.loadDataSummary = false;
    });
  };

  function correctDatetimeAfterParseIntoLocalZone(datetimeMillis) {
    if (datetimeMillis) {
      datetimeMillis = new Date(datetimeMillis);
      // restore the time to ignore timezone
      var userTimezoneOffset = datetimeMillis.getTimezoneOffset() * 60000;
      return new Date(datetimeMillis.getTime() + userTimezoneOffset);
    }
    return datetimeMillis;
  };

  //experiment json objects are retrieved from the 'experimentsVizService'
  // to create a scope variable for response type meta data.
  $scope.getExperiment = function () {
    experimentsVizService.getExperiment($scope.experimentId).then(
        function (response) {
          if (response.status === 404) {
            displayErrorMessage("Experiments ", response);
          } else {
            $scope.experiment = response.results[0];
            var experiment = response.results[0];
            if (experiment.visualizations.length > 0) {
              experiment.visualizations.forEach(function (v) {
                v.endDatetime = correctDatetimeAfterParseIntoLocalZone(v.endDatetime);
                v.startDatetime = correctDatetimeAfterParseIntoLocalZone(v.startDatetime);
              });
            }
            responseTypeData(experiment);
          }
        });
  };

  function getResponseTypeForAdditionalInputDefinitions(event) {
    if (event.text === "Form Duration") {
      return 'number';
    } else {
      return "open text";
    }
  }

  function getGroups() {
    $scope.groupInputs = [];
    $scope.groups = [];
    var experimentDefinedInputs = [];

    $scope.experiment.groups.forEach(function (group) {
      $scope.groups.push(group.name);
      group.inputs.forEach(function (input) {
        experimentDefinedInputs.push(input.name);
        // add variables for existing code. TODO remove these extras
        input.id = group.name + ":" + input.name;
        input.group =  group.name;
        input.userOrSystemDefined = false;

        $scope.groupInputs.push(input);
      });
    });


    experimentsVizService.getAdditionalInputsFromEventsData($scope.experimentId, experimentDefinedInputs).then(function (groupsList) {
      groupsList.data.customResponse.forEach(function (group) {
        if ((group.group_name)) {
          if ((group.text !== "foreground") && (group.text !== "referred_group") && (group.text !== "eodResponseTime")) {

            var responseType = getResponseTypeForAdditionalInputDefinitions(group);
            $scope.groupInputs.push({
              "id": group.group_name + ":" + group.text,
              "group": group.group_name,
              "name": group.text,
              "userOrSystemDefined": true,
              "responseType": responseType
            });
          }
        }
      });
    });
  }

  function responseTypeData(experiment) {
    responseTypeMap = new Map();
    experiment.groups.forEach(function (groups) {
      groups.inputs.forEach(function (input) {
        responseTypeMap.set(input.name, input);
      });
    });
  }

  function toggleParticipantSelectionButtons() {
    $scope.selectAllParticipants = false;
    $scope.deSelectAllParticipants = true;
  }

  function resetVariables() {
    toggleParticipantSelectionButtons();
    $scope.yAxisLabel = undefined;
  }

  $scope.selectAll = function () {
    $scope.currentVisualization.participants = $scope.participants;
    $scope.selectAllParticipants = false;
    $scope.deSelectAllParticipants = true;
  };

  $scope.deselectAll = function () {
    $scope.currentVisualization.participants = [];
    $scope.selectAllParticipants = true;
    $scope.deSelectAllParticipants = false;
  };

  $scope.getTemplate = function (isNewViz) {
    if (isNewViz) {
      var question = $scope.currentVisualization.question
      $scope.currentVisualization = newVizSchema();
      $scope.currentVisualization.question = question;
    }
    if (questionsMap.has($scope.currentVisualization.question)) {
      $scope.template = questionsMap.get($scope.currentVisualization.question);

      if ($scope.template === 1) {
        toggleVizControls(true, true, false, true, true, true, false);
      } else if ($scope.template === 2) {
        toggleVizControls(true, false, false, true, true, true, true);
      } else if ($scope.template === 3) {
        toggleVizControls(true, false, true, true, true, true, true);
      } else if ($scope.template === 4 || $scope.template === 5) {
        toggleVizControls(true, false, false, true, true, true, true);
      } else if ($scope.template === 6) {
        toggleVizControls(true, false, false, true, false, true, false);
      }
      resetVariables();
      getGroups();
      populateVizType(isNewViz);
      clearViz();
      if ($scope.template === 6) {
        $scope.deselectAll();
      }
    }
  };

  function toggleVizControls(dateCtrl, mulInputsCtrl, correlationVizCtrl, participantsCtrl, vizTypeCtrl, createCtrl, singleInputCtrl) {
    $scope.dateRangeControl = dateCtrl;
    $scope.multipleInputs = mulInputsCtrl;
    $scope.correlationViz = correlationVizCtrl;
    $scope.expParticipants = participantsCtrl;
    $scope.vizChartTypes = vizTypeCtrl;
    $scope.createBtn = createCtrl;
    $scope.singleInput = singleInputCtrl;
  }

  function populateVizType(isNewViz) {
    if (questionsMap.has($scope.currentVisualization.question)) {
      $scope.template = questionsMap.get($scope.currentVisualization.question);
      if ($scope.template === 1) {
        $scope.vizTypes = ["Box Plot", "Bar Chart", "Bubble Chart"];
      } else if ($scope.template === 2) {
        $scope.vizTypes = ["Box Plot", "Bar Chart"];
      } else if ($scope.template === 3) {
        $scope.vizTypes = ["Scatter Plot"];
      } else if ($scope.template === 4 || $scope.template === 5) {
        $scope.vizTypes = ["Scatter Plot"];
      } else if ($scope.template === 6) {
        $scope.vizTypes = ["App Usage Chart"];
      }
    }
    if (isNewViz) {
      $scope.currentVisualization.type = $scope.vizTypes[0];
    }
    axisLabels();
    $scope.displayTextMul();
    $scope.displayTextOne();
  }

  $scope.displayTextMul = function () {
    if (!$scope.currentVisualization.yAxisVariables) {
      if (($scope.currentVisualization.type === "Box Plot") || ($scope.currentVisualization.type === "Bar Chart")) {
        $scope.displayTextMultiple = "x Axis";
      }
      if ($scope.currentVisualization.type === "Bubble Chart") {
        $scope.displayTextMultiple = "Inputs";
      }
      if (($scope.currentVisualization.type === "Scatter Plot") && ($scope.template === 3)) {
        $scope.displayTextMultiple = "y Axis";
      }
    } else if ($scope.currentVisualization.yAxisVariables.length === 1) {
      $scope.displayTextMultiple = $scope.currentVisualization.yAxisVariables[0].name;
    } else {
      $scope.displayTextMultiple = dropDownDisplayText($scope.currentVisualization.yAxisVariables);
    }
    return $scope.displayTextMultiple;
  };

  $scope.displayTextOne = function () {
    if (!$scope.currentVisualization.xAxisVariable) {
      $scope.displayTextSingle = "x Axis";
    }
  };

  $scope.toggleDrawButton = function(bool_value) {
    // todo remove bool_value;
    if ($scope.template != 6 && getAllAxisVariablesFromViz($scope.currentVisualization).length == 0) {
      $scope.drawButton = false;
    } else {
      if (!requiredFieldsSetForTemplate(false)) {
        $scope.drawButton = false;
      } else {
        var compatibility = checkInputsAreCompatibleWithChartType($scope.currentVisualization);
        $scope.drawButton = compatibility.compatible;
        if (!compatibility.compatible) {
          alertAboutIncompatibleDataTypes(compatibility.incompatibleInputs);
        }
      }
    }
  };

  function axisLabels() {
    if ($scope.currentVisualization.type === "Bar Chart") {
      $scope.axisLabel1 = "x Axis";
      $scope.yAxisLabel = "Count";
    } else if (($scope.currentVisualization.type === "Scatter Plot") && ($scope.template === 4)) {
      $scope.axisLabel2 = "x Axis";
      $scope.yAxisLabel = "Date/Time Series";
    } else if (($scope.currentVisualization.type === "Scatter Plot") && ($scope.template === 3)) {
      $scope.axisLabel2 = "x Axis";
      $scope.axisLabel1 = "y Axis";
      $scope.yAxisLabel = undefined;
    } else if ($scope.currentVisualization.type === "Box Plot") {
      $scope.axisLabel1 = "x Axis";
      $scope.yAxisLabel = undefined;
    } else if ($scope.currentVisualization.type === "Bubble Chart") {
      $scope.axisLabel1 = "Inputs";
      $scope.yAxisLabel = undefined;
    }
    $scope.displayTextMul();
    $scope.displayTextOne();
  }

  function getPhoneSessionLogs(viz) {
    var startDatetimeStr = $filter('date')(viz.startDatetime, DATE_FORMAT_NO_TZ);
    var endDatetimeStr = $filter('date')(viz.endDatetime, DATE_FORMAT_NO_TZ);

    experimentsVizService.phoneSessionHttpPostBody(viz.experimentId,
      viz.participants.join(","),
      null, startDatetimeStr, endDatetimeStr)
      .then(function (phoneSessionLogList) {
      $scope.loadViz = false;
      if (phoneSessionLogList) {
        //console.log(JSON.stringify(phoneSessionLogList, null, 2));
        buildPhoneSessionViz(viz, phoneSessionLogList);
      } else {
        showAlert("Error", "There was an error retrieving data for your visualization. Please try again.\n" + phoneSessionLogList);
      }
    }, function(error) {
      showAlert("Error", "There was an error retrieving data for your visualization. Please try again.\n" + error);
      $scope.loadViz = false;
    });
  };

  function getEvents(viz) {
    var startDatetimeStr = $filter('date')(viz.startDatetime, DATE_FORMAT_NO_TZ);
    var endDatetimeStr = $filter('date')(viz.endDatetime, DATE_FORMAT_NO_TZ);

    var textsSet = [];
    var groups = [];
    if (viz.yAxisVariables && viz.yAxisVariables.length > 0) {
      viz.yAxisVariables.forEach(function (selectedInput) {
        textsSet.push(selectedInput.name);
        groups.push(selectedInput.group);
      });
    }

    if (viz.xAxisVariable) {
      textsSet.push(viz.xAxisVariable.name);
      groups.push(viz.xAxisVariable.group);
    }

    experimentsVizService.getEvents($scope.experimentId, groups, textsSet, viz.participants,
        startDatetimeStr, endDatetimeStr).then(function (events) {
      $scope.loadViz = false;
      if (events && events.data && events.data.customResponse) {
        //console.log(events.data.customResponse);
        buildViz(viz, events.data.customResponse);
      } else {
        showAlert("Error", "There was an error retrieving data for your visualization. Please try again.\n" + events);
      }
    }, function(error) {
      showAlert("Error", "There was an error retrieving data for your visualization. Please try again.\n" + error);
      $scope.loadViz = false;
    });
  }

  function pushOntoCommandHistory(viz, addedTitleString) {
    viz.historyTitle = getVizHistoryTitle(viz, addedTitleString);

    var currentPlaceInHistory = $scope.vizHistory.indexOf(viz);
    if (currentPlaceInHistory > -1) {
      $scope.vizHistory.splice(currentPlaceInHistory, 1);
      $scope.vizHistory.push(viz);
    } else {
      $scope.vizHistory.push(viz);
    }
    if ($scope.vizHistory.length > 1) {
      $scope.backButtonDisabled = false;
      $scope.forwardButtonDisabled = true;
    }
  }

  function variableCount(viz) {
    var count = 0;
    if (viz.xAxisVariable) {
      count += viz.xAxisVariable.length;
    }
    if (viz.yAxisVariables) {
      count += viz.yAxisVariables.length;
    }
    return count;
  }

  function buildViz(viz, events) {
    var zeroData = [];

    var groupByInputNameKeys = [];
    var groupByInputName = [];

    if (events) {
      groupByInputName = d3.nest()
        .key(function (d) {
          return d.text; // TODO do we actually need to group by both input.text + input.group?
        }).entries(events);

      groupByInputNameKeys = groupByInputName.map(function(e) { return e.key; });

      groupByInputName.forEach(function (eventsForInputName) {
        if (!eventsForInputName.values || eventsForInputName.values.length == 0) {
          zeroData.push(eventsForInputName.key);
        }
      });

      displayViz($scope.currentVisualization, groupByInputName);

      $scope.vizTemplate = true;

      pushOntoCommandHistory($scope.currentVisualization);
      $scope.currentVisualization = $scope.cloneVisualization($scope.currentVisualization);
    }

    if (viz.yAxisVariables) {
      viz.yAxisVariables.filter(function (input) {
        return groupByInputNameKeys.indexOf(input.name) === -1 && zeroData.indexOf(input.name) === -1;
      }).forEach(function (input) {
        zeroData.push(input.name)
      });
    }
    if (viz.xAxisVariable && groupByInputNameKeys.indexOf(viz.xAxisVariable.name) === -1 && zeroData.indexOf(viz.xAxisVariable.name) === -1) {
      zeroData.push(viz.xAxisVariable.name);
    }

    if (zeroData.length > 0) {
      var zeroDataTexts = zeroData.join(", ");
      showAlert("Zero data", "No data available for the selection: " + zeroDataTexts);
    }
  }

  function displayViz(viz, eventData) {
    if ($scope.template === 1) {
      if (($scope.currentVisualization.type === "Box Plot") && ($scope.template === 1)) {
        processBoxData(eventData);
      } else if (($scope.currentVisualization.type === "Bar Chart") && ($scope.template === 1)) {
        processBarChartData(eventData);
      } else if ($scope.currentVisualization.type === "Bubble Chart") {
        processBubbleChartData(eventData);
      }
    } else if ($scope.template === 2) {
      if (($scope.currentVisualization.type === "Box Plot")) {
        processBoxData(eventData);
      } else if (($scope.currentVisualization.type === "Bar Chart")) {
        processBarChartData(eventData);
      }
    } else if (($scope.currentVisualization.type === "Scatter Plot") && ($scope.template === 4)) {
        processXYPlotTimeSeries(eventData);
    } else if (($scope.currentVisualization.type === "Scatter Plot") && ($scope.template === 3)) {
      processScatterPlot(eventData);
    }

    $scope.saveDownload = true;
    $scope.editMode = true;
    $scope.editTextMode = true;
    $scope.drawButton = false;
    displayDescription(viz);
    displayTitle(viz);
  }

    function categoryColor(category) {
    if (category === "Home screen") {
      return "#dddddd";
    }
      // If the category passed in isn't in the array, return grey
      if (top_categories.indexOf(category) == -1) return "#A9A9A9";
      // Otherwise, return the proper color
      return d3.scale.category20().range()[ top_categories.indexOf(category) ];
    }

  /**
   *  param: phoneSessionLog - all phone sessions (with nested app sessions) for one person
   */
  function appTimelineWithSessions(phoneSessionLog) {

    var tooltip = d3.select("body")
      .append("div")
      .attr("class", "tooltip")
      .text("tooltip");

        /************** Create our graph objects ************/
          // Setup our graph.
        var last = new Date(phoneSessionLog["endTime"] + 1000 * 60 * 60);
        var first = new Date(phoneSessionLog["startTime"] - 1000 * 60 * 15);

        var margin = {top: 10, right: 10, bottom: 10, left: 40},
          width = 800; //$window.width() - margin.left - margin.right,
          height = 800; //$window.height() - margin.top - margin.bottom;

        // Define our scales (doing this before data processing so we can add source & target to path links)
        var x = d3.scale.linear()
          .range([0, width])
          .domain([0, 400]); // x axis will go from 0 to 200 (percentages for 0-100, esm to the right)
        var y = d3.time.scale()
          .range([height, 0])
          .domain([last, first]);

        // And our axes
        var yAxis = d3.svg.axis()
          .scale(y)
          .orient("left")
          .tickSubdivide(1);
        var xAxis = d3.svg.axis()
          .scale(x)
          .orient("top");

        // Create the svg

        var svg = d3.select(".vizContainer").append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .attr("id", "vis_id_svg")
          .append("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        // Add the axis
        svg.append("svg:g")
          .attr("class", "y axis")
          .attr("transform", "translate(" + margin.left + ",0)")
          .call(yAxis);

        // Add the zoom behavior
        var zoom = d3.behavior.zoom()
          .y(y).x(x)
          .on("zoom", update_axis)

        var rect = svg.append("rect")
          .attr("x", margin.left)
          .attr("y", margin.top)
          .attr("width", width - margin.left - margin.right)
          .attr("height", height - margin.top - margin.bottom)
          .attr("opacity", 0)
          .call(zoom);

        /************** Draw the data ************/

        // And now, start adding sessions...

        svg.selectAll("g.session" + i)
          .data(phoneSessionLog).enter()
          .append("rect")
          .attr("class", "timeline_session")
          .attr("x", function(d) {
            return margin.left;
          })
          .attr("y", function(d) {
            return Math.round( y(d["startTime"]));
          })
          .attr("width", 15)
          .attr("height", function(d) {
            return y(d["durationInSeconds"]);
          })
          .attr("fill", "#000")
          .on("mouseover", function(d) {
            tooltip.html("Device session from " + new Date(d["startTime"]) + " to " + new Date(d["endTime"]) + " (" + d["durationInSeconds"] + " seconds)");
            tooltip.style("top", (d3.event.pageY - 30) + "px").style("left", (d3.event.pageX + 10) + "px");
            tooltip.style("visibility", "visible");
          })
          .on("mouseout", function() {
            tooltip.style("visibility", "hidden");
          });



        for (var i in phoneSessionLog.phoneSessions) {
          var cur_percent = margin.left;


          // Add in the app cards
          var card_width = 30; //70
          var card_margin = 3;
          var card_height = 20; //40

          var num_app_sessions = phoneSessionLog.phoneSessions[i].appSessions.length;


          var cards = svg.selectAll("svg").append("svg")
            .append("g")
            .attr("class", "timeline_cards")
            .data(phoneSessionLog.phoneSessions[i].appSessions)
            .enter()
            .append("g")
            .attr("class", "timeline_card")
            .attr("leftPosition", function (datum, offset) {
              return offset;
            })
            .attr("app-dev", function(d) {
              var category = categoryMap[d["appName"]];
              var developer;
              if (!category) {
                developer = "Unknown";
              } else {
                developer = category.Developer;
              }
              return developer;
            })
            .attr("app", function(d) { return d["appName"]; })
            .attr("transform", function(d,i) {
              var x1 = (margin.left + 5 + 20) + (i * (card_width + card_margin));
              var y1 = Math.round(y(d["startTime"]));
              return "translate(" + x1 + "," + y1 + ")";
            })
            .on("mouseover", function(d) {
              tooltip.html(d["appName"] + ": " + new Date(d["startTime"]) + " (active for " + d["durationInSeconds"] + " second" + (d["durationInSeconds"] > 1 ? "s" : "") + ")");
              tooltip.style("top", (d3.event.pageY - 30) + "px").style("left", (d3.event.pageX + 10) + "px");
              tooltip.style("visibility", "visible");
            })
            .on("mouseout", function() {
              tooltip.style("visibility", "hidden");
            });


          cards
            .append("rect")
            .attr("rx", 5)
            .attr("ry", 5)
            .attr("class", "card_rect")
            .attr("width", card_width)
            .attr("height", function(d, j) {
              // Don't overlap the card in the next device session
              // var nextSessionStartTime = null;
              // if ((i + 1) < phoneSessionLog.phoneSessions.length) {
              //   nextSessionStartTime = phoneSessionLog.phoneSessions[i + 1];
              // }
              // if (nextSessionStartTime && (y(nextSessionStartTime) < (y(d["startTime"]) + card_height))) {
              //   var ch = y(nextSessionStartTime) - y(d["startTime"]);
              //   ch = ch < 1 ? 1 : ch;
              //   return ch;
              // } else {
              //   return card_height;
              // }
              var durationBasedHeight = y(d["durationInSeconds"]);
              if (durationBasedHeight < 1) {
                durationBasedHeight = 1;
              }
              return d3.max([durationBasedHeight, card_height]);
            })
            .attr("fill", function(d) {
              var entry = categoryMap[d["appName"]];
              var categoryName = null;
              if (entry) {
                categoryName = entry.Category;
              }
              return categoryColor(categoryName);
            });

          // And add the labels for the cards
          cards
            .append("text")
            .attr("class", "card_label")
            .attr("x", function(d,i) {
              return card_width / 2; //d3.select(this).parent().children("rect").attr("width") / 2;
            })
            .attr("y", function(d,i) {
              var nextSessionStartTime = null;
              if ((i + 1) < phoneSessionLog.phoneSessions.length) {
                nextSessionStartTime = phoneSessionLog.phoneSessions[i + 1];
              }
              if (nextSessionStartTime && (y(nextSessionStartTime) < (y(d["startTime"]) + card_height))) {
                var ch = y(nextSessionStartTime) - y(d["startTime"]);
                ch = ch < 1 ? 1 : ch;
                return ch / 2;
              } else {
                return card_height / 2;
              }
              // TODO calculate the real value //parent().children("rect").attr("height") / 2;
            })
            .attr("dy", ".35em")
            .style({"font-size": "10px", "text-anchor": "middle", "display": "block"})
            .text(function(d) {
              var app = d["appName"];
              if (app.length > 12) {
                app = app.slice(0,10) + "...";
              }
              return app;
            });
        }

        function update_axis() {
          var cw = card_width * d3.event.scale;
          var ch = card_height * d3.event.scale;
          var cm = card_margin * d3.event.scale;
          cw = cw > 75 ? 75 : cw;
          ch = ch > 40 ? 40 : ch;
          cm = cm > 5 ? 5 : cm;
          svg.select("g").call(yAxis).selectAll("text").style("font-size", "10px");
          svg.selectAll(".timeline_card")
            .attr("transform", function(d,k) {
              var leftOffset = d3.select(this).attr("leftPosition");
              var x1 = (margin.left + 5 + 20) + (leftOffset * (cw + cm));
              var y1 = Math.round(y(d["startTime"]));
              return "translate(" + x1 + "," + y1 + ")";
            });

          svg.selectAll(".card_rect")
            .attr("width", cw)
            .attr("height", function(d,i) {
              // Don't overlap the card in the next device session - find the y position of the first card in the next device session row
              // TODO unify this with the same function above that computes card height.
              var newHeight = ch;
              return newHeight < 1 ? 1 : newHeight;

            });

          svg.selectAll(".card_label")
            .attr("x", function() {
              return cw / 2;
            })
            .attr("y", function() {
              return  ch / 2;
            })
            .style("display", function(d,i) {
              return  ch >= 13 && cw > 20 ? "block" : "none"; // siblings("rect")
            });

          svg.selectAll(".timeline_session")
            .attr("y", function(d) {
              return Math.round( y(d["startTime"]) );
            })
            .attr("height", function(d) {
              return y(d["durationInSeconds"]);
            });
        }


        // Hacky - Update the width after everything's drawn (so the cards will be more visible)
        d3.select("#vis_id_svg").attr("width", "3500");
      }

  function buildPhoneSessionViz(viz, phoneSessionLogList) {
    var zeroData = [];

    if (phoneSessionLogList) {
      // draw the phone session viz
      d3.selectAll('.vizContainer' + "> *").remove();
      // plot the only person's phoneSessionLog (one+ phoneSessions consisting of one+ appSessions)
      var firstPhoneSessionLog = phoneSessionLogList.data[0];


      appTimelineWithSessions(firstPhoneSessionLog);
      $scope.vizTemplate = true;
      pushOntoCommandHistory($scope.currentVisualization);
      $scope.currentVisualization = $scope.cloneVisualization($scope.currentVisualization);
    }

    if (zeroData.length > 0) {
      var zeroDataTexts = zeroData.join(", ");
      showAlert("Zero data", "No data available for the selection: " + zeroDataTexts);
    }

    $scope.saveDownload = true;
    $scope.editMode = true;
    $scope.editTextMode = true;
    $scope.drawButton = false;
    displayDescription(viz);
    displayTitle(viz);
  }

  function checkIncompatibleInputs(selectedInputs) {
    var incompatibleInputs = [];
    // selectedInputs.forEach(function (input) {
    //   if (input.responseType) {
    //
    //     var index = $scope.responseData.map(function (e) {
    //       return e.key;
    //     }).indexOf(input.input);
    //
    //     $scope.responseData.splice(index, 1);
    //     incompatibleInputs.push(input.input);
    //   }
    // });
    return incompatibleInputs;
  }

  function clearViz() {
    d3.selectAll("svg > *").remove();
    $scope.vizTemplate = false;
    $scope.saveDownload = false;
    $scope.drawButton = false;
    $scope.vizDescription = null;
    $scope.vizTitle = null;
  }

  function createNewChartDescriptionText() {
    var participantsDesc = [];
    var dateDesc = " ";

    if ($scope.participantsCount === $scope.currentVisualization.participants.length) {
      participantsDesc.push("All participants")
    } else {
      participantsDesc = $scope.currentVisualization.participants.join(', ');
    }
    if ($scope.currentVisualization.startDatetime) {
      dateDesc = formatDate($scope.currentVisualization.startDatetime);
    }
    if ($scope.currentVisualization.endDatetime) {
      dateDesc = dateDesc + " " + formatDate($scope.currentVisualization.endDatetime);
    }

    var newDescription = "";
    if ($scope.template == 1) {
      newDescription = "Participants: " + participantsDesc + "<br>" + "Date Range: " + $scope.dateRange[0] + " - " + $scope.dateRange[1];
    } else if ($scope.template === 2 || $scope.template === 3 || $scope.template === 4 || $scope.template === 5) {
      newDescription = "Participants: " + participantsDesc + "<br>" + "Date Range: " + dateDesc;
    }
    return newDescription;
  }

  function displayDescription(viz) {
    if (!viz.description) {
      $scope.vizDescription = createNewChartDescriptionText();
    } else {
      // TODO do we create the local variable so that we can make the trustAsHtml call to sanitize it?
      $scope.vizDescription = $sce.trustAsHtml("<pre class='descText'>" + viz.description + "</pre>");
    }
  }

  function getDefaultTitleForViz(viz) {
    if (((viz.type === "Box Plot")
        || (viz.type === "Bar Chart")
        || (viz.type === "Bubble Chart"))
      && ($scope.template === 1)) {
      return "Distribution of responses";
    } else if ((viz.type === "Scatter Plot")
      && (viz.xAxisVariable)
      && ($scope.template === 3)) {
      return "Correlation";
    } else if ((viz.type === "Scatter Plot")
      && (viz.xAxisVariable)
      && ($scope.template === 4 || $scope.template === 5)) {
      return "Value over time by person.";
    } else if (((viz.type === "Box Plot")
        || (viz.type === "Bar Chart"))
      && (viz.xAxisVariable)
      && ($scope.template === 2)) {
      return "Distribution of responses";
    } else if ($scope.template === 6) {
      return "App Usage";
    }
    return "Edit Title";
  }

  function displayTitle(viz) {
    if (!viz.title) {
      viz.title = getDefaultTitleForViz(viz);
    }
  }

  function formatDate(dateValue) {
    return $filter('date')(new Date(dateValue), 'yyyy-MM-dd');
  }

  function formatTime(timeValue) {
    return $filter('date')(new Date(timeValue), 'HH:mm:ssZ');
  }

  // used by participantsControl.html
  $scope.getParticipantsLength = function () {

    if ($scope.currentVisualization.participants === undefined) {
      return "0 Participants";
    } else if ($scope.currentVisualization.participants.length === 1) {
      return $scope.currentVisualization.participants;
    } else {
      return dropDownDisplayText($scope.currentVisualization.participants);
    }
  };

  function dropDownDisplayText(selection) {
    if (selection.length === 1) {
      return selection;
    } else {
      if (selection === $scope.currentVisualization.yAxisVariables) {
        return selection.length + " Inputs";
      } else if (selection === $scope.currentVisualization.participants) {
        return selection.length + " Participants";
      }
    }
    return "";
  }

  function processXYPlotTimeSeries(responseData) {
    var xAxisMaxMin = [];
    var xAxisTickValues = [];
    var yValues = new Set();

    if ($scope.currentVisualization.startDatetime && $scope.currentVisualization.endDatetime) {
      xAxisMaxMin.push($scope.currentVisualization.startDatetime.getTime(),
        $scope.currentVisualization.endDatetime.getTime());
    } else if ($scope.dateRange && $scope.currentVisualization.startDatetime && $scope.currentVisualization.endDatetime) {
      $scope.dateRange.forEach(function (dateRange) {
        xAxisMaxMin.push(new Date(dateRange).getTime());
      });
    }

    function getAllDays() {
      var start_date = new Date(xAxisMaxMin[0]);
      var end_Date = new Date(xAxisMaxMin[1]);
      var dateRange = [];
      while (start_date < end_Date) {
        dateRange.push(start_date);
        start_date = new Date(start_date.setDate(
            start_date.getDate() + 1
        ));
      }
      return dateRange;
    }

    var xTickValues = getAllDays();
    xTickValues.forEach(function (value) {
      xAxisTickValues.push(value.getTime());
    });

    var groupByParticipants = d3.nest()
        .key(function (d) {
          return d.who;
        }).entries(responseData[0].values);

    var scatterPlotTimeSeries = [];
    groupByParticipants.forEach(function (participant) {
      var data = [];
      data.key = participant.key;
      data.values = [];
      participant.values.forEach(function (value) {
        data.values.push({
          x: new Date(value.response_time).getTime(),
          y: value.answer,
          size: Math.round(Math.random() * 100) / 100
        });
      });
      scatterPlotTimeSeries.push(data);
    });

    scatterPlotTimeSeries.forEach(function (plotData) {
      plotData.values.forEach(function (value) {
        yValues.add(value.y);
      })
    });
    function compareNumbers(a, b) {
      return a - b;
    }

    var sortedYValues = Array.from(yValues).sort(compareNumbers);
    var yAxisMaxMin = [];
    yAxisMaxMin.push(parseInt(sortedYValues[0]), parseInt(sortedYValues[(sortedYValues.length) - 1]));
    var yAxisTickValues = [];
    for (var i = 1; i < sortedYValues.length - 1; i++) {
      yAxisTickValues.push(parseInt(sortedYValues[i]));
    }
    drawXYPlotTimeSeries(xAxisMaxMin, yAxisMaxMin, xAxisTickValues, yAxisTickValues, scatterPlotTimeSeries);
  }

  function drawXYPlotTimeSeries(xAxisMaxMin, yAxisMaxMin, xAxisTickValues, yAxisTickValues, data) {
    d3.selectAll('.vizContainer' + "> *").remove();
    var input = $scope.currentVisualization.xAxisVariable.name;

    // create the chart
    var chart;
    nv.addGraph(function () {
      chart = nv.models.scatterChart()
          .showDistX(true)
          .showDistY(true)
          .useVoronoi(true)
          .interactive(true)
          .xDomain(xAxisMaxMin)
          .pointShape("circle")
          .pointSize(20)
          .pointRange([80, 80]) //set fixed point size on the scatter plot
          .height(550)
          .color(d3.scale.category20().range()) // c
          .duration(300);

      chart.legend.margin({top: 5, right: 0, left: 0, bottom: 30});

      chart.xAxis
          .rotateLabels(-45)
          .tickValues(xAxisTickValues)
          .tickFormat(function (d) {
            return d3.time.format('%m/%d/%y %H:%M:%S')(new Date(d));
          });
      chart.yAxis
          .tickFormat(d3.format('d'))
          .axisLabelDistance(10)
          .axisLabel(input.name);

      if ((input.responseType === "likert") || (input.responseType === "likert_smileys")) {
        var steps = 5; // default value
        if (input.likertSteps) {
          steps = input.likertSteps;
        }
        chart.yDomain([1, steps]);
        var yAxisTickValues = [];
        for (var t = 0; t < steps + 1; t++) {
          yAxisTickValues.push(t);
        }
        chart.yAxis.tickValues(yAxisTickValues);
      }
      else {
        chart.yDomain(yAxisMaxMin);
        chart.yAxis.tickValues(yAxisTickValues);
      }

      chart.tooltip(true);
      chart.tooltip.contentGenerator(function (d) {
        var rows =
            "<tr>" +
            "<td class='key'>" + 'Date:' + "</td>" +
            "<td class='x-value'><strong>" + $filter('date')(new Date(d.point.x), 'MM/dd/yyyy hh:mm:ss') + "</strong></td>" +
            "</tr>" +
            "<tr>" +
            "<td class='key'>" + 'Value:' + "</td>" +
            "<td class='x-value'><strong>" + d.point.y + "</strong></td>" +
            "</tr>";

        var header =
            "<thead>" +
            "<tr>" +
            "<td class='legend-color-guide'><div style='background-color: " + d.series.color + ";'></div></td>" +
            "<td class='key'><strong>" + d.series.key + "</strong></td>" +
            "</tr>" +
            "</thead>";

        return "<table>" +
            header +
            "<tbody>" +
            rows +
            "</tbody>" +
            "</table>";
      });

      d3.select('.vizContainer')
          .append('svg')
          .style('width', '98%')
          .style('height', 600)
          .style('margin-left', 15)
          .style('margin-top', 15)
          .style('background-color', 'white')
          .style('vertical-align', 'middle')
          .style('display', 'inline-block')
          .datum(data)
          .call(chart);

      nv.utils.windowResize(chart.update);
    });
  }

  function magicScatterPlot(responseData) {
    //console.log(responseData);
    var scatterPlotData = {};
    var currResultValues = {};
    var currWho;
    var currResponseTime;

    responseData.forEach(function (resultRow) {
      //console.log(resultRow);
      var rowWho = resultRow.who;
      var rowResponseTime = resultRow.response_time;
      var independentVars = [];
      $scope.currentVisualization.yAxisVariables.forEach(function (selectedInput) {
        independentVars.push(selectedInput.name);
      });
      //console.log(groupHasChanged(rowWho, rowResponseTime, currWho, currResponseTime));
      if (groupHasChanged(rowWho, rowResponseTime, currWho, currResponseTime)) {
        processCurrResultValues(currWho, currResponseTime, currResultValues, $scope.currentVisualization.xAxisVariable.name, independentVars);
        currWho = rowWho;
        currResponseTime = rowResponseTime;
        currResultValues = {};
      }
      addResultRowToCurrResultValues(resultRow, currResultValues, currWho, currResponseTime);
    });

    function addResultRowToCurrResultValues(resultRow, currResultValues, currWho, currRt) {
      var currText = resultRow.text;
      var currAnswer = resultRow.answer;
      var existingValues = currResultValues[currWho + currRt];
      if (existingValues === undefined) {
        existingValues = [];
        currResultValues[currWho + currRt] = existingValues;
      }
      existingValues.push([currText, currAnswer]);
    }


    function groupHasChanged(rowWho, rowRt, currWho, currRt) {
      return rowWho !== currWho && rowRt !== currRt;
    }

    function processCurrResultValues(currWho, currResponseTime, currResultValues, dependentVar, independentVars) {
      // console.log(currWho, currResponseTime, currResultValues, dependentVar, independentVars);
      var points = getPointsFromValues(currResultValues, dependentVar, independentVars);
      if (points.length > 0) {
        points.forEach(function (p) {
          var currRow = [p.y];
          if (currRow === undefined) {
            currRow = {key: points.y, values: []};
            [dependentVar] = currRow;
          }
          currRow.values.push(points);
        });
      }
    }

    function getPointsFromValues(values, dependentVar, independentVars) {
      //   if (values.length < 2) {
      //     return [];
      //   }
      //   var dependentVar = getDependentVarFrom(values, dependentVar);
      //   if (dependentVar === undefined) {
      //     return [];
      //   }
      //
      var points = [];
      values.forEach(function (iv) {
        var newPoint = {x: values.dependentVar.answer, y: values[iv]["answer"]};
        points.push(newPoint);
      });
      return points;
    }
  }


  function isMultipleSelectListResponse(variable) {
    // this cheat works because we have already gotten rid of all non-numeric types that might contain a ","
    return variable.indexOf(",") != -1;

    // TODO look up the type and see if it is a multiselect list
    // first pass in the right type that can be looked up for the particular xAxisVar or for one of the yAxisVars
    var response = $scope.currentVisualization.xAxisVariable && $scope.currentVisualization.xAxisVariable;
    var responseType = null;

    if (response && response.responseType) {
      responseType = response.responseType;
    } else {
      response = responseTypeMap.get($scope.currentVisualization.xAxisVariable.name);
      if (response) {
        responseType = response.responseType;
      }
      if (responsType === 'list' && response.multiselect) {
        return true;
      }
    }
    return false;
  }

  function splitOutResponses(answer) {
    return answer.split(",");
  }

  function processScatterPlot(responseData) {
    //console.log(responseData);
    if (responseData) {
      // TODO Split out xValue and yValue data from responseData
      var xValue = responseData.filter(function(d) { return d.key === $scope.currentVisualization.xAxisVariable.name});
      var yAxisVarNames = $scope.currentVisualization.yAxisVariables.map(function(v) { return v.name; });

      var yValue = responseData.filter(function(d) { return yAxisVarNames.indexOf(d.key) != -1;});
      var xAxisLabel = $scope.currentVisualization.xAxisVariable.name;

      var data = [];
      for (var i = 0; i < yValue.length; i++) {
        data.push({
          key: yValue[i].key,
          values: []
        });

        function getValue(collection, j) {
          if (collection) {
            var currXValue = collection.values[j];
            if (currXValue) {
              return currXValue.answer;
            }
          }
          return null;
        }

        if (xValue[0]) {
          for (var j = 0; j < xValue[0].values.length; j++) {
            var currX = getValue(xValue[0], j);
            var currY = getValue(yValue[i], j);

            var currXMultiples = [];
            var currYMultiples = [];
            if (isMultipleSelectListResponse(currX)) {
              currXMultiples = splitOutResponses(currX);
            } else {
              currXMultiples.push(currX);
            }

            if (isMultipleSelectListResponse(currY)) {
              currYMultiples = splitOutResponses(currY);
            } else {
              currYMultiples.push(currY);
            }

            currXMultiples.forEach(function(x1){
              currYMultiples.forEach(function(y1){
                if (x1 && y1) {
                  data[i].values.push({
                    x: x1,
                    y: y1,
                    size: 5
                  });
                }
              });
            });
          }
        }
      }
      //console.log(data);
      drawScatterPlot(data, xAxisLabel);
    }
  }

  function drawScatterPlot(data, xAxisLabel) {
    d3.selectAll(".vizContainer > *").remove();

    // create the chart
    var chart;
    nv.addGraph(function () {
      chart = nv.models.scatterChart()
          .showDistX(true)
          .showDistY(true)
          .useVoronoi(true)
          .pointShape("circle")
          .pointSize(20)
          .pointRange([80, 80]) //set the point size
          .height(500)
          .color(d3.scale.category20().range()) // c
          .duration(300);

      chart.legend.margin({top: 5, right: 0, left: 0, bottom: 30});

      chart.xAxis
          .rotateLabels(-45)
          .tickFormat(d3.format('d'))
          .axisLabel(xAxisLabel);

      var yAxisLabel = "";
      if (data && data[0]) {
        yAxisLabel = data[0].key
      }
      chart.yAxis
        .tickFormat(d3.format('d'))
        .axisLabel(yAxisLabel);

      d3.select('.vizContainer')
          .append('svg')
          .style('width', '98%')
          .style('height', 530)
          .style('margin-left', 15)
          .style('margin-top', 15)
          .style('background-color', 'white')
          .style('vertical-align', 'middle')
          .style('display', 'inline-block')
          .datum(data)
          .call(chart);

      nv.utils.windowResize(chart.update);
    });
  }

  function alertAboutIncompatibleDataTypes(incompatibleInputNames,
                                           compatibleInputNamesCount) {
    if (!incompatibleInputNames || incompatibleInputNames.length == 0) {
      return;
    }
    $mdDialog.show($mdDialog.confirm()
        .title('Incompatible Variable Types for Selected Visualization')
        .textContent('Incompatible Inputs: ' + incompatibleInputNames.map(function (i) {return i.name; }).join(", "))
        .ariaLabel('Incompatible Variable Types for Selected Visualization')
        .ok('ok')
    ).then(function () {
      // if (compatibleInputNamesCount === 0) {
      //   clearViz();
      //   $scope.loadViz = false;
      // } else {
        // TODO remove since we no longer put variable names in titles
         // remove names from title
        // incompatibleInputNames.forEach(function (title) {
        //   $scope.titles.splice($scope.titles.indexOf(title), 1);
        // });
        // $scope.currentVisualization.title = "Distribution of responses"; // for: " + $scope.titles.join(", ");
      // }
    });
  }

  function processBoxData(response) {

    var maxValue = 0;
    var minValue = 0;
    var boxPlotData = [];
    var keyText = undefined;
    var resKeys = [];
    var multipleKeys = "";

    function dataCount(dataSet) {
      var frequency = d3.nest()
          .key(function (d) {
            return $filter('date')(new Date(d.response_time), 'MM/dd/yyyy');
          }).sortKeys(d3.ascending)
          .rollup(function (v) {
            var answers = [];
            v.forEach(function (data) {
              answers.push(data.answer);
            });
            return {"answers": answers};
          })
          .entries(dataSet);
      return frequency;
    }

    if (response) {
      if ($scope.template === 2) {
        var dataGroupedByDay = dataCount(response[0].values);
        dataGroupedByDay.forEach(function (data) {
          var dataTransformed = transformBoxPlotData("vizByDay", data.key, data.values.answers);
          if (dataTransformed) {
            boxPlotData.push(dataTransformed);
          } else {
            keyText = response[0].key;
            multipleKeys = false;
          }
        });
      } else if ($scope.template === 1) {
        response.forEach(function (res) {
          var dataTransformed = transformBoxPlotData("vizByDateRange", res.key, res.values);
          if (dataTransformed) {
            boxPlotData.push(dataTransformed);
          }
        });
      }

      if (boxPlotData.length > 0) {
        var whiskers_high = [];
        var whiskers_low = [];
        boxPlotData.forEach(function (data) {
          whiskers_high.push(data.values.whisker_high);
          whiskers_low.push(data.values.whisker_low);
        });
        maxValue = d3.max(whiskers_high);
        minValue = d3.min(whiskers_low);
        drawBoxPlot(minValue, boxPlotData, maxValue);
      }
    }
  }

  function transformBoxPlotData(viz, key, values) {

    var data = [];
    var firstHalf = [];
    var secondHalf = [];

    var resData = {};
    resData.label = key;
    data = [];
    resData.values = {};
    var max, min, median, midPoint, q1, q3 = "";
    if (viz === "vizByDateRange") {
      values.forEach(function (val) {
        data.push(parseInt(val.answer));
      });
    } else if (viz === "vizByDay") {
      values.forEach(function (val) {
        data.push(parseInt(val));
      });
    }
    if ((data.length === 1) && (!isNaN(data[0]))) {
      resData.values = {Q1: data[0], Q2: data[0], Q3: data[0], whisker_low: data[0], whisker_high: data[0]};
    } else {
      function compareFunction(a, b) {
        return a - b;
      }

      data.sort(compareFunction);
      max = d3.max(data);
      min = d3.min(data);
      median = d3.median(data);
      midPoint = Math.floor((data.length / 2));
      firstHalf = data.slice(0, midPoint);
      secondHalf = data.slice(midPoint, data.length);
      q1 = d3.median(firstHalf);
      q3 = d3.median(secondHalf);
      if ((q1 !== undefined) && (median !== undefined) && (q3 !== undefined) && (min !== undefined) && (max !== undefined)) {
        resData.values = {Q1: q1, Q2: median, Q3: q3, whisker_low: min, whisker_high: max};
      } else {
        resData = undefined;
      }
    }
    resData.values.count = data.length;
    return resData;
  }

  function makeTicksWithNumberOfSteps(steps) {
    var yAxisTickValues = [];
    for (var t = 1; t < steps + 1; t++) {
      yAxisTickValues.push(t);
    }
    return yAxisTickValues;
  }

  function drawBoxPlot(min, boxPlotData, whisker_high) {
    d3.selectAll('.vizContainer' + "> *").remove();

    if (boxPlotData !== undefined) {
      nv.addGraph(function () {
        var chart = nv.models.boxPlotChart()
            .x(function (d) {
              return d.label;
            })
            .height(530)
            .staggerLabels(true)
            .maxBoxWidth(50)
            .yDomain([min, whisker_high]);

        chart.xAxis.showMaxMin(false);
        chart.yAxis.tickFormat(d3.format('d'));

        if (($scope.template === 2)) {
          var response = $scope.currentVisualization.xAxisVariable && $scope.currentVisualization.xAxisVariable;
          var responseType = null;

          if (response && response.responseType) {
            responseType = response.responseType;
          } else {
            response = responseTypeMap.get($scope.currentVisualization.xAxisVariable.name);
            if (response) {
              responseType = response.responseType;
            }
          }
          if (responseType && (responseType === "likert" || responseType === "likert_smileys")) {
            var steps = 5;
            if (response && response.likertSteps) {
              steps = response.likertSteps;
            }
            chart.yDomain([1, steps]);
            chart.yAxis.tickValues(makeTicksWithNumberOfSteps(steps));
          } else  if (responseType && responseType === 'list') {
            // TODO should we make the labels be the list choices themselves?
            chart.yDomain([1, response.listChoices.length]);
            chart.yAxis.tickValues(makeTicksWithNumberOfSteps(steps));
          } else {
            chart.yDomain([min, whisker_high]);
          }
        }
        chart.tooltip(true);
        chart.tooltip.contentGenerator(function (d) {
          if (d.data !== undefined) {
            var rows =
              "<tr>" +
              "<td class='key'>" + 'Count ' + "</td>" +
              "<td class='x-value' style='border-bottom:1pt solid gray;'><strong>" + d.data.values.count + "</strong></td>" +
              "</tr>" +
                "<tr>" +
                "<td class='key'>" + 'Max ' + "</td>" +
                "<td class='x-value'><strong>" + d.data.values.whisker_high + "</strong></td>" +
                "</tr>" +
                "<tr>" +
                "<td class='key'>" + '75% ' + "</td>" +
                "<td class='x-value'><strong>" + d.data.values.Q3 + "</strong></td>" +
                "</tr>" +
                "<tr>" +
                "<td class='key'>" + '50% ' + "</td>" +
                "<td class='x-value'><strong>" + d.data.values.Q2 + "</strong></td>" +
                "</tr>" +
                "<tr>" +
                "<td class='key'>" + '25%: ' + "</td>" +
                "<td class='x-value'>" + d.data.values.Q1 + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td class='key'>" + 'Min ' + "</td>" +
                "<td class='x-value'><strong>" + d.data.values.whisker_low + "</strong></td>" +
                "</tr>";

            var header =
                "<thead>" +
                "<tr>" +
                "<td class='legend-color-guide'><div style='background-color: " + d.series[0].color + ";'></div></td>" +
                "<td class='key'><strong>" + d.key + "</strong></td>" +
                "</tr>" +
                "</thead>";

            return "<table>" +
                header +
                "<tbody>" +
                rows +
                "</tbody>" +
                "</table>";
          }
        });
        chart.yAxis.axisLabel("Distribution");

        d3.select('.vizContainer')
            .append('svg')
            .on("mousedown", function () {
              d3.event.stopPropagation();
            })
            .on("mouseover", function () {
              d3.event.stopPropagation();
            })
            .on("mousemove", function () {
              d3.event.stopPropagation();
            })
            .on("mousemout", function () {
              d3.event.stopPropagation();
            })
            .style('width', '98%')
            .style('height', 570)
            .style('margin-left', 20)
            .style('margin-top', 15)
            .style('vertical-align', 'middle')
            .style('display', 'inline-block')
            .datum(boxPlotData)
            .call(chart);

        nv.utils.windowResize(chart.update);
      });
    }
  }

  function processBarChartData(res) {
    var compatibleResponses = [];

    if (res) {
      var barChartData = [];
      var values = [];
      if ($scope.template === 2) {
        barChartData = transformBarChartData_template2(res);
      } else if ($scope.template === 1) {
        res.forEach(function (response) {
          compatibleResponses.push(response);
          barChartData = transformDataforBarChart(undefined, compatibleResponses);
        });
      }

      if (barChartData) {
        barChartData.forEach(function (data) {
          data.values.forEach(function (value) {
            values.push(value.y);
          });
        });
      }

      // TODO - what is this doing?
      if (values) {
        var yAxisValues = values.filter(function (item, pos) {
          return values.indexOf(item) == pos;
        });
      }

      if (barChartData && barChartData.length > 0 &&
        yAxisValues && yAxisValues.length > 0) {
        drawMultiBarChart(barChartData, yAxisValues);
        $scope.vizTemplate = true;

      }
    }
  }

  function transformBarChartData_template2(res) {
    var maxDatesOfStudy = [];
    var studyDateRange = [];
    var responses_groupedByDate = [];
    var barChartData = {};
    var maxDatesOfStudyMap = new Map();

    function getGroupByDate(values) {
      var vizByDay = d3.nest()
          .key(function (d) {
            return $filter('date')(new Date(d.response_time), 'MM/dd/yyyy');
          }).sortKeys(d3.ascending)
          .rollup(function (v) {
            return v.length;
          })
          .entries(values);
      return vizByDay;
    }

    var vizGroupByAnswers = d3.nest()
        .key(function (d) {
          return d.answer
        })
        .rollup(function (v) {
          return v;
        })
        .entries(res[0].values);

    var barChartViz = [];
    vizGroupByAnswers.forEach(function (response) {
      if (response !== undefined) {
        barChartData = {};
        barChartData.key = response.key;
        responses_groupedByDate = getGroupByDate(response.values);
        maxDatesOfStudy.push(responses_groupedByDate);
        maxDatesOfStudyMap.set(response.key, responses_groupedByDate);
      }
    });

    var maxLength = maxDatesOfStudy.map(function (a) {
      return a.length;
    }).indexOf(Math.max.apply(Math, maxDatesOfStudy.map(function (a) {
      return a.length;
    })));

    var maxDateRange = maxDatesOfStudy[maxLength];
    maxDateRange.forEach(function (data) {
      studyDateRange.push(data.key);
    });

    maxDatesOfStudyMap.forEach(function (value, key) {
      var dateRange = [];
      var barChartValuesMap = new Map();
      var datesDiff = [];
      var chartData = {};

      chartData.key = key;
      value.forEach(function (dateValue) {
        barChartValuesMap.set(dateValue.key, dateValue.values);
        dateRange.push(dateValue.key);
      });
      datesDiff = studyDateRange.filter(function (n) {
        return !this.has(n)
      }, new Set(dateRange));

      if (datesDiff.length > 0) {
        datesDiff.forEach(function (d) {
          barChartValuesMap.set(d, 0);
        });
      }

      var barChartVals = [];
      barChartValuesMap.forEach(function (value, key) {
        var chartDataValues = {};
        chartDataValues.x = key;
        chartDataValues.y = value;
        barChartVals.push(chartDataValues);
      });
      chartData.values = barChartVals;
      barChartViz.push(chartData);
    });

    barChartViz.forEach(function (barChartVizData) {
      barChartVizData.values.sort(function (x, y) {
        return d3.ascending(x.x, y.x);
      });
    });
    return barChartViz;
  }

  function transformDataforBarChart(key, res) {

    var listChoicesMap = new Map();
    var barChartData = [];
    var responsesFrequency = [];

    //Utility functions
    //map answer indices with list choices
    function mapIndicesWithListChoices(index) {
      var listChoice = " ";
      var index = (parseInt(index) - 1).toString();
      if (listChoicesMap.has(index)) {
        listChoice = listChoicesMap.get(index);
      }
      return listChoice;
    }

    //frequency of the data
    function responseDataFrequency(dataSet) {
      var frequency = d3.nest()
          .key(function (d) {
            return d.answer;
          })
          .rollup(function (v) {
            var who = [];
            v.forEach(function (data) {
              who.push(data.who);
            });
            return {"count": v.length, "participants": who};
          })
          .entries(dataSet);
      return frequency;
    }

    res.forEach(function (responseData) {
      if (responseData !== undefined) {
        var listResponseData = [];
        var chartData = {};
        var choices = "";
        var responsesMap = new Map();
        var text = "";
        if (key !== undefined) {
          text = key;
        } else {
          text = responseData.key;
        }
        chartData.key = responseData.key;

        if (responseTypeMap.has(text)) {
          var responseType = responseTypeMap.get(text);

          if (responseType.responseType === "list") {
            for (var i in responseType.listChoices) {
              listChoicesMap.set(i, responseType.listChoices[i]);
            }
            responseData.values.forEach(function (response) {
              if (response !== undefined) {
                if (response.answer !== undefined) {
                  if (response.answer.length > 1) {
                    var answers = response.answer.split(",");
                    answers.forEach(function (a) {
                      choices = mapIndicesWithListChoices(a);
                      listResponseData.push({"who": response.who, "answer": choices, "index": a});
                    });
                  } else {
                    choices = mapIndicesWithListChoices(response.answer);
                    listResponseData.push({
                      "who": response.who,
                      "answer": choices,
                      "index": response.answer
                    });
                  }
                }
              }
            });
            responsesFrequency = responseDataFrequency(listResponseData);
          } else if (responseType.responseType === "likert" || responseType.responseType === "likert_smileys") {
            responsesFrequency = responseDataFrequency(responseData.values);
            if (responsesFrequency.length < 5) {
              responsesFrequency.forEach(function (resFrequency) {

                responsesMap.set(resFrequency.key, resFrequency.values);
              });
              var scales = ["1", "2", "3", "4", "5"];

              scales.forEach(function (scale) {
                var emptyData = {};
                if (!responsesMap.has(scale)) {
                  emptyData = {
                    key: scale,
                    values: {
                      count: 0,
                      participants: "None"
                    }
                  };
                  responsesFrequency.push(emptyData);
                }
              });
              responsesFrequency.sort(function (x, y) {
                return d3.ascending(x.key, y.key);
              });
            }
          } else {
            responsesFrequency = responseDataFrequency(responseData.values);
          }
        }

        var barChartVals = [];
        responsesFrequency.forEach(function (res) {
          var chartDataValues = {};
          chartDataValues.x = res.key;
          chartDataValues.y = res.values.count;
          chartDataValues.participants = res.values.participants;
          barChartVals.push(chartDataValues);
        });
        chartData.values = barChartVals;
        barChartData.push(chartData);
      }
    });
    return barChartData;
  }

  function drawMultiBarChart(barChartData, yAxisValues) {

    d3.selectAll('.vizContainer' + "> *").remove();
    yAxisValues.sort(d3.ascending);
    $timeout(function () {
      if (barChartData !== undefined) {
        var chart = nv.models.multiBarChart()
            .showControls(false)
            .showLegend(true)
            .height(612)
            .duration(500)
            .reduceXTicks(false);
        chart.yAxis.tickFormat(d3.format('.0f'));
        chart.yAxis.tickValues(yAxisValues);
        chart.yAxis.axisLabel("Count of responses");
        chart.yAxis.axisLabelDistance(70);
        chart.xAxis.axisLabel("Available options")
            .rotateLabels(0);
        chart.tooltip(true);
        chart.tooltip.contentGenerator(function (d) {
          var rows =
              "<tr>" +
              "<td class='key'>" + 'Value: ' + "</td>" +
              "<td class='x-value'>" + d.data.x + "</td>" +
              "</tr>" +
              "<tr>" +
              "<td class='key'>" + 'Count: ' + "</td>" +
              "<td class='x-value'><strong>" + d.data.y + "</strong></td>" +
              "</tr>";
          var header =
              "<thead>" +
              "<tr>" +
              "<td class='legend-color-guide'><div style='background-color: " + d.color + ";'></div></td>" +
              "<td class='key'><strong>" + d.data.key + "</strong></td>" +
              "</tr>" +
              "</thead>";
          return "<table>" +
              header +
              "<tbody>" +
              rows +
              "</tbody>" +
              "</table>";
        });
        var svg = d3.select('.vizContainer')
            .append('svg')
            .style('width', '98%!important')
            .style('height', 600)
            .style('margin', "auto")
            .style('display', 'block')
            .style('background-color', 'white')
            .style('vertical-align', 'middle')
            .datum(barChartData)
            .call(chart);

        nv.utils.windowResize(chart.update);
      }
    }, 1000);
  }

  function mapChoices(responseType, resData) {
    var listChoicesMap = new Map();
    var listResponseData = [];
    var choices = "";

    function mapIndicesWithListChoices(index) {
      var listChoice = " ";
      var index = (parseInt(index) - 1).toString();
      if (listChoicesMap.has(index)) {
        listChoice = listChoicesMap.get(index);
      }
      return listChoice;
    }

    for (var i in responseType.listChoices) {
      listChoicesMap.set(i, responseType.listChoices[i]);
    }
    resData.forEach(function (response) {
      if (response.answer) {
        if (response.answer.length > 1) {
          var answers = response.answer.split(",");
          answers.forEach(function (a) {
            choices = mapIndicesWithListChoices(a);
            listResponseData.push({"who": response.who, "answer": choices, "index": a});
          });
        } else {
          choices = mapIndicesWithListChoices(response.answer);
          listResponseData.push({
            "who": response.who,
            "answer": choices,
            "index": response.answer
          });
        }
      }
    });
    return listResponseData;
  }

  function processBubbleChartData(responseData) {

    var responses_bubbleChart = [];
    var vizData = [];
    var responseValues = [];
    var responsesCount = [];
    var collectiveResponses = [];

    responseData.forEach(function (data) {
      if (responseTypeMap.has(data.key)) {
        var responseType = responseTypeMap.get(data.key);
        if (responseType.responseType === "open text") {
          var stringsTokenized = tokenizeWords(data.values);
          var stringsLowerCase = vizResponseJson("open text", stringsTokenized);
          vizData.push(removeStopWords((stringsLowerCase)));
        } else if (responseType.responseType === "list") {
          var mapListChoices = mapChoices(responseType, data.values);
          vizData.push(vizResponseJson(responseType.responseType, mapListChoices));
        } else {
          var data = data.values;
          vizData.push(vizResponseJson(responseType.responseType, data));
        }
      } else {
        data.values.forEach(function (values) {
          responseValues.push(values.answer);
        });
        vizData.push(responseValues);
      }
    });
    vizData.forEach(function (responses) {
      responses.forEach(function (res) {
        collectiveResponses.push(res);
      });
    });

    responsesCount = countResponses(collectiveResponses);
    responses_bubbleChart = responsesCount;
    if (responses_bubbleChart !== undefined) {
      if (responses_bubbleChart.length > 0) {
        var bubbleChartData = responses_bubbleChart.map(function (d) {
          d.value = +d["values"];
          return d;
        });
        drawBubbleChart(bubbleChartData);
      }
    }
  }

  function vizResponseJson(type, responseData) {
    var responseJson = [];
    responseData.forEach(function (res) {
      if (type === "open text") {
        responseJson.push(res.toLowerCase());
      } else {
        responseJson.push(res.answer.toLowerCase());
      }
    });
    return responseJson;
  }

  function countResponses(dataSet) {
    var responsesCount = d3.nest()
        .key(function (d) {
          return d;
        })
        .rollup(function (v) {
          return v.length;
        })
        .entries(dataSet);
    return responsesCount;
  }

  function tokenizeWords(data) {
    var tokenizedWords = [];
    data.forEach(function (d) {
      if (d.answer !== undefined) {
        var splitWords = d.answer.split(" ");
      }
      if (splitWords !== undefined) {
        splitWords.forEach(function (word) {
          if ((word !== "") && (/^[a-zA-Z]/.test(word))) {
            tokenizedWords.push(word);
          }
        });
      }
    });
    return tokenizedWords;
  }

  function removeStopWords(tokenizedStrings) {
    var rootWords = [];
    //source - https://stackoverflow.com/questions/5631422/stop-word-removal-in-javascript
    var stopwords = ["a", "about", "above", "after", "again", "against", "all", "also", "am", "an", "and", "any", "are", "aren't",
      "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot",
      "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few",
      "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll",
      "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll",
      "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most",
      "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our",
      "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't",
      "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's",
      "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until",
      "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's",
      "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you",
      "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"];

    tokenizedStrings.forEach(function (str) {
      if (!stopwords.includes(str)) {
        rootWords.push(str);
      }
    });
    return rootWords;
  }

  function drawBubbleChart(data) {

    d3.selectAll('.vizContainer' + "> *").remove();
    if (data !== undefined) {

      var diameter = 800; //max size of the bubbles

      var bubble = d3.layout.pack()
          .sort(null)
          .size([diameter, diameter])
          .padding(1.5);

      var tooltip = d3.select("body")
          .append("div")
          .attr("class", "tooltip")
          .text("tooltip");

      var svg = d3.select('.vizContainer')
          .append("svg")
          .attr("display", "block")
          .attr("width", diameter)
          .attr("height", diameter)
          .style("margin", "auto")
          .style("margin-top", "0")
          .attr("class", "bubble");

      //convert data to a form required by the bubble chart viz
      var nodes = bubble.nodes({children: data}).filter(function (d) {
        return !d.children;
      });

      //setup the chart
      var bubbles = svg.append("g")
          .attr("transform", "translate(0,0)")
          .selectAll(".bubble")
          .data(nodes)
          .enter();

      //create the bubbles
      bubbles.append("circle")
          .attr("r", function (d) {
            return d.r;
          })
          .attr("cx", function (d) {
            return d.x;
          })
          .attr("cy", function (d) {
            return d.y;
          })
          .style("fill", "steelblue")
          .on("mouseover", function (d) {
            tooltip.html("Value: " + d.key + "<br/>Count: " + d.value);
            tooltip.style("visibility", "visible");
          })
          .on("mousemove", function () {
            return tooltip.style("top", (d3.event.pageY - 10) + "px").style("left", (d3.event.pageX + 10) + "px");
          })
          .on("mouseout", function () {
            return tooltip.style("visibility", "hidden");
          });

      //format the text for each bubble
      bubbles.append("text")
          .attr("x", function (d) {
            return d.x;
          })
          .attr("y", function (d) {
            return d.y + 5;
          })
          .style("text-anchor", "middle")
          .style("fill", "white")
          .style("pointer-events", "none")
          .text(function (d) {
            return d["key"].substring(0, d.r / 5); //trim the labels to fit the bubbles
          });
    }
  }

  //invoked by click of the draw button
  $scope.createViz = function () {
    if (requiredFieldsSetForTemplate(true)) {
      var checkResponse = checkInputsAreCompatibleWithChartType($scope.currentVisualization);
      if (checkResponse.compatible) {
        $scope.loadViz = true;
        if ($scope.template == 6) {
          getPhoneSessionLogs($scope.currentVisualization);
        } else {
          getEvents($scope.currentVisualization);
        }
      } else {
        alertAboutIncompatibleDataTypes(checkResponse.incompatibleInputs);
        $scope.drawButton = true;
      }
    } else {
      $scope.drawButton = true;
    }
  };

  function generateId() {
    return new Date().getUTCHours() + new Date().getUTCMinutes() + new Date().getUTCSeconds() + new Date().getUTCMilliseconds();
  };

  function vizRequiresNumericalData(viz) {
    return viz.type === "Box Plot" ||
      viz.type === "Bar Chart" ||
      viz.type === "Scatter Plot";
  }

  function vizRequiresTextableData(viz) {
    return viz.type === "Bubble Chart"
  }

  function inputIsTextable(input) {
    return input.responseType === "open text" ||
      input.responseType === "location" ||
      inputIsNumerical(input);
  }

  function inputIsNumerical(input) {
    return input.responseType === "number" ||
      input.responseType === "likert" ||
      input.responseType === "likert_smileys" ||
      input.responseType === "list";
  }

  function getAllAxisVariablesFromViz(viz) {
    var allInputs = [];
    if (viz.xAxisVariable) {
      allInputs.push(viz.xAxisVariable);
    }
    if (viz.yAxisVariables) {
      viz.yAxisVariables.forEach(function (v) {
        allInputs.push(v);
      });
    }
    return allInputs;
  }

  function checkInputsAreCompatibleWithChartType(viz) {
    var compatible = true;
    var incompatibleInputs = [];

    var allInputs = getAllAxisVariablesFromViz(viz);
    if (vizRequiresNumericalData(viz)) {
      allInputs.forEach(function (input) {
        if (!inputIsNumerical(input)) {
          incompatibleInputs.push(input);
        }
      });
    } else if (vizRequiresTextableData(viz)) {
      allInputs.forEach(function (input) {
        if (!inputIsTextable(input)) {
          incompatibleInputs.push(input);
        }
      });
    }

    return { "compatible" : incompatibleInputs.length == 0, "incompatibleInputs" : incompatibleInputs };
  };


  function newVizSchema() {
    var vizData = {};
    vizData.id = generateId();
    vizData.type = null;

    if ($scope.experimentId) {
      vizData.experimentId = $scope.experimentId;
    }

    vizData.title = null;
    vizData.description = null;

    vizData.question = null;

    if ($scope.participants) {
      vizData.participants = $scope.participants;
    } else {
      vizData.participants = [];
    }


    if ($scope.dateRange) {
      vizData.startDatetime = new Date($scope.dateRange[0]);
      var endDateRange = new Date($scope.dateRange[1]);
      endDateRange.setDate(endDateRange.getDate() + 1);
      vizData.endDatetime = endDateRange;
    }

    vizData.xAxisVariable = null;
    vizData.yAxisVariables = [];

    vizData.modifyDate = $filter('date')(new Date(), DATE_FORMAT);

    return vizData;
  }

  $scope.cloneVisualization = function(viz) {
    var newViz = {};
    angular.copy(viz, newViz);
    newViz.id = generateId(); // make it unique
    return newViz;
  }

  function copyCurrentVisualizationForEdit() {
    $scope.previousVisualization = $scope.currentVisualization;
    $scope.currentVisualization = $scope.cloneVisualization($scope.currentVisualization);
  }

  /**
   * Find the current viz in the list and go back 1.
   * Stop if we are at the first index.
   *
   * If we have made edits but not yet drawn (i.e. it hasn't been pushed onto the history)
   * then reset to the top item on the stack?
   */
  $scope.prevViz = function () {
    var currentViz = historyContainsViz($scope.currentVisualization); // handle the case of going back in history multiple times before hitting draw again (draw pushes a new viz onto $scope.currentViz and resets the history pointer.
    if (currentViz.length == 0) {
      currentViz = $scope.vizHistory[$scope.vizHistory.length - 1]; // any time we are navigating, we have to look at the last real viz, not the current editing one.
    } else {
      currentViz = currentViz[0];
    }

    var prevViz = null;
    $scope.vizHistory.forEach(function (viz) {
      if (viz.id === currentViz.id) {
        var currentIndex = $scope.vizHistory.indexOf(viz);
        var prevIndex = currentIndex - 1;
        if (prevIndex >= 0) {
          prevViz = $scope.vizHistory[prevIndex];
        } else { // rollover to last
          prevViz = $scope.vizHistory[$scope.vizHistory.length - 1];
        }
      } else {
        return;
      }
    });
    if (prevViz) {
      $scope.getSelectedViz(prevViz);
      $scope.forwardButtonDisabled = false;
    }
  };

  function historyContainsViz(viz) {
    var history = $scope.vizHistory.filter(function (h) { return h.id === viz.id; });
    return history;
  }

  $scope.nextViz = function () {
    var currentViz = historyContainsViz($scope.currentVisualization); // handle the case of going back in history multiple times before hitting draw again (draw pushes a new viz onto $scope.currentViz and resets the history pointer.
    if (currentViz.length == 0) {
      currentViz = $scope.vizHistory[$scope.vizHistory.length - 1]; // any time we are navigating, we have to look at the last real viz, not the current editing one.
    } else {
      currentViz = currentViz[0];
    }
    var nextViz = {};
    $scope.vizHistory.forEach(function (viz) {
      if (viz.id === currentViz.id) {
        var currIndex = $scope.vizHistory.indexOf(viz);
        var nextIndex = currIndex + 1;
        var limit = $scope.vizHistory.length;
        if (nextIndex < limit) {
          nextViz = $scope.vizHistory[currIndex + 1];
        } else { // rollover to beginning
          nextViz = $scope.vizHistory[0];
        }
      } else {
        return;
      }
    });
    $scope.getSelectedViz(nextViz);
  };

  $scope.editTitle = function () {
    copyCurrentVisualizationForEdit();
    $scope.editTitleTextMode = true;
    $scope.editTitleMode = false;
  };

  /**
   * This is called after the user has confirmed the edit of the title.
   */
  $scope.confirmTitle = function () {
    $scope.editTitleMode = true;
    $scope.editTitleTextMode = false;

    $scope.previousVisualization = null;
    pushOntoCommandHistory($scope.currentVisualization, "Title Edited");
  };

  $scope.resetTitle = function () {
    $scope.currentVisualization = $scope.previousVisualization;
    $scope.previousVisualization = null;

    $scope.editTitleMode = true;
    $scope.editTitleTextMode = false;
  };

  $scope.editDesc = function () {
    $scope.previousDescription = $scope.currentVisualization.description;

    $scope.editDescTextMode = true;
    $scope.descTextarea = {
      'height': '81px'
    };
    $scope.editDescMode = false;
    $scope.vizDescription = $scope.currentVisualization.description.replace(/<br\s*[\/]?>/gi, "\n");
  };

      /**
   * For description, we have a temp variable, $scope.vizDescription, to hold potentially bad html before we assign
   * to the model, $scope.currentVisualization
   */
  $scope.confirmDesc = function () {
    var newViz = $scope.cloneVisualization($scope.currentVisualization);
    newViz.description = $sce.trustAsHtml("<pre class='descText'>" + $scope.vizDescription + "</pre>");
    $scope.currentVisualization = newViz;

    pushOntoCommandHistory($scope.currentVisualization, "Description Edited");

    $scope.editDescMode = true;
    $scope.editDescTextMode = false;
  };

  $scope.resetDesc = function () {
    $scope.vizDescription = $sce.trustAsHtml("<pre class='descText'>" + $scope.previousDescription + "</pre>");
    $scope.previousDescription = null;

    $scope.editDescMode = true;
    $scope.editDescTextMode = false;
  };

  function requiredFieldsSetForTemplate(showAlert) {
    var msgTitle = "Required Fields";
    if (($scope.template === 1)) {
      if (!$scope.currentVisualization.type &&
        (!$scope.currentVisualization.yAxisVariables || $scope.currentVisualization.yAxisVariables.length == 0)) {
        if (showAlert) {
          showAlert(msgTitle, "Please select Viz Type and x axis value(s).");
        }
        return false;
      }
      if (!$scope.currentVisualization.type) {
        if (showAlert) {
          showAlert(msgTitle, "Please select type of visualization.");
        }
        return false;
      }
      if (!$scope.currentVisualization.yAxisVariables || $scope.currentVisualization.yAxisVariables.length == 0) {
        if (showAlert) {
          showAlert(msgTitle, "Please select the y axis value(s).");
        }
        return false;
      }
    } else if (($scope.template === 2)) {
      if ((!$scope.currentVisualization.type) && (!$scope.currentVisualization.xAxisVariable)) {
        if (showAlert) {
          showAlert(msgTitle, "Please select Viz Type and x axis value(s).");
        }
        return false;
      }
      if (!$scope.currentVisualization.type) {
        if (showAlert) {
          showAlert(msgTitle, "Please select Viz Type.");
        }
        return false;
      }
      if (!$scope.currentVisualization.xAxisVariable) {
        if (showAlert) {
          showAlert(msgTitle, "Please select the x axis value(s).");
        }
        return false;
      }
    } else if ($scope.template === 3) {
      if (!$scope.currentVisualization.type &&
        (!$scope.currentVisualization.yAxisVariables || $scope.currentVisualization.yAxisVariables.length == 0) &&
        !$scope.currentVisualization.xAxisVariable) {
        if (showAlert) {
          showAlert(msgTitle, "Please select Viz Type, x axis value and y axis value(s).");
        }
        return false;
      }
      if ((!$scope.currentVisualization.yAxisVariables || $scope.currentVisualization.yAxisVariables.length == 0)
        && !$scope.currentVisualization.xAxisVariable) {
        if (showAlert) {
          showAlert(msgTitle, "Please select x axis and y axis values.");
        }
        return false;
      }
      if (!$scope.currentVisualization.type) {
        if (showAlert) {
          showAlert(msgTitle, "Please select Viz Type.");
        }
        return false;
      }
      if (!$scope.currentVisualization.yAxisVariables || $scope.currentVisualization.yAxisVariables.length == 0) {
        if (showAlert) {
          showAlert(msgTitle, "Please select the y axis value(s).");
        }
        return false;
      }
      if (!$scope.currentVisualization.xAxisVariable) {
        if (showAlert) {
          showAlert(msgTitle, "Please select the x axis value.");
        }
        return false;
      }
    } else if ($scope.template === 4 || $scope.template === 5) {
      if (!$scope.currentVisualization.type && !$scope.currentVisualization.xAxisVariable) {
        if (showAlert) {
          showAlert(msgTitle, "Please select type of visualization and x axis value.");
        }
        return false;
      }
      if (!$scope.currentVisualization.type) {
        if (showAlert) {
          showAlert(msgTitle, "Please select type of visualization to draw.");
        }
        return false;
      }
      if (!$scope.currentVisualization.xAxisVariable) {
        if (showAlert) {
          showAlert(msgTitle, "Please select the x axis value.");
        }
        return false;
      }
    } else if ($scope.template === 6) {
      if ($scope.currentVisualization.participants.length != 1) {
        if (showAlert) {
          showAlert(msgTitle, "Please select exactly one participant.");
        }
        return false;
      }
    }
    return true;
  }

  function showAlert(messageTitle, messageContent) {
    $mdDialog.show(
        $mdDialog.alert()
            .title(messageTitle)
            .content(messageContent)
            .ariaLabel('Required Fields').ok("OK"));
  }

  function experimentHasVisualization(experiment, visualization) {
    return experiment.visualizations.filter(function(v) { return v.id === visualization.id; }).length > 0;
  };

  $scope.saveViz = function () {
    var editingExistingVisualization = experimentHasVisualization($scope.experiment, $scope.currentVisualization);
    if (!editingExistingVisualization) {
      $scope.experiment.visualizations.push($scope.currentVisualization);
    }
    experimentsVizService.saveVisualizations($scope.experiment).then(function (res) {
          var actionWord = editingExistingVisualization ? "Edit" : "Save";
          if (res.data[0].status === true) {
            showAlert(actionWord + " Status", "Visualization Saved");
            location.reload(); // TODO do we need to reload the whole page when we save a visualization?
          } else {
            $mdDialog.show($mdDialog.alert().title(actionWord + ' Status').content('Could not ' + actionWord.toLowerCase() + ' viz due to ' + res.data[0].errorMessage).ariaLabel('Success').ok('OK'));
          }
        });
  };

  $scope.reloadViz = function (viz) {
    $scope.getSelectedViz(viz);
  };

  $scope.getSelectedViz = function (viz) {
    clearViz();
    $scope.currentVisualization = viz;

    if (viz.question) {
      $scope.getTemplate();
    }
    $scope.drawButton = true;
  };

  function getVizHistoryTitle(viz, titleOrDescEdited) {
    var vizTexts = "";
    var texts = [];
    if (($scope.template === 1)) {
      if (viz.yAxisVariables.length > 1) {
        viz.yAxisVariables.forEach(function (text) {
          texts.push(text.name);
        });
        vizTexts = texts.join(", ");
      } else {
        vizTexts = viz.yAxisVariables[0].name;
      }
    } else if (($scope.template === 2) || ($scope.template === 4) || ($scope.template === 5)) {
      vizTexts = viz.xAxisVariable.name;
    } else if (($scope.template === 3)) {
      viz.yAxisVariables.forEach(function (text) {
        texts.push(text.name);
      });
      texts.push(viz.xAxisVariable.name);
      vizTexts = texts.join(", ");
    } else if (($scope.template === 6)) {
      // take the default
    }

    var title = "Q" + questionsMap.get(viz.question) + ", "
          + viz.type + ", "
          + vizTexts + ", "
          + viz.participants.length + " Participants" + ", "
          + $filter('date')(viz.startDatetime, 'MM/dd/yyyy') + "-"
          + $filter('date')(viz.endDatetime, 'MM/dd/yyyy');
    if (titleOrDescEdited) {
      title = title + ", " + titleOrDescEdited;
    }
    return title;
  }

  $scope.deleteViz = function (viz, index) {
    $mdDialog.show($mdDialog.confirm()
        .title('Delete Status')
        .content('Do you want to delete the viz: ' + viz.title + '?')
        .ariaLabel("Delete Viz")
        .cancel('Yes')
        .ok('No')).then(function () {
    }, function () {

        $scope.experiment.visualizations.splice(index, 1); // TODO delete this by matching the vizId
        experimentsVizService.saveVisualizations($scope.experiment).then(function (res) { // todo replace with plain ol saveExperiment
          if (res.data[0].status === true) {
            location.reload(); // TODO do we need to reload the whole page when we delete a saved viz?
          } else {
            $mdDialog.show($mdDialog.alert().title('Delete Status').content('Could not delete viz due to ' + res.data[0].errorMessage).ariaLabel('Success').ok('OK'));
          }
        });

    });
  };

  $scope.deleteAllSavedVisualizations = function () {
    $mdDialog.show($mdDialog.confirm()
        .title('Confirmation Status')
        .textContent('Do you want to delete all the visualizations?')
        .ariaLabel('Clear All').cancel('Yes')
        .ok('No')
    ).then(function () {
    }, function () {

        $scope.experiment.visualizations = [];
        experimentsVizService.saveVisualizations($scope.experiment).then(function (res) {
          if (res.data[0].status !== true) {
            $mdDialog.show($mdDialog.alert().title('Failure').content('Could not delete vizs due to ' + res.data[0].errorMessage).ariaLabel('Failure').ok('OK'));
          } else {
            location.reload();
          }
        });

    });
  };

  if (angular.isDefined($routeParams.experimentId)) {
    $scope.experimentId = parseInt($routeParams.experimentId, 10);
    $scope.getExperiment();
    $scope.loadDataSummary = true;
    $scope.dataSnapshot();
  }

  // function listValuesEqual(newValue, oldValue) {
  //   if (newValue == oldValue) {
  //     return true;
  //   }
  //   if (newValue && !oldValue) {
  //     return false;
  //   } else if (!newValue && oldValue) {
  //     return false;
  //   } else if (!newValue && !oldvalue) {
  //     return true;
  //   } else { //(newValue && oldValue)
  //     if (newValue.length != oldValue.length) {
  //       return false;
  //     }
  //     for (var index = 0; index < newValue.length; index++) {
  //       var newVal = newValue[index];
  //       var oldVal = oldValue[index];
  //       if (newVal.id !== oldVal.id) {
  //         return false;
  //       }
  //     }
  //     return true;
  //   }
  // };
  //

  function inputListChanged() {
    $scope.currentVisualization.title = null;
    $scope.currentVisualization.description = null;
  }

  function displayErrorMessage(data, error) {
    $scope.vizTemplate = false;
    var message = "";
    var errorData = "";
    if (data == "Query") {
      message = error.errorMessage;
      errorData = "";
    }
    else {
      message = error.statusText;
      errorData = data;
    }
    $scope.error = {
      data: errorData,
      code: error.status,
      message: message
    };
  }
      //
      // var categoryMap = {
      //   "Google App": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.googlequicksearchbox",
      //     "App": "Google App",
      //     "Developer": "Google Inc."
      //   },
      //   "System UI": {
      //     "Category": "Recent tasks",
      //     "Raw": "com.android.systemui",
      //     "App": "System UI",
      //     "Developer": "Google Inc."
      //   },
      //   "Google+": {
      //     "Category": "Social",
      //     "Raw": "com.google.android.apps.plus",
      //     "App": "Google+",
      //     "Developer": "Google Inc."
      //   },
      //   "Gmail": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.gm",
      //     "App": "Gmail",
      //     "Developer": "Google Inc."
      //   },
      //   "Email": {
      //     "Category": "Communication",
      //     "Raw": "com.samsung.android.email.ui",
      //     "App": "Email",
      //     "Developer": "Unknown"
      //   },
      //   "Chrome": {
      //     "Category": "Communication",
      //     "Raw": "com.android.chrome",
      //     "App": "Chrome",
      //     "Developer": "Google Inc."
      //   },
      //   "Docs": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.apps.docs.editors.docs",
      //     "App": "Docs",
      //     "Developer": "Google Inc."
      //   },
      //   "Messenger": {
      //     "Category": "Communication",
      //     "Raw": "com.facebook.orca",
      //     "App": "Messenger",
      //     "Developer": "Facebook"
      //   },
      //   "Google Opinion Rewards": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.paidtasks",
      //     "App": "Google Opinion Rewards",
      //     "Developer": "Google Inc."
      //   },
      //   "Yahoo Mail": {
      //     "Category": "Communication",
      //     "Raw": "com.yahoo.mobile.client.android.mail",
      //     "App": "Yahoo Mail",
      //     "Developer": "Yahoo"
      //   },
      //   "Hangouts": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.talk",
      //     "App": "Hangouts",
      //     "Developer": "Google Inc."
      //   },
      //   "Grindr": {
      //     "Category": "Social",
      //     "Raw": "com.grindrapp.android",
      //     "App": "Grindr",
      //     "Developer": "Grindr LLC"
      //   },
      //   "Jack'd": {
      //     "Category": "Social",
      //     "Raw": "mobi.jackd.android",
      //     "App": "Jack'd",
      //     "Developer": "Lucid Dreams LLC"
      //   },
      //   "JW Library": {
      //     "Category": "Books & Reference",
      //     "Raw": "org.jw.jwlibrary.mobile",
      //     "App": "JW Library",
      //     "Developer": "Jehovah's Witnesses"
      //   },
      //   "Google Play Store": {
      //     "Category": "Utility",
      //     "Raw": "com.android.vending",
      //     "App": "Google Play Store",
      //     "Developer": "Google Inc."
      //   },
      //   "ES File Explorer Pro": {
      //     "Category": "Productivity",
      //     "Raw": "com.estrongs.android.pop.pro",
      //     "App": "ES File Explorer Pro",
      //     "Developer": "ES Global"
      //   },
      //   "Package installer": {
      //     "Category": "Utility",
      //     "Raw": "com.android.packageinstaller",
      //     "App": "Package installer",
      //     "Developer": "Google Inc."
      //   },
      //   "Settings": {
      //     "Category": "Utility",
      //     "Raw": "com.android.settings",
      //     "App": "Settings",
      //     "Developer": "Google Inc."
      //   },
      //   "Google Play services": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.gms",
      //     "App": "Google Play services",
      //     "Developer": "Google Inc."
      //   },
      //   "Contacts": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.contacts",
      //     "App": "Contacts",
      //     "Developer": "Google Inc."
      //   },
      //   "Photos": {
      //     "Category": "Photography",
      //     "Raw": "com.google.android.apps.photos",
      //     "App": "Photos",
      //     "Developer": "Google Inc."
      //   },
      //   "Android System": {
      //     "Category": "Utility",
      //     "Raw": "android",
      //     "App": "Android System",
      //     "Developer": "Google Inc."
      //   },
      //   "Clock": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.deskclock",
      //     "App": "Clock",
      //     "Developer": "Google Inc."
      //   },
      //   "Samsung+": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.samsung.oh",
      //     "App": "Samsung+",
      //     "Developer": "Samsung Electronics Co. Ltd"
      //   },
      //   "Angry Birds 2": {
      //     "Category": "Casual",
      //     "Raw": "com.rovio.baba",
      //     "App": "Angry Birds 2",
      //     "Developer": "Rovio Entertainment Ltd."
      //   },
      //   "Angel Stone": {
      //     "Category": "Role Playing",
      //     "Raw": "com.fincon.angelstone",
      //     "App": "Angel Stone",
      //     "Developer": "Fincon"
      //   },
      //   "Video": {
      //     "Category": "Media & Video",
      //     "Raw": "com.samsung.android.video",
      //     "App": "Video",
      //     "Developer": "Unknown"
      //   },
      //   "Gallery": {
      //     "Category": "Photography",
      //     "Raw": "com.sec.android.gallery3d",
      //     "App": "Gallery",
      //     "Developer": "Google Inc."
      //   },
      //   "Sky Force": {
      //     "Category": "Arcade",
      //     "Raw": "pl.idreams.skyforcehd",
      //     "App": "Sky Force",
      //     "Developer": "Infinite Dreams"
      //   },
      //   "Drive": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.apps.docs",
      //     "App": "Drive",
      //     "Developer": "Google Inc."
      //   },
      //   "Yummly": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.yummly.android",
      //     "App": "Yummly",
      //     "Developer": "Yummly"
      //   },
      //   "Documents": {
      //     "Category": "Productivity",
      //     "Raw": "com.android.documentsui",
      //     "App": "Documents",
      //     "Developer": "Google Inc."
      //   },
      //   "Downloads": {
      //     "Category": "Utility",
      //     "Raw": "com.android.providers.downloads.ui",
      //     "App": "Downloads",
      //     "Developer": "Google Inc."
      //   },
      //   "Facebook": {
      //     "Category": "Social",
      //     "Raw": "com.facebook.katana",
      //     "App": "Facebook",
      //     "Developer": "Facebook"
      //   },
      //   "Pages Manager": {
      //     "Category": "Business",
      //     "Raw": "com.facebook.pages.app",
      //     "App": "Pages Manager",
      //     "Developer": "Facebook"
      //   },
      //   "Authy": {
      //     "Category": "Tools",
      //     "Raw": "com.authy.authy",
      //     "App": "Authy",
      //     "Developer": "Authy Inc"
      //   },
      //   "SoundCloud": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.soundcloud.android",
      //     "App": "SoundCloud",
      //     "Developer": "SoundCloud"
      //   },
      //   "Instagram": {
      //     "Category": "Social",
      //     "Raw": "com.instagram.android",
      //     "App": "Instagram",
      //     "Developer": "Instagram"
      //   },
      //   "Inbox": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.apps.inbox",
      //     "App": "Inbox",
      //     "Developer": "Google Inc."
      //   },
      //   "Netflix": {
      //     "Category": "Entertainment",
      //     "Raw": "com.netflix.mediaclient",
      //     "App": "Netflix",
      //     "Developer": "Netflix, Inc."
      //   },
      //   "Chrome Beta": {
      //     "Category": "Productivity",
      //     "Raw": "com.chrome.beta",
      //     "App": "Chrome Beta",
      //     "Developer": "Google Inc."
      //   },
      //   "Lightroom": {
      //     "Category": "Photography",
      //     "Raw": "com.adobe.lrmobile",
      //     "App": "Lightroom",
      //     "Developer": "Adobe"
      //   },
      //   "Messenger for Whatsapp": {
      //     "Category": "Communication",
      //     "Raw": "com.messenger.forwhatsapp",
      //     "App": "Messenger for Whatsapp",
      //     "Developer": "Pride Star Apps INC"
      //   },
      //   "Paco": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.pacoapp.paco",
      //     "App": "Paco",
      //     "Developer": "Paco Developers"
      //   },
      //   "Calendar": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.calendar",
      //     "App": "Calendar",
      //     "Developer": "Google Inc."
      //   },
      //   "HBO GO": {
      //     "Category": "Entertainment",
      //     "Raw": "com.HBO",
      //     "App": "HBO GO",
      //     "Developer": "Home Box Office Inc."
      //   },
      //   "Google Cast": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.chromecast.app",
      //     "App": "Google Cast",
      //     "Developer": "Google Inc."
      //   },
      //   "Amazon": {
      //     "Category": "Shopping",
      //     "Raw": "com.amazon.windowshop",
      //     "App": "Amazon",
      //     "Developer": "Amazon Mobile LLC"
      //   },
      //   "Android TV": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.tv.remote",
      //     "App": "Android TV",
      //     "Developer": "Google Inc."
      //   },
      //   "Solid Explorer": {
      //     "Category": "Productivity",
      //     "Raw": "pl.solidexplorer2",
      //     "App": "Solid Explorer",
      //     "Developer": "NeatBytes"
      //   },
      //   "Sign Language": {
      //     "Category": "Media & Video",
      //     "Raw": "org.jw.jwlibrary.signlanguage",
      //     "App": "Sign Language",
      //     "Developer": "Jehovah's Witnesses"
      //   },
      //   "Bluetooth": {
      //     "Category": "Utility",
      //     "Raw": "com.android.bluetooth",
      //     "App": "Bluetooth",
      //     "Developer": "Google Inc."
      //   },
      //   "ES File Explorer": {
      //     "Category": "Productivity",
      //     "Raw": "com.estrongs.android.pop",
      //     "App": "ES File Explorer",
      //     "Developer": "ES Global"
      //   },
      //   "TouchWiz home": {
      //     "Category": "Home screen",
      //     "Raw": "com.sec.android.app.launcher",
      //     "App": "TouchWiz home",
      //     "Developer": "Unknown"
      //   },
      //   "Weather": {
      //     "Category": "Weather",
      //     "Raw": "com.sec.android.widgetapp.at.hero.accuweather",
      //     "App": "Weather",
      //     "Developer": "Unknown"
      //   },
      //   "Squid": {
      //     "Category": "Productivity",
      //     "Raw": "com.steadfastinnovation.android.projectpapyrus",
      //     "App": "Squid",
      //     "Developer": "Steadfast Innovation, LLC"
      //   },
      //   "Outlook": {
      //     "Category": "Productivity",
      //     "Raw": "com.microsoft.office.outlook",
      //     "App": "Outlook",
      //     "Developer": "Microsoft Corporation"
      //   },
      //   "Keep": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.keep",
      //     "App": "Keep",
      //     "Developer": "Google Inc."
      //   },
      //   "PicsArt": {
      //     "Category": "Photography",
      //     "Raw": "com.picsart.studio",
      //     "App": "PicsArt",
      //     "Developer": "PicsArt"
      //   },
      //   "My Files": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.android.app.myfiles",
      //     "App": "My Files",
      //     "Developer": "Unknown"
      //   },
      //   "Word": {
      //     "Category": "Productivity",
      //     "Raw": "com.microsoft.office.word",
      //     "App": "Word",
      //     "Developer": "Microsoft Corporation"
      //   },
      //   "Connect": {
      //     "Category": "Communication",
      //     "Raw": "net.comcast.ottclient",
      //     "App": "Connect",
      //     "Developer": "Comcast Cable Communications Management, LLC"
      //   },
      //   "MDT Tracker": {
      //     "Category": "Transportation",
      //     "Raw": "gov.miamidade.TrainTracker",
      //     "App": "MDT Tracker",
      //     "Developer": "Miami-Dade County"
      //   },
      //   "AppMgr III": {
      //     "Category": "Tools",
      //     "Raw": "com.a0soft.gphone.app2sd",
      //     "App": "AppMgr III",
      //     "Developer": "Sam Lu"
      //   },
      //   "Media Storage": {
      //     "Category": "Utility",
      //     "Raw": "com.android.providers.media",
      //     "App": "Media Storage",
      //     "Developer": "Unknown"
      //   },
      //   "YouTube": {
      //     "Category": "Media & Video",
      //     "Raw": "com.google.android.youtube",
      //     "App": "YouTube",
      //     "Developer": "Google Inc."
      //   },
      //   "WallpaperPicker": {
      //     "Category": "Personalization",
      //     "Raw": "com.sec.android.app.wallpaperchooser",
      //     "App": "WallpaperPicker",
      //     "Developer": "Unknown"
      //   },
      //   "com.sec.android.wallpapercropper2": {
      //     "Category": "Personalization",
      //     "Raw": "com.sec.android.wallpapercropper2",
      //     "App": "com.sec.android.wallpapercropper2",
      //     "Developer": "Unknown"
      //   },
      //   "Google Keyboard": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.inputmethod.latin",
      //     "App": "Google Keyboard",
      //     "Developer": "Google Inc."
      //   },
      //   "AOL": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.aol.mobile.aolapp",
      //     "App": "AOL",
      //     "Developer": "AOL Inc."
      //   },
      //   "Home": {
      //     "Category": "Home screen",
      //     "Raw": "com.android.launcher",
      //     "App": "Home",
      //     "Developer": "Unknown"
      //   },
      //   "Weather Widget": {
      //     "Category": "Weather",
      //     "Raw": "com.sec.android.widgetapp.ap.hero.accuweather",
      //     "App": "Weather Widget",
      //     "Developer": "Unknown"
      //   },
      //   "PlayMemories Mobile": {
      //     "Category": "Media & Video",
      //     "Raw": "com.sony.playmemories.mobile",
      //     "App": "PlayMemories Mobile",
      //     "Developer": "Sony Corporation"
      //   },
      //   "Android Beta Program": {
      //     "Category": "Utility",
      //     "Raw": "com.android.yadayada",
      //     "App": "Android Beta Program",
      //     "Developer": "Unknown"
      //   },
      //   "WordPress": {
      //     "Category": "Social",
      //     "Raw": "org.wordpress.android",
      //     "App": "WordPress",
      //     "Developer": "Automattic, Inc"
      //   },
      //   "Receipt Hog": {
      //     "Category": "Shopping",
      //     "Raw": "com.infoscout.receipthog",
      //     "App": "Receipt Hog",
      //     "Developer": "ScoutIt Inc."
      //   },
      //   "Emby": {
      //     "Category": "Media & Video",
      //     "Raw": "com.mb.android",
      //     "App": "Emby",
      //     "Developer": "Emby Media"
      //   },
      //   "GunBros2": {
      //     "Category": "Action",
      //     "Raw": "com.glu.gunbros2",
      //     "App": "GunBros2",
      //     "Developer": "Glu"
      //   },
      //   "Duolingo": {
      //     "Category": "Education",
      //     "Raw": "com.duolingo",
      //     "App": "Duolingo",
      //     "Developer": "Duolingo"
      //   },
      //   "Hulu": {
      //     "Category": "Entertainment",
      //     "Raw": "com.hulu.plus",
      //     "App": "Hulu",
      //     "Developer": "Hulu"
      //   },
      //   "Samsung Galaxy": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.android.app.sns3",
      //     "App": "Samsung Galaxy",
      //     "Developer": "Unknown"
      //   },
      //   "PayPal": {
      //     "Category": "Finance",
      //     "Raw": "com.paypal.android.p2pmobile",
      //     "App": "PayPal",
      //     "Developer": "PayPal Mobile"
      //   },
      //   "Spotify": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.spotify.music",
      //     "App": "Spotify",
      //     "Developer": "Spotify Ltd."
      //   },
      //   "Nine": {
      //     "Category": "Business",
      //     "Raw": "com.ninefolders.hd3",
      //     "App": "Nine",
      //     "Developer": "9Folders Inc."
      //   },
      //   "LastPass": {
      //     "Category": "Productivity",
      //     "Raw": "com.lastpass.lpandroid",
      //     "App": "LastPass",
      //     "Developer": "LastPass"
      //   },
      //   "Dropbox": {
      //     "Category": "Productivity",
      //     "Raw": "com.dropbox.android",
      //     "App": "Dropbox",
      //     "Developer": "Dropbox, Inc."
      //   },
      //   "feedly": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.devhd.feedly",
      //     "App": "feedly",
      //     "Developer": "Feedly Team"
      //   },
      //   "OpenKeychain": {
      //     "Category": "Communication",
      //     "Raw": "org.sufficientlysecure.keychain",
      //     "App": "OpenKeychain",
      //     "Developer": "Sufficiently Secure"
      //   },
      //   "Google I/O": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.google.samples.apps.iosched",
      //     "App": "Google I/O",
      //     "Developer": "Google Inc."
      //   },
      //   "Commander": {
      //     "Category": "Strategy",
      //     "Raw": "com.lucasarts.starts_goo",
      //     "App": "Commander",
      //     "Developer": "Disney"
      //   },
      //   "Textra": {
      //     "Category": "Communication",
      //     "Raw": "com.textra",
      //     "App": "Textra",
      //     "Developer": "Delicious Inc."
      //   },
      //   "Empires & Allies": {
      //     "Category": "Strategy",
      //     "Raw": "com.zynga.empires2",
      //     "App": "Empires & Allies",
      //     "Developer": "Zynga"
      //   },
      //   "WhatsApp": {
      //     "Category": "Communication",
      //     "Raw": "com.whatsapp",
      //     "App": "WhatsApp",
      //     "Developer": "WhatsApp Inc."
      //   },
      //   "Phone": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.dialer",
      //     "App": "Phone",
      //     "Developer": "Google Inc."
      //   },
      //   "Google Voice": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.apps.googlevoice",
      //     "App": "Google Voice",
      //     "Developer": "Google Inc."
      //   },
      //   "Truecaller": {
      //     "Category": "Communication",
      //     "Raw": "com.truecaller",
      //     "App": "Truecaller",
      //     "Developer": "True Software Scandinavia AB"
      //   },
      //   "Camera": {
      //     "Category": "Photography",
      //     "Raw": "com.sec.android.app.camera",
      //     "App": "Camera",
      //     "Developer": "Google Inc."
      //   },
      //   "Sports Tracker": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.stt.android",
      //     "App": "Sports Tracker",
      //     "Developer": "Sports-tracker.com"
      //   },
      //   "Calculator": {
      //     "Category": "Tools",
      //     "Raw": "com.android.calculator2",
      //     "App": "Calculator",
      //     "Developer": "Unknown"
      //   },
      //   "Sense Home": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.htc.launcher",
      //     "App": "Sense Home",
      //     "Developer": "HTC Corporation"
      //   },
      //   "Sync Pro": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.laurencedawson.reddit_sync.pro",
      //     "App": "Sync Pro",
      //     "Developer": "Red Apps LTD"
      //   },
      //   "CaptivePortalLogin": {
      //     "Category": "Communication",
      //     "Raw": "com.android.captiveportallogin",
      //     "App": "CaptivePortalLogin",
      //     "Developer": "Unknown"
      //   },
      //   "Twitter": {
      //     "Category": "Social",
      //     "Raw": "com.twitter.android",
      //     "App": "Twitter",
      //     "Developer": "Twitter, Inc."
      //   },
      //   "Messages": {
      //     "Category": "Communication",
      //     "Raw": "com.htc.sense.mms",
      //     "App": "Messages",
      //     "Developer": "Unknown"
      //   },
      //   "EDS Lite": {
      //     "Category": "Tools",
      //     "Raw": "com.sovworks.edslite",
      //     "App": "EDS Lite",
      //     "Developer": "sovworks"
      //   },
      //   "Google Play Music": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.google.android.music",
      //     "App": "Google Play Music",
      //     "Developer": "Google Inc."
      //   },
      //   "360 Security": {
      //     "Category": "Tools",
      //     "Raw": "com.qihoo.security",
      //     "App": "360 Security",
      //     "Developer": "360 Mobile Security Limited"
      //   },
      //   "Curiosity": {
      //     "Category": "Education",
      //     "Raw": "com.curiosity.dailycuriosity",
      //     "App": "Curiosity",
      //     "Developer": "Curiosity.com"
      //   },
      //   "iFunny": {
      //     "Category": "Entertainment",
      //     "Raw": "mobi.ifunny",
      //     "App": "iFunny",
      //     "Developer": "iFunny Inc."
      //   },
      //   "Moto Voice": {
      //     "Category": "Tools",
      //     "Raw": "com.motorola.audiomonitor",
      //     "App": "Moto Voice",
      //     "Developer": "Motorola Mobility LLC."
      //   },
      //   "People": {
      //     "Category": "Communication",
      //     "Raw": "com.htc.contacts",
      //     "App": "People",
      //     "Developer": "Unknown"
      //   },
      //   "Piktures": {
      //     "Category": "Photography",
      //     "Raw": "com.diune.pictures",
      //     "App": "Piktures",
      //     "Developer": "DIUNE"
      //   },
      //   "Parkmobile": {
      //     "Category": "Transportation",
      //     "Raw": "net.sharewire.parkmobilev2",
      //     "App": "Parkmobile",
      //     "Developer": "Parkmobile, LLC"
      //   },
      //   "Ghostery": {
      //     "Category": "Communication",
      //     "Raw": "com.ghostery.android.ghostery",
      //     "App": "Ghostery",
      //     "Developer": "Ghostery, Inc."
      //   },
      //   "Sky Map": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.google.android.stardroid",
      //     "App": "Sky Map",
      //     "Developer": "Sky Map Devs"
      //   },
      //   "500px": {
      //     "Category": "Photography",
      //     "Raw": "com.fivehundredpx.viewer",
      //     "App": "500px",
      //     "Developer": "500px"
      //   },
      //   "Screen capture": {
      //     "Category": "Utility",
      //     "Raw": "com.samsung.android.app.scrollcapture",
      //     "App": "Screen capture",
      //     "Developer": "Unknown"
      //   },
      //   "letgo": {
      //     "Category": "Shopping",
      //     "Raw": "com.abtnprojects.ambatana",
      //     "App": "letgo",
      //     "Developer": "letgo"
      //   },
      //   "WWR": {
      //     "Category": "Action",
      //     "Raw": "com.pixonic.wwr",
      //     "App": "WWR",
      //     "Developer": "Pixonic LLC"
      //   },
      //   "Car Buying": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.kbb.mobile",
      //     "App": "Car Buying",
      //     "Developer": "Kelley Blue Book Co., Inc."
      //   },
      //   "TrueCar": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.truecar.mobile.android.consumer",
      //     "App": "TrueCar",
      //     "Developer": "TrueCar, Inc."
      //   },
      //   "PlayStation®App": {
      //     "Category": "Entertainment",
      //     "Raw": "com.scee.psxandroid",
      //     "App": "PlayStation®App",
      //     "Developer": "PlayStation Mobile Inc."
      //   },
      //   "Music": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.htc.music",
      //     "App": "Music",
      //     "Developer": "Unknown"
      //   },
      //   "OfferUp": {
      //     "Category": "Shopping",
      //     "Raw": "com.offerup",
      //     "App": "OfferUp",
      //     "Developer": "OfferUp Inc."
      //   },
      //   "imo": {
      //     "Category": "Communication",
      //     "Raw": "com.imo.android.imoim",
      //     "App": "imo",
      //     "Developer": "imo.im"
      //   },
      //   "Serve": {
      //     "Category": "Finance",
      //     "Raw": "com.serve.mobile",
      //     "App": "Serve",
      //     "Developer": "American Express"
      //   },
      //   "USAA": {
      //     "Category": "Finance",
      //     "Raw": "com.usaa.mobile.android.usaa",
      //     "App": "USAA",
      //     "Developer": "USAA"
      //   },
      //   "Tumblr": {
      //     "Category": "Social",
      //     "Raw": "com.tumblr",
      //     "App": "Tumblr",
      //     "Developer": "Tumblr, Inc."
      //   },
      //   "MP3 Music": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.winterpeg.airstrike.amalgamates",
      //     "App": "MP3 Music",
      //     "Developer": "Unknown"
      //   },
      //   "Message+": {
      //     "Category": "Communication",
      //     "Raw": "com.verizon.messaging.vzmsgs",
      //     "App": "Message+",
      //     "Developer": "Verizon - VZ"
      //   },
      //   "Snapchat": {
      //     "Category": "Social",
      //     "Raw": "com.snapchat.android",
      //     "App": "Snapchat",
      //     "Developer": "Snapchat Inc"
      //   },
      //   "T-Mobile": {
      //     "Category": "Tools",
      //     "Raw": "com.tmobile.pr.mytmobile",
      //     "App": "T-Mobile",
      //     "Developer": "T-Mobile USA"
      //   },
      //   "Maps": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.google.android.apps.maps",
      //     "App": "Maps",
      //     "Developer": "Google Inc."
      //   },
      //   "Galaxy Apps": {
      //     "Category": "Personalization",
      //     "Raw": "com.sec.android.app.samsungapps",
      //     "App": "Galaxy Apps",
      //     "Developer": "Unknown"
      //   },
      //   "U by BB&T": {
      //     "Category": "Finance",
      //     "Raw": "com.bbt.myfi",
      //     "App": "U by BB&T",
      //     "Developer": "BBT Mobile"
      //   },
      //   "Multimedia UI Service Layer": {
      //     "Category": "Unknown",
      //     "Raw": "com.sec.android.mmapp",
      //     "App": "Multimedia UI Service Layer",
      //     "Developer": "Unknown"
      //   },
      //   "Samsung Link": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.pcw",
      //     "App": "Samsung Link",
      //     "Developer": "Unknown"
      //   },
      //   "Samsung account": {
      //     "Category": "Utility",
      //     "Raw": "com.osp.app.signin",
      //     "App": "Samsung account",
      //     "Developer": "Unknown"
      //   },
      //   "Google Play Newsstand": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.google.android.apps.magazines",
      //     "App": "Google Play Newsstand",
      //     "Developer": "Google Inc."
      //   },
      //   "SleepyTime Plus": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.icechen1.sleepytime.plus",
      //     "App": "SleepyTime Plus",
      //     "Developer": "Yu Chen Hou"
      //   },
      //   "Updater": {
      //     "Category": "Utility",
      //     "Raw": "com.htc.updater",
      //     "App": "Updater",
      //     "Developer": "Unknown"
      //   },
      //   "HTC DM": {
      //     "Category": "Utility",
      //     "Raw": "com.htc.omadm.settings",
      //     "App": "HTC DM",
      //     "Developer": "Unknown"
      //   },
      //   "RemoteLink": {
      //     "Category": "Productivity",
      //     "Raw": "com.gm.onstar.mobile.mylink",
      //     "App": "RemoteLink",
      //     "Developer": "OnStar, LLC"
      //   },
      //   "Videogame Sounds": {
      //     "Category": "Entertainment",
      //     "Raw": "com.brjps.sonidosdevideojuegos",
      //     "App": "Videogame Sounds",
      //     "Developer": "xrooney"
      //   },
      //   "Kik": {
      //     "Category": "Communication",
      //     "Raw": "kik.android",
      //     "App": "Kik",
      //     "Developer": "Kik Interactive"
      //   },
      //   "Story Album": {
      //     "Category": "Photography",
      //     "Raw": "com.samsung.android.app.episodes",
      //     "App": "Story Album",
      //     "Developer": "Unknown"
      //   },
      //   "Wi-Fi Direct": {
      //     "Category": "Communication",
      //     "Raw": "com.samsung.android.app.FileShareClient",
      //     "App": "Wi-Fi Direct",
      //     "Developer": "Unknown"
      //   },
      //   "SMS Rage Faces": {
      //     "Category": "Entertainment",
      //     "Raw": "com.toucheapps.smsragefaces",
      //     "App": "SMS Rage Faces",
      //     "Developer": "Touche Apps"
      //   },
      //   "Shazam": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.shazam.android",
      //     "App": "Shazam",
      //     "Developer": "Shazam Entertainment Limited"
      //   },
      //   "Visual Voicemail": {
      //     "Category": "Communication",
      //     "Raw": "com.att.mobile.android.vvm",
      //     "App": "Visual Voicemail",
      //     "Developer": "AT&T Services, Inc."
      //   },
      //   "Pandora": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.pandora.android",
      //     "App": "Pandora",
      //     "Developer": "Pandora"
      //   },
      //   "Money Manager": {
      //     "Category": "Finance",
      //     "Raw": "com.realbyteapps.moneymanagerfree",
      //     "App": "Money Manager",
      //     "Developer": "Realbyte Inc."
      //   },
      //   "LinkedIn": {
      //     "Category": "Social",
      //     "Raw": "com.linkedin.android",
      //     "App": "LinkedIn",
      //     "Developer": "LinkedIn"
      //   },
      //   "Pocket Beta": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.ideashower.readitlater.pro",
      //     "App": "Pocket Beta",
      //     "Developer": "Read It Later"
      //   },
      //   "Android Pay": {
      //     "Category": "Finance",
      //     "Raw": "com.google.android.apps.walletnfcrel",
      //     "App": "Android Pay",
      //     "Developer": "Google Inc."
      //   },
      //   "InstaRepost": {
      //     "Category": "Social",
      //     "Raw": "com.repost",
      //     "App": "InstaRepost",
      //     "Developer": "InstaRepost"
      //   },
      //   "VLC": {
      //     "Category": "Media & Video",
      //     "Raw": "org.videolan.vlc",
      //     "App": "VLC",
      //     "Developer": "Videolabs"
      //   },
      //   "car-net": {
      //     "Category": "Transportation",
      //     "Raw": "com.verizontelematics.vwcarnet",
      //     "App": "car-net",
      //     "Developer": "Volkswagen of America, Inc."
      //   },
      //   "Kaspersky Internet Security": {
      //     "Category": "Tools",
      //     "Raw": "com.kms.free",
      //     "App": "Kaspersky Internet Security",
      //     "Developer": "Kaspersky Lаb"
      //   },
      //   "Uber": {
      //     "Category": "Transportation",
      //     "Raw": "com.ubercab",
      //     "App": "Uber",
      //     "Developer": "Uber Technologies, Inc."
      //   },
      //   "AB POP!": {
      //     "Category": "Casual",
      //     "Raw": "com.rovio.ABstellapop",
      //     "App": "AB POP!",
      //     "Developer": "Rovio Entertainment Ltd."
      //   },
      //   "Mint": {
      //     "Category": "Finance",
      //     "Raw": "com.mint",
      //     "App": "Mint",
      //     "Developer": "Intuit Inc"
      //   },
      //   "Chase": {
      //     "Category": "Finance",
      //     "Raw": "com.chase.sig.android",
      //     "App": "Chase",
      //     "Developer": "JPMorgan Chase"
      //   },
      //   "GroupMe": {
      //     "Category": "Communication",
      //     "Raw": "com.groupme.android",
      //     "App": "GroupMe",
      //     "Developer": "groupme"
      //   },
      //   "News & Weather": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.google.android.apps.genie.geniewidget",
      //     "App": "News & Weather",
      //     "Developer": "Google Inc."
      //   },
      //   "CCleaner": {
      //     "Category": "Tools",
      //     "Raw": "com.piriform.ccleaner",
      //     "App": "CCleaner",
      //     "Developer": "Piriform"
      //   },
      //   "SunPass": {
      //     "Category": "Transportation",
      //     "Raw": "com.Sunpass.Sunpass_Android",
      //     "App": "SunPass",
      //     "Developer": "Florida's Turnpike Enterprise"
      //   },
      //   "Lyft": {
      //     "Category": "Transportation",
      //     "Raw": "me.lyft.android",
      //     "App": "Lyft",
      //     "Developer": "Lyft, Inc."
      //   },
      //   "Train Up": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.trainup",
      //     "App": "Train Up",
      //     "Developer": "Marketing @ Google"
      //   },
      //   "MyRadar": {
      //     "Category": "Weather",
      //     "Raw": "com.acmeaom.android.myradar",
      //     "App": "MyRadar",
      //     "Developer": "ACME AtronOmatic"
      //   },
      //   "WeatherBug Elite": {
      //     "Category": "Weather",
      //     "Raw": "com.aws.android.elite",
      //     "App": "WeatherBug Elite",
      //     "Developer": "Unknown"
      //   },
      //   "Spaces": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.social.spaces",
      //     "App": "Spaces",
      //     "Developer": "Google Inc."
      //   },
      //   "Google Text-to-speech Engine": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.tts",
      //     "App": "Google Text-to-speech Engine",
      //     "Developer": "Google Inc."
      //   },
      //   "Flipboard": {
      //     "Category": "News & Magazines",
      //     "Raw": "flipboard.app",
      //     "App": "Flipboard",
      //     "Developer": "Flipboard"
      //   },
      //   "Skype": {
      //     "Category": "Communication",
      //     "Raw": "com.skype.raider",
      //     "App": "Skype",
      //     "Developer": "Skype"
      //   },
      //   "Juice Jam": {
      //     "Category": "Puzzle",
      //     "Raw": "air.com.sgn.juicejam.gp",
      //     "App": "Juice Jam",
      //     "Developer": "SGN"
      //   },
      //   "Word Trek": {
      //     "Category": "Word",
      //     "Raw": "in.playsimple.word_up",
      //     "App": "Word Trek",
      //     "Developer": "PlaySimple Games"
      //   },
      //   "Google Play Games": {
      //     "Category": "Entertainment",
      //     "Raw": "com.google.android.play.games",
      //     "App": "Google Play Games",
      //     "Developer": "Google Inc."
      //   },
      //   "KeepSafe": {
      //     "Category": "Media & Video",
      //     "Raw": "com.kii.safe",
      //     "App": "KeepSafe",
      //     "Developer": "Keepsafe"
      //   },
      //   "Travel Channel": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.travelchannel.watcher",
      //     "App": "Travel Channel",
      //     "Developer": "Travel Channel, LLC"
      //   },
      //   "Hermit": {
      //     "Category": "Tools",
      //     "Raw": "com.chimbori.hermitcrab",
      //     "App": "Hermit",
      //     "Developer": "Chimbori"
      //   },
      //   "Zoe Video Editor": {
      //     "Category": "Media & Video",
      //     "Raw": "com.htc.zero",
      //     "App": "Zoe Video Editor",
      //     "Developer": "HTC Creative Labs"
      //   },
      //   "Samsung Milk Music": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.samsung.mdl.radio",
      //     "App": "Samsung Milk Music",
      //     "Developer": "Samsung Electronics Co. Ltd"
      //   },
      //   "Simple": {
      //     "Category": "Finance",
      //     "Raw": "com.banksimple",
      //     "App": "Simple",
      //     "Developer": "Simple Finance Technology Corp."
      //   },
      //   "Science Journal": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.forscience.whistlepunk",
      //     "App": "Science Journal",
      //     "Developer": "Marketing @ Google"
      //   },
      //   "Power": {
      //     "Category": "Utility",
      //     "Raw": "com.htc.htcpowermanager",
      //     "App": "Power",
      //     "Developer": "Unknown"
      //   },
      //   "Digg": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.diggreader",
      //     "App": "Digg",
      //     "Developer": "Digg"
      //   },
      //   "X1 Remote": {
      //     "Category": "Entertainment",
      //     "Raw": "com.xfinity.remote",
      //     "App": "X1 Remote",
      //     "Developer": "Comcast"
      //   },
      //   "Snapseed": {
      //     "Category": "Photography",
      //     "Raw": "com.niksoftware.snapseed",
      //     "App": "Snapseed",
      //     "Developer": "Google Inc."
      //   },
      //   "Adobe Acrobat": {
      //     "Category": "Productivity",
      //     "Raw": "com.adobe.reader",
      //     "App": "Adobe Acrobat",
      //     "Developer": "Adobe"
      //   },
      //   "Podbean": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.podbean.app.podcast",
      //     "App": "Podbean",
      //     "Developer": "Podbean Tech LLC"
      //   },
      //   "Scribd": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.scribd.app.reader0",
      //     "App": "Scribd",
      //     "Developer": "Scribd, Inc."
      //   },
      //   "PopupuiReceiver": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.android.app.popupuireceiver",
      //     "App": "PopupuiReceiver",
      //     "Developer": "Unknown"
      //   },
      //   "Quick connect": {
      //     "Category": "Utility",
      //     "Raw": "com.samsung.android.qconnect",
      //     "App": "Quick connect",
      //     "Developer": "Unknown"
      //   },
      //   "Cookie Jam": {
      //     "Category": "Puzzle",
      //     "Raw": "air.com.sgn.cookiejam.gp",
      //     "App": "Cookie Jam",
      //     "Developer": "SGN"
      //   },
      //   "myMetro": {
      //     "Category": "Tools",
      //     "Raw": "com.nuance.nmc.sihome.metropcs",
      //     "App": "myMetro",
      //     "Developer": "MetroPCS Wireless Inc."
      //   },
      //   "App Lock": {
      //     "Category": "Tools",
      //     "Raw": "com.getkeepsafe.applock",
      //     "App": "App Lock",
      //     "Developer": "Keepsafe"
      //   },
      //   "Square Cash": {
      //     "Category": "Finance",
      //     "Raw": "com.squareup.cash",
      //     "App": "Square Cash",
      //     "Developer": "Square, Inc."
      //   },
      //   "Q-dance Radio": {
      //     "Category": "Music & Audio",
      //     "Raw": "net.manageapps.app_qdance",
      //     "App": "Q-dance Radio",
      //     "Developer": "Q-dance"
      //   },
      //   "Avast Mobile Security": {
      //     "Category": "Tools",
      //     "Raw": "com.avast.android.mobilesecurity",
      //     "App": "Avast Mobile Security",
      //     "Developer": "AVAST Software"
      //   },
      //   "Castle of Illusion": {
      //     "Category": "Adventure",
      //     "Raw": "com.disney.castleofillusion_goo",
      //     "App": "Castle of Illusion",
      //     "Developer": "Disney"
      //   },
      //   "View video": {
      //     "Category": "Media & Video",
      //     "Raw": "com.htc.video",
      //     "App": "View video",
      //     "Developer": "HTC Corporation"
      //   },
      //   "Translate": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.translate",
      //     "App": "Translate",
      //     "Developer": "Google Inc."
      //   },
      //   "Amex Mobile": {
      //     "Category": "Finance",
      //     "Raw": "com.americanexpress.android.acctsvcs.us",
      //     "App": "Amex Mobile",
      //     "Developer": "American Express"
      //   },
      //   "Moto": {
      //     "Category": "Personalization",
      //     "Raw": "com.motorola.moto",
      //     "App": "Moto",
      //     "Developer": "Motorola Mobility LLC."
      //   },
      //   "People edge": {
      //     "Category": "Productivity",
      //     "Raw": "com.samsung.android.service.peoplestripe",
      //     "App": "People edge",
      //     "Developer": "GALAXY Labs"
      //   },
      //   "Active applications": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.android.app.taskmanager",
      //     "App": "Active applications",
      //     "Developer": "Unknown"
      //   },
      //   "Beep'nGo": {
      //     "Category": "Shopping",
      //     "Raw": "com.mobeam.beepngo",
      //     "App": "Beep'nGo",
      //     "Developer": "Mobeam Inc."
      //   },
      //   "MyFitnessPal": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.myfitnesspal.android",
      //     "App": "MyFitnessPal",
      //     "Developer": "MyFitnessPal, Inc."
      //   },
      //   "AliExpress": {
      //     "Category": "Shopping",
      //     "Raw": "com.alibaba.aliexpresshd",
      //     "App": "AliExpress",
      //     "Developer": "Alibaba.com Hong Kong Limited"
      //   },
      //   "Wish": {
      //     "Category": "Shopping",
      //     "Raw": "com.contextlogic.wish",
      //     "App": "Wish",
      //     "Developer": "Wish Inc."
      //   },
      //   "Amazon Shopping": {
      //     "Category": "Shopping",
      //     "Raw": "com.amazon.mShop.android.shopping",
      //     "App": "Amazon Shopping",
      //     "Developer": "Amazon Mobile LLC"
      //   },
      //   "myChevrolet": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.gm.chevrolet.nomad.ownership",
      //     "App": "myChevrolet",
      //     "Developer": "General Motors (GM)"
      //   },
      //   "Eat24 Yelp": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.eat24.app",
      //     "App": "Eat24 Yelp",
      //     "Developer": "EAT24"
      //   },
      //   "ESPN": {
      //     "Category": "Sports",
      //     "Raw": "com.espn.score_center",
      //     "App": "ESPN",
      //     "Developer": "ESPN Inc"
      //   },
      //   "Memo": {
      //     "Category": "Tools",
      //     "Raw": "com.samsung.android.app.memo",
      //     "App": "Memo",
      //     "Developer": "Unknown"
      //   },
      //   "Amazon Kindle": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.amazon.kindle",
      //     "App": "Amazon Kindle",
      //     "Developer": "Amazon Mobile LLC"
      //   },
      //   "Domino's": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.dominospizza",
      //     "App": "Domino's",
      //     "Developer": "Domino's Pizza LLC"
      //   },
      //   "ねこあつめ": {
      //     "Category": "Casual",
      //     "Raw": "jp.co.hit_point.nekoatsume",
      //     "App": "ねこあつめ",
      //     "Developer": "Hit-Point Co.,Ltd."
      //   },
      //   "Airbnb": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.airbnb.android",
      //     "App": "Airbnb",
      //     "Developer": "Airbnb, Inc"
      //   },
      //   "ADPMobile": {
      //     "Category": "Business",
      //     "Raw": "com.adpmobile.android",
      //     "App": "ADPMobile",
      //     "Developer": "ADP, LLC"
      //   },
      //   "Hancom Office Editor": {
      //     "Category": "Productivity",
      //     "Raw": "com.hancom.office.editor.hidden",
      //     "App": "Hancom Office Editor",
      //     "Developer": "Unknown"
      //   },
      //   "Telegram": {
      //     "Category": "Communication",
      //     "Raw": "org.telegram.messenger",
      //     "App": "Telegram",
      //     "Developer": "Telegram Messenger LLP"
      //   },
      //   "Vimeo": {
      //     "Category": "Entertainment",
      //     "Raw": "com.vimeo.android.videoapp",
      //     "App": "Vimeo",
      //     "Developer": "Vimeo Mobile"
      //   },
      //   "Glide": {
      //     "Category": "Communication",
      //     "Raw": "com.glidetalk.glideapp",
      //     "App": "Glide",
      //     "Developer": "Glide "
      //   },
      //   "Cloud": {
      //     "Category": "Productivity",
      //     "Raw": "com.vcast.mediamanager",
      //     "App": "Cloud",
      //     "Developer": "Verizon - VZ"
      //   },
      //   "Car": {
      //     "Category": "Tools",
      //     "Raw": "com.htc.AutoMotive",
      //     "App": "Car",
      //     "Developer": "HTC Corporation"
      //   },
      //   "ProtonMail": {
      //     "Category": "Communication",
      //     "Raw": "ch.protonmail.android",
      //     "App": "ProtonMail",
      //     "Developer": "ProtonMail"
      //   },
      //   "Sheets": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.apps.docs.editors.sheets",
      //     "App": "Sheets",
      //     "Developer": "Google Inc."
      //   },
      //   "Venmo": {
      //     "Category": "Finance",
      //     "Raw": "com.venmo",
      //     "App": "Venmo",
      //     "Developer": "PayPal, Inc."
      //   },
      //   "Washé": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.thewasheapp.washe",
      //     "App": "Washé",
      //     "Developer": "Execute Apps Ltd."
      //   },
      //   "Video Player": {
      //     "Category": "Media & Video",
      //     "Raw": "com.samsung.android.video",
      //     "App": "Video Player",
      //     "Developer": "Unknown"
      //   },
      //   "Phone Call Management": {
      //     "Category": "Communication",
      //     "Raw": "com.android.server.telecom",
      //     "App": "Phone Call Management",
      //     "Developer": "Unknown"
      //   },
      //   "FlashFire": {
      //     "Category": "Tools",
      //     "Raw": "eu.chainfire.flash",
      //     "App": "FlashFire",
      //     "Developer": "Chainfire"
      //   },
      //   "Tags": {
      //     "Category": "Utility",
      //     "Raw": "com.google.android.tag",
      //     "App": "Tags",
      //     "Developer": "Unknown"
      //   },
      //   "Nfc Service": {
      //     "Category": "Utility",
      //     "Raw": "com.android.nfc",
      //     "App": "Nfc Service",
      //     "Developer": "Unknown"
      //   },
      //   "Call Management": {
      //     "Category": "Communication",
      //     "Raw": "com.android.server.telecom",
      //     "App": "Call Management",
      //     "Developer": "Unknown"
      //   },
      //   "Android Auto": {
      //     "Category": "Transportation",
      //     "Raw": "com.google.android.projection.gearhead",
      //     "App": "Android Auto",
      //     "Developer": "Google Inc."
      //   },
      //   "Pokémon\nJukebox": {
      //     "Category": "Card",
      //     "Raw": "jp.pokemon.music",
      //     "App": "Pokémon\nJukebox",
      //     "Developer": "Unknown"
      //   },
      //   "My Account": {
      //     "Category": "Tools",
      //     "Raw": "com.comcast.cvs.android",
      //     "App": "My Account",
      //     "Developer": "Comcast Cable Corporation, LLC"
      //   },
      //   "Lookout": {
      //     "Category": "Tools",
      //     "Raw": "com.lookout",
      //     "App": "Lookout",
      //     "Developer": "Lookout Mobile Security"
      //   },
      //   "Reverse Lookup": {
      //     "Category": "Communication",
      //     "Raw": "com.esmobile.reverselookupfree",
      //     "App": "Reverse Lookup",
      //     "Developer": "Nomadic Ratio"
      //   },
      //   "YouMail": {
      //     "Category": "Communication",
      //     "Raw": "com.youmail.android.vvm",
      //     "App": "YouMail",
      //     "Developer": "YouMail, Inc"
      //   },
      //   "Charging Station": {
      //     "Category": "Entertainment",
      //     "Raw": "com.apriva.mobile.wallet.davebusters",
      //     "App": "Charging Station",
      //     "Developer": "Apriva"
      //   },
      //   "Enhanced features": {
      //     "Category": "Utility",
      //     "Raw": "com.samsung.android.coreapps",
      //     "App": "Enhanced features",
      //     "Developer": "Unknown"
      //   },
      //   "Starbucks": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.starbucks.mobilecard",
      //     "App": "Starbucks",
      //     "Developer": "Starbucks Coffee Company"
      //   },
      //   "Uber Partner": {
      //     "Category": "Transportation",
      //     "Raw": "com.ubercab.driver",
      //     "App": "Uber Partner",
      //     "Developer": "Uber Technologies, Inc."
      //   },
      //   "GEICO Mobile": {
      //     "Category": "Finance",
      //     "Raw": "com.geico.mobile",
      //     "App": "GEICO Mobile",
      //     "Developer": "GEICO Insurance"
      //   },
      //   "Sunrise": {
      //     "Category": "Productivity",
      //     "Raw": "am.sunrise.android.calendar",
      //     "App": "Sunrise",
      //     "Developer": "Unknown"
      //   },
      //   "Eventbrite": {
      //     "Category": "Entertainment",
      //     "Raw": "com.eventbrite.attendee",
      //     "App": "Eventbrite",
      //     "Developer": "Eventbrite"
      //   },
      //   "Today Calendar": {
      //     "Category": "Productivity",
      //     "Raw": "com.underwood.calendar_beta",
      //     "App": "Today Calendar",
      //     "Developer": "Jack Underwood"
      //   },
      //   "Authenticator": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.authenticator2",
      //     "App": "Authenticator",
      //     "Developer": "Google Inc."
      //   },
      //   "Video Calling": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.apps.tachyon",
      //     "App": "Video Calling",
      //     "Developer": "Google Inc."
      //   },
      //   "Pixlr": {
      //     "Category": "Photography",
      //     "Raw": "com.pixlr.express",
      //     "App": "Pixlr",
      //     "Developer": "Autodesk Inc."
      //   },
      //   "AZ Screen Recorder": {
      //     "Category": "Media & Video",
      //     "Raw": "com.hecorat.screenrecorder.free",
      //     "App": "AZ Screen Recorder",
      //     "Developer": "Hecorat"
      //   },
      //   "Dashlane": {
      //     "Category": "Productivity",
      //     "Raw": "com.dashlane",
      //     "App": "Dashlane",
      //     "Developer": "Dashlane"
      //   },
      //   "Speedtest": {
      //     "Category": "Tools",
      //     "Raw": "org.zwanoo.android.speedtest",
      //     "App": "Speedtest",
      //     "Developer": "Ookla"
      //   },
      //   "The Weather Channel": {
      //     "Category": "Weather",
      //     "Raw": "com.weather.Weather",
      //     "App": "The Weather Channel",
      //     "Developer": "The Weather Channel"
      //   },
      //   "Bluetooth Share": {
      //     "Category": "Utility",
      //     "Raw": "com.android.bluetooth",
      //     "App": "Bluetooth Share",
      //     "Developer": "Unknown"
      //   },
      //   "Setup Wizard": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.android.app.setupwizard",
      //     "App": "Setup Wizard",
      //     "Developer": "Unknown"
      //   },
      //   "Voice Mail": {
      //     "Category": "Communication",
      //     "Raw": "com.samsung.vvm",
      //     "App": "Voice Mail",
      //     "Developer": "Unknown"
      //   },
      //   "Classroom": {
      //     "Category": "Education",
      //     "Raw": "com.google.android.apps.classroom",
      //     "App": "Classroom",
      //     "Developer": "Google Inc."
      //   },
      //   "Print Spooler": {
      //     "Category": "Utility",
      //     "Raw": "com.android.printspooler",
      //     "App": "Print Spooler",
      //     "Developer": "Unknown"
      //   },
      //   "TWRP Manager": {
      //     "Category": "Tools",
      //     "Raw": "com.jmz.soft.twrpmanager",
      //     "App": "TWRP Manager",
      //     "Developer": "Jmz Software"
      //   },
      //   "SuperSU": {
      //     "Category": "Tools",
      //     "Raw": "eu.chainfire.supersu",
      //     "App": "SuperSU",
      //     "Developer": "Chainfire"
      //   },
      //   "Quick Reboot": {
      //     "Category": "Tools",
      //     "Raw": "phongit.quickreboot",
      //     "App": "Quick Reboot",
      //     "Developer": "PhongIT"
      //   },
      //   "Zipcar": {
      //     "Category": "Transportation",
      //     "Raw": "com.zc.android",
      //     "App": "Zipcar",
      //     "Developer": "Zipcar"
      //   },
      //   "Facer": {
      //     "Category": "Personalization",
      //     "Raw": "com.jeremysteckling.facerrel",
      //     "App": "Facer",
      //     "Developer": "Little Labs, Inc."
      //   },
      //   "S Note": {
      //     "Category": "Tools",
      //     "Raw": "com.sec.android.app.snotebook",
      //     "App": "S Note",
      //     "Developer": "Unknown"
      //   },
      //   "Messaging": {
      //     "Category": "Communication",
      //     "Raw": "com.android.mms",
      //     "App": "Messaging",
      //     "Developer": "Unknown"
      //   },
      //   "SleepyTime": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.icechen1.sleepytime",
      //     "App": "SleepyTime",
      //     "Developer": "Yu Chen Hou"
      //   },
      //   "reddit is fun": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.andrewshu.android.reddit",
      //     "App": "reddit is fun",
      //     "Developer": "TalkLittle"
      //   },
      //   "Tinder": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.tinder",
      //     "App": "Tinder",
      //     "Developer": "Tinder"
      //   },
      //   "Bumble": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.bumble.app",
      //     "App": "Bumble",
      //     "Developer": "Bumble Trading Inc"
      //   },
      //   "Kobo eBooks": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.kobobooks.android",
      //     "App": "Kobo eBooks",
      //     "Developer": "Kobo eBooks"
      //   },
      //   "Google On": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.apps.access.wifi.consumer",
      //     "App": "Google On",
      //     "Developer": "Google Inc."
      //   },
      //   "Hornet": {
      //     "Category": "Social",
      //     "Raw": "com.hornet.android",
      //     "App": "Hornet",
      //     "Developer": "Hornet Networks Limited"
      //   },
      //   "Badoo": {
      //     "Category": "Social",
      //     "Raw": "com.badoo.mobile",
      //     "App": "Badoo",
      //     "Developer": "Badoo"
      //   },
      //   "Hangouts Dialer": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.apps.hangoutsdialer",
      //     "App": "Hangouts Dialer",
      //     "Developer": "Google Inc."
      //   },
      //   "Poweramp": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.maxmpz.audioplayer",
      //     "App": "Poweramp",
      //     "Developer": "Max MP"
      //   },
      //   "edX": {
      //     "Category": "Education",
      //     "Raw": "org.edx.mobile",
      //     "App": "edX",
      //     "Developer": "edX"
      //   },
      //   "CBORD Mobile ID": {
      //     "Category": "Tools",
      //     "Raw": "com.cbord.csg.mobileid",
      //     "App": "CBORD Mobile ID",
      //     "Developer": "The CBORD Group, Inc"
      //   },
      //   "Instacart": {
      //     "Category": "Shopping",
      //     "Raw": "com.instacart.client",
      //     "App": "Instacart",
      //     "Developer": "Instacart"
      //   },
      //   "S Health": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.sec.android.app.shealth",
      //     "App": "S Health",
      //     "Developer": "Samsung Electronics Co.,  Ltd."
      //   },
      //   "TuneIn Radio": {
      //     "Category": "Music & Audio",
      //     "Raw": "tunein.player",
      //     "App": "TuneIn Radio",
      //     "Developer": "TuneIn Inc"
      //   },
      //   "Nova Launcher": {
      //     "Category": "Personalization",
      //     "Raw": "com.teslacoilsw.launcher",
      //     "App": "Nova Launcher",
      //     "Developer": "TeslaCoil Software"
      //   },
      //   "Fit": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.google.android.apps.fitness",
      //     "App": "Fit",
      //     "Developer": "Google Inc."
      //   },
      //   "BofA": {
      //     "Category": "Finance",
      //     "Raw": "com.infonow.bofa",
      //     "App": "BofA",
      //     "Developer": "Bank of America"
      //   },
      //   "MTG Familiar": {
      //     "Category": "Tools",
      //     "Raw": "com.gelakinetic.mtgfam",
      //     "App": "MTG Familiar",
      //     "Developer": "gelakinetic"
      //   },
      //   "Coffee Meets Bagel": {
      //     "Category": "Social",
      //     "Raw": "com.coffeemeetsbagel",
      //     "App": "Coffee Meets Bagel",
      //     "Developer": "Coffee Meets Bagel"
      //   },
      //   "Briefing": {
      //     "Category": "News & Magazines",
      //     "Raw": "flipboard.boxer.app",
      //     "App": "Briefing",
      //     "Developer": "Flipboard"
      //   },
      //   "Slack": {
      //     "Category": "Business",
      //     "Raw": "com.Slack",
      //     "App": "Slack",
      //     "Developer": "Slack Technologies Inc."
      //   },
      //   "Guidebook": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.guidebook.android",
      //     "App": "Guidebook",
      //     "Developer": "Guidebook Inc"
      //   },
      //   "hello": {
      //     "Category": "Unknown",
      //     "Raw": "com.hellotext.hello",
      //     "App": "hello",
      //     "Developer": "Unknown"
      //   },
      //   "Firefox": {
      //     "Category": "Communication",
      //     "Raw": "org.mozilla.firefox",
      //     "App": "Firefox",
      //     "Developer": "Mozilla"
      //   },
      //   "Dunkin’": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.skcc.corfire.dd",
      //     "App": "Dunkin’",
      //     "Developer": "Unknown"
      //   },
      //   "Malwarebytes Anti-Malware": {
      //     "Category": "Tools",
      //     "Raw": "org.malwarebytes.antimalware",
      //     "App": "Malwarebytes Anti-Malware",
      //     "Developer": "Malwarebytes"
      //   },
      //   "Remote Link": {
      //     "Category": "Tools",
      //     "Raw": "com.asus.remotelink.full",
      //     "App": "Remote Link",
      //     "Developer": "ZenUI, ASUS HIT TEAM"
      //   },
      //   "Archos Video": {
      //     "Category": "Media & Video",
      //     "Raw": "com.archos.mediacenter.video",
      //     "App": "Archos Video",
      //     "Developer": "Archos S.A."
      //   },
      //   "Poweramp Full Version Unlocker": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.maxmpz.audioplayer.unlock",
      //     "App": "Poweramp Full Version Unlocker",
      //     "Developer": "Max MP"
      //   },
      //   "Relay": {
      //     "Category": "News & Magazines",
      //     "Raw": "free.reddit.news",
      //     "App": "Relay",
      //     "Developer": "DBrady"
      //   },
      //   "BBC News": {
      //     "Category": "News & Magazines",
      //     "Raw": "bbc.mobile.news.ww",
      //     "App": "BBC News",
      //     "Developer": "BBC Worldwide (Ltd)"
      //   },
      //   "NPR News": {
      //     "Category": "News & Magazines",
      //     "Raw": "org.npr.android.news",
      //     "App": "NPR News",
      //     "Developer": "NPR"
      //   },
      //   "Stitcher": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.stitcher.app",
      //     "App": "Stitcher",
      //     "Developer": "Stitcher, Inc"
      //   },
      //   "Scruff": {
      //     "Category": "Social",
      //     "Raw": "com.appspot.scruffapp",
      //     "App": "Scruff",
      //     "Developer": "Perry Street Software, Inc."
      //   },
      //   "Oculus": {
      //     "Category": "Utility",
      //     "Raw": "com.oculus.horizon",
      //     "App": "Oculus",
      //     "Developer": "Unknown"
      //   },
      //   "DealNews": {
      //     "Category": "Shopping",
      //     "Raw": "com.dealnews.android.ui",
      //     "App": "DealNews",
      //     "Developer": "dealnews.com, Inc"
      //   },
      //   "tTorrent (no ads)": {
      //     "Category": "Media & Video",
      //     "Raw": "hu.tagsoft.ttorrent.noads",
      //     "App": "tTorrent (no ads)",
      //     "Developer": "tagsoft"
      //   },
      //   "StockTwits": {
      //     "Category": "Finance",
      //     "Raw": "org.stocktwits.android.activity",
      //     "App": "StockTwits",
      //     "Developer": "StockTwits, Inc."
      //   },
      //   "Timely": {
      //     "Category": "Lifestyle",
      //     "Raw": "ch.bitspin.timely",
      //     "App": "Timely",
      //     "Developer": "Bitspin"
      //   },
      //   "I Can't Wake Up! Free": {
      //     "Category": "Tools",
      //     "Raw": "com.kog.alarmclock",
      //     "App": "I Can't Wake Up! Free",
      //     "Developer": "Kog Creations"
      //   },
      //   "Samsung text-to-speech engine": {
      //     "Category": "Utility",
      //     "Raw": "com.samsung.SMT",
      //     "App": "Samsung text-to-speech engine",
      //     "Developer": "Unknown"
      //   },
      //   "Hound": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.hound.android.app",
      //     "App": "Hound",
      //     "Developer": "SoundHound Inc."
      //   },
      //   "NYTimes": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.nytimes.android",
      //     "App": "NYTimes",
      //     "Developer": "The New York Times Company"
      //   },
      //   "WeChat": {
      //     "Category": "Communication",
      //     "Raw": "com.tencent.mm",
      //     "App": "WeChat",
      //     "Developer": "WeChat Tencent"
      //   },
      //   "CM Security": {
      //     "Category": "Tools",
      //     "Raw": "com.cleanmaster.security",
      //     "App": "CM Security",
      //     "Developer": "Cheetah Mobile (AppLock & AntiVirus)"
      //   },
      //   "GrooVe IP Pro": {
      //     "Category": "Communication",
      //     "Raw": "com.gvoip",
      //     "App": "GrooVe IP Pro",
      //     "Developer": "snrb Labs"
      //   },
      //   "Gear Fit Manager": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.samsung.android.wms",
      //     "App": "Gear Fit Manager",
      //     "Developer": "Unknown"
      //   },
      //   "QR Droid": {
      //     "Category": "Productivity",
      //     "Raw": "la.droid.qr",
      //     "App": "QR Droid",
      //     "Developer": "DroidLa"
      //   },
      //   "CamScanner": {
      //     "Category": "Productivity",
      //     "Raw": "com.intsig.camscanner",
      //     "App": "CamScanner",
      //     "Developer": "INTSIG Information Co.,Ltd"
      //   },
      //   "ColorNote": {
      //     "Category": "Productivity",
      //     "Raw": "com.socialnmobile.dictapps.notepad.color.note",
      //     "App": "ColorNote",
      //     "Developer": "Notes"
      //   },
      //   "Clean Master": {
      //     "Category": "Tools",
      //     "Raw": "com.cleanmaster.mguard",
      //     "App": "Clean Master",
      //     "Developer": "Cheetah Mobile"
      //   },
      //   "Security policy updates": {
      //     "Category": "Productivity",
      //     "Raw": "com.policydm",
      //     "App": "Security policy updates",
      //     "Developer": "Samsung Electronics Co.,  Ltd."
      //   },
      //   "Software Update": {
      //     "Category": "Utility",
      //     "Raw": "com.wssyncmldm",
      //     "App": "Software Update",
      //     "Developer": "Unknown"
      //   },
      //   "CamScanner Full Version": {
      //     "Category": "Productivity",
      //     "Raw": "com.intsig.lic.camscanner",
      //     "App": "CamScanner Full Version",
      //     "Developer": "INTSIG"
      //   },
      //   "Restaurant.com": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.restaurant.mobile",
      //     "App": "Restaurant.com",
      //     "Developer": "Restaurant.com"
      //   },
      //   "Seeking Alpha": {
      //     "Category": "Finance",
      //     "Raw": "com.seekingalpha.webwrapper",
      //     "App": "Seeking Alpha",
      //     "Developer": "SeekingAlpha"
      //   },
      //   "Lumosity": {
      //     "Category": "Education",
      //     "Raw": "com.lumoslabs.lumosity",
      //     "App": "Lumosity",
      //     "Developer": "Lumos Labs, Inc."
      //   },
      //   "IBM Verse": {
      //     "Category": "Business",
      //     "Raw": "com.lotus.sync.traveler",
      //     "App": "IBM Verse",
      //     "Developer": "IBM Collaboration Solutions"
      //   },
      //   "Fasten": {
      //     "Category": "Transportation",
      //     "Raw": "com.fasten",
      //     "App": "Fasten",
      //     "Developer": "Fasten Inc."
      //   },
      //   "Yelp": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.yelp.android",
      //     "App": "Yelp",
      //     "Developer": "Yelp, Inc"
      //   },
      //   "RealCalc": {
      //     "Category": "Productivity",
      //     "Raw": "uk.co.nickfines.RealCalcPlus",
      //     "App": "RealCalc",
      //     "Developer": "Quartic Software"
      //   },
      //   "AirDroid": {
      //     "Category": "Tools",
      //     "Raw": "com.sand.airdroid",
      //     "App": "AirDroid",
      //     "Developer": "SAND STUDIO"
      //   },
      //   "DCU Mobile Banking": {
      //     "Category": "Finance",
      //     "Raw": "com.Vertifi.Mobile.P211391825",
      //     "App": "DCU Mobile Banking",
      //     "Developer": "Vertifi Software"
      //   },
      //   "Nglish Translator": {
      //     "Category": "Education",
      //     "Raw": "com.nglish.spanish.english.translator",
      //     "App": "Nglish Translator",
      //     "Developer": "Merriam-Webster Inc."
      //   },
      //   "TinyScanner": {
      //     "Category": "Business",
      //     "Raw": "com.appxy.tinyscanner",
      //     "App": "TinyScanner",
      //     "Developer": "Appxy"
      //   },
      //   "Prep4GRE": {
      //     "Category": "Education",
      //     "Raw": "com.LTGExamPracticePlatform.Prep4GRE",
      //     "App": "Prep4GRE",
      //     "Developer": "LTG Exam Prep Platform, Inc."
      //   },
      //   "NextRadio": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.nextradioapp.nextradio",
      //     "App": "NextRadio",
      //     "Developer": "Next Radio, LLC"
      //   },
      //   "Samsung keyboard": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.android.inputmethod",
      //     "App": "Samsung keyboard",
      //     "Developer": "Unknown"
      //   },
      //   "The Weather Channel for Samsung": {
      //     "Category": "Weather",
      //     "Raw": "com.weather.samsung",
      //     "App": "The Weather Channel for Samsung",
      //     "Developer": "Unknown"
      //   },
      //   "Crackle": {
      //     "Category": "Entertainment",
      //     "Raw": "com.gotv.crackle.handset",
      //     "App": "Crackle",
      //     "Developer": "Crackle"
      //   },
      //   "Dictionary": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.dictionary.paid",
      //     "App": "Dictionary",
      //     "Developer": "Dictionary.com, LLC"
      //   },
      //   "Tales of Link": {
      //     "Category": "Role Playing",
      //     "Raw": "com.bandainamcoent.tolink_ww",
      //     "App": "Tales of Link",
      //     "Developer": "BANDAI NAMCO Entertainment Inc."
      //   },
      //   "Transit": {
      //     "Category": "Transportation",
      //     "Raw": "com.thetransitapp.droid",
      //     "App": "Transit",
      //     "Developer": "Transit App, Inc."
      //   },
      //   "Intercom": {
      //     "Category": "Communication",
      //     "Raw": "io.intercom.android",
      //     "App": "Intercom",
      //     "Developer": "Intercom, Inc"
      //   },
      //   "Nest": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.nest.android",
      //     "App": "Nest",
      //     "Developer": "Nest Labs, Inc."
      //   },
      //   "Medium": {
      //     "Category": "Social",
      //     "Raw": "com.medium.reader",
      //     "App": "Medium",
      //     "Developer": "A Medium Corporation"
      //   },
      //   "App of the Night": {
      //     "Category": "Entertainment",
      //     "Raw": "com.appturbo.appofthenight",
      //     "App": "App of the Night",
      //     "Developer": "Appturbo"
      //   },
      //   "Smart Switch": {
      //     "Category": "Tools",
      //     "Raw": "com.sec.android.easyMover",
      //     "App": "Smart Switch",
      //     "Developer": "Samsung Electronics Co.,  Ltd."
      //   },
      //   "Smart Manager": {
      //     "Category": "Utility",
      //     "Raw": "com.samsung.android.sm",
      //     "App": "Smart Manager",
      //     "Developer": "Unknown"
      //   },
      //   "Help": {
      //     "Category": "Utility",
      //     "Raw": "com.samsung.helphub",
      //     "App": "Help",
      //     "Developer": "Unknown"
      //   },
      //   "CPU-Z": {
      //     "Category": "Tools",
      //     "Raw": "com.cpuid.cpu_z",
      //     "App": "CPU-Z",
      //     "Developer": "CPUID"
      //   },
      //   "Currency": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.xe.currency",
      //     "App": "Currency",
      //     "Developer": "XE.com Inc."
      //   },
      //   "POLARIS Office 5": {
      //     "Category": "Productivity",
      //     "Raw": "com.infraware.polarisoffice5",
      //     "App": "POLARIS Office 5",
      //     "Developer": "Unknown"
      //   },
      //   "AppDialer": {
      //     "Category": "Productivity",
      //     "Raw": "name.pilgr.appdialer",
      //     "App": "AppDialer",
      //     "Developer": "Aleksey Masny"
      //   },
      //   "Orbot": {
      //     "Category": "Communication",
      //     "Raw": "org.torproject.android",
      //     "App": "Orbot",
      //     "Developer": "The Tor Project"
      //   },
      //   "VpnDialogs": {
      //     "Category": "Utility",
      //     "Raw": "com.android.vpndialogs",
      //     "App": "VpnDialogs",
      //     "Developer": "Unknown"
      //   },
      //   "Wunderlist": {
      //     "Category": "Productivity",
      //     "Raw": "com.wunderkinder.wunderlistandroid",
      //     "App": "Wunderlist",
      //     "Developer": "6 Wunderkinder GmbH"
      //   },
      //   "Usage Manager": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.att.devicehealthshortcut",
      //     "App": "Usage Manager",
      //     "Developer": "Unknown"
      //   },
      //   "Task manager": {
      //     "Category": "Utility",
      //     "Raw": "com.sec.android.app.controlpanel",
      //     "App": "Task manager",
      //     "Developer": "Unknown"
      //   },
      //   "BackBeat FIT": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.plantronics.wfu.genesis",
      //     "App": "BackBeat FIT",
      //     "Developer": "Plantronics Inc."
      //   },
      //   "Miitomo": {
      //     "Category": "Social",
      //     "Raw": "com.nintendo.zaaa",
      //     "App": "Miitomo",
      //     "Developer": "Nintendo Co., Ltd."
      //   },
      //   "RSA SecurID": {
      //     "Category": "Communication",
      //     "Raw": "com.rsa.securidapp",
      //     "App": "RSA SecurID",
      //     "Developer": "RSA, The Security Division of EMC"
      //   },
      //   "Okta Verify": {
      //     "Category": "Productivity",
      //     "Raw": "com.okta.android.auth",
      //     "App": "Okta Verify",
      //     "Developer": "Okta Inc."
      //   },
      //   "Firecracker": {
      //     "Category": "Education",
      //     "Raw": "me.firecracker.dailyreview",
      //     "App": "Firecracker",
      //     "Developer": "Firecracker Inc."
      //   },
      //   "Beta": {
      //     "Category": "Unknown",
      //     "Raw": "io.crash.air",
      //     "App": "Beta",
      //     "Developer": "Unknown"
      //   },
      //   "Key Ring": {
      //     "Category": "Shopping",
      //     "Raw": "com.froogloid.kring.google.zxing.client.android",
      //     "App": "Key Ring",
      //     "Developer": "Mobestream Media"
      //   },
      //   "Agent": {
      //     "Category": "Business",
      //     "Raw": "com.airwatch.androidagent",
      //     "App": "Agent",
      //     "Developer": "AirWatch"
      //   },
      //   "WiFi": {
      //     "Category": "Communication",
      //     "Raw": "com.comcast.hsf",
      //     "App": "WiFi",
      //     "Developer": "Comcast Cable Corporation, LLC"
      //   },
      //   "OverDrive": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.overdrive.mobile.android.mediaconsole",
      //     "App": "OverDrive",
      //     "Developer": "OverDrive, Inc."
      //   },
      //   "BlueMail": {
      //     "Category": "Productivity",
      //     "Raw": "me.bluemail.mail",
      //     "App": "BlueMail",
      //     "Developer": "Blue Mail Inc."
      //   },
      //   "Merrill Edge": {
      //     "Category": "Finance",
      //     "Raw": "com.ml.mobile.edge",
      //     "App": "Merrill Edge",
      //     "Developer": "Merrill Lynch, Pierce, Fenner, and Smith Inc."
      //   },
      //   "Trebuchet": {
      //     "Category": "Business",
      //     "Raw": "com.cyanogenmod.trebuchet",
      //     "App": "Trebuchet",
      //     "Developer": "Unknown"
      //   },
      //   "WeatherBug": {
      //     "Category": "Weather",
      //     "Raw": "com.aws.android",
      //     "App": "WeatherBug",
      //     "Developer": "Earth Networks"
      //   },
      //   "Software update": {
      //     "Category": "Utility",
      //     "Raw": "com.wssyncmldm",
      //     "App": "Software update",
      //     "Developer": "Unknown"
      //   },
      //   "CyanogenMod Resolver": {
      //     "Category": "Utility",
      //     "Raw": "org.cyanogenmod.resolver",
      //     "App": "CyanogenMod Resolver",
      //     "Developer": "Unknown"
      //   },
      //   "Aldiko": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.aldiko.android",
      //     "App": "Aldiko",
      //     "Developer": "Aldiko Limited"
      //   },
      //   "Texture": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.nim.discovery",
      //     "App": "Texture",
      //     "Developer": "Next Issue Media"
      //   },
      //   "Universal switch": {
      //     "Category": "Utility",
      //     "Raw": "com.samsung.android.universalswitch",
      //     "App": "Universal switch",
      //     "Developer": "Unknown"
      //   },
      //   "Oodles Books": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.oodles.download.free.ebooks.reader",
      //     "App": "Oodles Books",
      //     "Developer": "Oodles"
      //   },
      //   "20 Minute Meals": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.zolmo.twentymm",
      //     "App": "20 Minute Meals",
      //     "Developer": "Zolmo"
      //   },
      //   "Campus": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.google.corp.bizapps.rews.campus.android",
      //     "App": "Campus",
      //     "Developer": "Google Inc."
      //   },
      //   "GBus": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.google.android.apps.thehub",
      //     "App": "GBus",
      //     "Developer": "Google Inc."
      //   },
      //   "YT Kids": {
      //     "Category": "Entertainment",
      //     "Raw": "com.google.android.apps.youtube.kids",
      //     "App": "YT Kids",
      //     "Developer": "Google Inc."
      //   },
      //   "T-Mobile My Account": {
      //     "Category": "Tools",
      //     "Raw": "com.tmobile.pr.mytmobile",
      //     "App": "T-Mobile My Account",
      //     "Developer": "T-Mobile USA"
      //   },
      //   "Call": {
      //     "Category": "Communication",
      //     "Raw": "com.lge.ltecall",
      //     "App": "Call",
      //     "Developer": "Unknown"
      //   },
      //   "Shutdown Monitor": {
      //     "Category": "Utility",
      //     "Raw": "com.lge.shutdownmonitor",
      //     "App": "Shutdown Monitor",
      //     "Developer": "Unknown"
      //   },
      //   "Whitepages ID": {
      //     "Category": "Communication",
      //     "Raw": "com.webascender.callerid",
      //     "App": "Whitepages ID",
      //     "Developer": "Hiya"
      //   },
      //   "360 Security Lite": {
      //     "Category": "Tools",
      //     "Raw": "com.qihoo.security.lite",
      //     "App": "360 Security Lite",
      //     "Developer": "360 Mobile Security Limited"
      //   },
      //   "reddit is fun golden platinum": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.andrewshu.android.redditdonation",
      //     "App": "reddit is fun golden platinum",
      //     "Developer": "TalkLittle"
      //   },
      //   "Action Launcher 3": {
      //     "Category": "Personalization",
      //     "Raw": "com.actionlauncher.playstore",
      //     "App": "Action Launcher 3",
      //     "Developer": "Chris Lacy"
      //   },
      //   "Five Guys": {
      //     "Category": "Shopping",
      //     "Raw": "com.fiveguys.olo.android",
      //     "App": "Five Guys",
      //     "Developer": "Five Guys"
      //   },
      //   "Todoist": {
      //     "Category": "Productivity",
      //     "Raw": "com.todoist",
      //     "App": "Todoist",
      //     "Developer": "Doist"
      //   },
      //   "Pushbullet": {
      //     "Category": "Productivity",
      //     "Raw": "com.pushbullet.android",
      //     "App": "Pushbullet",
      //     "Developer": "Pushbullet"
      //   },
      //   "Divide by Sheep": {
      //     "Category": "Puzzle",
      //     "Raw": "air.com.tinybuildgames.sheep",
      //     "App": "Divide by Sheep",
      //     "Developer": "tinyBuild"
      //   },
      //   "Twitch": {
      //     "Category": "Entertainment",
      //     "Raw": "tv.twitch.android.app",
      //     "App": "Twitch",
      //     "Developer": "Twitch Interactive, Inc."
      //   },
      //   "Google Play Movies & TV": {
      //     "Category": "Media & Video",
      //     "Raw": "com.google.android.videos",
      //     "App": "Google Play Movies & TV",
      //     "Developer": "Google Inc."
      //   },
      //   "Hue": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.philips.lighting.hue",
      //     "App": "Hue",
      //     "Developer": "Philips Consumer Lifestyle"
      //   },
      //   "com.lge.upsell": {
      //     "Category": "Unknown",
      //     "Raw": "com.lge.upsell",
      //     "App": "com.lge.upsell",
      //     "Developer": "Unknown"
      //   },
      //   "Moodle Mobile": {
      //     "Category": "Education",
      //     "Raw": "com.moodle.moodlemobile",
      //     "App": "Moodle Mobile",
      //     "Developer": "Moodle Pty Ltd."
      //   },
      //   "Fitbit": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.fitbit.FitbitMobile",
      //     "App": "Fitbit",
      //     "Developer": "Fitbit, Inc."
      //   },
      //   "Sprint Family Locator": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.locationlabs.finder.sprint",
      //     "App": "Sprint Family Locator",
      //     "Developer": "Safely"
      //   },
      //   "Swipetimes": {
      //     "Category": "Productivity",
      //     "Raw": "lc.st.free",
      //     "App": "Swipetimes",
      //     "Developer": "Leon Chiver"
      //   },
      //   "MetroWEB": {
      //     "Category": "Communication",
      //     "Raw": "com.android.browser",
      //     "App": "MetroWEB",
      //     "Developer": "Unknown"
      //   },
      //   "Viggle": {
      //     "Category": "Entertainment",
      //     "Raw": "com.functionx.viggle",
      //     "App": "Viggle",
      //     "Developer": "Viggle"
      //   },
      //   "GO Weather EX": {
      //     "Category": "Weather",
      //     "Raw": "com.gau.go.launcherex.gowidget.weatherwidget",
      //     "App": "GO Weather EX",
      //     "Developer": "GO Dev Team ^_^"
      //   },
      //   "IF": {
      //     "Category": "Productivity",
      //     "Raw": "com.ifttt.ifttt",
      //     "App": "IF",
      //     "Developer": "IFTTT"
      //   },
      //   "drupe": {
      //     "Category": "Communication",
      //     "Raw": "mobi.drupe.app",
      //     "App": "drupe",
      //     "Developer": "drupe - Contacts Your Way"
      //   },
      //   "Piano Tiles 2": {
      //     "Category": "Arcade",
      //     "Raw": "com.cmplay.tiles2",
      //     "App": "Piano Tiles 2",
      //     "Developer": "Clean Master Games"
      //   },
      //   "Kids A-Z": {
      //     "Category": "Education",
      //     "Raw": "com.learninga_z.onyourown",
      //     "App": "Kids A-Z",
      //     "Developer": "Learning A-Z"
      //   },
      //   "NCP Mobile": {
      //     "Category": "Shopping",
      //     "Raw": "com.ncp.ncpmobile",
      //     "App": "NCP Mobile",
      //     "Developer": "National Consumer Panel"
      //   },
      //   "Product Diary": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.getmobee.diary",
      //     "App": "Product Diary",
      //     "Developer": "Nielsen"
      //   },
      //   "Jiffy": {
      //     "Category": "Productivity",
      //     "Raw": "com.nordicusability.jiffy",
      //     "App": "Jiffy",
      //     "Developer": "Nordic Usability GmbH"
      //   },
      //   "Weather Timeline": {
      //     "Category": "Weather",
      //     "Raw": "com.samruston.weather",
      //     "App": "Weather Timeline",
      //     "Developer": "Sam Ruston"
      //   },
      //   "Internet": {
      //     "Category": "Communication",
      //     "Raw": "com.sec.android.app.sbrowser",
      //     "App": "Internet",
      //     "Developer": "Samsung Electronics Co.,  Ltd."
      //   },
      //   "AppSales": {
      //     "Category": "Shopping",
      //     "Raw": "net.tsapps.appsales",
      //     "App": "AppSales",
      //     "Developer": "ts-apps"
      //   },
      //   "StarWarsMicroFighters": {
      //     "Category": "Arcade",
      //     "Raw": "com.wb.amzn.starwars.microfighters",
      //     "App": "StarWarsMicroFighters",
      //     "Developer": "arner Bros. International Enterprises`"
      //   },
      //   "SwiftOpen": {
      //     "Category": "Personalization",
      //     "Raw": "apps.ipsofacto.swiftopen",
      //     "App": "SwiftOpen",
      //     "Developer": "Unknown"
      //   },
      //   "GO SMS Pro": {
      //     "Category": "Communication",
      //     "Raw": "com.jb.gosms",
      //     "App": "GO SMS Pro",
      //     "Developer": "GO Dev Team"
      //   },
      //   "AntennaPod": {
      //     "Category": "Media & Video",
      //     "Raw": "de.danoeh.antennapod",
      //     "App": "AntennaPod",
      //     "Developer": "AntennaPod"
      //   },
      //   "Sprint - Featured Apps": {
      //     "Category": "Utility",
      //     "Raw": "com.sprint.w.v8",
      //     "App": "Sprint - Featured Apps",
      //     "Developer": "Unknown"
      //   },
      //   "Reddit": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.reddit.frontpage",
      //     "App": "Reddit",
      //     "Developer": "reddit Inc."
      //   },
      //   "DoggCatcher": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.snoggdoggler.android.applications.doggcatcher.v1_0",
      //     "App": "DoggCatcher",
      //     "Developer": "DoggCatcher"
      //   },
      //   "Drudge Report": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.iavian.dreport",
      //     "App": "Drudge Report",
      //     "Developer": "iAvian Technologies"
      //   },
      //   "Appstore": {
      //     "Category": "Utility",
      //     "Raw": "com.amazon.venezia",
      //     "App": "Appstore",
      //     "Developer": "Unknown"
      //   },
      //   "SMS Backup & Restore": {
      //     "Category": "Tools",
      //     "Raw": "com.riteshsahu.SMSBackupRestore",
      //     "App": "SMS Backup & Restore",
      //     "Developer": "Carbonite"
      //   },
      //   "Swarm": {
      //     "Category": "Social",
      //     "Raw": "com.foursquare.robin",
      //     "App": "Swarm",
      //     "Developer": "Foursquare"
      //   },
      //   "SiriusXM": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.sirius",
      //     "App": "SiriusXM",
      //     "Developer": "Sirius XM Radio Inc"
      //   },
      //   "Project Fi": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.tycho",
      //     "App": "Project Fi",
      //     "Developer": "Google Inc."
      //   },
      //   "GO Keyboard": {
      //     "Category": "Tools",
      //     "Raw": "com.jb.emoji.gokeyboard",
      //     "App": "GO Keyboard",
      //     "Developer": "GO Keyboard Dev Team"
      //   },
      //   "Vine": {
      //     "Category": "Social",
      //     "Raw": "co.vine.android",
      //     "App": "Vine",
      //     "Developer": "Vine Labs"
      //   },
      //   "TeamSnap": {
      //     "Category": "Sports",
      //     "Raw": "com.teamsnap.teamsnap",
      //     "App": "TeamSnap",
      //     "Developer": "TeamSnap, Inc."
      //   },
      //   "Todo.txt": {
      //     "Category": "Productivity",
      //     "Raw": "com.todotxt.todotxttouch",
      //     "App": "Todo.txt",
      //     "Developer": "Gina Trapani"
      //   },
      //   "metroZONE": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.mobileposse.client",
      //     "App": "metroZONE",
      //     "Developer": "Mobile Posse"
      //   },
      //   "Material Player": {
      //     "Category": "Music & Audio",
      //     "Raw": "de.ph1b.audiobook",
      //     "App": "Material Player",
      //     "Developer": "Paul Woitaschek"
      //   },
      //   "Evernote": {
      //     "Category": "Productivity",
      //     "Raw": "com.evernote",
      //     "App": "Evernote",
      //     "Developer": "Evernote Corporation"
      //   },
      //   "Amazon Music": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.amazon.mp3",
      //     "App": "Amazon Music",
      //     "Developer": "Amazon Mobile LLC"
      //   },
      //   "OneNote": {
      //     "Category": "Productivity",
      //     "Raw": "com.microsoft.office.onenote",
      //     "App": "OneNote",
      //     "Developer": "Microsoft Corporation"
      //   },
      //   "Kairo Club": {
      //     "Category": "Casual",
      //     "Raw": "net.kairosoft.android.kairoclub",
      //     "App": "Kairo Club",
      //     "Developer": "Kairosoft Co.,Ltd"
      //   },
      //   "Genisys": {
      //     "Category": "Finance",
      //     "Raw": "org.genisyscu.mobile",
      //     "App": "Genisys",
      //     "Developer": "Genisys Credit Union"
      //   },
      //   "Photo Editor": {
      //     "Category": "Photography",
      //     "Raw": "com.sec.android.mimage.photoretouching",
      //     "App": "Photo Editor",
      //     "Developer": "Unknown"
      //   },
      //   "Ibotta": {
      //     "Category": "Shopping",
      //     "Raw": "com.ibotta.android",
      //     "App": "Ibotta",
      //     "Developer": "Ibotta"
      //   },
      //   "Walmart": {
      //     "Category": "Shopping",
      //     "Raw": "com.walmart.android",
      //     "App": "Walmart",
      //     "Developer": "Walmart"
      //   },
      //   "Viber": {
      //     "Category": "Communication",
      //     "Raw": "com.viber.voip",
      //     "App": "Viber",
      //     "Developer": "Viber Media S.à r.l."
      //   },
      //   "Waze": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.waze",
      //     "App": "Waze",
      //     "Developer": "Waze"
      //   },
      //   "Mini Info": {
      //     "Category": "Productivity",
      //     "Raw": "com.dynotes.miniinfo",
      //     "App": "Mini Info",
      //     "Developer": "Dynotes"
      //   },
      //   "1Tap Cleaner": {
      //     "Category": "Tools",
      //     "Raw": "com.a0soft.gphone.acc.free",
      //     "App": "1Tap Cleaner",
      //     "Developer": "Sam Lu"
      //   },
      //   "Voicemail": {
      //     "Category": "Communication",
      //     "Raw": "com.coremobility.app.vnotes",
      //     "App": "Voicemail",
      //     "Developer": "Unknown"
      //   },
      //   "Fire TV": {
      //     "Category": "Tools",
      //     "Raw": "com.amazon.storm.lightning.client.aosp",
      //     "App": "Fire TV",
      //     "Developer": "Amazon Mobile LLC"
      //   },
      //   "Meijer": {
      //     "Category": "Shopping",
      //     "Raw": "com.meijer.mobile.meijer",
      //     "App": "Meijer",
      //     "Developer": "Meijer"
      //   },
      //   "MSQRD": {
      //     "Category": "Social",
      //     "Raw": "me.msqrd.android",
      //     "App": "MSQRD",
      //     "Developer": "Masquerade Technologies, Inc"
      //   },
      //   "DMCU Mobile": {
      //     "Category": "Finance",
      //     "Raw": "com.dmcu.dmcumobile",
      //     "App": "DMCU Mobile",
      //     "Developer": "Diversified Members Credit Union"
      //   },
      //   "Dialer": {
      //     "Category": "Communication",
      //     "Raw": "com.android.dialer",
      //     "App": "Dialer",
      //     "Developer": "Unknown"
      //   },
      //   "Meme Creator": {
      //     "Category": "Entertainment",
      //     "Raw": "com.gentoozero.memecreator",
      //     "App": "Meme Creator",
      //     "Developer": "Gentoozero"
      //   },
      //   "GameStop": {
      //     "Category": "Entertainment",
      //     "Raw": "com.gamestop.powerup",
      //     "App": "GameStop",
      //     "Developer": "GameStop"
      //   },
      //   "Stop313": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.ngeosone.stop313",
      //     "App": "Stop313",
      //     "Developer": "NgeosOne LLC"
      //   },
      //   "IMDb": {
      //     "Category": "Entertainment",
      //     "Raw": "com.imdb.mobile",
      //     "App": "IMDb",
      //     "Developer": "IMDb"
      //   },
      //   "Fashion Empire": {
      //     "Category": "Role Playing",
      //     "Raw": "com.frenzoo.FashionEmpireBoutiqueGirlGame",
      //     "App": "Fashion Empire",
      //     "Developer": "Frenzoo"
      //   },
      //   "Citizens Bank": {
      //     "Category": "Finance",
      //     "Raw": "com.citizensbank.androidapp",
      //     "App": "Citizens Bank",
      //     "Developer": "Citizens Bank, N.A."
      //   },
      //   "Puffin": {
      //     "Category": "Communication",
      //     "Raw": "com.cloudmosa.puffinFree",
      //     "App": "Puffin",
      //     "Developer": "CloudMosa Inc."
      //   },
      //   "Skyforce Unite!": {
      //     "Category": "Strategy",
      //     "Raw": "net.kairosoft.android.airplane_en",
      //     "App": "Skyforce Unite!",
      //     "Developer": "Kairosoft Co.,Ltd"
      //   },
      //   "Bonbon Cakery": {
      //     "Category": "Casual",
      //     "Raw": "net.kairosoft.android.okashi_en",
      //     "App": "Bonbon Cakery",
      //     "Developer": "Kairosoft Co.,Ltd"
      //   },
      //   "Plex": {
      //     "Category": "Media & Video",
      //     "Raw": "com.plexapp.android",
      //     "App": "Plex",
      //     "Developer": "Plex, Inc."
      //   },
      //   "Groupon": {
      //     "Category": "Shopping",
      //     "Raw": "com.groupon",
      //     "App": "Groupon",
      //     "Developer": "Groupon, Inc."
      //   },
      //   "aTimeLogger": {
      //     "Category": "Productivity",
      //     "Raw": "com.aloggers.atimeloggerapp",
      //     "App": "aTimeLogger",
      //     "Developer": "BGCI"
      //   },
      //   "Time Meter": {
      //     "Category": "Productivity",
      //     "Raw": "com.rk.timemeter",
      //     "App": "Time Meter",
      //     "Developer": "Kapp Development / Time Tracking and Time Analysis"
      //   },
      //   "RescueTime": {
      //     "Category": "Productivity",
      //     "Raw": "com.rescuetime.android",
      //     "App": "RescueTime",
      //     "Developer": "RescueTime Team"
      //   },
      //   "Truecaller Service": {
      //     "Category": "Communication",
      //     "Raw": "com.truecaller.cyanogen",
      //     "App": "Truecaller Service",
      //     "Developer": "Unknown"
      //   },
      //   "Cyanogen Account": {
      //     "Category": "Utility",
      //     "Raw": "com.cyanogen.ambient.core",
      //     "App": "Cyanogen Account",
      //     "Developer": "Unknown"
      //   },
      //   "Magic 2015": {
      //     "Category": "Card",
      //     "Raw": "com.stainlessgames.D15",
      //     "App": "Magic 2015",
      //     "Developer": "Wizards of the Coast LLC"
      //   },
      //   "Google Dialer": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.dialer",
      //     "App": "Google Dialer",
      //     "Developer": "Google Inc."
      //   },
      //   "DraftKings": {
      //     "Category": "Sports",
      //     "Raw": "com.draftkings.dknativermgGP",
      //     "App": "DraftKings",
      //     "Developer": "DraftKings, Inc."
      //   },
      //   "Fenix": {
      //     "Category": "Social",
      //     "Raw": "it.mvilla.android.fenix",
      //     "App": "Fenix",
      //     "Developer": "mvilla"
      //   },
      //   "Sonos": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.sonos.acr",
      //     "App": "Sonos",
      //     "Developer": "Sonos, Inc"
      //   },
      //   "Job Search": {
      //     "Category": "Business",
      //     "Raw": "com.indeed.android.jobsearch",
      //     "App": "Job Search",
      //     "Developer": "Indeed Jobs"
      //   },
      //   "BodySpace": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.bodybuilding.mobile",
      //     "App": "BodySpace",
      //     "Developer": "Bodybuilding.com"
      //   },
      //   "Location Picker": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.htc.android.locationpicker",
      //     "App": "Location Picker",
      //     "Developer": "Unknown"
      //   },
      //   "Any.do": {
      //     "Category": "Productivity",
      //     "Raw": "com.anydo",
      //     "App": "Any.do",
      //     "Developer": "Any.do"
      //   },
      //   "XDA Premium 4": {
      //     "Category": "Communication",
      //     "Raw": "com.quoord.tapatalkxdapre.activity",
      //     "App": "XDA Premium 4",
      //     "Developer": "xda-developers"
      //   },
      //   "Pocket": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.ideashower.readitlater.pro",
      //     "App": "Pocket",
      //     "Developer": "Read It Later"
      //   },
      //   "eBay": {
      //     "Category": "Shopping",
      //     "Raw": "com.ebay.mobile",
      //     "App": "eBay",
      //     "Developer": "eBay Mobile"
      //   },
      //   "ESPN FFL": {
      //     "Category": "Sports",
      //     "Raw": "com.espn.fantasy.lm.football",
      //     "App": "ESPN FFL",
      //     "Developer": "ESPN Inc"
      //   },
      //   "Slickdeals": {
      //     "Category": "Shopping",
      //     "Raw": "net.slickdeals.android",
      //     "App": "Slickdeals",
      //     "Developer": "Slickdeals"
      //   },
      //   "NBC Bay Area": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.nbcuni.nbcots.nbcbayarea.android",
      //     "App": "NBC Bay Area",
      //     "Developer": "NBCUniversal Media, LLC"
      //   },
      //   "Cartwheel": {
      //     "Category": "Shopping",
      //     "Raw": "com.target.socsav",
      //     "App": "Cartwheel",
      //     "Developer": "Target Corporation"
      //   },
      //   "Bing": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.microsoft.bing",
      //     "App": "Bing",
      //     "Developer": "Microsoft Corporation"
      //   },
      //   "shopkick": {
      //     "Category": "Shopping",
      //     "Raw": "com.shopkick.app",
      //     "App": "shopkick",
      //     "Developer": "The Best Shopping Companion"
      //   },
      //   "Poshmark": {
      //     "Category": "Shopping",
      //     "Raw": "com.poshmark.app",
      //     "App": "Poshmark",
      //     "Developer": "Poshmark, Inc"
      //   },
      //   "e-Rewards": {
      //     "Category": "Social",
      //     "Raw": "com.researchNow.eRewards",
      //     "App": "e-Rewards",
      //     "Developer": "e-Rewards, Research Now Group"
      //   },
      //   "GlobalTestMarket": {
      //     "Category": "Business",
      //     "Raw": "com.lumi.globalttestmarket",
      //     "App": "GlobalTestMarket",
      //     "Developer": "GMI"
      //   },
      //   "Toluna": {
      //     "Category": "Social",
      //     "Raw": "com.toluna.webservice",
      //     "App": "Toluna",
      //     "Developer": "Toluna Android App"
      //   },
      //   "iPoll": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.usamp.mobile",
      //     "App": "iPoll",
      //     "Developer": "Instantly, Inc."
      //   },
      //   "Pinterest": {
      //     "Category": "Social",
      //     "Raw": "com.pinterest",
      //     "App": "Pinterest",
      //     "Developer": "Pinterest, Inc."
      //   },
      //   "SBTV": {
      //     "Category": "Media & Video",
      //     "Raw": "com.swagbuckstvmobile.views",
      //     "App": "SBTV",
      //     "Developer": "Prodege"
      //   },
      //   "Connected": {
      //     "Category": "Communication",
      //     "Raw": "com.linkedin.android.connected",
      //     "App": "Connected",
      //     "Developer": "Unknown"
      //   },
      //   "Metal": {
      //     "Category": "Communication",
      //     "Raw": "com.nam.fbwrapper",
      //     "App": "Metal",
      //     "Developer": "Nam Nghiem"
      //   },
      //   "Elmo Calls": {
      //     "Category": "Educational",
      //     "Raw": "com.sesame.apps.elmocalls.android",
      //     "App": "Elmo Calls",
      //     "Developer": "Sesame Workshop"
      //   },
      //   "Imgur": {
      //     "Category": "Entertainment",
      //     "Raw": "com.imgur.mobile",
      //     "App": "Imgur",
      //     "Developer": "Imgur"
      //   },
      //   "MX Player": {
      //     "Category": "Media & Video",
      //     "Raw": "com.mxtech.videoplayer.ad",
      //     "App": "MX Player",
      //     "Developer": "J2 Interactive"
      //   },
      //   "Pocket Casts": {
      //     "Category": "News & Magazines",
      //     "Raw": "au.com.shiftyjelly.pocketcasts",
      //     "App": "Pocket Casts",
      //     "Developer": "Shifty Jelly"
      //   },
      //   "OruxMaps": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.orux.oruxmaps",
      //     "App": "OruxMaps",
      //     "Developer": "jose vazquez"
      //   },
      //   "CBS": {
      //     "Category": "Entertainment",
      //     "Raw": "com.cbs.app",
      //     "App": "CBS",
      //     "Developer": "CBS Interactive, Inc."
      //   },
      //   "Flipagram": {
      //     "Category": "Media & Video",
      //     "Raw": "com.cheerfulinc.flipagram",
      //     "App": "Flipagram",
      //     "Developer": "Flipagram, inc."
      //   },
      //   "Sports": {
      //     "Category": "Sports",
      //     "Raw": "com.protrade.sportacular",
      //     "App": "Sports",
      //     "Developer": "Yahoo"
      //   },
      //   "Match": {
      //     "Category": "Social",
      //     "Raw": "com.match.android.matchmobile",
      //     "App": "Match",
      //     "Developer": "Match.com LLC"
      //   },
      //   "Epic": {
      //     "Category": "Role Playing",
      //     "Raw": "com.rovio.gold",
      //     "App": "Epic",
      //     "Developer": "Rovio Entertainment Ltd."
      //   },
      //   "Strava": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.strava",
      //     "App": "Strava",
      //     "Developer": "Strava Inc."
      //   },
      //   "QKSMS": {
      //     "Category": "Communication",
      //     "Raw": "com.moez.QKSMS",
      //     "App": "QKSMS",
      //     "Developer": "QK Labs"
      //   },
      //   "Wells Fargo": {
      //     "Category": "Finance",
      //     "Raw": "com.wf.wellsfargomobile",
      //     "App": "Wells Fargo",
      //     "Developer": "Wells Fargo Mobile"
      //   },
      //   "FanDuel": {
      //     "Category": "Sports",
      //     "Raw": "com.fanduel.android.self",
      //     "App": "FanDuel",
      //     "Developer": "FanDuel"
      //   },
      //   "Cheezburger": {
      //     "Category": "Entertainment",
      //     "Raw": "com.cheezburger.icanhas",
      //     "App": "Cheezburger",
      //     "Developer": "Cheezburger, Inc"
      //   },
      //   "CNN": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.cnn.mobile.android.phone",
      //     "App": "CNN",
      //     "Developer": "CNN"
      //   },
      //   "OsmAnd+": {
      //     "Category": "Travel & Local",
      //     "Raw": "net.osmand.plus",
      //     "App": "OsmAnd+",
      //     "Developer": "OsmAnd"
      //   },
      //   "Swagbucks": {
      //     "Category": "Entertainment",
      //     "Raw": "com.prodege.swagbucksmobile",
      //     "App": "Swagbucks",
      //     "Developer": "Prodege"
      //   },
      //   "McDonald's": {
      //     "Category": "Shopping",
      //     "Raw": "com.mcdonalds.app",
      //     "App": "McDonald's",
      //     "Developer": "McDonalds USA, LLC"
      //   },
      //   "Podcast Addict": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.bambuna.podcastaddict",
      //     "App": "Podcast Addict",
      //     "Developer": "Xavier Guillemane"
      //   },
      //   "Appy Gamer": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.mobilesrepublic.appygamer",
      //     "App": "Appy Gamer",
      //     "Developer": "News Republic"
      //   },
      //   "Angry Birds": {
      //     "Category": "Arcade",
      //     "Raw": "com.rovio.angrybirds",
      //     "App": "Angry Birds",
      //     "Developer": "Rovio Entertainment Ltd."
      //   },
      //   "Candy Crush Saga": {
      //     "Category": "Casual",
      //     "Raw": "com.king.candycrushsaga",
      //     "App": "Candy Crush Saga",
      //     "Developer": "King"
      //   },
      //   "TV Go": {
      //     "Category": "Entertainment",
      //     "Raw": "com.xfinity.playnow",
      //     "App": "TV Go",
      //     "Developer": "Comcast"
      //   },
      //   "WatchMaker Premium": {
      //     "Category": "Personalization",
      //     "Raw": "slide.watchFrenzy.premium",
      //     "App": "WatchMaker Premium",
      //     "Developer": "androidslide"
      //   },
      //   "Safeway": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.safeway.client.android.safeway",
      //     "App": "Safeway",
      //     "Developer": "Albertsons Companies, LLC."
      //   },
      //   "S Voice": {
      //     "Category": "Communication",
      //     "Raw": "com.vlingo.midas",
      //     "App": "S Voice",
      //     "Developer": "Unknown"
      //   },
      //   "Capital One": {
      //     "Category": "Finance",
      //     "Raw": "com.konylabs.capitalone",
      //     "App": "Capital One",
      //     "Developer": "Capital One Services, LLC"
      //   },
      //   "PulsePoint": {
      //     "Category": "Medical",
      //     "Raw": "mobi.firedepartment",
      //     "App": "PulsePoint",
      //     "Developer": "PulsePoint Foundation"
      //   },
      //   "SmartNews": {
      //     "Category": "News & Magazines",
      //     "Raw": "jp.gocro.smartnews.android",
      //     "App": "SmartNews",
      //     "Developer": "SmartNews, Inc."
      //   },
      //   "Google Contacts": {
      //     "Category": "Communication",
      //     "Raw": "com.google.android.contacts",
      //     "App": "Google Contacts",
      //     "Developer": "Google Inc."
      //   },
      //   "Prime Now": {
      //     "Category": "Shopping",
      //     "Raw": "com.amazon.now",
      //     "App": "Prime Now",
      //     "Developer": "Amazon Mobile LLC"
      //   },
      //   "Forest Mania": {
      //     "Category": "Puzzle",
      //     "Raw": "com.spacegame.forestmania",
      //     "App": "Forest Mania",
      //     "Developer": "TaoGames Limited"
      //   },
      //   "Audio Manager": {
      //     "Category": "Media & Video",
      //     "Raw": "com.hideitpro",
      //     "App": "Audio Manager",
      //     "Developer": "ANUJ TENANI"
      //   },
      //   "SODA": {
      //     "Category": "Productivity",
      //     "Raw": "com.techneos.soda.client.micro",
      //     "App": "SODA",
      //     "Developer": "Confirmit"
      //   },
      //   "Zillow": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.zillow.android.zillowmap",
      //     "App": "Zillow",
      //     "Developer": "Zillow"
      //   },
      //   "Tasker": {
      //     "Category": "Tools",
      //     "Raw": "net.dinglisch.android.taskerm",
      //     "App": "Tasker",
      //     "Developer": "Crafty Apps EU"
      //   },
      //   "Sound Picker": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.htc.sdm",
      //     "App": "Sound Picker",
      //     "Developer": "Unknown"
      //   },
      //   "System updates": {
      //     "Category": "Utility",
      //     "Raw": "com.cyngn.fota",
      //     "App": "System updates",
      //     "Developer": "Unknown"
      //   },
      //   "Motif": {
      //     "Category": "Finance",
      //     "Raw": "com.motifinvesting.motif",
      //     "App": "Motif",
      //     "Developer": "Motif Investing, Inc."
      //   },
      //   "Vivino": {
      //     "Category": "Lifestyle",
      //     "Raw": "vivino.web.app",
      //     "App": "Vivino",
      //     "Developer": "Vivino"
      //   },
      //   "Delectable": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.delectable.mobile",
      //     "App": "Delectable",
      //     "Developer": "Delectable Inc"
      //   },
      //   "Farm Heroes Saga": {
      //     "Category": "Casual",
      //     "Raw": "com.king.farmheroessaga",
      //     "App": "Farm Heroes Saga",
      //     "Developer": "King"
      //   },
      //   "Shop Your Way": {
      //     "Category": "Shopping",
      //     "Raw": "com.sears.shopyourway",
      //     "App": "Shop Your Way",
      //     "Developer": "Sears Brands LLC"
      //   },
      //   "Slice": {
      //     "Category": "Shopping",
      //     "Raw": "com.slice",
      //     "App": "Slice",
      //     "Developer": "Slice"
      //   },
      //   "YouTube Gaming": {
      //     "Category": "Entertainment",
      //     "Raw": "com.google.android.apps.youtube.gaming",
      //     "App": "YouTube Gaming",
      //     "Developer": "Google Inc."
      //   },
      //   "Chromecast": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.apps.chromecast.app",
      //     "App": "Chromecast",
      //     "Developer": "Google Inc."
      //   },
      //   "Target": {
      //     "Category": "Shopping",
      //     "Raw": "com.target.ui",
      //     "App": "Target",
      //     "Developer": "Target Corporation"
      //   },
      //   "NPR One": {
      //     "Category": "News & Magazines",
      //     "Raw": "org.npr.one",
      //     "App": "NPR One",
      //     "Developer": "NPR"
      //   },
      //   "Layout": {
      //     "Category": "Photography",
      //     "Raw": "com.instagram.layout",
      //     "App": "Layout",
      //     "Developer": "Instagram"
      //   },
      //   "cLock": {
      //     "Category": "Tool",
      //     "Raw": "com.cyanogenmod.lockclock",
      //     "App": "cLock",
      //     "Developer": "Unknown"
      //   },
      //   "Bluelight Filter": {
      //     "Category": "Health & Fitness",
      //     "Raw": "jp.ne.hardyinfinity.bluelightfilter.free",
      //     "App": "Bluelight Filter",
      //     "Developer": "Hardy-infinity"
      //   },
      //   "NightyNight": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.foxandsheep.nightynight",
      //     "App": "NightyNight",
      //     "Developer": "Fox & Sheep"
      //   },
      //   "LTE Discovery": {
      //     "Category": "Tools",
      //     "Raw": "net.simplyadvanced.ltediscovery",
      //     "App": "LTE Discovery",
      //     "Developer": "Simply Advanced"
      //   },
      //   "Peel Smart Remote": {
      //     "Category": "Entertainment",
      //     "Raw": "tv.peel.app",
      //     "App": "Peel Smart Remote",
      //     "Developer": "Peel Technologies Inc."
      //   },
      //   "Visual VM": {
      //     "Category": "Utility",
      //     "Raw": "com.htc.vvm",
      //     "App": "Visual VM",
      //     "Developer": "Unknown"
      //   },
      //   "Taco Bell": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.tacobell.ordering",
      //     "App": "Taco Bell",
      //     "Developer": "Taco Bell Mobile"
      //   },
      //   "HtcMobileData": {
      //     "Category": "Utility",
      //     "Raw": "com.htc.mobiledata",
      //     "App": "HtcMobileData",
      //     "Developer": "Unknown"
      //   },
      //   "Romance Quotes": {
      //     "Category": "Entertainment",
      //     "Raw": "com.happysun.lovequotespics",
      //     "App": "Romance Quotes",
      //     "Developer": "HappySunMobile"
      //   },
      //   "Street View": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.google.android.street",
      //     "App": "Street View",
      //     "Developer": "Google Inc."
      //   },
      //   "BET NOW": {
      //     "Category": "Entertainment",
      //     "Raw": "com.bet.shows",
      //     "App": "BET NOW",
      //     "Developer": "BET Networks"
      //   },
      //   "Banking": {
      //     "Category": "Finance",
      //     "Raw": "com.bbva.compassBuzz",
      //     "App": "Banking",
      //     "Developer": "BBVA"
      //   },
      //   "AdAway": {
      //     "Category": "Utility",
      //     "Raw": "org.adaway",
      //     "App": "AdAway",
      //     "Developer": "Unknown"
      //   },
      //   "Timer": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.runtastic.android.timer",
      //     "App": "Timer",
      //     "Developer": "Runtastic"
      //   },
      //   "1Weather": {
      //     "Category": "Weather",
      //     "Raw": "com.handmark.expressweather",
      //     "App": "1Weather",
      //     "Developer": "OneLouder Apps"
      //   },
      //   "Yidio": {
      //     "Category": "Entertainment",
      //     "Raw": "com.yidio.androidapp",
      //     "App": "Yidio",
      //     "Developer": "Yidio LLC"
      //   },
      //   "Daily K Pop": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.daily.k.pop",
      //     "App": "Daily K Pop",
      //     "Developer": "Kreasi Elemen"
      //   },
      //   "Extreme Call Blocker": {
      //     "Category": "Tools",
      //     "Raw": "com.greythinker.punchback",
      //     "App": "Extreme Call Blocker",
      //     "Developer": "GreyThinker"
      //   },
      //   "TV": {
      //     "Category": "Entertainment",
      //     "Raw": "com.xfinity.cloudtvr",
      //     "App": "TV",
      //     "Developer": "Comcast"
      //   },
      //   "Speak": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.htc.HTCSpeaker",
      //     "App": "Speak",
      //     "Developer": "HTC Corporation"
      //   },
      //   "Phone Services": {
      //     "Category": "Communication",
      //     "Raw": "com.android.phone",
      //     "App": "Phone Services",
      //     "Developer": "Unknown"
      //   },
      //   "MusicFX": {
      //     "Category": "Music & Audio",
      //     "Raw": "com.android.musicfx",
      //     "App": "MusicFX",
      //     "Developer": "Unknown"
      //   },
      //   "Cloud Print": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.apps.cloudprint",
      //     "App": "Cloud Print",
      //     "Developer": "Google Inc."
      //   },
      //   "HTML Viewer": {
      //     "Category": "Productivity",
      //     "Raw": "com.android.htmlviewer",
      //     "App": "HTML Viewer",
      //     "Developer": "Unknown"
      //   },
      //   "Google Services Framework": {
      //     "Category": "Utility",
      //     "Raw": "com.google.android.gsf",
      //     "App": "Google Services Framework",
      //     "Developer": "Google Inc."
      //   },
      //   "Android System WebView": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.webview",
      //     "App": "Android System WebView",
      //     "Developer": "Google Inc."
      //   },
      //   "YouTube Kids": {
      //     "Category": "Entertainment",
      //     "Raw": "com.google.android.apps.youtube.kids",
      //     "App": "YouTube Kids",
      //     "Developer": "Google Inc."
      //   },
      //   "Hair Salon": {
      //     "Category": "Educational",
      //     "Raw": "com.bearhugmedia.android_barbiehairsalon",
      //     "App": "Hair Salon",
      //     "Developer": "Bear Hug Media Inc"
      //   },
      //   "Bubbles for Toddlers": {
      //     "Category": "Educational",
      //     "Raw": "co.romesoft.toddlers.bubbles",
      //     "App": "Bubbles for Toddlers",
      //     "Developer": "romeLab"
      //   },
      //   "Pop Balloon Kids": {
      //     "Category": "Casual",
      //     "Raw": "com.bubadu.popballoonkids",
      //     "App": "Pop Balloon Kids",
      //     "Developer": "Bubadu"
      //   },
      //   "Pony Baby Care": {
      //     "Category": "Casual",
      //     "Raw": "air.com.netfunmedia.ponybabycare",
      //     "App": "Pony Baby Care",
      //     "Developer": "Net Fun Media"
      //   },
      //   "Peppas Party": {
      //     "Category": "Educational",
      //     "Raw": "com.p2games.peppasparty",
      //     "App": "Peppas Party",
      //     "Developer": "Unknown"
      //   },
      //   "Peppa Paintbox": {
      //     "Category": "Educational",
      //     "Raw": "air.com.peppapig.paintbox",
      //     "App": "Peppa Paintbox",
      //     "Developer": "Entertainment One"
      //   },
      //   "BetterBatteryStats": {
      //     "Category": "Tools",
      //     "Raw": "com.asksven.betterbatterystats",
      //     "App": "BetterBatteryStats",
      //     "Developer": "Sven Knispel"
      //   },
      //   "VSCO": {
      //     "Category": "Photography",
      //     "Raw": "com.vsco.cam",
      //     "App": "VSCO",
      //     "Developer": "VSCO"
      //   },
      //   "Cell Broadcasts": {
      //     "Category": "Utility",
      //     "Raw": "com.android.cellbroadcastreceiver",
      //     "App": "Cell Broadcasts",
      //     "Developer": "Unknown"
      //   },
      //   "TalkBack": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.marvin.talkback",
      //     "App": "TalkBack",
      //     "Developer": "Google Inc."
      //   },
      //   "Titanium Backup": {
      //     "Category": "Tools",
      //     "Raw": "com.keramidas.TitaniumBackup",
      //     "App": "Titanium Backup",
      //     "Developer": "Titanium Track"
      //   },
      //   "FX": {
      //     "Category": "Business",
      //     "Raw": "nextapp.fx",
      //     "App": "FX",
      //     "Developer": "NextApp, Inc."
      //   },
      //   "LoopWall": {
      //     "Category": "Personalization",
      //     "Raw": "com.koncius.loopwall",
      //     "App": "LoopWall",
      //     "Developer": "Audrius Koncius"
      //   },
      //   "Live wallpaper picker": {
      //     "Category": "Unknown",
      //     "Raw": "com.android.wallpaper.livepicker",
      //     "App": "Live wallpaper picker",
      //     "Developer": "Unknown"
      //   },
      //   "Remote Control": {
      //     "Category": "Tools",
      //     "Raw": "com.google.android.tv.remote",
      //     "App": "Remote Control",
      //     "Developer": "Google Inc."
      //   },
      //   "Meo Watch Face": {
      //     "Category": "Personalization",
      //     "Raw": "com.face.watch.meo",
      //     "App": "Meo Watch Face",
      //     "Developer": "Siamcybersoft Apps"
      //   },
      //   "Plants Vs Zombies 2": {
      //     "Category": "Casual",
      //     "Raw": "com.ea.game.pvz2_na",
      //     "App": "Plants Vs Zombies 2",
      //     "Developer": "ELECTRONIC ARTS"
      //   },
      //   "Plants vs. Zombies Free": {
      //     "Category": "Strategy",
      //     "Raw": "com.ea.game.pvzfree_row",
      //     "App": "Plants vs. Zombies Free",
      //     "Developer": "ELECTRONIC ARTS"
      //   },
      //   "WPS Office": {
      //     "Category": "Business",
      //     "Raw": "cn.wps.moffice_eng",
      //     "App": "WPS Office",
      //     "Developer": "Kingsoft Office Software Corporation Limited"
      //   },
      //   "Schedule power on & off": {
      //     "Category": "Unknown",
      //     "Raw": "com.mediatek.schpwronoff",
      //     "App": "Schedule power on & off",
      //     "Developer": "Unknown"
      //   },
      //   "My Talking Tom": {
      //     "Category": "Casual",
      //     "Raw": "com.outfit7.mytalkingtomfree",
      //     "App": "My Talking Tom",
      //     "Developer": "Outfit7"
      //   },
      //   "Ice Princess": {
      //     "Category": "Educational",
      //     "Raw": "com.hugsnhearts.android_frozen_beauty_queen",
      //     "App": "Ice Princess",
      //     "Developer": "Hugs N Hearts"
      //   },
      //   "Toddler Coloring Book Free": {
      //     "Category": "Education",
      //     "Raw": "com.androidcave.toddlercoloringbook.free",
      //     "App": "Toddler Coloring Book Free",
      //     "Developer": "Rad Lemur Kids Games"
      //   },
      //   "Talking Tom 2": {
      //     "Category": "Entertainment",
      //     "Raw": "com.outfit7.talkingtom2free",
      //     "App": "Talking Tom 2",
      //     "Developer": "Outfit7"
      //   },
      //   "Kids ABC Letters Lite": {
      //     "Category": "Educational",
      //     "Raw": "zok.android.letters",
      //     "App": "Kids ABC Letters Lite",
      //     "Developer": "Intellijoy Educational Games for Kids"
      //   },
      //   "Kids Preschool Puzzles Lite": {
      //     "Category": "Educational",
      //     "Raw": "zok.android.shapes",
      //     "App": "Kids Preschool Puzzles Lite",
      //     "Developer": "Intellijoy Educational Games for Kids"
      //   },
      //   "My Little Pony": {
      //     "Category": "Casual",
      //     "Raw": "com.gameloft.android.ANMP.GloftPOHM",
      //     "App": "My Little Pony",
      //     "Developer": "Gameloft"
      //   },
      //   "Study Bible": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.logos.androidfaithlife",
      //     "App": "Study Bible",
      //     "Developer": "Faithlife"
      //   },
      //   "SYNCit HD": {
      //     "Category": "Tools",
      //     "Raw": "com.lenovo.lps.cloud.sync.row",
      //     "App": "SYNCit HD",
      //     "Developer": "Lenovo Group"
      //   },
      //   "Wash Post": {
      //     "Category": "News & Magazines",
      //     "Raw": "com.washingtonpost.rainbow",
      //     "App": "Wash Post",
      //     "Developer": "The Washington Post"
      //   },
      //   "Redfin": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.redfin.android",
      //     "App": "Redfin",
      //     "Developer": "Redfin"
      //   },
      //   "Pokémon GO": {
      //     "Category": "Adventure",
      //     "Raw": "com.nianticlabs.pokemongo",
      //     "App": "Pokémon GO",
      //     "Developer": "Niantic, Inc."
      //   },
      //   "Moves": {
      //     "Category": "Health & Fitness",
      //     "Raw": "com.protogeo.moves",
      //     "App": "Moves",
      //     "Developer": "ProtoGeo"
      //   },
      //   "Hipmunk": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.hipmunk.android",
      //     "App": "Hipmunk",
      //     "Developer": "Hipmunk, Inc."
      //   },
      //   "Cert Installer": {
      //     "Category": "Unknown",
      //     "Raw": "com.google.android.apps.certinstaller2",
      //     "App": "Cert Installer",
      //     "Developer": "Unknown"
      //   },
      //   "Browser": {
      //     "Category": "Personalization",
      //     "Raw": "com.cyngn.browser",
      //     "App": "Browser",
      //     "Developer": "Revolution By Arin Sanghi"
      //   },
      //   "TripAdvisor": {
      //     "Category": "Travel & Local",
      //     "Raw": "com.tripadvisor.tripadvisor",
      //     "App": "TripAdvisor",
      //     "Developer": "TripAdvisor"
      //   },
      //   "Mobizen": {
      //     "Category": "Productivity",
      //     "Raw": "com.rsupport.mvagent",
      //     "App": "Mobizen",
      //     "Developer": "RSUPPORT Co., Ltd."
      //   },
      //   "Permissions": {
      //     "Category": "Unknown",
      //     "Raw": "examples.baku.io.permissions",
      //     "App": "Permissions",
      //     "Developer": "Unknown"
      //   },
      //   "Advencha": {
      //     "Category": "Social",
      //     "Raw": "com.whis.mobilize",
      //     "App": "Advencha",
      //     "Developer": "Advencha Inc"
      //   },
      //   "Wikipedia": {
      //     "Category": "Books & Reference",
      //     "Raw": "org.wikipedia",
      //     "App": "Wikipedia",
      //     "Developer": "Wikimedia Foundation"
      //   },
      //   "Google Play Books": {
      //     "Category": "Books & Reference",
      //     "Raw": "com.google.android.apps.books",
      //     "App": "Google Play Books",
      //     "Developer": "Google Inc."
      //   },
      //   "Eat": {
      //     "Category": "Unknown",
      //     "Raw": "com.google.corp.bizapps.rews.food.android",
      //     "App": "Eat",
      //     "Developer": "Unknown"
      //   },
      //   "Uber SDK": {
      //     "Category": "Unknown",
      //     "Raw": "com.uber.sdk.android.rides.samples",
      //     "App": "Uber SDK",
      //     "Developer": "Unknown"
      //   },
      //   "Transmission": {
      //     "Category": "Puzzle",
      //     "Raw": "com.lojugames.games.transmission",
      //     "App": "Transmission",
      //     "Developer": "Science Museum"
      //   },
      //   "Sleep": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.urbandroid.sleep",
      //     "App": "Sleep",
      //     "Developer": "Urbandroid Team"
      //   },
      //   "Device Policy": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.apps.enterprise.dmagent",
      //     "App": "Device Policy",
      //     "Developer": "Google Inc."
      //   },
      //   "OpenTable": {
      //     "Category": "Lifestyle",
      //     "Raw": "com.opentable",
      //     "App": "OpenTable",
      //     "Developer": "OpenTable"
      //   },
      //   "Podkicker": {
      //     "Category": "Media & Video",
      //     "Raw": "ait.podka",
      //     "App": "Podkicker",
      //     "Developer": "qvga"
      //   },
      //   "Stocard": {
      //     "Category": "Shopping",
      //     "Raw": "de.stocard.stocard",
      //     "App": "Stocard",
      //     "Developer": "Stocard GmbH"
      //   },
      //   "Skype plug-in": {
      //     "Category": "Unknown",
      //     "Raw": "com.skype.android.spice",
      //     "App": "Skype plug-in",
      //     "Developer": "Unknown"
      //   },
      //   "Arts & Culture": {
      //     "Category": "Education",
      //     "Raw": "com.google.android.apps.cultural",
      //     "App": "Arts & Culture",
      //     "Developer": "Google Inc."
      //   },
      //   "FFT WotL": {
      //     "Category": "Simulation",
      //     "Raw": "com.square_enix.android_googleplay.FFT_en2",
      //     "App": "FFT WotL",
      //     "Developer": "SQUARE ENIX Co.,Ltd."
      //   },
      //   "RT Editor Demo": {
      //     "Category": "Libraries & Demo",
      //     "Raw": "com.onegravity.rteditor.demo",
      //     "App": "RT Editor Demo",
      //     "Developer": "1gravity LLC"
      //   },
      //   "Mojo Shell (dev)": {
      //     "Category": "Unknown",
      //     "Raw": "org.chromium.mojo.shell",
      //     "App": "Mojo Shell (dev)",
      //     "Developer": "Unknown"
      //   },
      //   "Slides": {
      //     "Category": "Productivity",
      //     "Raw": "com.google.android.apps.docs.editors.slides",
      //     "App": "Slides",
      //     "Developer": "Google Inc."
      //   },
      //   "Pixel Launcher": {
      //     "Category": "Home screen",
      //     "Raw": "com.google.android.apps.nexuslauncher",
      //     "App": "Pixel Launcher",
      //     "Developer": "Google Inc."
      //   }
      // };

      var top_categories = ["Communication", "Search", "News & Magazines", "Social networking", "Data collection", "Sports", "Utility", "Media & Video", "Browser", "Tools", "Productivity", "Dating", "Photography", "Music & Audio", "Travel & Local", "Entertainment", "Shopping", "Music & Video", "Health & Fitness", "Other"];
      var colors = ["#1f77b4", "#aec7e8", "#ff7f0e", "#ffbb78", "#2ca02c", "#98df8a", "#d62728", "#ff9896", "#9467bd", "#c5b0d5", "#8c564b", "#c49c94", "#e377c2", "#f7b6d2", "#7f7f7f", "#c7c7c7", "#bcbd22", "#dbdb8d", "#17becf", "#9edae5", "#BDBDBD"];

    }]);