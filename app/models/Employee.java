package models;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

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
	
	public String firstName;
	public String lastName;
	public String email;
	
	public String jiraID;
	
	public String description;
	
	public int defaultStoryPointsPerSprint;
	
	@OneToMany(mappedBy="employee", cascade=CascadeType.ALL)
	public Set<EmployeeSprint> employeeSprints;
	
	public static Finder<Long, Employee> find = new Finder<Long, Employee>(Long.class, Employee.class);

}
