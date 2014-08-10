package utils;

import controllers.Authentication;
import controllers.routes;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security.Authenticator;

public class ScrumMasterAuthenticationUtil extends Authenticator {
	
    @Override
    public String getUsername(Context ctx) {
		if (!Authentication.isScrumMaster()) {
			return null;				
		}
		
		return ctx.session().get("scrummaster_id");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.index());
    }


}
