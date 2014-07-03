package controllers;

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
		
		return ok(views.html.ReportController.velocityChart.render(sprints));
	}
}
