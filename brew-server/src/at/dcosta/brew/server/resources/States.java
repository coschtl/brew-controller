package at.dcosta.brew.server.resources;

import java.text.DateFormat;
import java.util.ArrayList;
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
import at.dcosta.brew.db.InteractionDB;
import at.dcosta.brew.db.Journal;
import at.dcosta.brew.db.JournalEntry;
import at.dcosta.brew.db.ManualAction;
import at.dcosta.brew.db.ManualAction.Type;
import at.dcosta.brew.msg.I18NTexts;
import at.dcosta.brew.server.BrewJournal;
import at.dcosta.brew.server.BrewJournalEntry;
import at.dcosta.brew.server.ChartPoint;
import at.dcosta.brew.server.Relay;
import at.dcosta.brew.server.Sensor;
import at.dcosta.brew.server.SystemState;

@Path("states")
public class States {

	private final IOLog ioLog;
	private final BrewDB brewDB;
	private final InteractionDB interactionDB;
	private final Journal journalDB;

	public States() {
		ioLog = new IOLog();
		brewDB = new BrewDB();
		interactionDB = new InteractionDB();
		journalDB = new Journal();
	}

	@GET
	@Path("brewJournal")
	@Produces(MediaType.APPLICATION_JSON)
	public BrewJournal brewJournal(@QueryParam(value = "brew") int brewId) {
		BrewJournal journal = new BrewJournal();
		DateFormat df = I18NTexts.getTimeFormat(DateFormat.MEDIUM);
		for (JournalEntry entry : journalDB.getEntries(brewId)) {
			journal.addBrewJournalEntry(new BrewJournalEntry(df.format(entry.getTimestamp()), entry.getText()));
		}
		return journal;
	}

	@GET
	@Path("chartData")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ChartPoint> chartData2(@QueryParam(value = "brew") int brewId,
			@QueryParam(value = "componentId") String componentId) {
		List<ChartPoint> points = new ArrayList<>();
		List<IOData> entries = ioLog.getEntries(brewDB.getBrewById(brewId), componentId);
		for (IOData ioData : entries) {
			ChartPoint chartPoint = new ChartPoint();
			chartPoint.setX(ioData.getMeasureTime());
			chartPoint.setY(Double.toString(ioData.getValue()));
			points.add(chartPoint);
		}
		return points;
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
		if (brew != null && brew.getEndTime() == null) {
			List<ManualAction> systemActions = interactionDB.getSystemActions(brew.getStartTime());
			if (systemActions.size() > 0) {
				ManualAction lastAction = systemActions.get(systemActions.size() - 1);
				state.setPaused(lastAction.getType() == Type.PAUSE);
			}
		}
		state.setBrewFinished(brew != null && brew.getEndTime() != null);
		return state;
	}

}
