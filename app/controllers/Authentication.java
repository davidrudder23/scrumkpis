package controllers;

import java.util.List;

import models.ScrumMaster;
import play.Logger;
import play.mvc.Result;
import utils.StringUtils;

public class Authentication extends ParentController {

	public static ScrumMaster getLoggedInScrumMaster() {
		ScrumMaster scrumMaster = null;
		
		String scrumMasterId = session().get("userid");
		if (StringUtils.isEmpty(scrumMasterId)) return scrumMaster;
		
		scrumMaster = ScrumMaster.find.byId(StringUtils.getLong(scrumMasterId));
		
		return scrumMaster;
	}
	
    public static Result login() {
    	return ok(views.html.Authentication.login.render());
    }
    
    public static Result authenticate() {
    	String name = getFormValue("name");
    	String password = getFormValue("password");
    	
    	List<ScrumMaster> scrumMasters = ScrumMaster.find.where().eq("email", name).findList();
    	if ((scrumMasters!=null) && (scrumMasters.size()>0)) {
    		ScrumMaster scrumMaster = scrumMasters.get(0);
        	String hashedPassword = ScrumMaster.hashPassword(name, scrumMaster.salt, password);
        	if (scrumMaster.hashedPassword.equals(hashedPassword)) {
        		session().put("userid", scrumMaster.id+"");
        	}
    	}
    	
    	return redirect(controllers.routes.Application.index());
    }

    public static Result logout() {
		session().remove("userid");
    	return redirect(controllers.routes.Application.index());
    }
    
    public static Result signup() {
    	return ok(views.html.Authentication.signup.render());
    }

    public static Result doSignup() {
    	
    	String firstName = getFormValue("first-name");
    	String lastName = getFormValue("last-name");
    	String email = getFormValue("email");
    	String password = getFormValue("password");
    	
    	if (StringUtils.isEmpty(email)) {
    		flash().put("error-email", "please specify an email address");
        	return redirect(routes.Authentication.signup());
    	}
    	
    	int numWithEmail = ScrumMaster.find.where().eq("email", email).findRowCount();
    	Logger.debug (numWithEmail+" found with email "+email);
    	if (numWithEmail>0) {
    		flash().put("error-email", "email address is already taken");
        	return redirect(routes.Authentication.signup());
    	}
    	
    	ScrumMaster scrumMaster = new ScrumMaster();
    	scrumMaster.email = email;
    	scrumMaster.firstName = firstName;
    	scrumMaster.lastName = lastName;
    	scrumMaster.salt = ScrumMaster.generateSalt();
    	scrumMaster.hashedPassword = ScrumMaster.hashPassword(email, scrumMaster.salt, password);
    	scrumMaster.save();
    	
    	session().put("userid", scrumMaster.id+"");
    	
    	return redirect(routes.Application.index());
    }
}
