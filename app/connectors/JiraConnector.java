package connectors;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.SearchResult;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

import com.avaje.ebean.Ebean;

import controllers.Authentication;
import controllers.routes;
import play.Logger;
import play.cache.Cache;
import utils.StringUtils;
import models.ConnectorConfiguration;
import models.Employee;
import models.EmployeeSprint;
import models.JiraIssue;
import models.OpenTicketLog;
import models.ScrumMaster;
import models.Sprint;

public class JiraConnector extends Connector {

	@Override
	public void run(ScrumMaster scrumMaster) throws ConnectorException {
		String jiraURL = ConnectorConfiguration.getValue(scrumMaster, getName(), "jiraURL");
		String username = ConnectorConfiguration.getValue(scrumMaster, getName(), "username");
		String password = ConnectorConfiguration.getValue(scrumMaster, getName(), "password");
		String velocityQuery = ConnectorConfiguration.getValue(scrumMaster, getName(), "velocityQuery");
		String churnQuery = ConnectorConfiguration.getValue(scrumMaster, getName(), "churnQuery");
		String untestedQuery = ConnectorConfiguration.getValue(scrumMaster, getName(), "untestedQuery");
		String storyPointsCustomFieldID = ConnectorConfiguration.getValue(scrumMaster, getName(), "storyPointsCustomFieldID");
		String resolverCustomFieldID = ConnectorConfiguration.getValue(scrumMaster, getName(), "resolverCustomFieldID");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		if ((StringUtils.isEmpty(jiraURL)) ||
				(StringUtils.isEmpty(username)) ||
				(StringUtils.isEmpty(password)) ||
				(StringUtils.isEmpty(velocityQuery))) {
			throw new ConnectorException("Jira configuration is not complete");
		}
		
		if (StringUtils.isEmpty(storyPointsCustomFieldID)) {
			// Default for DTDO - yes, I'm cheating so I don't have to remember
			storyPointsCustomFieldID = "10002";
		}

		if (StringUtils.isEmpty(resolverCustomFieldID)) {
			// Default for DTDO - yes, I'm cheating so I don't have to remember
			resolverCustomFieldID = "10320";
		}

		try {
			List<Sprint> activeSprints = Sprint.find.where().eq("scrumMaster", scrumMaster).eq("locked", false).findList();
	        BasicCredentials creds = new BasicCredentials(username, password);
			JiraClient jiraClient = new JiraClient(jiraURL,creds);
			
			for (Sprint sprint: activeSprints) {
				calculateVelocity(velocityQuery, storyPointsCustomFieldID, resolverCustomFieldID, sdf, jiraClient, sprint);
				calculateChurn(churnQuery, resolverCustomFieldID, sdf, jiraClient, sprint);
			}
			
			calculateUntested(untestedQuery, jiraClient, scrumMaster);

		} catch (JiraException e) {
			e.printStackTrace();
			Logger.warn("Could not access jira at url "+jiraURL, e);
		}		
	}

	private void calculateVelocity(String velocityQuery, String storyPointsCustomFieldID, String resolverCustomFieldID, SimpleDateFormat sdf,
			JiraClient jiraClient, Sprint sprint) throws JiraException {
		// Calculate velocity
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		String mungedQuery = velocityQuery.replaceAll("%s", "'"+sdf.format(sprint.startDate)+"'");
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(sprint.startDate);
		endDate.add(Calendar.DAY_OF_YEAR, scrumMaster.sprintLengthInDays);
		
		mungedQuery = mungedQuery.replaceAll("%e", "'"+sdf.format(endDate.getTime())+"'");
		
		Logger.debug ("Using query "+mungedQuery);
		
		SearchResult searchResult = jiraClient.searchIssues(mungedQuery, 1000);

		List<Issue> issues = searchResult.issues;
		Logger.debug ("Got "+issues.size()+" issues with query \""+velocityQuery+"\"");
		
		HashMap<String, Float> storyPointsPerResolver = new HashMap<String, Float>();
		for (Issue issue: issues) {
			float storyPoints = StringUtils.getFloat(issue.getField("customfield_"+storyPointsCustomFieldID).toString(), (float)0);
			String resolver = issue.getField("customfield_"+resolverCustomFieldID).toString();
			Logger.debug ("Story points for issue "+issue.getKey()+"="+storyPoints + " val="+issue.getField("customfield_"+storyPointsCustomFieldID));
			Logger.debug ("Resolver for issue "+issue.getKey()+"="+resolver);
			
			if (storyPoints<=0) {
				continue;
			}
			if (StringUtils.isEmpty(resolver)) {
				continue;
			}
			Float alreadyCountedStoryPoints = storyPointsPerResolver.get(resolver);
			if (alreadyCountedStoryPoints == null) {
				alreadyCountedStoryPoints = new Float(0);
			}
			alreadyCountedStoryPoints = new Float(alreadyCountedStoryPoints.floatValue()+storyPoints);
			storyPointsPerResolver.put(resolver, alreadyCountedStoryPoints);
			
			// Add it to the database
			if (JiraIssue.find.where().eq("jiraKey", issue.getKey()).findRowCount()<=0) {
				JiraIssue model = new JiraIssue();
				model.jiraKey = issue.getKey();
				model.authorName = issue.getReporter().getDisplayName();
				model.authorEmail = issue.getReporter().getEmail();
				model.resolutionSprint = sprint;
				model.summary = issue.getSummary();
				Employee employee = Employee.find.where().eq("jiraID", resolver).findUnique();
				if (employee != null) {
					model.resolver = employee;
				}
				model.save();
			}
		}

		List<EmployeeSprint> employeeSprints = sprint.getEmployeeSprints();
		Logger.debug (employeeSprints.size()+" employee sprints");
		for (EmployeeSprint employeeSprint: employeeSprints) {
			Sprint tmp = employeeSprint.sprint;
			Logger.debug ("Sprint: "+tmp.name);

			Logger.debug ("Employee: "+employeeSprint.employee.id);
			Logger.debug ("Employee first name: "+employeeSprint.employee.firstName);
			Logger.debug ("Employee jira IS: "+employeeSprint.employee.jiraID);
			Employee employee = employeeSprint.employee;
			if (StringUtils.isEmpty(employee.jiraID)) {
				continue;
			}
			
			Float points = storyPointsPerResolver.get(employee.jiraID);
			Logger.debug ("Assigning "+points+" to "+employee.jiraID);
			if (points != null) {
				employeeSprint.storyPointsCompleted = points.floatValue();
				employeeSprint.save();
			}
			
		}
	}
	
