package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import net.rcarz.jiraclient.Issue;
import connectors.JiraConnector;
import play.db.ebean.Model;
import utils.StringUtils;

@Entity
public class Employee extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4090140235165671986L;

	@Id
	public Long id;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	@ManyToOne
	public ScrumMaster scrumMaster;
	
	public String username;
	
	public String firstName;
	public String lastName;
	public String email;
	
	public String jiraID;
	
	public String description;
	
	public int defaultStoryPointsPerSprint;
	
	public String color = StringUtils.getRandomColor();
	
	@OneToMany(mappedBy="employee", cascade=CascadeType.ALL)
	public Set<EmployeeSprint> employeeSprints;
	
	public static Finder<Long, Employee> find = new Finder<Long, Employee>(Long.class, Employee.class);

	public List<Issue> getBookmarks() {
		List<JiraBookmark> bookmarkLinks = JiraBookmark.find.where().eq("employee", this).findList();
		List<Issue> bookmarks = new ArrayList<>();
		
		for (JiraBookmark bookmarkLink: bookmarkLinks) {
			Issue issue = JiraConnector.getIssue(scrumMaster, bookmarkLink.jiraKey);
			if (issue != null) {
				bookmarks.add(issue);
			}
		}
		
		return bookmarks;
	}
	
	public boolean isBookmark(String jiraKey) {
		JiraBookmark bookmark = JiraBookmark.find.where().eq("jiraKey", jiraKey).eq("employee", this).findUnique();
		return bookmark != null;
	}
}
