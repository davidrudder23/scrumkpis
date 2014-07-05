package connectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.avaje.ebean.Ebean;

import models.ConnectorConfiguration;
import play.Logger;
import play.db.ebean.EbeanPlugin;
import utils.StringUtils;

public abstract class Connector {
	
	public abstract String getName();
	
	public abstract void run();
	
	public abstract List<String> getParameterNames();
	
	public static List<Connector> getConnectors() {
		List<Connector> connectors = new ArrayList<Connector>();
		
		connectors.add(new JiraConnector());
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
		
		Logger.warn ("Connector "+connectorName+" not found");
		return null;
	}
	
	public String getValue(String name) {
		return ConnectorConfiguration.getValue(getName(), name);
	}
	
	public void setValue (String name, String value) {
		ConnectorConfiguration.setValue(getName(), name, value);
	}
}
