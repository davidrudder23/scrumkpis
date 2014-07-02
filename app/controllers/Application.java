package controllers;

import java.util.List;

import models.ScrumMaster;
import models.Sprint;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;
import views.html.index;

@Authenticated(AuthenticationUtil.class)
public class Application extends ParentController {

    public static Result index() {
    	ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
    	if (scrumMaster == null) {
    		return redirect(controllers.routes.Authentication.login());
    	}
    	List<Sprint> activeSprints = Sprint.find.where().eq("active", Boolean.TRUE).findList();
    	Logger.debug("Got "+activeSprints.size()+" activeSprints");
    	return ok(index.render("Your new application is ready.", activeSprints));
    }

 
}
