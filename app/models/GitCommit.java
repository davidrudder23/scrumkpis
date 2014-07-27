package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class GitCommit extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1686311678333182301L;

	@Id
	public Long id;
	
	@ManyToOne
	public Sprint sprint;

	@ManyToOne
	public Employee employee;
	
	public String commitId;
	
	public String repoName;

	public String message;
	
	public String commiterEmail;
	
	public Date date;

	public static Finder<Long, GitCommit> find = new Finder<Long, GitCommit>(Long.class, GitCommit.class);

}
