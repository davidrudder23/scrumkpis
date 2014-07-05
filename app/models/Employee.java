package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class Employee extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4090140235165671986L;

	@Id
	public Long id;
	
	@ManyToOne
	public ScrumMaster scrumMaster;
	
	public String firstName;
	public String lastName;
	public String email;
	
	public String description;
	
	public int defaultStoryPointsPerSprint;
	
	public static Finder<Long, Employee> find = new Finder<Long, Employee>(Long.class, Employee.class);

}
