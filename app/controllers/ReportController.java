package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.ScrumMaster;
import models.Sprint;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;

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
}
