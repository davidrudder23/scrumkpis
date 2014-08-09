package utils;

import controllers.routes;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security.Authenticator;

public class AuthenticationUtil extends Authenticator {
	
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("scrummaster_id");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Authentication.login());
    }


}
