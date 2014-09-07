package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class EmployeeSprint extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2819857682599744535L;

	@Id
	public Long id;
	
	@ManyToOne
	public Employee employee;
	
	@ManyToOne
	public Sprint sprint;
	
	public float storyPointsAvailable;
	
	public float storyPointsCompleted;
	
	public int numReopens;
	
	public List<JiraIssue> getResolvedJiraIssues() {
		List<JiraIssue> issues = JiraIssue.find.where().eq("scrumMaster", employee.scrumMaster).eq("resolver", employee).eq("resolutionSprint", sprint).findList();
		return issues;
	}
	
	public static Finder<Long, EmployeeSprint> find = new Finder<Long, EmployeeSprint>(Long.class, EmployeeSprint.class);
}
