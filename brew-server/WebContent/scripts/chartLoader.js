var myChart;

function drawChart(componentId, labelString, canvasId, responsiveRendering) {
	 var r = new XMLHttpRequest();
		r.open("GET", contextPath + "/v1/api/states/chartData?brew=" + getBrewId() + "&componentId=" + componentId, true);
		r.onreadystatechange = function() {
			if (r.readyState != 4 || r.status != 200) {
				return;
			}
			var chartData = JSON.parse(r.responseText);
			
			 var ctx = document.getElementById(canvasId);
			 var config = {
			            type: 'line',
			            data: {
			                labels: chartData.labels,
			                datasets: [{
			                    label: labelString,
			                    data: chartData.data,
			                    fill: false,
			                }]
			            },
			            options: {
			                responsive: responsiveRendering,
			            }
			        }; 
			if (myChart != null) {
				myChart.destroy();
			} 
			myChart = new Chart(ctx,config);
		};
		r.send();
 }