package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class JiraIssue extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1686311678333182301L;

	@Id
	public Long id;
	
	@ManyToOne
	public Sprint resolutionSprint;

	@ManyToOne
	public Employee resolver;
	
	public String jiraKey;
	
	public static Finder<Long, JiraIssue> find = new Finder<Long, JiraIssue>(Long.class, JiraIssue.class);

}
