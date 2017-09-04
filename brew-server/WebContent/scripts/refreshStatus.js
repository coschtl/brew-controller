var brewIsFinished = false;

function loadStatus() {
	var r = new XMLHttpRequest();
	r.open("GET", contextPath + "/v1/api/states/system?brew=" + getBrewId(), true);
	r.onreadystatechange = function() {
		if (r.readyState != 4 || r.status != 200) {
			return;
		}
		var statusData = JSON.parse(r.responseText);
		if (statusData.temperatures[0] == null) {
			show("noStatusAvailable");
			hide("statusTable");
			return;
		}
		
		if (statusData.paused) {
			document.getElementById("pausedText").style.display = "block";
		} else {
			document.getElementById("pausedText").style.display = "none";
		}

		hide("noStatusAvailable");
		show("statusTable");
		statusData["heaters"] = assureArraySize(statusData["heaters"], 2);
		statusData["temperatures"] = assureArraySize(statusData["temperatures"], 2);
		
		if (statusData.brewFinished) {
			brewIsFinished = true;
			clearInterval(submenuReloadIntervalId);
			clearInterval(statusReloadIntervalId);
			statusData.temperatures[0].value = null;
			statusData.temperatures[1].value = null;
			statusData.avgTemp.value = null;
			statusData.heaters[0].on = false;
			statusData.heaters[1].on = false;
			statusData.stirrer.on = false;
			statusData.stirrerRunning = false;
			statusData.timeString = null;
		}
		
		setLinkedPngImage("icon_stirrer", statusData.stirrer, "motor_", "ON / OFF");
		setLinkedPngImage("icon_heater_1", statusData.heaters[0], "heater_", "ON / OFF");
		setLinkedPngImage("icon_heater_2", statusData.heaters[1], "heater_", "ON / OFF");
		setDecimalValue("temperature1", statusData.temperatures[0].value, "°C");
		setLink("temperature1_icon", statusData.temperatures[0], "Temperature [°C]");
		setDecimalValue("temperature2", statusData.temperatures[1].value, "°C");
		setLink("temperature2_icon", statusData.temperatures[1], "Temperature [°C]");
		setDecimalValue("avgTemp", statusData.avgTemp.value, "°C");
		
		var avgTemp = document.getElementById("Average");
		avgTemp.onclick = function() {
			actionComponent = avgTemp;
			actionLabelString = "Temperature [°C]";
			if (statusData.paused) {
				document.getElementById("resume").style.display = "block";
				document.getElementById("pause").style.display = "none";
			} else {
				document.getElementById("resume").style.display = "none";
				document.getElementById("pause").style.display = "block";
			}
			document.getElementById("switchOFF").style.display = "none";
			document.getElementById("switchON").style.display = "none";
			document.getElementById("switchAUTO").style.display = "none";
			overlay('display');
		};

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
	
	document.getElementById("resume").style.display = "none";	
	document.getElementById("pause").style.display = "none";	
	if (brewIsFinished || component.on == null) {
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

function performAction(action) {
	if ("showChart" == action) {
		drawChart(actionComponent.id , actionLabelString, "chartCanvas", false);
		document.getElementById("clickChartTab").click(); 
	} else {
		var url;
		if ("resume" == action) {
			url = contextPath + "/v1/api/devices/system/" + action;
		} else if ("pause" == action) {
			url = contextPath + "/v1/api/devices/system/" + action;
		} else {
			url = contextPath + "/v1/api/devices/relay/" + actionComponent.id + "/"+ action;
		}
		fetch (url, {
			  method: 'PUT',
			}).then((response) => {
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