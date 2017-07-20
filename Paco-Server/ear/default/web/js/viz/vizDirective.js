/**
 * Created by muthuk on 6/27/17.
 */
"use strict";

pacoApp.directive('lineChart', [ function() {
    return {
        restrict: 'E',
        scope: {
            // Bind the data to the directive scope.
            responseInfo: '=',
            responseData: '=',
            // Allow the user to change the dimensions of the chart.
            height: '@',
            width: '@'
        },
        link: function (scope, element) {
            scope.$watch('responseData', function (responseData) {
                if (responseData) {
                    var margin = {top: 20, right: 20, bottom: 30, left: 10},
                        width = 960 - margin.left - margin.right,
                        height = 500 - margin.top - margin.bottom;

                    // set the ranges
                    var x = d3.scale.ordinal().rangeRoundBands([0, width], 1);
                    var y = d3.scale.linear().range([height, 0]);

                    var xAxis = d3.svg.axis().scale(x)
                        .orient("bottom").outerTickSize(0);
                    var yAxis = d3.svg.axis().scale(y)
                        .orient("left").ticks(5);

                    var tooltip = d3.select(element[0])
                        .append("div")
                        .style("position", "absolute")
                        .style("z-index", "10")
                        .style("visibility", "hidden")
                        .style("color", "white")
                        .style("padding", "8px")
                        .style("background-color", "rgba(0, 0, 0, 0.75)")
                        .style("border-radius", "6px")
                        .style("font", "12px sans-serif")
                        .text("tooltip");

                    // define the line
                    var valueline = d3.svg.line()
                        .x(function (d) {
                            return x(d.who);
                        })
                        .y(function (d) {
                            return y(d.answer);
                        });

                    var svg = d3.select(element[0]).append("svg")
                        .attr("width", width + margin.left + margin.right)
                        .attr("height", height + margin.top + margin.bottom)
                        .append("g")
                        .attr("transform",
                            "translate(" + margin.left + "," + margin.top + ")");

                    // Get the data
                    responseData.forEach(function (d) {
                        d.id = d.who;

                        var participant = d.who.split('@');
                        d.who = participant[0];
                        //console.log(d.answer);
                        d.answer = +d.answer;
                    });

                    //Scale the range of the data
                    x.domain(responseData.map(function (d) {
                        return d.who;
                    }));
                    y.domain([0, d3.max(responseData, function (d) {
                        return d.answer;
                    })]);

                    svg.append("path")
                        .attr("class", "line")
                        .attr("d", valueline(responseData));

                    //draw the plots
                    svg.selectAll("dot")
                        .data(responseData)
                        .enter().append("circle")
                        .attr("r", 4)
                        .attr("cx", function (d) {
                            return x(d.who);
                        })
                        .attr("cy", function (d) {
                            return y(d.answer);
                        })
                        .on("mouseover", function (d) {
                            tooltip.text(d.who + " : " + d.answer);
                            return tooltip.style("visibility", "visible");
                        })
                        .on("mousemove", function () {
                            return tooltip.style("top", (event.pageY - 10) + "px").style("left", (event.pageX + 10) + "px");
                        })
                        .on("mouseout", function () {
                            return tooltip.style("visibility", "hidden");
                        });

                    svg.append("g")
                        .attr("class", "x axis")
                        .attr("transform", "translate( 0," + height + ")")
                        .call(xAxis)
                        .selectAll("text")
                        .attr("y", 6)
                        .attr("x", 6)
                        .style("text-anchor", "middle");

                    svg.append("g")
                        .attr("class", "y axis")
                        .attr("transform", "translate(70,0)")
                        .call(yAxis);
                }
            });
        }
    }
}]);

