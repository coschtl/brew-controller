package at.dcosta.brew.server.resources;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

import at.dcosta.brew.db.Brew;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.InteractionDB;
import at.dcosta.brew.db.ManualAction;
import at.dcosta.brew.db.ManualAction.Type;
import at.dcosta.brew.server.BrewServerException;

@Path("devices")
public class Devices {

	// private static final String _GPIO_ = "_GPIO_";
	private final InteractionDB interactionDB;
	private final BrewDB brewDB;

	public Devices() {
		interactionDB = new InteractionDB();
		brewDB = new BrewDB();
	}

	@PUT
	@Path("system/{action}")
	public void resumePause(@PathParam("action") String action) {
		ManualAction manualAction = new ManualAction();
		manualAction.setTarget("system");
		Type type = Type.fromAction(action);
		manualAction.setType(type);

		String guid;
		Brew runningBrew = brewDB.getRunningBrew();
		if (runningBrew == null) {
			throw new BrewServerException("Can not set system action. No brew running!", Status.BAD_REQUEST);
		}
		List<ManualAction> systemActions = interactionDB.getSystemActions(runningBrew.getStartTime());
		if (systemActions.isEmpty()) {
			if (type == Type.RESUME) {
				return;
			}
			guid = UUID.randomUUID().toString();
		} else {
			ManualAction lastAction = systemActions.get(systemActions.size() - 1);
			System.out.println("lastAction: " + lastAction);
			if (lastAction.getType() == type) {
				return;
			}
			guid = lastAction.getType() == Type.PAUSE ? lastAction.getArguments() : UUID.randomUUID().toString();
		}
		manualAction.setArguments(guid);
		System.out.println(manualAction);
		interactionDB.addEntry(manualAction);
	}

	@PUT
	@Path("relay/{deviceId}/{action}")
	public void switchRelay(@PathParam("deviceId") String deviceId, @PathParam("action") String action,
			@QueryParam("arguments") String arguments) {
		ManualAction manualAction = new ManualAction();
		manualAction.setTarget(deviceId);
		manualAction.setType(Type.fromAction(action));
		manualAction.setArguments(arguments);
		System.out.println(manualAction);
		interactionDB.addEntry(manualAction);
	}

	// private String deviceId2PI4JPin(String deviceId) {
	// System.out.println(deviceId);
	// int pos = deviceId.indexOf(_GPIO_);
	// if (pos < 1) {
	// throw new IllegalArgumentException("unknown deviceId: " + deviceId);
	// }
	// return deviceId.substring(pos + _GPIO_.length());
	// }

}
