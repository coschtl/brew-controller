package at.dcosta.brew.server.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import at.dcosta.brew.db.InteractionDB;
import at.dcosta.brew.db.ManualAction;
import at.dcosta.brew.db.ManualAction.Type;

@Path("devices")
public class Devices {
	
	//private static final String _GPIO_ = "_GPIO_";
	private final InteractionDB interactionDB;

	public Devices() {
		interactionDB = new InteractionDB();
	}

	@PUT
	@Path("relay/{deviceId}/{action}")
	public void switchRelay(@PathParam("deviceId") String deviceId, @PathParam("action") String action,
			@QueryParam("duration") @DefaultValue("-1") int duration, @QueryParam("arguments") String arguments) {
		ManualAction manualAction = new ManualAction();
		manualAction.setTarget(deviceId);
		manualAction.setType(Type.fromAction(action));
		manualAction.setDurationMinutes(duration);
		manualAction.setArguments(arguments);
		System.out.println(manualAction);
		interactionDB.addEntry(manualAction);
	}
	
//	private String deviceId2PI4JPin(String deviceId) {
//		System.out.println(deviceId);
//		int pos = deviceId.indexOf(_GPIO_);
//		if (pos < 1) {
//			throw new IllegalArgumentException("unknown deviceId: " + deviceId);
//		}
//		return deviceId.substring(pos + _GPIO_.length());
//	}

}
