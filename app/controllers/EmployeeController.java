package controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.avaje.ebean.Ebean;

import utils.StringUtils;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;
import models.Employee;
import models.ScrumMaster;
import models.Sprint;

@Authenticated(AuthenticationUtil.class)
public class EmployeeController extends ParentController {


	public static Result employees() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		Logger.debug ("ScrumMaster="+scrumMaster);
		List<Employee> employees = Employee.find.where().eq("scrumMaster", scrumMaster).findList();
		Collections.sort(employees, new Comparator<Employee>() {

			@Override
			public int compare(Employee employeeA, Employee employeeB) {
				if ((employeeA==null) && (employeeB==null)) {
					return 0;
				}
				if (employeeA==null) {
					return 1;
				}
				if (employeeB==null) {
					return -1;
				}
				
				if ((employeeA.lastName==null) && (employeeB.lastName==null)) {
					return 0;
				}
				if (employeeA.lastName==null) {
					return 1;
				}
				if (employeeB.lastName==null) {
					return -1;
				}
				return employeeA.lastName.compareTo(employeeB.lastName);
			}
			
		});
		return ok(views.html.EmployeeController.employees.render(employees));
	}
	
	public static Result editEmployee(Long id) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		Employee employee = Employee.find.byId(id);
		String title = "Edit Employee";
		if (employee != null) {
			if (!employee.scrumMaster.equals(scrumMaster)) {
				flash().put("error", "You do not manage this employee");
				Logger.debug ("You do not manage this employee - "+employee.scrumMaster+" vs "+scrumMaster);
				return redirect(routes.EmployeeController.employees());
			}
		} else {
			title = "New Employee";
			employee = new Employee();
			employee.id=new Long(0);
			employee.defaultStoryPointsPerSprint = 15;
		}
		return ok(views.html.EmployeeController.editEmployee.render(employee, title));
	}
	
	public static Result updateEmployee(Long id) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		Employee employee = Employee.find.byId(id);
		boolean isNew = false;
		if (employee == null) {
			employee = new Employee();
			employee.scrumMaster = scrumMaster;
			isNew = true;
		} else {
			if (!employee.scrumMaster.equals(scrumMaster)) {
				flash().put("error", "You do not manage this employee");
				return redirect(routes.EmployeeController.employees());
			}
		}
		
		employee.firstName = getFormValue("first-name");
		employee.lastName = getFormValue("last-name");
		employee.email = getFormValue("email");
		employee.defaultStoryPointsPerSprint = StringUtils.getInt(getFormValue("story-points"), 15);
		employee.description = getFormValue("description");
		employee.jiraID = getFormValue("jira-id");
		Logger.debug ("jiraid="+employee.jiraID);
		
		employee.save();
		
		return redirect(controllers.routes.EmployeeController.employees());
	}
	
	public static Result deleteEmployee(Long id) {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();

		Employee employee = Employee.find.byId(id);
		if (employee != null) {
			if (!employee.scrumMaster.equals(scrumMaster)) {
				flash().put("error", "You do not manage this employee");
				return redirect(routes.EmployeeController.employees());
			}
			
			employee.delete();
		}
		
		return redirect(routes.EmployeeController.employees());
	}

}
