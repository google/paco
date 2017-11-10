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

  // TODO refactor to use a viz as model for ui
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
    // {
    //   qno: 5,
    //   question: "What is the value of this variable over time for everyone?"
    // }, {
    //   qno: 6,
    //   question: "How many people in total and basic demographics.",
    // }, {
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
      if (data.data[0] !== undefined) {
        $scope.responseCounts.push(data.data[0].schedR, data.data[0].missedR, data.data[0].selfR);
        $scope.loadResponseCounts = true;
      }
    });

    experimentsVizService.getParticipants($scope.experimentId).then(function (participants) {
      $scope.participants = [];
        participants.data.customResponse.forEach(function (participant) {
          $scope.participants.push(participant.who);
        });
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

  function resetVariables() {
    $scope.selectAllParticipants = false;
    $scope.deSelectAllParticipants = true;
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
      } else if ($scope.template === 4) {
        toggleVizControls(true, false, false, true, true, true, true);
      }
      resetVariables();
      getGroups();
      populateVizType(isNewViz);
      clearViz();
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
      }
      if ($scope.template === 2) {
        $scope.vizTypes = ["Box Plot", "Bar Chart"];
      }
      if ($scope.template === 3) {
        $scope.vizTypes = ["Scatter Plot"];
      }
      if ($scope.template === 4) {
        $scope.vizTypes = ["Scatter Plot"];
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
    if ($scope.currentVisualization.yAxisVariables === undefined) {
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
    if ($scope.currentVisualization.xAxisVariable === undefined) {
      $scope.displayTextSingle = "x Axis";
    }
  };

  $scope.toggleDrawButton = function(bool_value) {
    // todo remove bool_value;
    if (getAllAxisVariablesFromViz($scope.currentVisualization).length == 0) {
      $scope.drawButton = false;
    } else {
      var allFieldsSet = requiredFieldsSetForTemplate(false);
      if (!allFieldsSet) {
        $scope.drawButton = false;
      } else {
        var compatibility = checkInputsAreCompatibleWithChartType($scope.currentVisualization);
        $scope.drawButton = compatibility.compatible;
        if (!compatibility.compatible) {
          allFields
          alertAboutIncompatibleDataTypes(compatibility.incompatibleInputs);
        }
      }


    }
  };

      // $scope.getInputs = function () {
  //   // $scope.inputNames = [];
  //   // $scope.groupsSet = new Set();
  //   // if ($scope.currentVisualization.yAxisVariables !== undefined) {
  //   //   $scope.currentVisualization.yAxisVariables.forEach(function (input) {
  //   //     $scope.inputNames.push(input.input);
  //   //     $scope.groupsSet.add(input.group);
  //   //   });
  //   // }
  //   $scope.drawButton = false;
  // };

  // $scope.getInput1 = function () {
  //   $scope.drawButton = false;
  // };

  // $scope.getSelectedType = function () {
  //   $scope.drawButton = false;
  // };

  // $scope.getParticipants = function () {
  //   $scope.drawButton = false;
  // };

  // $scope.getDatetime = function () {
  //   $scope.drawButton = false;
  // };

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
      if (events.data.customResponse) {
        //console.log(events.data.customResponse);
        buildViz(viz, events.data.customResponse);
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
    } else if ($scope.template === 2 || $scope.template === 3 || $scope.template === 4) {
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
      && ($scope.template === 4)) {
      return "Value over time by person.";
    } else if (((viz.type === "Box Plot")
        || (viz.type === "Bar Chart"))
      && (viz.xAxisVariable)
      && ($scope.template === 2)) {
      return "Distribution of responses";
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
            if (currX && currY) {
              data[i].values.push({
                x: currX,
                y: currY,
                size: 5
              });
            }
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
          var response = responseTypeMap.get($scope.currentVisualization.xAxisVariable.name);
          var responseType = response.responseType;
          if ((responseType === "likert") || (responseType === "likert_smileys")) {
            var steps = 5;
            if (response.likertSteps) {
              steps = response.likertSteps;
            }
            chart.yDomain([1, steps]);
            var yAxisTickValues = [];
            for (var t = 0; t < steps + 1; t++) {
              yAxisTickValues.push(t);
            }
            chart.yAxis.tickValues(yAxisTickValues);
          }
          else {
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
        getEvents($scope.currentVisualization);
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
    } else if ($scope.template === 4) {
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
    } else if (($scope.template === 2) || ($scope.template === 4)) {
      vizTexts = viz.xAxisVariable.name;
    } else if (($scope.template === 3)) {
      viz.yAxisVariables.forEach(function (text) {
        texts.push(text.name);
      });
      texts.push(viz.xAxisVariable.name);
      vizTexts = texts.join(", ");
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
}]);