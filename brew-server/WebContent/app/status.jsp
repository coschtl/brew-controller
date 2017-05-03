<!DOCTYPE html>
<%@page import="at.dcosta.brew.server.gui.JspUtil"%>
<%@page import="at.dcosta.brew.server.SystemState"%> 
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<html>
<head>
<title>Brew Controller Status</title>
<style type="text/css">
img.statusIcon {
	height: 50px;
	width: 50px;
}
</style>
<%
	SystemState state = (SystemState) request.getAttribute("systemState");
	if (state == null) {
		state = new SystemState();
	}
%>
</head>
<script type="text/javascript">
	function update() {
		window.setTimeout((function() {
			;
			location.reload()
		}), 3000);
	}
</script>
<body onload="update();">
	<p><%=DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date())%>:</p>
	<table>
		<tr>
			<td></td>
			<td colspan="2" align="center"><%=JspUtil.getValue(state.getRotation(), "U/min")%><br />
			<br /></td>
			<td></td>
		</tr>
		<tr>
			<td></td>
			<td colspan="2" align="center"><img class="statusIcon"
				src="theme/<%=JspUtil.getOnOffPngImageName(state.isStirrerRunning(), "motor_")%>" /></td>
			<td></td>
		</tr>
		<tr>
			<td></td>
			<td colspan="2" rowspan="2"><img height="150px" width="100px"
				src="theme/pot.png" /></td>
			<td><img class="statusIcon" src="theme/thermometer.png" /><br />&nbsp;<%=JspUtil.getValue(state.getTemperature(0), "°C")%></td>
		</tr>
		<tr>
			<td><img class="statusIcon" src="theme/thermometer.png" /><br /><%=JspUtil.getValue(state.getTemperature(0), "°C")%>&nbsp;</td>
			<td></td>
		</tr>
		<tr>
			<td></td>
			<td><img class="statusIcon"
				src="theme/<%=JspUtil.getOnOffPngImageName(state.isHeaterOn(0), "heater_")%>" /></td>
			<td><img class="statusIcon"
				src="theme/<%=JspUtil.getOnOffPngImageName(state.isHeaterOn(1), "heater_")%>" /></td>
			<td></td>
		</tr> 
		<% if (state.getAvgTemp() != null) { %>
				<tr>
					<td></td>
					<td colspan="2" align="center"><%=JspUtil.getValue(state.getAvgTemp(), "°C")%><br />
					<br /></td>
					<td></td>
				</tr>
		<% } %>	
	</table>
</body>
</html>