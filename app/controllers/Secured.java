package controllers;

import models.ModelException;
import models.entities.ClientSession;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class Secured extends Security.Authenticator {
	 
    @Override
    public String getUsername(Context ctx) {
    	try {
        	String ssid = ctx.session().get("ssid");
        	if (ssid == null) {
        		return null;
        	} else {
        		ClientSession cs = ClientSession.find(ssid);
        		if (cs == null) {
        			// В базе данных нет сессии с таким ID. Удаляем сессию
        			ctx.session().clear();
        			return null;
        		}
        		return ssid;
        	}
    	} catch (ModelException e) {
    		return null;
    	}
    }
 
    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.loginForm());
    }	
 
}