package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;

import play.db.ebean.Model;

@Entity
public class EmployeeSprint extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2819857682599744535L;

	@Id
	public Long id;
	
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	public Employee employee;
	
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	public Sprint sprint;
	
	public float storyPointsAvailable;
	
	public float storyPointsCompleted;
	
	public int numReopens;
	
	public static Finder<Long, EmployeeSprint> find = new Finder<Long, EmployeeSprint>(Long.class, EmployeeSprint.class);
}
