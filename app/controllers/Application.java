package controllers;

import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;
import views.html.index;

@Authenticated(AuthenticationUtil.class)
public class Application extends ParentController {

    public static Result index() {
    	return ok(index.render());
    }

 
}
