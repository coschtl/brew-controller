package at.dcosta.brew.server.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.server.Relay;
import at.dcosta.brew.server.Sensor;
import at.dcosta.brew.server.SystemState;

public class StatusServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		InputStream cfgIn = getClass().getClassLoader().getResourceAsStream("configuration.properties");
		try {
			Configuration.initialize(cfgIn);
			cfgIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.init();
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		IOLog ioLog = new IOLog();
		List<IOData> entries = ioLog.getLatestEntries();
		SystemState state = new SystemState();
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
		request.setAttribute("systemState", state);
		request.getRequestDispatcher("app/status.jsp").forward(request, response);
	}

}
