package controllers;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import models.Employee;
import models.ScrumMaster;
import play.Logger;
import play.mvc.Result;
import utils.StringUtils;

public class Authentication extends ParentController {

	public static ScrumMaster getLoggedInScrumMaster() {
		ScrumMaster scrumMaster = null;
		
		String scrumMasterId = session().get("scrummaster_id");
		if (StringUtils.isEmpty(scrumMasterId)) return scrumMaster;
		
		scrumMaster = ScrumMaster.find.byId(StringUtils.getLong(scrumMasterId));
		
		return scrumMaster;
	}
	
	public static ScrumMaster getLoggedInEmployee() {
		ScrumMaster scrumMaster = null;
		
		String scrumMasterId = session().get("employee_id");
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

    	boolean authenticated = false; 
    	
    	List<ScrumMaster> scrumMasters = ScrumMaster.find.where().eq("email", name).findList();
    	if ((scrumMasters!=null) && (scrumMasters.size()>0)) {
    		ScrumMaster scrumMaster = scrumMasters.get(0);
        	String hashedPassword = ScrumMaster.hashPassword(name, scrumMaster.salt, password);
        	if (scrumMaster.hashedPassword.equals(hashedPassword)) {
        		session().put("scrummaster_id", scrumMaster.id+"");
        		authenticated = true; 
        	}
    	}
    	
    	
    	// If login as scrum master didn't work, try NTLM
    	if (!authenticated) {
    		scrumMasters = ScrumMaster.find.where().isNotNull("NTLMAuthenticationURL").findList();
    		for (ScrumMaster scrumMaster: scrumMasters) {
	    		try {
			    	Authenticator ntlmAuthenticator = new NTMLAuthenticator(name, password);		  
					Authenticator.setDefault(ntlmAuthenticator);
					URL url = new URL(scrumMaster.NTLMAuthenticationURL);
					URLConnection connection = url.openConnection();
					connection.getInputStream();
					Logger.debug ("Authentication succeeded!");
					
					session().put("employee_id", scrumMaster.id+"");
					Employee employee = Employee.find.where().eq("scrumMaster", scrumMaster).eq("username", name).findUnique();
					if (employee == null) {
						employee = new Employee();
						employee.username = name;
						employee.scrumMaster = scrumMaster;
						employee.description = "Auto-created during authentication";
						employee.save();
					}
					
					Logger.debug ("Scrummaster_id="+scrumMaster.id);
					Logger.debug ("Employee_id="+employee.id);
					session().put("scrummaster_id", scrumMaster.id+"");
					session().put("employee_id", employee.id+"");
					break;
	    		} catch (Exception anyExc) {
	    			Logger.debug("NTLM login failed for user "+name, anyExc);
	    		}
	    	}
    	}
    	
    	
    	return redirect(controllers.routes.Application.index());
    }

    public static Result logout() {
		session().remove("scrummaster_id");
		session().remove("employee_id");
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
    	
    	session().put("scrummaster_id", scrumMaster.id+"");
    	
    	return redirect(routes.Application.index());
    }
}

class NTMLAuthenticator extends Authenticator {
	String username;
	String password;
	
	public NTMLAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		Logger.debug ("Authenticating with "+username+":"+password);
		return new PasswordAuthentication(username, password.toCharArray());
	}
}
