// Modified from http://www.javascriptkit.com/javatutors/loadjavascriptcss.shtml

function renderHistogram(graphTarget, data, axisProperties, margins){
    margins = margins||{top: 20, right: 20, bottom: 20, left: 20};

    var svg = d3.select(graphTarget);

    var width = +svg.attr("width") - margins.left - margins.right,
    height = +svg.attr("height") - margins.top - margins.bottom;

    var x = d3.scaleBand().rangeRound([0, width]).padding(0.1),
        y = d3.scaleLinear().rangeRound([height, 0]);

    var g = svg.append("g")
        .attr("transform", "translate(" + margins.left + "," + margins.top + ")");

    var convertedValues = convertNumbersInRow(data['entries'], axisProperties['axisyfield']);

    graphCallBack(convertedValues, x, y, g, axisProperties);

}

function graphCallBack(data, x, y, g, axisProperties) {
  var maxHeight = Math.max.apply(null,y.range());
  x.domain(data.map(function(d) { return d[axisProperties['axisxfield']]; }));
  y.domain([0, d3.max(data, function(d) { return d[axisProperties['axisyfield']]; })]);

  g.append("g")
      .attr("class", "axis axis--x")
      .attr("transform", "translate(0," + maxHeight + ")")
      .call(d3.axisBottom(x))
      .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", "rotate(-65)" );

  g.append("g")
      .attr("class", "axis axis--y")
      .call(d3.axisLeft(y).ticks(axisProperties['axisyticks'], axisProperties['axisyd3format']));

  g.append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", -60)
      .attr("x",0 - (maxHeight / 2))
      .attr("dy", "1em")
      .style("text-anchor", "middle")
      .text(axisProperties['axisylabel']);

  g.selectAll(".bar")
    .data(data)
    .enter().append("rect")
      .attr("class", "bar")
      .attr("x", function(d) { return x(d[axisProperties['axisxfield']]); })
      .attr("y", function(d) { return y(d[axisProperties['axisyfield']]); })
      .attr("width", x.bandwidth())
      .attr("height", function(d) { return maxHeight - y(d[axisProperties['axisyfield']]); });
}

function convertNumbersInRow(d, fieldToConvert) {
  return d.map(
     function(r){
        r[fieldToConvert] = +r[fieldToConvert];
        return r;
     }
  )
}
