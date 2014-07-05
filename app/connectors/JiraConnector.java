package connectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import models.ConnectorConfiguration;

public class JiraConnector extends Connector {

	@Override
	public void run() {
		String jiraURL = ConnectorConfiguration.getValue(getName(), "jiraURL");
		String username = ConnectorConfiguration.getValue(getName(), "username");
		String password = ConnectorConfiguration.getValue(getName(), "password");
		String query= ConnectorConfiguration.getValue(getName(), "query");;
		
		if ((StringUtils.isEmpty(jiraURL)) ||
				(StringUtils.isEmpty(username)) ||
				(StringUtils.isEmpty(password)) ||
				(StringUtils.isEmpty(query))) {
			Logger.error("Jira configuration is not complete");
			return;
		}
		
		
	}
	
	public List<String> getParameterNames() {
		List<String> paramNames = new ArrayList<String>();
		paramNames.add("jiraURL");
		paramNames.add("username");
		paramNames.add("password");
		paramNames.add("query");
		return paramNames;
	}

	
	public String getName() {
		return "Jira";
	}

}
