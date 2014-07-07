package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.Logger;
import play.db.ebean.Model;

@Entity
public class Sprint extends Model {
	
	public static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyy");

	/**
	 * 
	 */
	private static final long serialVersionUID = 3529154498300657232L;

	@Id
	public Long id;
	
	public String name;

	@Column(length = 2048)
	public String description;

	public Date startDate = new Date();

	public Boolean locked = false;
	
	@ManyToOne
	public ScrumMaster scrumMaster;
	
	public List<EmployeeSprint> getEmployeeSprints() {
		List<EmployeeSprint> employeeSprints = EmployeeSprint.find.where().eq("sprint", this).findList();
		return employeeSprints;
	}

	public List<Employee> getEmployees() {
		List<EmployeeSprint> employeeSprints = EmployeeSprint.find.where().eq("sprint", this).findList();
		List<Employee> employees = new ArrayList<Employee>();
		
		for (EmployeeSprint employeeSprint: employeeSprints) {
			employees.add(employeeSprint.employee);
		}
		return employees;
	}
	
	public void addEmployee(Employee employee) {

		if (employee == null) return;
		
		int numFound = EmployeeSprint.find.where().eq("employee", employee).eq("sprint", this).findRowCount();
		if (numFound > 0) {
			Logger.info("Did not add employee "+employee+" because he's already in sprint "+name);
			return;
		}
		
		EmployeeSprint employeeSprint = new EmployeeSprint();
		employeeSprint.employee = employee;
		employeeSprint.sprint = this;
		employeeSprint.storyPointsAvailable = employee.defaultStoryPointsPerSprint;
		employeeSprint.storyPointsCompleted = 0;
		employeeSprint.save();
	}
	
	public void removeEmployee(Employee employee) {
		if (employee == null) return;

		List<EmployeeSprint> employeeSprints = EmployeeSprint.find.where().eq("sprint", this).eq("employee", employee).findList();
		for (EmployeeSprint employeeSprint: employeeSprints) {
			employeeSprint.delete();
		}
	}
	
	public int getTotalStoryPointsPromised() {
		int points = 0;
		List<EmployeeSprint> employeeSprints = getEmployeeSprints();
		for (EmployeeSprint employeeSprint: employeeSprints) {
			points += employeeSprint.storyPointsAvailable;
		}
		return points;
	}
	
	public int getTotalStoryPointsCompleted() {
		int points = 0;
		List<EmployeeSprint> employeeSprints = getEmployeeSprints();
		for (EmployeeSprint employeeSprint: employeeSprints) {
			points += employeeSprint.storyPointsCompleted;
		}
		return points;
	}
	
	public String toString() {
		return name+" ("+dateFormatter.format(startDate)+")";
	}
	
	public static Finder<Long, Sprint> find = new Finder<Long, Sprint>(Long.class, Sprint.class);

}
