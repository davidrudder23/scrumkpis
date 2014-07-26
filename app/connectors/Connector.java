package connectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.avaje.ebean.Ebean;

import models.ConnectorConfiguration;
import models.ScrumMaster;
import play.Logger;
import play.db.ebean.EbeanPlugin;
import utils.StringUtils;

public abstract class Connector {
	
	public abstract String getName();
	
	public abstract void run(ScrumMaster scrumMaster) throws ConnectorException;
	
	public abstract List<String> getParameterNames();
	
	public static List<Connector> getConnectors() {
		List<Connector> connectors = new ArrayList<Connector>();
		
		connectors.add(new JiraConnector());
		connectors.add(new GitConnector());
		return connectors;
	}
	
	public static Connector getConnector(String connectorName) {
		if (StringUtils.isEmpty(connectorName)) {
			Logger.warn ("getConnector called with null connector name");
			return null;
		}
		
		if (connectorName.equalsIgnoreCase("jira")) {
			return new JiraConnector();
		}
		
		if (connectorName.equalsIgnoreCase("git")) {
			return new GitConnector();
		}
		
		Logger.warn ("Connector "+connectorName+" not found");
		return null;
	}
	
	public String getValue(ScrumMaster scrumMaster, String name) {
		return ConnectorConfiguration.getValue(scrumMaster, getName(), name);
	}
	
	public void setValue (ScrumMaster scrumMaster, String name, String value) {
		ConnectorConfiguration.setValue(scrumMaster, getName(), name, value);
	}
}
