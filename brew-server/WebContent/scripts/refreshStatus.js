function loadStatus() {
	var r = new XMLHttpRequest();
	r.open("GET", contextPath + "/v1/api/states/system", true);
	r.onreadystatechange = function() {
		console.log(r);
		if (r.readyState != 4 || r.status != 200) {
			return;
		}
		var statusData = JSON.parse(r.responseText);
		if (statusData.temperatures[0] == null) {
			show("noStatusAvailable");
			hide("statusTable");
			return;
		}
		hide("noStatusAvailable");
		show("statusTable");
		
		setLinkedPngImage("icon_stirrer", statusData.stirrer, "motor_", "ON / OFF");
		setLinkedPngImage("icon_heater_1", statusData.heaters[0], "heater_", "ON / OFF");
		setLinkedPngImage("icon_heater_2", statusData.heaters[1], "heater_", "ON / OFF");
		setDecimalValue("temperature1", statusData.temperatures[0].value, "°C");
		setLink("temperature1_icon", statusData.temperatures[0], "Temperature [°C]");
		setDecimalValue("temperature2", statusData.temperatures[1].value, "°C");
		setLink("temperature2_icon", statusData.temperatures[1], "Temperature [°C]");
		setDecimalValue("avgTemp", statusData.avgTemp.value, "°C");
		setLink("temperatureAvg_icon", statusData.avgTemp, "Temperature [°C]");
		setValue("time", statusData.timeString);
		if (statusData.stirrerRunning) {
			setDecimalValue("rotationSpeed", statusData.rotation, "U/min");
		} else {
			setValue("rotationSpeed", "");
		}
	};
	r.send();
}

function setLinkedPngImage(id, relay, imageprefix, labelString) {
	if (relay != null) {
		setPngImage(id, relay.on, imageprefix);
		setLink(id, relay, labelString);
	} else {
		setPngImage(id, false, imageprefix);
	}
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

var actionComponent;
var actionLabelString;

function setLink(id, component, labelString) {
	document.getElementById(id).onclick = function() { 
		setAction(id, component, labelString);	
	};
}

function setAction(id, component, labelString) {
	actionComponent = component;
	actionLabelString = labelString;
	
	console.log(component);
	
	if (component.on == null) {
		performAction('showChart');
		return;
	} else if (component.on) {
		document.getElementById("switchOFF").style.display = "block";
		document.getElementById("switchON").style.display = "none";
	} else  {
		document.getElementById("switchON").style.display = "block";
		document.getElementById("switchOFF").style.display = "none";
	}
	overlay('display');
}

function performAction(action, time) {
	if ("showChart" == action) {
		drawChart(actionComponent.id , actionLabelString, "chartCanvas", false);
		document.getElementById("clickChartTab").click(); 
	} else {
		var url = contextPath + "/v1/api/devices/relay/" + actionComponent.id + "/"+ action;
		if (time != null) {
			url += "?duration=" + time;
		}
		fetch (url, {
			  method: 'PUT',
			}).then((response) => {
				console.log(response);
				if (response.status != 204) {
					error(response);
				}
			}).catch((data) => {
				error(data)
			});
	}
	overlay('hide');
}

function setDecimalValue(id, value, scale) {
	var text = value == null ? null : value.toLocaleString( undefined, { maximumFractionDigits: 2 });
	setValue(id, text, scale);
}

function setValue(id, text, scale) {
	if (text != null) {
		if (scale != null) {
			text += "&nbsp;" + scale;
		}
		document.getElementById(id).innerHTML = text;
	}
}