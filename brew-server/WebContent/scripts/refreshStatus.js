function loadStatus() {
	var r = new XMLHttpRequest();
	r.open("GET", contextPath + "/v1/api/states/system", true);
	r.onreadystatechange = function() {
		if (r.readyState != 4 || r.status != 200) {
			return;
		}
		var statusData = JSON.parse(r.responseText);
		
		setLinkedPngImage("icon_stirrer", statusData.stirrer, "motor_", "ON / OFF");
		setLinkedPngImage("icon_heater_1", statusData.heaters[0], "heater_", "ON / OFF");
		setLinkedPngImage("icon_heater_2", statusData.heaters[1], "heater_", "ON / OFF");
		setValue("temperature1", statusData.temperatures[0].value, "°C");
		setLink("temperature1_icon", statusData.temperatures[0].id, "Temperature [°C]");
		setValue("temperature2", statusData.temperatures[1].value, "°C");
		setLink("temperature2_icon", statusData.temperatures[1].id, "Temperature [°C]");
		setValue("avgTemp", statusData.avgTemp, "°C");
		setLink("temperatureAvg_icon", "Average", "Temperature [°C]");
		setValue("time", statusData.timeString);
		if (statusData.stirrerRunning) {
			setValue("rotationSpeed", statusData.rotation, "U/min");
		} else {
			setValue("rotationSpeed", "");
		}
	};
	r.send();
}

function setLinkedPngImage(id, relay, imageprefix, labelString) {
	setPngImage(id, relay.on, imageprefix);
	setLink(id, relay.id, labelString);
}

function setPngImage(id, booleanValue, imageprefix) {
	var imageFile = imageprefix;
	if (booleanValue) {
		imageFile += "ON";
	} else {
		imageFile += "OFF";
	}
	imageFile += ".png";
	setImage(id, imageFile);
}


function setImage(id, imageFile) {
	document.getElementById(id).src = "../theme/" + imageFile;
}

function setLink(id, componentId, labelString) {
	document.getElementById(id).onclick = function(){ 
			drawChart(componentId , labelString, "chartCanvas", false);
			document.getElementById("clickChartTab").click(); 
	};
}
	
function setValue(id, text, scale) {
	if (text != null) {
		if (scale != null) {
			text += "&nbsp;" + scale;
		}
		document.getElementById(id).innerHTML = text;
	}
}