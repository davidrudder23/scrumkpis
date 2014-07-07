package controllers;

import java.util.List;

import models.ScrumMaster;
import connectors.Connector;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;

@Authenticated(AuthenticationUtil.class)
public class ConnectorController extends ParentController {

	public static Result listConnectors() {
		List<Connector> connectors = Connector.getConnectors();
		
		return ok(views.html.ConnectorController.listConnectors.render(connectors));
	}

	public static Result editConnector(String connectorName) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		Connector connector = Connector.getConnector(connectorName);
		if (connector == null) {
			flash().put("error", "Connector "+connectorName+" not found");
			return redirect(controllers.routes.Application.index());
		}
		return ok(views.html.ConnectorController.editConnector.render(scrumMaster, connector));
	}
	
	public static Result updateConnector(String connectorName) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
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
				connector.setValue(scrumMaster, paramName, value);
			}
		}
		
		return redirect(controllers.routes.ConnectorController.editConnector(connectorName));
	}
	
	public static Result runConnector(String connectorName) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		Connector connector = Connector.getConnector(connectorName);
		connector.run(scrumMaster);
		return redirect(controllers.routes.SprintController.sprints());

	}

}
