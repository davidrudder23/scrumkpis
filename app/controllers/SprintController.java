package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Employee;
import models.EmployeeSprint;
import models.ScrumMaster;
import models.Sprint;
import play.Logger;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import scala.Array;
import utils.AuthenticationUtil;
import utils.StringUtils;

@Authenticated(AuthenticationUtil.class)
public class SprintController extends ParentController {
    public static Form<Sprint> sprintForm = Form.form(Sprint.class);

	public static Result sprints() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		List<Sprint> activeSprints = Sprint.find.where().eq("active", Boolean.TRUE).eq("scrumMaster", scrumMaster).findList();
		return ok(views.html.SprintController.sprints.render(activeSprints));
	}
	public static Result addSprint() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		Sprint sprint = new Sprint();
		sprint.name = getFormValue("name");
		sprint.description = getFormValue("description");

		if (sprintForm.hasErrors()) {
			Logger.debug("Sprint Form had errors - " + sprintForm.errors());
		}
		Logger.debug("Got sprint " + sprint.name);

		sprint.locked = false;
		sprint.startDate = new Date();
		sprint.scrumMaster = scrumMaster;
		sprint.save();
		return redirect(controllers.routes.SprintController.sprints());
	}
	
	public static Result deleteSprint(Long sprintId) {
		Sprint sprint = Sprint.find.byId(sprintId);
		if (sprint != null) {
			List<EmployeeSprint> employeeSprints = sprint.getEmployeeSprints();
			for (EmployeeSprint employeeSprint: employeeSprints) {
				employeeSprint.delete();
			}
			sprint.delete();
		}
		return redirect(controllers.routes.SprintController.sprints());
	}
	
	public static Result editSprint(Long sprintId) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();

		Sprint sprint = Sprint.find.byId(sprintId);
		String title = "Edit Sprint";
		if (sprint == null) {
			title = "New Sprint";
			sprint = new Sprint();
		}
		
		List<Employee> employeesInList = sprint.getEmployees();
		
		List<Employee> employees = new ArrayList<Employee>();
		List<Employee> tmpEmployees = Employee.find.where().eq("scrumMaster", scrumMaster).findList();
		for (Employee employee: tmpEmployees) {
			if (!employeesInList.contains(employee)) {
				employees.add(employee);
			}
		}
		
		return ok(views.html.SprintController.editSprint.render(sprint, title, employees, scrumMaster));
	}
	
	public static Result addEmployee() {
		Long sprintId = StringUtils.getLong(getFormValue("sprint-id"));
		Long employeeId = StringUtils.getLong(getFormValue("employee-id"));
		
		Sprint sprint = Sprint.find.byId(sprintId);
		Employee employee = Employee.find.byId(employeeId);

		sprint.addEmployee(employee);
		return redirect(routes.SprintController.editSprint(sprintId));
		
	}
	
	public static Result removeEmployee(Long sprintId, Long employeeId) {
		Sprint sprint = Sprint.find.byId(sprintId);
		Employee employee = Employee.find.byId(employeeId);
		if ((employee != null) && (sprint != null)) {
			sprint.removeEmployee(employee);
		}
		
		return redirect(routes.SprintController.editSprint(sprintId));
	}

	public static Result updateSprint() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();

		Long sprintId = StringUtils.getLong(getFormValue("sprintid"));
		Sprint sprint = Sprint.find.byId(sprintId);

		boolean newSprint = false;
		if (sprint == null) {
			sprint = new Sprint();
			newSprint = true;
		}
		
		String name = getFormValue("name");
		if (!StringUtils.isEmpty(name)) {
			sprint.name = name;
		}
		String description = getFormValue("description");
		if (!StringUtils.isEmpty(description)) {
			sprint.description = description;
		}
		
		sprint.scrumMaster = scrumMaster;
		sprint.locked = false;
		
		sprint.save();
		
		if (newSprint) {
			List<Employee> employees = Employee.find.where().eq("scrumMaster", scrumMaster).findList();
			for (Employee employee: employees) {
				sprint.addEmployee(employee);
			}
		}
		
		// Update the employee's story points
		List<EmployeeSprint> employeeSprints = sprint.getEmployeeSprints();
		for (EmployeeSprint employeeSprint: employeeSprints) {
			int storyPointsAvailable = StringUtils.getInt(getFormValue("employeesprint-"+employeeSprint.id+"-storyPointsAvailable"), -1);
			Logger.debug ("Setting storyPointsAvailable to "+storyPointsAvailable+" for employeeSprint id "+employeeSprint.id);
			int storyPointsCompleted = StringUtils.getInt(getFormValue("employeesprint-"+employeeSprint.id+"-storyPointsCompleted"), -1);
			int numReopens = StringUtils.getInt(getFormValue("employeesprint-"+employeeSprint.id+"-numReopens"), -1);
			
			if (storyPointsAvailable >= 0) {
				employeeSprint.storyPointsAvailable = storyPointsAvailable;
			}
			if (storyPointsCompleted >= 0) {
				employeeSprint.storyPointsCompleted = storyPointsCompleted;
			}

			if (numReopens >= 0) {
				employeeSprint.numReopens = numReopens;
			}
			employeeSprint.save();
		}
		
		return redirect(routes.SprintController.editSprint(sprint.id));
	}


}
