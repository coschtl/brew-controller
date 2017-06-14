var queryArgs = null;

function getQueryArguments() {
	if (queryArgs != null) {
		return queryArgs;
	}
	var path = location.search;
	if (path == null || path.length < 3) {
		queryArgs = new Object();
	} else {
		queryArgs = new Object();
		path = path.substring(1);
		var pairs = path.split("&");
		for (var i=0; i< pairs.length; i++) {
			var aktArg = pairs[i].split("=");
			queryArgs[aktArg[0]] = aktArg[1];
		}
	}
	return queryArgs;
}

function getBrewId() {
	args = getQueryArguments();
	if (args["brew"] == null) {
		return -1;
	}
	return args["brew"];
}

function assureKeys(obj, keys) {
	for(var i=0; i<keys.length; i++) {
		if (obj[keys[i]] == null) {
			obj[keys[i]] = new Object();
		}
	}
	return obj;
}
function assureArraySize(obj, size) {
	for(var i=0; i<size; i++) {
		if (obj[i] == null) {
			obj[i] = new Object();
		}
	}
	return obj;
}

function byId(id) {
	return document.getElementById(id);
}

function empty(id) {
	var elm = byId(id);
	return elm.value == null || elm.value == "";
}

function alertEmpty(id, name) {
	if (empty(id) ) {
		alert(name + " fehlt!");
		return true;
	}
	return false;
}

function errorFromHeader(response) {
	error(response.headers.get('x-server-error'));
}

function error(text) {
	var elm = byId("error");
	if (elm == null) {
		alert(text);
	} else {
		elm.innerHTML = maskXml(text);
		show("error");
	}
}

function messageFromHeader(response) {
	message(response.headers.get('x-server-message'));
}

function message(text) {
	var elm = byId("message");
	if (elm == null) {
		alert(text);
	} else {
		elm.innerHTML = maskXml(text);
		show("message");
	}
}

function clearMessages() {
	byId("error").innerHTML = "";
	hide("error");
	byId("message").innerHTML = "";
	hide("message");
}

function show(id, disp) {
	if (disp == null) {
		disp = "block";
	}
	byId(id).style.display = disp;
}

function hide(id) {
	byId(id).style.display = "none";
}

function maskXml(xml) {
	if (xml == null) {
		return xml;
	}
	return xml.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&apos;");
}

function htmlToUnicode(xml) {
	if (xml == null) {
		return xml;
	}
	return xml.replace(/<br\s*\/>/g, "\\n").replace(/<\/?ul>/g, "\\n").replace(/<\/li>/g, "\\n").replace(/<li>/g, "\\t•").replace(/&nbsp;/g, " ");
}