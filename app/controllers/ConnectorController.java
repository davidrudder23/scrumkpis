package controllers;

import java.util.List;

import connectors.Connector;
import play.Logger;
import play.mvc.Result;

public class ConnectorController extends ParentController {

	public static Result listConnectors() {
		List<Connector> connectors = Connector.getConnectors();
		
		return ok(views.html.ConnectorController.listConnectors.render(connectors));
	}

	public static Result editConnector(String connectorName) {
		Connector connector = Connector.getConnector(connectorName);
		if (connector == null) {
			flash().put("error", "Connector "+connectorName+" not found");
			return redirect(controllers.routes.Application.index());
		}
		return ok(views.html.ConnectorController.editConnector.render(connector));
	}
	
	public static Result updateConnector(String connectorName) {
		Logger.debug ("connector name is "+connectorName);
		Connector connector = Connector.getConnector(connectorName);
		if (connector == null) {
			Logger.debug ("connector is "+connector);
			flash().put("error", "Could not find connector "+connectorName);
			return redirect(controllers.routes.ConnectorController.listConnectors());
		}
		List<String> paramNames = connector.getParameterNames();
		
		for (String paramName: paramNames) {
			String value = getFormValue("paramName-"+paramName);
			Logger.debug ("Setting param "+paramName+" to "+value);
			if (value != null) {
				connector.setValue(paramName, value);
			}
		}
		
		return redirect(controllers.routes.ConnectorController.editConnector(connectorName));
	}
	
	public static Result runConnector(String connectorName) {
		Connector connector = Connector.getConnector(connectorName);
		connector.run();
		return redirect(controllers.routes.ConnectorController.editConnector(connectorName));

	}

}
