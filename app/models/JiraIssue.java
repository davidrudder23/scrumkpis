package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import net.rcarz.jiraclient.Issue;
import connectors.JiraConnector;
import play.db.ebean.Model;

@Entity
public class JiraIssue extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1686311678333182301L;

	@Id
	public Long id;
	
	public String authorName;
	
	public String authorEmail;
	
	@ManyToOne
	public Sprint resolutionSprint;

	@ManyToOne
	public Employee resolver;
	
	public String jiraKey;
	
	public String summary;
	
	public static Finder<Long, JiraIssue> find = new Finder<Long, JiraIssue>(Long.class, JiraIssue.class);
	
	public static JiraIssue findByJiraKey(ScrumMaster scrumMaster, String jiraKey) {
		JiraIssue jiraIssue = find.where().eq("jiraKey", jiraKey).findUnique();
		
		if (jiraIssue == null) {
			Issue issue = JiraConnector.getIssue(scrumMaster, jiraKey);
			if (issue != null) {
				jiraIssue = new JiraIssue();
				jiraIssue.jiraKey = jiraKey;
				jiraIssue.authorName = issue.getReporter().getDisplayName();
				jiraIssue.authorEmail = issue.getReporter().getEmail();
				jiraIssue.summary = issue.getSummary();
				jiraIssue.save();
			}
		}
		
		return jiraIssue;
	}
	
	public boolean isBookmark(Employee employee) {
		JiraBookmark bookmark = JiraBookmark.find.where().eq("jiraKey", jiraKey).eq("employee", employee).findUnique();
		return bookmark != null;
	}

}
