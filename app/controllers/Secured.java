package controllers;


import controllers.routes;
import play.*;
import play.mvc.*;
import play.mvc.Http.*;

public class Secured extends Security.Authenticator {
	
    @Override
    public String getUsername(Context ctx) {
		String url;
		if (ctx.request().method().equalsIgnoreCase("GET")) {
			url = ctx.request().uri();
		} else {
			url = "";
		}
    	ctx.flash().put("url", url);
    	Logger.debug("Secured: redirect URL: " + url);
        return ctx.session().get("email");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }
    
}