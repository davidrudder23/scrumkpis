package connectors;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.SearchResult;
import net.rcarz.jiraclient.ICredentials;
import net.rcarz.jiraclient.IssueLink;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.WorkLog;
import play.Logger;
import utils.StringUtils;
import models.ConnectorConfiguration;
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
				
				HashMap<String, Integer> storyPointsPerResolver = new HashMap<String, Integer>();
				for (Issue issue: issues) {
					int storyPoints = StringUtils.getInt(issue.getField("customfield_"+storyPointsCustomFieldID).toString(), 0);
					String resolver = issue.getField("customfield_"+resolverCustomFieldID).toString();
					Logger.debug ("Story points for issue "+issue.getKey()+"="+storyPoints);
					Logger.debug ("Resolver for issue "+issue.getKey()+"="+resolver);
					
					if (storyPoints<=0) {
						continue;
					}
					if (StringUtils.isEmpty(resolver)) {
						continue;
					}
					Integer alreadyCountedStoryPoints = storyPointsPerResolver.get(resolver);
					if (alreadyCountedStoryPoints == null) {
						alreadyCountedStoryPoints = new Integer(0);
					}
					alreadyCountedStoryPoints = new Integer(alreadyCountedStoryPoints.intValue()+storyPoints);
					storyPointsPerResolver.put(resolver, alreadyCountedStoryPoints);
				}
			
				List<EmployeeSprint> employeeSprints = sprint.getEmployeeSprints();
			}
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
		paramNames.add("storyPointsCustomFieldID");
		paramNames.add("resolverCustomFieldID");
		return paramNames;
	}

	
	public String getName() {
		return "Jira";
	}

}
