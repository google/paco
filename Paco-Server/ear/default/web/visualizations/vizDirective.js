/**
 * Created by muthuk on 6/27/17.
 */

app.directive('lineChart', function () {
    return {
        restrict: 'E',
        scope: {
            // Bind the data to the directive scope.
            data: '=',
            // Allow the user to change the dimensions of the chart.
            height: '@',
            width: '@'
        },
        // The svg element is needed by D3.

        link: function (scope, element) {

            var margin = {top: 20, right: 20, bottom: 30, left: 50},
                width = 960 - margin.left - margin.right,
                height = 500 - margin.top - margin.bottom;

            // set the ranges
            var x = d3.scale.ordinal().rangeRoundBands([0, width]);
            var y = d3.scale.linear().range([height, 0]);

            var xAxis = d3.svg.axis().scale(x)
                .orient("bottom").ticks(5);
            var yAxis = d3.svg.axis().scale(y)
                .orient("left").ticks(5);

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

            // define the line
            var valueline = d3.svg.line()
                .x(function (d) {
                    //console.log(d.who);
                    return x(d.who);
                })
                .y(function (d) {
                    //console.log(d.value);
                    return y(d.value);
                });

            var svg = d3.select(element[0]).append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform",
                    "translate(" + margin.left + "," + margin.top + ")");

            // Get the data
            var data = scope.data;

            data.forEach(function (d) {
                d.id = d.who;

                var participant = d.who.split('@');
                d.who = participant[0];

                d.value = +d.value;
            }, function (error) {
                console.log(error);
            });

            // Scale the range of the data
            x.domain(data.map(function (d) {
                return d.who;
            }));
            y.domain([0, d3.max(data, function (d) {
                return d.value;
            })]);

            svg.append("path")
                .attr("class", "line")
                .attr("d", valueline(data));


            //draw the scatterplot
            svg.selectAll("dot")
                .data(data)
                .enter().append("circle")
                .attr("r", 5)
                .attr("cx", function (d) {
                    return x(d.who);
                })
                .attr("cy", function (d) {
                    return y(d.value);
                })
                .on("mouseover", function (d) {
                    tooltip.text(d.who + " : " + d.value);
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
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);
            svg.append("g")
                .attr("class", "y axis")
                .call(yAxis);

        }
    }
});

