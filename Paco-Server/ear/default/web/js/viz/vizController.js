"use strict";

pacoApp.controller('VizCtrl', ['$scope', '$element', '$compile', 'experimentsVizService', '$timeout', '$routeParams', '$filter', function ($scope, $element, $compile, experimentsVizService, $timeout, $routeParams, $filter) {


  $scope.responseMetadata = [];
  $scope.group = "";
  $scope.input = "";
  $scope.selectedInputs = [];
  $scope.inputs = [];
  $scope.inputNames = [];
  $scope.selectedType = "";
  $scope.vizChartTypes = [];
  $scope.responseData = [];
  $scope.question = "";
  $scope.selectedQues = "";
  $scope.participants = [];
  $scope.selectedParticipants = [];
  $scope.vizTemplate = false;
  $scope.vizTitle = "";
  $scope.groupInputs = [];
  $scope.selectedGroup = "";
  $scope.groupInputsMap = new Map();
  $scope.template2 = false;


  var responseTypeMap = new Map();
  var responseMetaData = [];
  var responses = [];
  var questionsMap = new Map();
  var getEvents = "";
  var inputsList = [];


  $scope.questions = [{
    qno: 1,
    question: "What is the distribution of responses for the variable?",
  }, {
    qno: 2,
    question: "What is the distribution of responses for the variable by day?",
  }, {
    qno: 3,
    question: "What is the value of the variable over time for a given person?",
  }, {
    qno: 4,
    question: "What is the value of this variable over time for everyone?"
  }, {
    qno: 5,
    question: "How do the responses for input 1 relate to the responses for variable 2?"
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


  //experiment json objects are retrieved from the 'experimentsVizService'
  // to create a scope variable for response type meta data.
  $scope.getExperiment = function () {
    experimentsVizService.getExperiment($scope.experimentId).then(
        function (experiment) {
          if (experiment.status === 404) {
            displayErrorMessage("Experiments ", experiment);
          }
          else {
            $scope.experimentDataModel = {
              id: experiment.results[0].id,
              title: experiment.results[0].title,
              creator: experiment.results[0].creator,
              date: experiment.results[0].modifyDate
            };
            getGroups(experiment.results[0]);
          }
        });
  };

  function getGroups(experiment) {
    $scope.groupInputs = [];
    responseMetaData = [];
    responseTypeMap = new Map();
    $scope.inputs = [];
    $scope.groups = [];
    experiment.groups.forEach(function (groups) {
      $scope.groupInputsMap.set(groups.name, groups.inputs);
      $scope.groups.push(groups.name);
      groups.inputs.forEach(function (input) {
        $scope.groupInputs.push({"group": groups.name, "inputs": input.name});
        $scope.inputs.push(groups.name + ": " + input.name);
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

  var inputs = [];
  $scope.getTemplate = function () {
    var template = "";
    if (questionsMap.has($scope.selectedQues)) {
      template = questionsMap.get($scope.selectedQues);
      var vizContainer = angular.element(document.querySelector('.vizContainer'));
      if (template === 1) {
        $scope.template2 = false;
        $scope.vizTypes = ["Box Plot", "Bar Chart", "Bar Chart with Density plot"];
      } else if (template == 2) {
        $scope.template2 = true;
        $scope.selectedInputs = undefined;
        $scope.selectedType = undefined;
        $scope.selectedParticipants = [];
        $scope.vizTypes = ["Box Plot", "Bar Chart", "Bar Chart with Density plot"];
      }
    }
  };

  $scope.getInputs = function () {
    $scope.participants = [];
    $scope.inputNames = [];
    $scope.groupsSet = new Set();
    inputs = $scope.selectedInputs;
    inputsList = [];
    inputs.forEach(function (input) {
      $scope.inputNames.push(input.inputs);
      $scope.groupsSet.add(input.group);
    });

    experimentsVizService.getParticipants($scope.experimentId).then(function (participants) {
      participants.data.customResponse.forEach(function (participant) {
        $scope.participants.push(participant.who);
      });
    });
    $scope.selectedParticipants = $scope.participants;
  };

  function getEventsResponses() {

    var startDate, endDate, startTime, endTime = "";

    if ($scope.startDate != undefined) {
      startDate = formatDate($scope.startDate);
    }
    if ($scope.endDate != undefined) {
      endDate = formatDate($scope.endDate);
    }
    if ($scope.startTime != undefined) {
      startTime = formatTime($scope.startTime);
    }
    if ($scope.endTime != undefined) {
      endTime = formatTime($scope.endTime);
    }

    var responses = [];
    $scope.groupsSet.forEach(function (group) {

      getEvents = experimentsVizService.getEvents($scope.experimentId, group, $scope.selectedParticipants, startDate, startTime, endDate, endTime).then(function (events) {
        if (events.status === 404) {
          displayErrorMessage('Events', events);
        }
        if (events.data.status === "Success") {
          return events.data.events;
        } else if (events.data.status === "Failure") {
          displayErrorMessage('Query', events.data);
        }
      });
    });

    $scope.responseData = [];
    var responses = [];
    $scope.inputNames.forEach(function (input) {
      getEvents.then(function (events) {
        responses = experimentsVizService.getResponses(events, input);
        if (responses.length > 0) {
          $scope.responseData.push({
            key: input,
            values: responses
          });
        } else {
          $scope.zeroData = "No Data Available";
        }
      });
    });
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


  $scope.inputsLength = "";
  $scope.getInputsLength = function () {
    var inputsLength = "";
    if ($scope.selectedInputs === undefined) {
      inputsLength = "0 Inputs";
    } else if ($scope.selectedInputs.length === 1) {
      inputsLength = $scope.selectedInputs[0].inputs;
    } else {
      $scope.inputsLength = dropDownDisplayText($scope.selectedInputs);
      inputsLength = $scope.inputsLength;
    }
    return inputsLength;
  };

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

  var convertToClassName = function (selectedValue) {
    var inputText = selectedValue;
    var split_selectedValue = inputText.split(" ");
    var join_selectedValue = split_selectedValue.join(".");
    return join_selectedValue;
  };

  function vizHeader(inputName) {

    $timeout(function () {
      var vizClassName = convertToClassName(inputName);
      var vizClass = angular.element(document.querySelector('.' + vizClassName));
      vizClass.prepend("<h3 class='vizHeader'>" + $scope.question + "</h3>");
      vizClass.prepend($compile("<md-button  delete-viz class='md-warn' data=" + '.' + vizClassName + ">" + "x" + "</md-button>")($scope));
    });
  }

  function processBoxData(res) {
    var response = res;
    var label = "";
    var maxValue = 0;
    var data = [];
    var firstHalf = [];
    var secondHalf = [];

    var boxPlotData = [];

    $timeout(function () {
      response.forEach(function (res) {
        var resData = {};
        label = res.key;
        resData.label = label;
        data = [];
        resData.values = {};
        var max, min, median, midPoint, q1, q3 = "";
        res.values.forEach(function (val) {
          data.push(parseInt(val.answer));
        });
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
        resData.values = {Q1: q1, Q2: median, Q3: q3, whisker_low: min, whisker_high: max};
        boxPlotData.push(resData);
      });

      var whiskers_high = [];
      boxPlotData.forEach(function (data) {
        whiskers_high.push(data.values.whisker_high);
      });
      maxValue = d3.max(whiskers_high);
      drawBoxPlot(boxPlotData, maxValue);
    }, 50);

  }

  function drawBoxPlot(boxPlotData, whisker_high) {
    d3.selectAll('.vizContainer' + "> *").remove();
    var inputName = [];
    $timeout(function () {

      function title() {
        boxPlotData.forEach(function (data) {
          inputName.push(" " + data.label);
        });

        return inputName;
      }

      $scope.vizTitle = "Distribution of responses for: " + title();
      nv.addGraph(function () {
        var chart = nv.models.boxPlotChart()
            .x(function (d) {
              return d.label;
            })
            .height(500)
            .staggerLabels(true)
            .maxBoxWidth(50)
            .yDomain([0, whisker_high]);

        chart.xAxis.showMaxMin(false);
        chart.tooltip(true);
        chart.tooltip.contentGenerator(function (d) {
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
            .style('height', 580)
            .style('margin-left', 20)
            .style('margin-top', 50)
            .style('background-color', 'white')
            .style('vertical-align', 'middle')
            .style('display', 'inline-block')
            .datum(boxPlotData)
            .call(chart);

        nv.utils.windowResize(chart.update);

        return chart;
      });

    }, 50);
  }

  function processBarChartData(res) {

    var resData = res;
    var listChoicesMap = new Map();

    //Utility functions
    //map answer indices with list choices
    function mapIndicesWithListChoices(index) {
      var listChoice = " ";
      var index = (parseInt(index) - 1).toString();
      if (listChoicesMap.has(index)) {
        listChoice = listChoicesMap.get(index);
      }
      return listChoice;
    };

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

    var barChartData = [];
    $timeout(function () {

      resData.forEach(function (responseData) {
        if (responseData !== null && responseData !== undefined) {
          var listResponseData = [];
          var chartData = {};
          var choices = "";
          var responsesFrequency = [];
          var responsesMap = new Map();
          chartData.key = responseData.key;

          if (responseTypeMap.has(responseData.key)) {
            var responseType = responseTypeMap.get(responseData.key);
            var response_type = responseType.responseType;
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
    }, 50);
    d3.selectAll('.vizContainer' + "> *").remove();
    drawMultiBarChart(barChartData);

  }

  function drawMultiBarChart(barChartData) {

    var inputName = [];

    $timeout(function () {
      function title() {
        barChartData.forEach(function (data) {
          inputName.push(" " + data.key);
        });

        return inputName;
      }

      $scope.vizTitle = "Distribution of responses for: " + title();
      var chart = nv.models.multiBarChart()
          .showControls(true).showLegend(true)
          .height(580)
          .duration(500);

      chart.yAxis.tickFormat(d3.format('.0f'));
      chart.yAxis.axisLabel("Count of responses");
      chart.xAxis.axisLabel("Available options");
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
            "</tr>" +
            "<tr>" +
            "<td id='participant'>" + 'Participants: ' + "</td>" +
            "<td class='x-value'><strong>" + d.data.participants + "</strong></td>" + "</tr>";

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
          .style('height', 580)
          .style('margin-left', 20)
          .style('margin-top', 20)
          .style('background-color', 'white')
          .style('vertical-align', 'middle')
          .datum(barChartData)
          .call(chart);

      nv.utils.windowResize(chart.update);

      return chart;

    }, 50);
  }

  function processBubbleChartData(data) {
    //frequency of the data
    var responsesFrequency = d3.nest()
        .key(function (d) {
          return d.answer;
        })
        .rollup(function (v) {
          return v.length;
        })
        .entries(data[0].values);

    var bubbleChartData = responsesFrequency.map(function (d) {
      d.value = +d["values"];
      return d;
    });
    drawBubbleChart(data[0].key, bubbleChartData);
  }

  function drawBubbleChart(key, data) {

    var svgClassName = "viz" + " " + key + " " + $scope.selectedType;
    vizHeader(svgClassName);

    // var bubbleChartData = data[0].values;
    var diameter = 600; //max size of the bubbles

    var color = d3.scale.category20c(); //color category

    var bubble = d3.layout.pack()
        .sort(null)
        .size([diameter, diameter])
        .padding(1);

    var tooltip = d3.select("body")
        .append("div")
        .attr("class", "tooltip")
        .text("tooltip");

    var svg = d3.select('.vizContainer')
        .append('div')
        .attr('class', svgClassName)
        .append("svg")
        .attr("display", "block")
        .attr("width", diameter)
        .attr("height", diameter)
        .style("margin", "auto")
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
        .style("fill", function (d, i) {
          return color(i);
        })
        .on("mouseover", function (d) {
          tooltip.text(d.key + ": " + d.value);
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
        .style({
          "fill": "black",
          "font-family": "Helvetica Neue, Helvetica, Arial, san-serif",
          "font-size": "12px"
        });
  }

  $scope.createViz = function () {
    $scope.vizTemplate = true;
    getEventsResponses();
    if ($scope.selectedType === "Box Plot") {
      processBoxData($scope.responseData);

    }
    if ($scope.selectedType === "Bar Chart") {
      processBarChartData($scope.responseData);
    }
  };

  if (angular.isDefined($routeParams.experimentId)) {
    $scope.experimentId = parseInt($routeParams.experimentId, 10);
    $scope.getExperiment();
  }

  function displayErrorMessage(data, error) {
    $scope.vizTemplate = false;
    var message = "";
    var errorData = "";
    if (data == "Query") {
      message = error.errorMessage;
      errorData = "";
    } else {
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