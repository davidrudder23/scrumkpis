package controllers;

import java.util.List;

import connectors.JiraConnector;
import models.ConnectorConfiguration;
import models.Employee;
import models.JiraBookmark;
import models.JiraIssue;
import models.ScrumMaster;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;

@Authenticated(AuthenticationUtil.class)
public class JiraController extends ParentController {
	
	public static Result index() {
		Employee employee = Authentication.getLoggedInEmployee();
		List<Issue> bookmarks = employee.getBookmarks();
		
		return ok(views.html.JiraController.index.render(bookmarks));
	}
	
	public static Result issue(String issueKey) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		Employee employee = Authentication.getLoggedInEmployee();
		Issue issue = JiraConnector.getIssue(scrumMaster, issueKey);
		
		if (issue != null) {
			return ok(views.html.JiraController.issue.render(issue, employee));
		} else {
			return redirect(routes.JiraController.index());
		}

	}
	
	public static Result addBookmark(String jiraKey) {
		Employee employee = Authentication.getLoggedInEmployee();
		
		JiraBookmark bookmark = JiraBookmark.find.where().eq("jiraKey", jiraKey).eq("employee", employee).findUnique();
		if (bookmark == null) {
			bookmark = new JiraBookmark();
			bookmark.employee = employee;
			bookmark.jiraKey = jiraKey;
			bookmark.save();
		}
		return redirect(routes.JiraController.issue(jiraKey));
	}
	
	public static Result removeBookmark(String jiraKey) {
		Employee employee = Authentication.getLoggedInEmployee();
		
		List<JiraBookmark> bookmarks = JiraBookmark.find.where().eq("jiraKey", jiraKey).eq("employee", employee).findList();
		if (bookmarks != null) {
			for (JiraBookmark bookmark: bookmarks) {
				bookmark.delete();
			}
		}
		return redirect(routes.JiraController.issue(jiraKey));
	}
	
}
