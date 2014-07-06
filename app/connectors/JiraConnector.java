package connectors;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.SearchResult;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

import com.avaje.ebean.Ebean;

import play.Logger;
import utils.StringUtils;
import models.ConnectorConfiguration;
import models.Employee;
import models.EmployeeSprint;
import models.ScrumMaster;
import models.Sprint;

public class JiraConnector extends Connector {

	@Override
	public void run(ScrumMaster scrumMaster) {
		String jiraURL = ConnectorConfiguration.getValue(scrumMaster, getName(), "jiraURL");
		String username = ConnectorConfiguration.getValue(scrumMaster, getName(), "username");
		String password = ConnectorConfiguration.getValue(scrumMaster, getName(), "password");
		String query= ConnectorConfiguration.getValue(scrumMaster, getName(), "query");
		String storyPointsCustomFieldID = ConnectorConfiguration.getValue(scrumMaster, getName(), "storyPointsCustomFieldID");
		String resolverCustomFieldID = ConnectorConfiguration.getValue(scrumMaster, getName(), "resolverCustomFieldID");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		
		if ((StringUtils.isEmpty(jiraURL)) ||
				(StringUtils.isEmpty(username)) ||
				(StringUtils.isEmpty(password)) ||
				(StringUtils.isEmpty(query))) {
			Logger.error("Jira configuration is not complete");
			return;
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
				String mungedQuery = query.replaceAll("%s", "'"+sdf.format(sprint.startDate)+"'");
				Calendar endDate = Calendar.getInstance();
				endDate.setTime(sprint.startDate);
				endDate.add(Calendar.DAY_OF_YEAR, 14);
				
				mungedQuery = mungedQuery.replaceAll("%e", "'"+sdf.format(endDate.getTime())+"'");
				
				Logger.debug ("Using query "+mungedQuery);
				
				SearchResult searchResult = jiraClient.searchIssues(mungedQuery, 1000);
	
				List<Issue> issues = searchResult.issues;
				Logger.debug ("Got "+issues.size()+" issues with query \""+query+"\"");
				
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
		} catch (JiraException e) {
			e.printStackTrace();
			Logger.warn("Could not access jira at url "+jiraURL, e);
		}		
	}
	
	public List<String> getParameterNames() {
		List<String> paramNames = new ArrayList<String>();
		paramNames.add("jiraURL");
		paramNames.add("username");
		paramNames.add("password");
		paramNames.add("query");
		paramNames.add("storyPointsCustomFieldID");
		paramNames.add("resolverCustomFieldID");
		return paramNames;
	}

	
	public String getName() {
		return "Jira";
	}

}
