package controllers;

import java.util.List;

import connectors.JiraConnector;
import models.ConnectorConfiguration;
import models.ScrumMaster;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import play.Logger;
import play.mvc.Result;

public class JiraController extends ParentController {
	
	public static Result index() {
		return ok(views.html.JiraController.index.render());
	}
	
	public static Result issue(String issueKey) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		JiraConnector jiraConnector = new JiraConnector();
		String jiraURL = ConnectorConfiguration.getValue(scrumMaster, jiraConnector.getName(), "jiraURL");
		String username = ConnectorConfiguration.getValue(scrumMaster, jiraConnector.getName(), "username");
		String password = ConnectorConfiguration.getValue(scrumMaster, jiraConnector.getName(), "password");
        BasicCredentials creds = new BasicCredentials(username, password);
        JiraClient jiraClient = new JiraClient(jiraURL,creds);
        
        try {
			Issue issue = jiraClient.getIssue(issueKey);
			return ok(views.html.JiraController.issue.render(issue));
		} catch (JiraException e) {
			Logger.warn("Jira error", e);
			return redirect(routes.JiraController.index());
		}

	}

}