	private void calculateChurn(String churnQuery, String resolverCustomFieldID, SimpleDateFormat sdf, JiraClient jiraClient, Sprint sprint) throws JiraException {
		// Calculate velocity
		String mungedQuery = churnQuery.replaceAll("%s", "'"+sdf.format(sprint.startDate)+"'");
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(sprint.startDate);
		endDate.add(Calendar.DAY_OF_YEAR, 14);
		
		mungedQuery = mungedQuery.replaceAll("%e", "'"+sdf.format(endDate.getTime())+"'");
		
		Logger.debug ("Using query "+mungedQuery);
		
		SearchResult searchResult = jiraClient.searchIssues(mungedQuery, 1000);

		List<Issue> issues = searchResult.issues;
		Logger.debug ("Got "+issues.size()+" issues with query \""+churnQuery+"\"");
		
		HashMap<String, Integer> reopensPerResolver = new HashMap<String, Integer>();
		for (Issue issue: issues) {
			String resolver = issue.getField("customfield_"+resolverCustomFieldID).toString();
			Logger.debug ("Resolver for issue "+issue.getKey()+"="+resolver);
			if (StringUtils.isEmpty(resolver)) {
				continue;
			}

			Integer countedReopens = reopensPerResolver.get(resolver);
			if (countedReopens == null) {
				countedReopens = new Integer(0);
			}
			countedReopens = new Integer(countedReopens.intValue()+1);
			reopensPerResolver.put(resolver, countedReopens);
		}

		List<EmployeeSprint> employeeSprints = sprint.getEmployeeSprints();
		Logger.debug (employeeSprints.size()+" employee sprints");
		for (EmployeeSprint employeeSprint: employeeSprints) {
			Sprint tmp = employeeSprint.sprint;
			Logger.debug ("Sprint: "+tmp.name);

			Logger.debug ("Employee: "+employeeSprint.employee.id);
			Logger.debug ("Employee first name: "+employeeSprint.employee.firstName);
			Logger.debug ("Employee jira IS: "+employeeSprint.employee.jiraID);
			Employee employee = employeeSprint.employee;
			if (StringUtils.isEmpty(employee.jiraID)) {
				continue;
			}
			
			Integer reopens = reopensPerResolver.get(employee.jiraID);
			Logger.debug ("Assigning "+reopens+" to "+employee.jiraID);
			if (reopens != null) {
				employeeSprint.numReopens = reopens.intValue();
				employeeSprint.save();
			}
			
		}
	}
	
	private void calculateUntested(String untestedQuery, JiraClient jiraClient, ScrumMaster scrumMaster) throws JiraException {
		Logger.debug ("Looking for untested tickets with "+untestedQuery);
		SearchResult searchResult = jiraClient.searchIssues(untestedQuery, 600);

		List<Issue> issues = searchResult.issues;
		Logger.debug ("found "+issues.size()+" issues");
		
		OpenTicketLog openTicketLog = new OpenTicketLog();
		openTicketLog.date = new Date();
		openTicketLog.numOpenTickets = issues.size();
		openTicketLog.scrumMaster = scrumMaster;
		openTicketLog.save();
	}
	
	public List<String> getParameterNames() {
		List<String> paramNames = new ArrayList<String>();
		paramNames.add("jiraURL");
		paramNames.add("username");
		paramNames.add("password");
		paramNames.add("velocityQuery");
		paramNames.add("churnQuery");
		paramNames.add("untestedQuery");
		paramNames.add("storyPointsCustomFieldID");
		paramNames.add("resolverCustomFieldID");
		return paramNames;
	}
	
	public static Issue getIssue(ScrumMaster scrumMaster, String jiraKey) {
		Issue issue = (Issue) Cache.get("jiraissue_"+jiraKey+"_"+scrumMaster.id);
		if (issue != null) {
			return issue;
		}
		
		JiraConnector jiraConnector = new JiraConnector();
		String jiraURL = ConnectorConfiguration.getValue(scrumMaster, jiraConnector.getName(), "jiraURL");
		String username = ConnectorConfiguration.getValue(scrumMaster, jiraConnector.getName(), "username");
		String password = ConnectorConfiguration.getValue(scrumMaster, jiraConnector.getName(), "password");
        BasicCredentials creds = new BasicCredentials(username, password);
        JiraClient jiraClient = new JiraClient(jiraURL,creds);
        
        try {
			issue = jiraClient.getIssue(jiraKey);
			Cache.set("jiraissue_"+jiraKey+"_"+scrumMaster.id, issue);
		} catch (JiraException e) {
			Logger.warn("Jira error", e);
		}

        return issue;
	}

	
	public String getName() {
		return "Jira";
	}

}
