package models;

import javax.persistence.Entity;
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
	
	public int storyPointsAvailable;
	
	public int storyPointsCompleted;
	
	public int numReopens;
	
	public static Finder<Long, EmployeeSprint> find = new Finder<Long, EmployeeSprint>(Long.class, EmployeeSprint.class);
}
