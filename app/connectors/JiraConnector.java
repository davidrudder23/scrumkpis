package connectors;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.SearchResult;
import net.rcarz.jiraclient.ICredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

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

		try {
	        BasicCredentials creds = new BasicCredentials(username, password);
			JiraClient jiraClient = new JiraClient(jiraURL,creds);
			SearchResult searchResult = jiraClient.searchIssues(query);
			List<Issue> issues = searchResult.issues;
			Logger.debug ("Got "+issues.size()+" issues with query \""+query+"\"");
		} catch (JiraException e) {
			Logger.warn("Could not access jira at url "+jiraURL, e);
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
