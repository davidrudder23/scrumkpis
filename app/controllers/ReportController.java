package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.Employee;
import models.OpenTicketLog;
import models.ScrumMaster;
import models.Sprint;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;
import utils.StringUtils;

@Authenticated(AuthenticationUtil.class)
public class ReportController extends ParentController {

	
	public static Result velocityChart() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		List<Sprint> sprints = scrumMaster.getSprints();
		Collections.sort(sprints, new Comparator<Sprint>() {

			@Override
			public int compare(Sprint sprintA, Sprint sprintB) {
				if ((sprintA==null) && (sprintB==null)) {
					return 0;
				}
				if (sprintA==null) {
					return 1;
				}
				if (sprintB==null) {
					return -1;
				}
				
				if ((sprintA.startDate==null) && (sprintB.startDate==null)) {
					return 0;
				}
				if (sprintA.startDate==null) {
					return 1;
				}
				if (sprintB.startDate==null) {
					return -1;
				}
				return sprintA.startDate.compareTo(sprintB.startDate);
			}
			
		});
		return ok(views.html.ReportController.velocityChart.render(sprints));
	}
	
	public static Result individualVelocityChart() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		List<Sprint> sprints = scrumMaster.getSprints();
		Collections.sort(sprints, new Comparator<Sprint>() {

			@Override
			public int compare(Sprint sprintA, Sprint sprintB) {
				if ((sprintA==null) && (sprintB==null)) {
					return 0;
				}
				if (sprintA==null) {
					return 1;
				}
				if (sprintB==null) {
					return -1;
				}
				
				if ((sprintA.startDate==null) && (sprintB.startDate==null)) {
					return 0;
				}
				if (sprintA.startDate==null) {
					return 1;
				}
				if (sprintB.startDate==null) {
					return -1;
				}
				return sprintA.startDate.compareTo(sprintB.startDate);
			}
			
		});
		
		List<Employee> employees = new ArrayList<Employee>();
		for (Sprint sprint: sprints) {
			for (Employee employee: sprint.getEmployees()) {
				if (!employees.contains(employee)) {
					employees.add(employee);
				}
			}
		}
		return ok(views.html.ReportController.individualVelocityChart.render(sprints, employees));
	}
	
	public static Result openTicketsChart() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		List<OpenTicketLog> openTickets = OpenTicketLog.find.where().eq("scrumMaster", scrumMaster).findList();
		Collections.sort(openTickets, new Comparator<OpenTicketLog>() {

			@Override
			public int compare(OpenTicketLog o1, OpenTicketLog o2) {
				return o1.date.compareTo(o2.date);
			}
			
		});
		return ok(views.html.ReportController.openTicketsChart.render(openTickets));
	}
}