pacoApp.directive('bubbleChart', function () {

    return {
        restrict: 'EA',
        transclude: true,
        scope: {
            // Bind the data to the directive scope.
            responseInfo: '=',
            responseData: '=',
            // Allow the user to change the dimensions of the chart.
            height: '@',
            width: '@'
        },
        link: function (scope, elem, attrs) {

            scope.$watch('responseData', function (responseData) {

                if (responseData) {
                    var diameter = 760; //max size of the bubbles

                    var color = d3.scale.category20c(); //color category

                    var bubble = d3.layout.pack()
                        .sort(null)
                        .size([diameter, diameter])
                        .padding(1.5);

                    var tooltip = d3.select("body")
                        .append("div")
                        .style("position", "absolute")
                        .style("z-index", "10")
                        .style("visibility", "hidden")
                        .style("color", "white")
                        .style("padding", "8px")
                        .style("background-color", "rgba(0, 0, 0, 0.75)")
                        .style("border-radius", "6px")
                        .style("font", "12px sans-serif")
                        .text("tooltip");

                    var svg = d3.select(elem[0])
                        .append("svg")
                        .attr("width", diameter)
                        .attr("height", diameter)
                        .attr("class", "bubble");

                    //frequency of the data
                    var responsesFrequency = d3.nest()
                        .key(function (d) {
                            return d.answer;
                        })
                        .rollup(function (v) {
                            return v.length;
                        })
                        .entries(responseData);

                    var data = responsesFrequency.map(function (d) {
                        d.value = +d["values"];
                        return d;
                    });

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
            });
        }
    };
});

pacoApp.directive('barChart', function () {
    return {
        restrict: 'EA',
        transclude: true,
        scope: {
            // Bind the data to the directive scope.
            responseInfo: '=',
            responseData: '=',
            height: '@',
            width: '@'
        },
        link: function (scope, elem, attrs) {

            var responseType = "";
            var listResponseData = [];
            var listChoicesMap = new Map();
            var choices = "";
            var responsesFrequency = "";

            scope.$watch('responseInfo', function (responseInfo) {
                if (responseInfo) {
                    responseType = responseInfo;
                }
            });

            //map answer indices with list choices
            function mapIndicesWithListChoices(index) {
                var listChoice = " ";
                var index = parseInt(index);
                if (listChoicesMap.has(index)) {
                    listChoice = listChoicesMap.get(index);
                }
                return listChoice;
            }

            //split the participant email id
            function parseEmailId(who) {
                var participant = who.split('@');
                return participant;
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

            scope.$watch('responseData', function (responseData) {
                if (responseData) {
                    if (responseType.responseType === "list") {
                        for (var i in responseType.listChoices) {
                            listChoicesMap.set(parseInt(i)+1, responseType.listChoices[i]);
                        }
                        responseData.forEach(function (response) {
                            if (response.answer.length > 1) {
                                var answers = response.answer.split(",");
                                var who = "";
                                answers.forEach(function (a) {
                                    choices = mapIndicesWithListChoices(a);
                                    who = parseEmailId(response.who);
                                    listResponseData.push({"who": who, "answer": choices, "index": a});
                                });
                            } else {
                                response.id = response.who;
                                var participant = response.who.split('@');
                                response.who = participant[0];
                                choices = mapIndicesWithListChoices(response.answer);
                                listResponseData.push({
                                    "who": response.who,
                                    "answer": choices,
                                    "index": response.answer
                                });
                            }
                        });
                        responsesFrequency = responseDataFrequency(listResponseData);
                    } else {
                        responsesFrequency = responseDataFrequency(responseData);
                    }

                    // set the dimensions of the canvas
                    var margin = {top: 20, right: 20, bottom: 70, left: 40},
                        width = 900 - margin.left - margin.right,
                        height = 600 - margin.top - margin.bottom;

                    // set the ranges
                    var x = d3.scale.ordinal().rangeRoundBands([0, width], 0.5);

                    var y = d3.scale.linear().range([height, 0]);

                    // add the SVG element
                    var svg = d3.select(elem[0]).append("svg")
                        .attr("width", width + margin.left + margin.right)
                        .attr("height", height + margin.top + margin.bottom)
                        .append("g")
                        .attr("transform",
                            "translate(" + margin.left + "," + margin.top + ")");

                    var tooltip = d3.select("body")
                        .append("div")
                        .style("position", "absolute")
                        .style("z-index", "10")
                        .style("visibility", "hidden")
                        .style("color", "white")
                        .style("padding", "8px")
                        .style("background-color", "rgba(0, 0, 0, 0.75)")
                        .style("border-radius", "6px")
                        .style("font", "12px sans-serif")
                        .style("width", "150px")
                        .style("height", "auto")
                        .style("word-wrap", "break-word")
                        .text("tooltip");

                    // define the axis
                    var xAxis = d3.svg.axis()
                        .scale(x)
                        .orient("bottom");

                    var yAxis = d3.svg.axis()
                        .scale(y)
                        .orient("left")
                        .ticks(5)
                        .tickFormat(d3.format("d"));

                    // scale the range of the data
                    x.domain(responsesFrequency.map(function (d) {
                        // console.log(d.key);
                        return d.key;
                    }));
                    y.domain([0, d3.max(responsesFrequency, function (d) {
                        return d.values.count;
                    })]);

                    // add axis
                    svg.append("g")
                        .attr("class", "x axis")
                        .attr("transform", "translate(0," + height + ")")
                        .call(xAxis)
                        .selectAll("text")
                        .style("text-anchor", "end")
                        .attr("dx", "-.8em")
                        .attr("dy", "-.55em")
                        .attr("transform", "rotate(-45)");

                    svg.append("g")
                        .attr("class", "y axis")
                        .call(yAxis)
                        .append("text")
                        .attr("transform", "rotate(-90)")
                        .attr("y", 5)
                        .attr("dy", ".71em")
                        .style("text-anchor", "end")
                        .text("Frequency");

                    // Add bar chart
                    svg.selectAll("bar")
                        .data(responsesFrequency)
                        .enter().append("rect")
                        .attr("class", "bar")
                        .attr("x", function (d) {
                            return x(d.key);
                        })
                        .attr("width", 70)
                        .attr("y", function (d) {
                            // console.log(d.values.count);
                            return y(d.values.count);
                        })
                        .attr("height", function (d) {
                            return height - y(d.values.count);
                        })
                        .on("mouseover", function (d) {
                            tooltip.html("<span>" + "Data: " + d.key + "<span/>" + "<br/>" + "<span>" + "Frequency:" + d.values.count + "<span/>" + "<br/>" + "<span>" + "Participants:" + d.values.participants + "<span/>");
                            tooltip.style("visibility", "visible");
                        })
                        .on("mousemove", function () {
                            return tooltip.style("top", (d3.event.pageY - 10) + "px").style("left", (d3.event.pageX + 10) + "px");
                        })
                        .on("mouseout", function () {
                            return tooltip.style("visibility", "hidden");
                    });
                }
            });
        }
    }
});
