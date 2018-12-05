pacoApp.controller('VizCtrl', ['$scope', '$element', '$compile', 'experimentsVizService', '$timeout', '$routeParams', '$filter', '$mdDialog', '$sce', function ($scope, $element, $compile, experimentsVizService, $timeout, $routeParams, $filter, $mdDialog, $sce) {

  $scope.dateRangeControl = false;
  $scope.multipleInputs = false;
  $scope.expParticipants = false;
  $scope.vizChartTypes = false;
  $scope.createBtn = false;
  $scope.singleInput = false;
  $scope.vizs = [];
  $scope.vizTable = false;
  $scope.renderSavedViz = false;
  $scope.saveDownload = false;
  $scope.renderVisualization = false;
  $scope.editMode = true;
  $scope.editDescMode = true;
  $scope.backButton = true;
  $scope.forwardButton = true;
  $scope.vizHistory = [];
  $scope.drawButton = true;
  $scope.selectAllParticipants = false;
  $scope.deSelectAllParticipants = true;

  var responseTypeMap = new Map();
  var responseMetaData = [];
  var responses = [];
  var questionsMap = new Map();
  var getEvents = "";
  var vizIndex = 0;

  $scope.questions = [{
    qno: 1,
    question: "Show the distribution of responses for the variable?",
  }, {
    qno: 2,
    question: "Compare distribution of responses for the variable by day?",
  }, {
    qno: 3,
    question: "How do the responses for input 1 relate to the responses for variable 2?",
  }, {
    qno: 4,
    question: "What is the value of the variable over time for each person?"
  }, {
    qno: 5,
    question: "What is the value of this variable over time for everyone?"
  }, {
    qno: 6,
    question: "How many people in total and basic demographics.",
  }, {
    qno: 7,
    question: "Stats: Spread of # of devices, average by use from high to low"
  }, {
    qno: 8,
    question: "Stats:range of time on devices, any differences by demographics?"
  }, {
    qno: 9,
    question: "No.of apps in total and ranges of time spent, and differences by demographics?"
  }, {
    qno: 10,
    question: "App usage by category"
  }, {
    qno: 11,
    question: "App usage by time of day with ESM responses"
  }];

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
      $scope.participantsCount = participants.data.customResponse.length;
      $scope.loadParticipantsCount = true;
    });

    experimentsVizService.getStartDate($scope.experimentId).then(function (data) {
      var format = 'MM/dd/yyyy';
      if (data.data.customResponse !== undefined) {
        if (data.data.customResponse.length > 0) {
          var format_startDate = $filter('date')(new Date(data.data.customResponse[0].response_time), format);
          $scope.dateRange[0] = format_startDate;
        }
      }
      $scope.loadStartDate = true;
    });

    experimentsVizService.getEndDate($scope.experimentId).then(function (data) {
      var format = 'MM/dd/yyyy';
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

  //experiment json objects are retrieved from the 'experimentsVizService'
  // to create a scope variable for response type meta data.
  $scope.getExperiment = function () {
    experimentsVizService.getExperiment($scope.experimentId).then(
        function (experiment) {
          if (experiment.status === 404) {
            displayErrorMessage("Experiments ", experiment);
          }
          else {
            $scope.vizs = experiment.results[0].visualizations;
            if (experiment.results[0].visualizations.length >= 1) {
              $scope.vizTable = true;
            }
            $scope.experimentDataModel = {
              id: experiment.results[0].id,
              title: experiment.results[0].title,
              creator: experiment.results[0].creator,
              date: experiment.results[0].modifyDate
            };
            responseTypeData(experiment.results[0]);
          }
        });
  };

  function getGroups() {
    $scope.groupInputs = [];
    $scope.groups = [];

    experimentsVizService.getExperiment($scope.experimentId).then(
        function (experiment) {
          if (experiment.status === 500) {
            displayErrorMessage("Experiment", experiment);
          } else {
            experiment.results[0].groups.forEach(function (groups) {
              $scope.groups.push(groups.name);
              groups.inputs.forEach(function (input) {
                $scope.groupInputs.push({
                  "id": groups.name + ":" + input.name,
                  "group": groups.name,
                  "input": input.name,
                  "responseType": input.responseType
                });
              });
            });
          }
        });
  }

  $scope.getResponseType = function (input) {
    return input.responseType;
  };

  function responseTypeData(experiment) {
    responseMetaData = [];
    responseTypeMap = new Map();
    experiment.groups.forEach(function (groups) {
      groups.inputs.forEach(function (input) {
        if (input.responseType == "likert") {
          responseMetaData.push({
            "name": input.name,
            "responseType": input.responseType,
            "text": input.text,
            "leftsidelabel": input.leftSideLabel,
            "rightsidelabel": input.rightSideLabel
          });
        } else if (input.responseType == "list") {
          responseMetaData.push({
            "name": input.name,
            "responseType": input.responseType,
            "text": input.text,
            "listChoices": input.listChoices
          });
        } else {
          responseMetaData.push({
            "name": input.name,
            "responseType": input.responseType,
            "text": input.text
          });
        }
      });
      responseMetaData.forEach(function (response) {
        responseTypeMap.set(response.name, response);
      });
    });
  }

  function resetVariables() {
    $scope.selectedInputs = undefined;
    $scope.selectedType = undefined;
    $scope.startDate = undefined;
    $scope.endDate = undefined;
    $scope.startTime = undefined;
    $scope.endTime = undefined;
    $scope.startDateTime = undefined;
    $scope.endDateTime = undefined;
    $scope.selectedInput1 = undefined;
    $scope.xPlotInput = [];
    $scope.selectedParticipants = $scope.participants;
    $scope.selectAllParticipants = false;
    $scope.deSelectAllParticipants = true;
    $scope.yAxisLabel = undefined;
  }

  function populateVizParams() {
    resetVariables();
    getGroups();
    populateParticipants();
    $scope.selectedParticipants = $scope.participants;
    populateVizType();
    $scope.startDate = $filter('date')(new Date($scope.dateRange[0]), 'EEE, dd MMM yyyy HH:mm:ss Z');
    $scope.endDate = $filter('date')(new Date($scope.dateRange[1]), 'EEE, dd MMM yyyy HH:mm:ss Z');
  }

  $scope.selectAll = function () {
    $scope.selectedParticipants = $scope.participants;
    $scope.selectAllParticipants = false;
    $scope.deSelectAllParticipants = true;
  };

  $scope.deselectAll = function () {
    $scope.selectedParticipants = [];
    $scope.selectAllParticipants = true;
    $scope.deSelectAllParticipants = false;
  };

  $scope.getTemplate = function () {
    if (questionsMap.has($scope.selectedQues)) {
      $scope.template = questionsMap.get($scope.selectedQues);
      if ($scope.template === 1) {
        toggleVizControls(true, true, false, true, true, true, false);
        populateVizParams();
        clearViz();
      } else if ($scope.template === 2) {
        toggleVizControls(true, false, false, true, true, true, true);
        populateVizParams();
        clearViz();
      } else if ($scope.template === 3) {
        toggleVizControls(true, false, true, true, true, true, true);
        populateVizParams();
        clearViz();
      } else if ($scope.template === 4) {
        toggleVizControls(true, false, false, true, true, true, true);
        populateVizParams();
        clearViz();
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

  function populateVizType() {
    if (questionsMap.has($scope.selectedQues)) {
      $scope.template = questionsMap.get($scope.selectedQues);
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
    $scope.selectedType = $scope.vizTypes[0];
    axisLabels();
    $scope.displayTextMul();
    $scope.displayTextOne();
  }

  $scope.displayTextMul = function () {
    if ($scope.selectedInputs === undefined) {
      if (($scope.selectedType === "Box Plot") || ($scope.selectedType === "Bar Chart")) {
        $scope.displayTextMultiple = "x Axis";
      }
      if ($scope.selectedType === "Bubble Chart") {
        $scope.displayTextMultiple = "Inputs";
      }
      if (($scope.selectedType === "Scatter Plot") && ($scope.template === 3)) {
        $scope.displayTextMultiple = "y Axis";
      }
    } else if ($scope.selectedInputs.length === 1) {
      $scope.displayTextMultiple = $scope.selectedInputs[0].input;
    } else {
      $scope.displayTextMultiple = dropDownDisplayText($scope.selectedInputs);
    }
    return $scope.displayTextMultiple;
  };

  $scope.displayTextOne = function () {
    if ($scope.selectedInputs === undefined) {
      $scope.displayTextSingle = "x Axis";
    }
  };

  function populateParticipants() {
    $scope.participants = [];
    experimentsVizService.getParticipants($scope.experimentId).then(function (participants) {
      participants.data.customResponse.forEach(function (participant) {
        $scope.participants.push(participant.who);
      });
    });
  }

  $scope.getInputs = function () {
    $scope.inputNames = [];
    $scope.groupsSet = new Set();
    if ($scope.selectedInputs !== undefined) {
      $scope.selectedInputs.forEach(function (input) {
        $scope.inputNames.push(input.input);
        $scope.groupsSet.add(input.group);
      });
    }
    $scope.drawButton = false;
  };

  $scope.getInput1 = function () {
    $scope.drawButton = false;
  };

  $scope.getSelectedType = function () {
    $scope.drawButton = false;
  };

  $scope.getParticipants = function () {
    $scope.drawButton = false;
  };

  $scope.getDateTime = function () {
    $scope.drawButton = false;
  };

  function axisLabels() {
    if ($scope.selectedType === "Bar Chart") {
      $scope.axisLabel1 = "x Axis";
      $scope.yAxisLabel = "Count";
    } else if (($scope.selectedType === "Scatter Plot") && ($scope.template === 4)) {
      $scope.axisLabel2 = "x Axis";
      $scope.yAxisLabel = "Date/Time Series";
    } else if (($scope.selectedType === "Scatter Plot") && ($scope.template === 3)) {
      $scope.axisLabel2 = "x Axis";
      $scope.axisLabel1 = "y Axis";
      $scope.yAxisLabel = undefined;
    } else if ($scope.selectedType === "Box Plot") {
      $scope.axisLabel1 = "x Axis";
      $scope.yAxisLabel = undefined;
    } else if ($scope.selectedType === "Bubble Chart") {
      $scope.axisLabel1 = "Inputs";
      $scope.yAxisLabel = undefined;
    }
    $scope.displayTextMul();
    $scope.displayTextOne();
  }

  function getEventsResponses() {
    $scope.startDateTime = undefined;
    $scope.endDateTime = undefined;

    // start and end date/time stamp based on a date
    if ($scope.startDate != undefined) {
      $scope.startDateTime = formatDate($scope.startDate) + " " + "00:00:00";
      $scope.endDateTime = formatDate($scope.startDate) + " " + "23:59:59";
    }
    // start and end date/time stamp based on a date range
    if ($scope.endDate != undefined) {
      $scope.startDateTime = formatDate($scope.startDate) + " " + "00:00:00";
      $scope.endDateTime = formatDate($scope.endDate) + " " + "23:59:59";
    }
    // start and end date/time stamp based on a date and time range
    if ($scope.startDate != undefined && $scope.startTime != undefined && $scope.endTime != undefined) {
      $scope.startDateTime = formatDate($scope.startDate) + " " + formatTime($scope.startTime);
      $scope.endDateTime = formatDate($scope.startDate) + " " + formatTime($scope.endTime);
    }
    // start and end date/time stamp based on a date range and time range
    if ($scope.startDate != undefined && $scope.endDate != undefined && $scope.startTime != undefined && $scope.endTime != undefined) {
      $scope.startDateTime = formatDate($scope.startDate) + " " + formatTime($scope.startTime);
      $scope.endDateTime = formatDate($scope.endDate) + " " + formatTime($scope.endTime);
    }

    if ($scope.selectedInputs !== undefined) {
      var groupNInputs = [];
      $scope.responseData = [];
      var responses = [];
      var key = "";
      $scope.selectedInputs.forEach(function (inputs) {
        var data = {};
        data.key = inputs.input;
        getEvents = experimentsVizService.getEvents($scope.experimentId, inputs.group, inputs.input, $scope.selectedParticipants, $scope.startDateTime, $scope.endDateTime).then(function (events) {
          if (events.data.customResponse !== undefined) {
            if (events.data.customResponse.length > 0) {
              responses = events.data.customResponse;
              data.values = responses;
              $scope.responseData.push(data);
            } else {
              showAlert("Zero data", "No data available for the selection: " + inputs.input);
            }
          }
        });
      });
    }

    if ($scope.selectedInput1 !== undefined) {
      var responses = [];
      var data = {};
      $scope.xPlotInput = [];
      $scope.singleInputResponseData = [];
      data.key = $scope.selectedInput1.input;
      getEvents = experimentsVizService.getEvents($scope.experimentId, $scope.selectedInput1.group, $scope.selectedInput1.input, $scope.selectedParticipants, $scope.startDateTime, $scope.endDateTime).then(function (events) {
        if (events.data.customResponse !== undefined) {
          if (events.data.customResponse.length > 0) {
            responses = events.data.customResponse;
            data.values = responses;
            if ($scope.template === 3) {
              $scope.xPlotInput.push(data);
            }
            if (($scope.template === 4) || ($scope.template === 2)) {
              $scope.singleInputResponseData.push(data);
            }
          }
        }
      });
    }
  }

  function displayViz(viz) {
    if (($scope.selectedType === "Box Plot") && ($scope.template === 1)) {
      processBoxData($scope.responseData);
    }
    if (($scope.selectedType === "Bar Chart") && ($scope.template === 1)) {
      processBarChartData($scope.responseData);
    }
    if ($scope.selectedType === "Bubble Chart") {
      processBubbleChartData($scope.responseData);
    }
    if (($scope.selectedType === "Box Plot") && ($scope.template === 2)) {
      processBoxData($scope.singleInputResponseData);
    }
    if (($scope.selectedType === "Bar Chart") && ($scope.template === 2)) {
      processBarChartData($scope.singleInputResponseData);
    }
    if (($scope.selectedType === "Scatter Plot") && ($scope.template === 4)) {
      processXYPlotTimeSeries($scope.singleInputResponseData);
    }
    if (($scope.selectedType === "Scatter Plot") && ($scope.template === 3)) {
      processScatterPlot($scope.responseData);
      $scope.vizTemplate = true;
    }
    displayDescription(viz);
    displayTitle(viz);
  }

  function clearViz() {
    d3.selectAll('.vizContainer' + "> *").remove();
    $scope.vizTemplate = false;
    $scope.saveDownload = false;
    $scope.drawButton = true;
  }

  function displayDescription(viz) {

    if (viz !== undefined) {
      $scope.vizDesc = viz.vizDesc;
      $scope.vizDescription = $sce.trustAsHtml("<pre class='descText'>" + viz.vizDesc + "</pre>");
    } else {
      var participantsDesc = [];
      var dateDesc = " ";
      var timeDesc = " ";
      if ($scope.participantsCount === $scope.selectedParticipants.length) {
        participantsDesc.push("All participants")
      } else {
        participantsDesc = $scope.selectedParticipants.join(', ');
      }
      if ($scope.startDate != undefined) {
        dateDesc = formatDate($scope.startDate);
      }
      if ($scope.startDate != undefined && $scope.endDate != undefined) {
        dateDesc = formatDate($scope.startDate) + " - " + formatDate($scope.endDate);
      }
      if ($scope.startDate === undefined && $scope.endDate === undefined) {
        dateDesc = $scope.dateRange[0] + " - " + $scope.dateRange[1];
      }
      if ($scope.startTime != undefined) {
        timeDesc = "Time Range: " + formatTime($scope.startTime);
      }
      if ($scope.startTime != undefined && $scope.endTime != undefined) {
        timeDesc = "Time Range: " + formatTime($scope.startTime) + " - " + formatTime($scope.endTime);
      }

      if ($scope.template == 1) {
        $scope.vizDesc = "Participants: " + participantsDesc + "<br>"
            + "Date Range: " + $scope.dateRange[0] + " - " + $scope.dateRange[1];
        $scope.vizDescription = $scope.vizDesc;
      } else if ($scope.template === 2 || $scope.template === 3 || $scope.template === 4) {
        $scope.vizDesc = "Participants: " + participantsDesc + "<br>" + "Date Range: " + dateDesc + "<br>" + timeDesc;
        $scope.vizDescription = $scope.vizDesc;
      }
    }
  }

  function displayTitle(viz) {
    if ($scope.selectedInputs !== undefined) {
      if ($scope.responseData !== undefined) {
        var inputNames = [];
        $scope.titles = [];
        $scope.selectedInputs.forEach(function (input) {
          $scope.titles.push(input.input);
        });
      }
    }
    if (viz !== undefined) {
      $scope.vizTitle = viz.vizTitle;
    } else {
      if ((($scope.selectedType === "Box Plot") || ($scope.selectedType === "Bar Chart") || ($scope.selectedType === "Bubble Chart")) && ($scope.template === 1)) {
        $scope.vizTitle = "Distribution of responses for: " + $scope.titles.join(", ");
      } else if (($scope.selectedType === "Scatter Plot") && ($scope.selectedInput1 !== undefined) && ($scope.template === 3)) {
        $scope.vizTitle = "Correlation between '" + $scope.selectedInput1.input + "' and '" + $scope.titles.join(", ") + "'";
      } else if (($scope.selectedType === "Scatter Plot") && ($scope.selectedInput1 !== undefined) && ($scope.template === 4)) {
        $scope.vizTitle = "Value of '" + $scope.selectedInput1.input + "' over time for each person.";
      } else if ((($scope.selectedType === "Box Plot") || ($scope.selectedType === "Bar Chart")) && ($scope.selectedInput1 !== undefined) && ($scope.template === 2)) {
        $scope.vizTitle = "Distribution of responses for: " + $scope.selectedInput1.input;
      }
    }
  }

  function formatDate(dateValue) {
    var format = 'yyyy/MM/dd';
    var formattedDate = $filter('date')(new Date(dateValue), format);
    return formattedDate;
  }

  function formatTime(timeValue) {
    var format = 'HH:mm:ss';
    var formattedTime = $filter('date')(new Date(timeValue), format);
    return formattedTime;
  }

  $scope.participantsLength = "";
  $scope.getParticipantsLength = function () {
    var participantsLength = "";
    if ($scope.selectedParticipants === undefined) {
      participantsLength = "0 Participants";
    } else if ($scope.selectedParticipants.length === 1) {
      participantsLength = $scope.selectedParticipants;
    } else {
      $scope.participantsLength = dropDownDisplayText($scope.selectedParticipants);
      participantsLength = $scope.participantsLength;
    }
    return participantsLength;
  };

  function dropDownDisplayText(selection) {
    var display_text = "";
    if (selection.length === 1) {
      display_text = selection;
    } else {
      if (selection === $scope.selectedInputs) {
        display_text = selection.length + " Inputs";
      } else if (selection === $scope.selectedParticipants) {
        display_text = selection.length + " Participants";
      }
    }
    return display_text;
  }

  function processXYPlotTimeSeries(responseData) {

    $timeout(function () {
      var xAxisMaxMin = [];
      var xAxisTickValues = [];
      var yValues = new Set();

      if ($scope.startDateTime !== undefined && $scope.endDateTime !== undefined) {
        xAxisMaxMin.push(new Date($scope.startDateTime).getTime(), new Date($scope.endDateTime).getTime());
      } else if ($scope.dateRange !== undefined && $scope.startDateTime === undefined && $scope.endDateTime === undefined) {
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
      $scope.loadViz = false;
      $scope.vizTemplate = true;
    }, 1000);
  }

  function drawXYPlotTimeSeries(xAxisMaxMin, yAxisMaxMin, xAxisTickValues, yAxisTickValues, data) {

    d3.selectAll('.vizContainer' + "> *").remove();
    $timeout(function () {
      var response = responseTypeMap.get($scope.selectedInput1.input);
      var responseType = response.responseType;
      // create the chart
      var chart;
      nv.addGraph(function () {
        chart = nv.models.scatterChart()
            .showDistX(true)
            .showDistY(true)
            .useVoronoi(true)
            .interactive(true)
            .xDomain(xAxisMaxMin)
            .height(550)
            .color(d3.scale.category10().range())
            .duration(300);

        chart.xAxis
        // .rotateLabels(-45)
            .tickValues(xAxisTickValues)
            .tickFormat(function (d) {
              return d3.time.format('%m/%d/%y %H:%M:%S')(new Date(d));
            });
        chart.yAxis
            .tickFormat(d3.format('d'));

        if ((responseType === "likert") || (responseType === "likert_smileys")) {
          chart.yDomain([1, 5]);
          chart.yAxis.tickValues([2, 3, 4]);
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
              "<td class='x-value'><strong>" + $filter('date')(new Date(d.point.x), 'dd/MM/yyyy hh:mm:ss') + "</strong></td>" +
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
        return chart;
      });
    }, 1000);
  }

  function processScatterPlot(responseData) {

    $timeout(function () {
      if ($scope.xPlotInput !== undefined && responseData !== undefined) {
        var xValue = $scope.xPlotInput;
        var yValue = responseData;

        var data = [];
        for (var i = 0; i < yValue.length; i++) {
          data.push({
            key: yValue[i].key,
            values: []
          });

          for (var j = 0; j < xValue[0].values.length; j++) {
            data[i].values.push({
              x: xValue[0].values[j].answer,
              y: yValue[i].values[j].answer,
              size: Math.round(Math.random() * 100) / 100
            });
          }
        }
        drawScatterPlot(data);
        $scope.loadViz = false;
      }
    }, 1000);
  }

  function drawScatterPlot(data) {
    d3.selectAll('.vizContainer' + "> *").remove();

    $timeout(function () {
      // create the chart
      var chart;
      nv.addGraph(function () {
        chart = nv.models.scatterChart()
            .showDistX(true)
            .showDistY(true)
            .useVoronoi(true)
            .height(500)
            .color(d3.scale.category10().range())
            .duration(300);

        chart.xAxis
        // .rotateLabels(-45)
            .tickFormat(d3.format('.0f'))
            .axisLabel($scope.xPlotInput[0].key);
        chart.yAxis.tickFormat(d3.format('.0f'));

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
        return chart;
      });
    }, 1000);
  }

  function incompatibleDataTypes(keyText, multipleKeys, vizLength) {
    if (keyText !== undefined) {
      $mdDialog.show($mdDialog.confirm()
          .title('Incompatible Data')
          .textContent('Could not display the viz(s) " ' + keyText + ' " due to incompatible data type(s).')
          .ariaLabel('Incompatible Data')
          .ok('ok')
      ).then(function () {
        if ((!multipleKeys) && (vizLength === 0)) {
          clearViz();
          $scope.loadViz = false;
        } else {
          var eliminatedTitles = keyText.split(",");
          eliminatedTitles.forEach(function (title) {
            $scope.titles.splice($scope.titles.indexOf(title), 1);
          });
          $scope.vizTitle = "Distribution of responses for: " + $scope.titles.join(", ");
        }
      });
    }
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

    $timeout(function () {
      if (response !== undefined) {
        if ($scope.template === 2) {
          var vizByDay = dataCount(response[0].values);
          vizByDay.forEach(function (data) {
            var dataTransformed = transformBoxPlotData("vizByDay", data.key, data.values.answers);
            if (dataTransformed !== undefined) {
              boxPlotData.push(dataTransformed);
            } else {
              keyText = response[0].key;
              multipleKeys = false;
            }
          });
          $scope.loadViz = false;
        } else if ($scope.template === 1) {
          response.forEach(function (res) {
            var dataTransformed = transformBoxPlotData("vizByDateRange", res.key, res.values);
            if (dataTransformed !== undefined) {
              boxPlotData.push(dataTransformed);
            } else {
              resKeys.push(res.key);
              keyText = resKeys.join(",");
              if (resKeys.length === 1) {
                multipleKeys = false;
              } else if (resKeys.length > 1) {
                multipleKeys = true;
              }
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
          $scope.loadViz = false;
        }
        incompatibleDataTypes(keyText, multipleKeys, boxPlotData.length)
      }
      $scope.vizTemplate = true;
    }, 1000);
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
        if ($scope.template === 2) {
          var response = responseTypeMap.get($scope.selectedInput1.input);
          var responseType = response.responseType;
          if ((responseType === "likert") || (responseType === "likert_smileys")) {
            chart.yDomain([1, 5]);
            chart.yAxis.tickValues([2, 3, 4]);
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
        chart.yAxis.axisLabel("Distribution of responses");

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
            // .style('background-color', 'white')
            .style('vertical-align', 'middle')
            .style('display', 'inline-block')
            .datum(boxPlotData)
            .call(chart);

        nv.utils.windowResize(chart.update);
        return chart;
      });
    }
  }

  function processBarChartData(res) {
    var keyText = undefined;
    var multipleKeys = false;
    var resKeys = [];
    var compatibleResponses = [];

    if (res !== undefined) {
      $timeout(function () {
        var barChartData = [];
        var values = [];
        if ($scope.template === 2) {
          barChartData = transformBarChartData_template2(res);
        } else if ($scope.template === 1) {
          res.forEach(function (response) {
            if (responseTypeMap.get(response.key).responseType === "open text") {
              resKeys.push(response.key);
              keyText = resKeys.join(",");
              if (resKeys.length === 1) {
                multipleKeys = false;
              } else if (resKeys.length > 1) {
                multipleKeys = true;
              }
            } else {
              compatibleResponses.push(response);
              barChartData = transformDataforBarChart(undefined, compatibleResponses);
            }
          });
        }
        if (barChartData !== undefined) {
          barChartData.forEach(function (data) {
            data.values.forEach(function (value) {
              values.push(value.y);
            });
          });
        }
        if (values !== undefined) {
          var yAxisValues = values.filter(function (item, pos) {
            return values.indexOf(item) == pos;
          });
        }
        if (barChartData !== undefined && yAxisValues !== undefined) {
          if ((barChartData.length > 0) && (yAxisValues.length > 0)) {
            drawMultiBarChart(barChartData, yAxisValues);
            $scope.vizTemplate = true;
          }
          incompatibleDataTypes(keyText, multipleKeys, barChartData.length);
        }
        $scope.loadViz = false;
      }, 1000);
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
    yAxisValues.sort(d3.ascending);
    d3.selectAll('.vizContainer' + "> *").remove();

    $timeout(function () {
      if (barChartData !== undefined) {
        var chart = nv.models.multiBarChart()
            .showControls(false)
            .showLegend(true)
            .height(700)
            .duration(500)
            .reduceXTicks(false);
        chart.yAxis.tickFormat(d3.format('.0f'));
        chart.yAxis.tickValues(yAxisValues);
        chart.yAxis.axisLabel("Count of responses");
        chart.yAxis.axisLabelDistance(70);
        chart.xAxis.axisLabel("Available options");
        // .rotateLabels(-45);
        chart.tooltip(true);
        chart.tooltip.contentGenerator(function (d) {
          var rows =
              "<tr>" +
              "<td class='key'>" + 'Data: ' + "</td>" +
              "<td class='x-value'>" + d.data.x + "</td>" +
              "</tr>" +
              "<tr>" +
              "<td class='key'>" + 'Frequency: ' + "</td>" +
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
            .style('width', '98%')
            .style('height', 600)
            .style('margin', "auto")
            .style('display', 'block')
            .style('background-color', 'white')
            .style('vertical-align', 'middle')
            .datum(barChartData)
            .call(chart);
        nv.utils.windowResize(chart.update);
        return chart;
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
    $timeout(function () {
      responses_bubbleChart = preProcessStrings(responseData);
      if (responses_bubbleChart !== undefined) {
        if (responses_bubbleChart.length > 0) {
          var bubbleChartData = responses_bubbleChart.map(function (d) {
            d.value = +d["values"];
            return d;
          });
          drawBubbleChart(bubbleChartData);
          $scope.loadViz = false;
        }
      }
      $scope.vizTemplate = true;
    }, 1000);
  }

  function preProcessStrings(responseData) {
    var vizData = [];
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
      }
    });
    vizData.forEach(function (responseData) {
      responseData.forEach(function (data) {
        collectiveResponses.push(data);
      })
    });

    responsesCount = countResponses(collectiveResponses);
    return responsesCount;
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
    var stopwords = ["a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
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
    $timeout(function () {
      if (data !== undefined) {

        var diameter = 800; //max size of the bubbles

        var bubble = d3.layout.pack()
            .sort(null)
            .size([diameter, diameter])
            .padding(1);

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

        //bubbles needs very specific format, convert data to this.
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
              tooltip.html(d.key + ": " + d.value);
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
            .attr("text-anchor", "middle")
            .text(function (d) {
              return d["key"];
            })
            .style("fill", "white")
            .style("font-size", function (d) {
              return Math.min(d.r, (d.r - 8) / this.getComputedTextLength() * 20) + "px";
            })
      }
    }, 1000);
  }


  $scope.createViz = function (viz, event) {
    var reqFieldsCheck = reqFieldsValidation();
    if (reqFieldsCheck) {
      $scope.loadViz = true;
      getEventsResponses();
      if ($scope.currentViz !== undefined) {
        displayViz($scope.currentViz);
      } else if (viz !== undefined) {
        displayViz(viz);
      } else {
        displayViz();
      }
      if (event !== undefined) {
        var vizParamsJson = vizJson();
        var vizLogTitle = getvizLogTitle(vizParamsJson[0]);
        vizParamsJson[0].vizLogTitle = vizLogTitle;
        $scope.vizHistory.push(vizParamsJson);
        if ($scope.vizHistory.length > 1) {
          $scope.backButton = false;
          $scope.forwardButton = false;
        }
      }
      $scope.saveDownload = true;
      $scope.editMode = true;
      $scope.editTextMode = true;
      $scope.drawButton = true;
    }
  };

  function vizJson() {
    var vizs = [];
    var vizData = {};
    vizData.singleText = {};
    vizData.expId = "";
    vizData.vizTitle = "";
    vizData.dateCreated = "";
    vizData.vizQues = "";
    vizData.multipleTexts = [];
    vizData.participants = [];
    vizData.vizType = "";
    vizData.startDateTime = "";
    vizData.endDateTime = "";

    $scope.vizId = new Date().getUTCHours() + new Date().getUTCMinutes() + new Date().getUTCSeconds() + new Date().getUTCMilliseconds();

    if ($scope.selectedInput1 !== undefined) {
      vizData.singleText = $scope.selectedInput1;
    }
    if ($scope.selectedQues !== undefined) {
      vizData.vizQues = $scope.selectedQues;
    }
    if ($scope.selectedType !== undefined) {
      vizData.vizType = $scope.selectedType;
    }
    if ($scope.selectedInputs !== undefined) {
      vizData.multipleTexts = $scope.selectedInputs;
    }
    if ($scope.experimentId !== undefined) {
      vizData.expId = $scope.experimentId;
    }
    if ($scope.vizTitle !== "") {
      vizData.vizTitle = $scope.vizTitle;
    }
    if ($scope.selectedParticipants.length > 0) {
      vizData.participants = $scope.selectedParticipants;
    }
    if ($scope.startDateTime !== undefined) {
      vizData.startDateTime = $filter('date')(new Date($scope.startDateTime), 'EEE, dd MMM yyyy HH:mm:ss Z');
    } else {
      if ($scope.dateRange !== undefined) {
        vizData.startDateTime = $filter('date')(new Date($scope.dateRange[0]), 'EEE, dd MMM yyyy HH:mm:ss Z');
      }
    }
    if ($scope.endDateTime !== undefined) {
      vizData.endDateTime = $filter('date')(new Date($scope.endDateTime), 'EEE, dd MMM yyyy HH:mm:ss Z');
    } else {
      if ($scope.dateRange !== undefined) {
        vizData.endDateTime = $filter('date')(new Date($scope.dateRange[1]), 'EEE, dd MMM yyyy HH:mm:ss Z');
      }
    }
    if ($scope.vizDesc != "") {
      vizData.vizDesc = $scope.vizDesc;
    }
    vizData.vizId = $scope.vizId;
    vizData.dateCreated = $filter('date')(new Date(), 'EEE, dd MMM yyyy HH:mm:ss Z');

    if (vizData.vizId != undefined && vizData.expId != undefined && vizData.vizQues != undefined) {
      vizs.push({
        "vizId": vizData.vizId,
        "experimentId": vizData.expId,
        "vizTitle": vizData.vizTitle,
        "modifyDate": vizData.dateCreated,
        "question": vizData.vizQues,
        "multipleTexts": vizData.multipleTexts,
        "participants": vizData.participants,
        "vizType": vizData.vizType,
        "vizDesc": vizData.vizDesc,
        "startDateTime": vizData.startDateTime,
        "endDateTime": vizData.endDateTime,
        "singleText": vizData.singleText
      });
    } else {
      $mdDialog.show($mdDialog.alert().content('Insufficient data').ariaLabel('Failure').ok('OK'));
    }
    return vizs;

  }

  $scope.editTitle = function () {
    $scope.defaultTitle = $scope.vizTitle;
    $scope.editTitleTextMode = true;
    $scope.editTitleMode = false;
  };

  $scope.editDesc = function () {
    $scope.defaultDesc = $scope.vizDesc;
    $scope.editDescTextMode = true;
    $scope.descTextarea = {
      'height': '81px'
    };
    $scope.editDescMode = false;

    var text = $scope.vizDesc;
    var desc = text.replace(/<br\s*[\/]?>/gi, "\n");
    $scope.vizDescription = desc;
  };

  $scope.prevViz = function () {
    var prevViz = {};
    $scope.vizHistory.forEach(function (viz) {
      if (viz[0].vizId === $scope.vizId) {
        var currentIndex = $scope.vizHistory.indexOf(viz);
        var prevIndex = currentIndex - 1;
        if (prevIndex >= 0) {
          prevViz = $scope.vizHistory[prevIndex];
        } else if (prevIndex < 0) {
          prevViz = $scope.vizHistory[0];
        }
      } else {
        return;
      }
    });
    clearViz();
    $scope.currentViz = prevViz[0];
    setParams(prevViz[0]);
  };

  $scope.nextViz = function () {
    var nextViz = {};
    $scope.vizHistory.forEach(function (viz) {
      if (viz[0].vizId === $scope.vizId) {
        var currIndex = $scope.vizHistory.indexOf(viz);
        var nextIndex = currIndex + 1;
        var limit = $scope.vizHistory.length;
        if (nextIndex < limit) {
          nextViz = $scope.vizHistory[currIndex + 1];
        } else if (nextIndex >= limit) {
          nextViz = $scope.vizHistory[limit - 1];
        }
      } else {
        return;
      }
    });
    clearViz();
    $scope.currentViz = nextViz[0];
    setParams(nextViz[0]);
  };

  $scope.confirmTitle = function () {
    $scope.editTitleMode = true;
    $scope.editTitleTextMode = false;
    var vizJson_titleEdited = vizJson();
    var vizLogTitle = getvizLogTitle(vizJson_titleEdited[0], "Title Edited");
    vizJson_titleEdited[0].vizLogTitle = vizLogTitle;

    $scope.vizHistory.push(vizJson_titleEdited);
    if ($scope.vizHistory.length > 1) {
      $scope.backButton = false;
      $scope.forwardButton = false;
    }
  };

  $scope.resetTitle = function () {
    $scope.vizTitle = $scope.defaultTitle;
    $scope.editTitleMode = true;
    $scope.editTitleTextMode = false;
  };

  $scope.confirmDesc = function () {
    $scope.vizDesc = $scope.vizDescription;
    var vizJson_descEdited = vizJson();
    var vizLogTitle = getvizLogTitle(vizJson_descEdited[0], "Description Edited");
    vizJson_descEdited[0].vizLogTitle = vizLogTitle;

    $scope.vizHistory.push(vizJson_descEdited);
    $scope.vizDescription = $sce.trustAsHtml("<pre class='descText'>" + $scope.vizDesc + "</pre>");
    if ($scope.vizHistory.length > 1) {
      $scope.backButton = false;
      $scope.forwardButton = false;
    }
    $scope.editDescMode = true;
    $scope.editDescTextMode = false;
  };

  $scope.resetDesc = function () {
    $scope.vizDesc = $scope.defaultDesc;
    $scope.vizDescription = $sce.trustAsHtml("<pre class='descText'>" + $scope.vizDesc + "</pre>");
    $scope.editDescMode = true;
    $scope.editDescTextMode = false;
  };

  function reqFieldsValidation() {
    var msgTitle = "Required Fields";
    if (($scope.template === 1)) {
      if (($scope.selectedType === undefined) && ($scope.selectedInputs === undefined)) {
        showAlert(msgTitle, "Please select Viz Type and x axis value(s).");
        return false;
      }
      if ($scope.selectedType === undefined) {
        showAlert(msgTitle, "Please select Viz Type.");
        return false;
      }
      if ($scope.selectedInputs === undefined) {
        showAlert(msgTitle, "Please select the x axis value(s).");
        return false;
      }
    }

    if (($scope.template === 2)) {
      if (($scope.selectedType === undefined) && ($scope.selectedInput1 === undefined)) {
        showAlert(msgTitle, "Please select Viz Type and x axis value(s).");
        return false;
      }
      if ($scope.selectedType === undefined) {
        showAlert(msgTitle, "Please select Viz Type.");
        return false;
      }
      if ($scope.selectedInput1 === undefined) {
        showAlert(msgTitle, "Please select the x axis value(s).");
        return false;
      }
    }

    if ($scope.template === 3) {
      if ((($scope.selectedType === undefined) && ($scope.selectedInputs === undefined) && ($scope.selectedInput1 === undefined))) {
        showAlert(msgTitle, "Please select Viz Type, x axis value and y axis value(s).");
        return false;
      }
      if ($scope.selectedType === undefined) {
        showAlert(msgTitle, "Please select Viz Type.");
        return false;
      }
      if (($scope.selectedInputs === undefined) && ($scope.selectedInput1 === undefined)) {
        showAlert(msgTitle, "Please select x axis and y axis values.");
        return false;
      }
      if (($scope.selectedInputs === undefined)) {
        showAlert(msgTitle, "Please select the y axis value(s).");
        return false;
      }
      if (($scope.selectedInput1 === undefined)) {
        showAlert(msgTitle, "Please select the x axis value.");
        return false;
      }
    }

    if ($scope.template === 4) {
      if (($scope.selectedType === undefined) && ($scope.selectedInput1 === undefined)) {
        showAlert(msgTitle, "Please select Viz Type and x axis value.");
        return false;
      }
      if ($scope.selectedType === undefined) {
        showAlert(msgTitle, "Please select Viz Type.");
        return false;
      }
      if ($scope.selectedInput1 === undefined) {
        showAlert(msgTitle, "Please select the x axis value.");
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

  $scope.saveViz = function () {
    if ($scope.renderSavedViz) {
      experimentsVizService.getExperiment($scope.experimentId).then(function successCallback(experimentData) {
        experimentData.results[0].visualizations[vizIndex].experimentId = $scope.experimentId;
        experimentData.results[0].visualizations[vizIndex].modifyDate = $filter('date')(new Date(), 'EEE, dd MMM yyyy HH:mm:ss Z');
        experimentData.results[0].visualizations[vizIndex].participants = $scope.selectedParticipants;
        experimentData.results[0].visualizations[vizIndex].question = $scope.selectedQues;
        experimentData.results[0].visualizations[vizIndex].multipleTexts = $scope.selectedInputs;
        experimentData.results[0].visualizations[vizIndex].vizTitle = $scope.vizTitle;
        experimentData.results[0].visualizations[vizIndex].vizType = $scope.selectedType;
        experimentData.results[0].visualizations[vizIndex].vizDesc = $scope.vizDesc;
        experimentData.results[0].visualizations[vizIndex].startDateTime = $filter('date')(new Date($scope.startDateTime), 'EEE, dd MMM yyyy HH:mm:ss Z');
        experimentData.results[0].visualizations[vizIndex].endDateTime = $filter('date')(new Date($scope.endDateTime), 'EEE, dd MMM yyyy HH:mm:ss Z');
        if ($scope.template === 3) {
          experimentData.results[0].visualizations[vizIndex].singleText = $scope.selectedInput1;
        } else if ($scope.template === 4) {
          experimentData.results[0].visualizations[vizIndex].singleText = $scope.selectedInput1;
        }
        experimentsVizService.saveVisualizations(experimentData.results[0]).then(function (res) {
          if (res.data[0].status === true) {
            showAlert("Edit Status", "Viz Edited" + "<br>" + " Saving Viz...");
            $timeout(function () {
              location.reload();
            }, 1000);
          } else {
            $mdDialog.show($mdDialog.alert().title('Edit Status').content('Could not edit viz due to ' + res.data[0].errorMessage).ariaLabel('Success').ok('OK'));
          }
        });
      });
    } else {
      var saveVizs = vizJson();
      if (saveVizs.length > 0) {
        experimentsVizService.getExperiment($scope.experimentId).then(function successCallback(experimentData) {
          saveVizs.forEach(function (viz) {
            experimentData.results[0].visualizations.push(viz);
          });

          experimentsVizService.saveVisualizations(experimentData.results[0]).then(function (res) {
            if (res.data[0].status === true) {
              showAlert("Save Status", "Saving Viz...");
              $timeout(function () {
                location.reload();
              }, 1000);
            } else {
              $mdDialog.show($mdDialog.alert().title('Save Status').content('Could not save viz due to error: ' + res.data[0].errorMessage).ariaLabel('Success').ok('OK'));
            }
          });
        });
      }
    }
  };

  $scope.renderViz = function (viz, index) {
    $scope.vizId = viz.vizId;
    renderSavedViz(viz, index, "savedViz");
  };

  function setParams(viz) {
    var startTime = "";
    var endTime = "";
    var startDate = "";
    var endDate = "";

    if (viz.question !== undefined) {
      $scope.selectedQues = viz.question;
      $scope.getTemplate();
    }

    if (viz.vizType !== undefined) {
      $scope.selectedType = viz.vizType;
    }

    if (viz.multipleTexts !== undefined) {
      $scope.selectedInputs = [];
      $scope.selectedInputs = viz.multipleTexts;
      $scope.getInputs();
    }

    if (viz.singleText !== undefined) {
      if ($scope.template === 2) {
        $scope.selectedInput1 = viz.singleText;
      }
      if ($scope.template === 3) {
        $scope.selectedInput1 = viz.singleText;
      }
      if ($scope.template === 4) {
        $scope.selectedInput1 = viz.singleText;
      }
    }

    if (viz.participants !== undefined) {
      $scope.selectedParticipants = viz.participants;
    }

    if (viz.startDateTime != undefined && viz.endDateTime != undefined) {
      $scope.startDateTime = viz.startDateTime;
      $scope.endDateTime = viz.endDateTime;
      startDate = $filter('date')(new Date($scope.startDateTime), 'dd/MM/yyyy');
      endDate = $filter('date')(new Date($scope.endDateTime), 'dd/MM/yyyy');
      if (startDate == endDate) {
        $scope.startDate = $filter('date')(new Date($scope.startDateTime), 'EEE, dd MMM yyyy HH:mm:ss Z');
        $scope.endDate = undefined;
      } else {
        $scope.startDate = $filter('date')(new Date($scope.startDateTime), 'EEE, dd MMM yyyy HH:mm:ss Z');
        $scope.endDate = $filter('date')(new Date($scope.endDateTime), 'EEE, dd MMM yyyy HH:mm:ss Z');
      }
      startTime = $filter('date')($scope.startDateTime, 'HH:mm:ss');
      endTime = $filter('date')($scope.endDateTime, 'HH:mm:ss');
      if (startTime == '00:00:00' || startTime == '23:59:59') {
        $scope.startTime = undefined;
      } else {
        $scope.startTime = new Date($filter('date')($scope.startDateTime, 'EEE, dd MMM yyyy HH:mm:ss Z'));
      }
      if (endTime == '00:00:00' || endTime == '23:59:59') {
        $scope.endTime = undefined;
      } else {
        $scope.endTime = new Date($filter('date')($scope.endDateTime, 'EEE, dd MMM yyyy HH:mm:ss Z'));
      }
    }
    $scope.vizId = viz.vizId;
    if (viz.vizTitle !== undefined) {
      $scope.vizTitle = viz.vizTitle;
    }
    if (viz.vizDesc !== undefined) {
      $scope.vizDesc = viz.vizDesc;
    }

  }

  function renderSavedViz(viz, index, vizMode) {
    $scope.renderVisualization = true;
    setParams(viz);
    $scope.createViz(viz, undefined);
    if (vizMode === "savedViz") {
      $scope.renderSavedViz = true;
      vizIndex = index;
    }
  }

  $scope.getSelectedViz = function (vizLog) {
    clearViz();
    $scope.currentViz = vizLog[0];
    setParams(vizLog[0]);
    $scope.vizId = vizLog[0].vizId;
  };

  function getvizLogTitle(vizLog, titleOrDescEdited) {
    var vizTexts = "";
    var texts = [];
    if (($scope.template === 1)) {
      if (vizLog.multipleTexts.length > 1) {
        vizLog.multipleTexts.forEach(function (text) {
          texts.push(text.input);
        });
        vizTexts = texts.join(", ");
      } else {
        vizTexts = vizLog.multipleTexts[0].input;
      }
    }
    if (($scope.template === 2) || ($scope.template === 4)) {
      vizTexts = vizLog.singleText.input;
    }
    if (($scope.template === 3)) {
      vizLog.multipleTexts.forEach(function (text) {
        texts.push(text.input);
      });
      texts.push(vizLog.singleText.input);
      vizTexts = texts.join(", ");
    }

    var vizHistoryLogTitle = "";
    if ((vizLog !== undefined) && (titleOrDescEdited === undefined)) {
      vizHistoryLogTitle = "Q" + questionsMap.get(vizLog.question) + "," + vizLog.vizType + "," + vizTexts + "," + vizLog.participants.length + " Participants" + "," + $filter('date')(new Date(vizLog.startDateTime), 'dd/MM/yy') + "-"
          + $filter('date')(new Date(vizLog.endDateTime), 'dd/MM/yy');
    } else if ((vizLog !== undefined) && (titleOrDescEdited !== undefined)) {
      vizHistoryLogTitle = "Q" + questionsMap.get(vizLog.question) + "," + vizLog.vizType + "," + vizTexts + "," + vizLog.participants.length + " Participants" + "," + $filter('date')(new Date(vizLog.startDateTime), 'dd/MM/yy') + "-"
          + $filter('date')(new Date(vizLog.endDateTime), 'dd/MM/yy') + "," + titleOrDescEdited;
    }
    return vizHistoryLogTitle;
  }

  $scope.deleteViz = function (viz, index) {
    $mdDialog.show($mdDialog.confirm()
        .title('Delete Status')
        .content('Do you want to delete the viz: ' + viz.vizTitle + '?')
        .ariaLabel("Delete Viz")
        .cancel('Yes')
        .ok('No')).then(function () {
    }, function () {
      experimentsVizService.getExperiment($scope.experimentId).then(function successCallback(experimentData) {
        experimentData.results[0].visualizations.splice(index, 1);
        experimentsVizService.saveVisualizations(experimentData.results[0]).then(function (res) {
          if (res.data[0].status === true) {
            location.reload();
          } else {
            $mdDialog.show($mdDialog.alert().title('Delete Status').content('Could not delete viz due to ' + res.data[0].errorMessage).ariaLabel('Success').ok('OK'));
          }
        });
      });
    });
  };

  $scope.clearVizTable = function () {
    $mdDialog.show($mdDialog.confirm()
        .title('Confirmation Status')
        .textContent('Do you want to delete all the visualizations?')
        .ariaLabel('Clear All').cancel('Yes')
        .ok('No')
    ).then(function () {
    }, function () {
      experimentsVizService.getExperiment($scope.experimentId).then(function successCallback(experimentData) {
        experimentData.results[0].visualizations = [];
        experimentsVizService.saveVisualizations(experimentData.results[0]).then(function (res) {
          if (res.data[0].status === true) {
            $scope.vizTable = false;
          } else {
            $mdDialog.show($mdDialog.alert().title('Failure').content('Could not delete vizs due to ' + res.data[0].errorMessage).ariaLabel('Failure').ok('OK'));
          }
        });
      });
    });
  };

  if (angular.isDefined($routeParams.experimentId)) {
    $scope.experimentId = parseInt($routeParams.experimentId, 10);
    $scope.getExperiment();
    $scope.loadDataSummary = true;
    $scope.dataSnapshot();

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