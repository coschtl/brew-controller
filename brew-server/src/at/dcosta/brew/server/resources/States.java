package at.dcosta.brew.server.resources;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import at.dcosta.brew.db.Brew;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.server.ChartData;
import at.dcosta.brew.server.Relay;
import at.dcosta.brew.server.Sensor;
import at.dcosta.brew.server.SystemState;

@Path("states")
public class States {

	private final IOLog ioLog;
	private final BrewDB brewDB;

	public States() {
		ioLog = new IOLog();
		brewDB = new BrewDB();
	}

	@GET
	@Path("chartData")
	@Produces(MediaType.APPLICATION_JSON)
	public ChartData chartData(@QueryParam(value = "brew") int brewId,
			@QueryParam(value = "componentId") String componentId) {
		ChartData data = new ChartData();
		DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		String id = null;
		List<IOData> entries = ioLog.getEntries(brewDB.getBrewById(brewId), componentId);
		int modulo = entries.size() / 10;
		if (modulo == 0) {
			modulo = 1;
		}
		int i = 0;
		int max = entries.size() - 1;
		for (IOData ioData : entries) {
			if (id == null) {
				id = ioData.getComponentId();
			} else if (!id.equals(ioData.getComponentId())) {
				continue;
			}
			if (i == 0 || i == max || i % modulo == 0) {
				data.addLabel(df.format(ioData.getMeasureTime()));
			} else {
				data.addLabel("");
			}
			data.addDataValue(Double.toString(ioData.getValue()));
			i++;
		}
		return data;
	}

	@GET
	@Path("system/runningBrew")
	@Produces(MediaType.APPLICATION_JSON)
	public at.dcosta.brew.server.Brew getRunningBrew() {
		Brew runningBrew = brewDB.getRunningBrew();
		at.dcosta.brew.server.Brew dto = new at.dcosta.brew.server.Brew();
		if (runningBrew == null) {
			dto.setId(-1);
		} else {
			dto.setId(runningBrew.getId());
		}
		return dto;
	}

	@SuppressWarnings("incomplete-switch")
	@GET
	@Path("system")
	@Produces(MediaType.APPLICATION_JSON)
	public SystemState getSystemState(@QueryParam("brew") int brewId) {
		Brew brew = brewDB.getBrewById(brewId);
		List<IOData> entries = ioLog.getLatestEntries(brew);
		SystemState state = new SystemState();
		state.setTimeString(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()));
		for (IOData entry : entries) {
			switch (entry.getComponentType()) {
			case RELAY: {
				if (entry.getComponentId().startsWith("Heater")) {
					state.addHeater(new Relay(entry.getComponentId(), entry.getValue() > 0));
				}
				if (entry.getComponentId().startsWith("Stirrer")) {
					state.setStirrerRunning(new Relay(entry.getComponentId(), entry.getValue() > 0));
				}
			}
				break;
			case ROTATION_SPEED_SENSOR:
				state.setRotation(entry.getValue());
				break;
			case TEMPERATURE_SENSOR: {
				if (entry.getComponentId().startsWith("Average")) {
					state.setAvgTemp(entry.getValue());
				} else {
					state.addTemperature(new Sensor(entry.getComponentId(), entry.getValue()));
				}
			}
				break;
			}
		}
		state.setBrewFinished(brew != null && brew.getEndTime() != null);
		return state;
	}

}