app.directive('bubbleChart', function () {

    return {
        restrict: 'EA',
        transclude: true,
        scope: {
            data: '=',
            height: '@',
            width: '@'
        },
        link: function (scope, elem, attrs) {
            //console.log(scope.data);

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

            var svg = d3.select("body")
                .append("svg")
                .attr("width", diameter)
                .attr("height", diameter)
                .attr("class", "bubble");

            var data = scope.data;
            //convert numerical values from strings to numbers
            data = data.map(function (d) {
                d.value = +d["frequency"];
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
                .style("fill", function(d, i) { return color(i); })
                .on("mouseover", function (d) {
                    tooltip.text(d.number + ": " + d.frequency);
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
                    return d["number"];
                })
                .style({
                    "fill": "black",
                    "font-family": "Helvetica Neue, Helvetica, Arial, san-serif",
                    "font-size": "12px"
                });
        }
    };
});

app.directive('barChart', function () {
    return {
        restrict: 'EA',
        transclude: true,
        scope: {
            data: '='
        },
        link: function (scope, elem, attrs) {
            // set the dimensions of the canvas
            var margin = {top: 20, right: 20, bottom: 70, left: 40},
                width = 900 - margin.left - margin.right,
                height = 350 - margin.top - margin.bottom;

            // set the ranges
            var x = d3.scale.ordinal().rangeRoundBands([0, width], .05);

            var y = d3.scale.linear().range([height, 0]);

            // define the axis
            var xAxis = d3.svg.axis()
                .scale(x)
                .orient("bottom");

            var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left")
                .ticks(10);

            //console.log(elem[0]);
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
                .text("tooltip");

            var data = scope.data;
            data.forEach(function (d) {
                //console.log(d);
                d.id = d.who;
                var participant = d.who.split('@');
                //console.log(participant[0]);
                d.who = participant[0];

                d.value = +d.value;
            });

            // scale the range of the data
            x.domain(data.map(function (d) {
                return d.who;
            }));
            y.domain([0, d3.max(data, function (d) {
                return d.value;
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
                .data(data)
                .enter().append("rect")
                .attr("class", "bar")
                .attr("x", function (d) {
                    return x(d.who);
                })
                .attr("width", x.rangeBand())
                .attr("y", function (d) {
                    return y(d.value);
                })
                .attr("height", function (d) {
                    return height - y(d.value);
                })
                .on("mouseover", function (d) {
                    tooltip.text(d.id + " : " + d.value);
                    tooltip.style("visibility", "visible");
                })
                .on("mousemove", function () {
                    return tooltip.style("top", (d3.event.pageY - 10) + "px").style("left", (d3.event.pageX + 10) + "px");
                })
                .on("mouseout", function () {
                    return tooltip.style("visibility", "hidden");
                });
        }
    }

});

app.directive('scatterPlot', function () {

    return {
        restrict: 'EA',
        transclude: true,
        scope: {
            data: '='
        },
        link: function (scope, elem, attrs) {

            var margin = {
                    top: 20,
                    right: 20,
                    bottom: 30,
                    left: 40
                },
                width = 1000 - margin.left - margin.right,
                height = 500 - margin.top - margin.bottom;

            var x = d3.scale.linear()
                .range([0, width]);

            var y = d3.scale.linear()
                .range([height, 0]);

            var xAxis = d3.svg.axis()
                .scale(x)
                .orient("bottom");

            var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left");

            var svg = d3.select(elem[0]).append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

            var data = create_data(1000);

            data.forEach(function (d) {
                d.x = +d.x;
                d.y = +d.y;
                d.yhat = +d.yhat;
            });

            var line = d3.svg.line()
                .x(function (d) {
                    return x(d.x);
                })
                .y(function (d) {
                    return y(d.yhat);
                });

            x.domain(d3.extent(data, function (d) {
                return d.x;
            }));
            y.domain(d3.extent(data, function (d) {
                return d.y;
            }));

            svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis)
                .append("text")
                .attr("class", "label")
                .attr("x", width)
                .attr("y", -6)
                .style("text-anchor", "end")
                .text("X-Value");

            svg.append("g")
                .attr("class", "y axis")
                .call(yAxis)
                .append("text")
                .attr("class", "label")
                .attr("transform", "rotate(-90)")
                .attr("y", 6)
                .attr("dy", ".71em")
                .style("text-anchor", "end")
                .text("Y-Value");

            svg.selectAll(".dot")
                .data(data)
                .enter().append("circle")
                .attr("class", "dot")
                .attr("r", 3.5)
                .attr("cx", function (d) {
                    return x(d.x);
                })
                .attr("cy", function (d) {
                    return y(d.y);
                });

            svg.append("path")
                .datum(data)
                .attr("class", "line")
                .attr("d", line);

            function create_data(nsamples) {
                var x = [];
                var y = [];
                var n = nsamples;
                var x_mean = 0;
                var y_mean = 0;
                var term1 = 0;
                var term2 = 0;
                var noise_factor = 100;
                var noise = 0;
                // create x and y values
                for (var i = 0; i < n; i++) {
                    noise = noise_factor * Math.random();
                    noise *= Math.round(Math.random()) == 1 ? 1 : -1;
                    y.push(i / 5 + noise);
                    x.push(i + 1);
                    x_mean += x[i]
                    y_mean += y[i]
                }
                // calculate mean x and y
                x_mean /= n;
                y_mean /= n;

                // calculate coefficients
                var xr = 0;
                var yr = 0;
                for (i = 0; i < x.length; i++) {
                    xr = x[i] - x_mean;
                    yr = y[i] - y_mean;
                    term1 += xr * yr;
                    term2 += xr * xr;

                }
                var b1 = term1 / term2;
                var b0 = y_mean - (b1 * x_mean);
                // perform regression

                yhat = [];
                // fit line using coeffs
                for (i = 0; i < x.length; i++) {
                    yhat.push(b0 + (x[i] * b1));
                }

                var data = [];
                for (i = 0; i < y.length; i++) {
                    data.push({
                        "yhat": yhat[i],
                        "y": y[i],
                        "x": x[i]
                    })
                }
                return (data);
            }
        }
    }
});