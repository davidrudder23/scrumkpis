package controllers;

import java.util.Date;
import java.util.List;

import models.ScrumMaster;
import models.Sprint;
import play.Logger;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
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

		sprint.active = Boolean.TRUE;
		sprint.startDate = new Date();
		sprint.scrumMaster = scrumMaster;
		sprint.save();
		return redirect(controllers.routes.SprintController.sprints());
	}
	
	public static Result deleteSprint(Long sprintId) {
		Sprint.find.byId(sprintId).delete();
		return redirect(controllers.routes.SprintController.sprints());
	}
	
	public static Result editSprint(Long sprintId) {
		Sprint sprint = Sprint.find.byId(sprintId);
		String title = "Edit Sprint";
		if (sprint == null) {
			title = "New Sprint";
			sprint = new Sprint();
		}
		
		return ok(views.html.SprintController.editSprint.render(sprint, title));
	}
	
	public static Result updateSprint() {
		Long sprintId = StringUtils.getLong(getFormValue("sprintid"));
		Sprint sprint = Sprint.find.byId(sprintId);
		
		if (sprint == null) {
			sprint = new Sprint();
		}
		
		String name = getFormValue("name");
		if (!StringUtils.isEmpty(name)) {
			sprint.name = name;
		}
		String description = getFormValue("description");
		if (!StringUtils.isEmpty(description)) {
			sprint.description = description;
		}
		
		sprint.save();
		
		return redirect(routes.SprintController.editSprint(sprintId));
	}

}
