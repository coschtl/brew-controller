var myChart;

function drawChart(componentId, labelString, canvasId, responsiveRendering) {
	 var r = new XMLHttpRequest();
		r.open("GET", contextPath + "/v1/api/states/chartData?brew=" + getBrewId() + "&componentId=" + componentId, true);
		r.onreadystatechange = function() {
			if (r.readyState != 4 || r.status != 200) {
				return;
			}
			var dateTimeReviver = function (key, value) {
			    var a;
			    if (typeof value === 'string') {
			        a = /.+T.+/.exec(value);
			        if (a) {
			            return new Date(value);
			        }
			    }
			    return value;
			}
			var chartData = JSON.parse(r.responseText,dateTimeReviver);
			var ctx = document.getElementById(canvasId).getContext("2d");
			var data = [
			    {
			       label: 'Temperature',
			       strokeColor: '#A31515',
			       data: chartData,
			    },
			  ];
			var  options = {
                responsive: responsiveRendering,
                bezierCurve: false,
				showTooltips: true,
				scaleShowHorizontalLines: true,
				scaleShowLabels: true,
				scaleType: "date",
				scaleLabel: "<%=value%>°C",
				scaleDateTimeFormat: "dd.mm.yyyy, hh:MM:ss"
            };
			if (myChart != null) {
				myChart.destroy();
			} 
			myChart = new Chart(ctx).Scatter(data, options);
		};
		r.send();
 }