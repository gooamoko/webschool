package controllers;

import models.ModelException;
import models.entities.Account;
import models.entities.ClientSession;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.about;
import views.html.errorPage;
import views.html.index;
import views.html.login;
import views.html.ajax.loginForm;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Добро пожаловать!"));
    }
    
    public static Result about() {
    	return ok(about.render());
    }
    
    public static Result login() {
    	try {
        	DynamicForm loginForm = Form.form().bindFromRequest();
            String userLogin = loginForm.get("login");
            String userPassword = loginForm.get("password");
            Account account = Account.auth(userLogin, userPassword);
            if (account != null) {
            	// Создаем новый объект-сессию
            	String ipaddr = request().remoteAddress();
            	ClientSession session = new ClientSession(ipaddr);
            	// связываем сессию с учеткой
            	session.setAccount(account);
            	// Сохраняем сессию в базе
            	session.save();
            	// отправляем идентификатор на сторону клиента
            	session("ssid", session.ssid);
            } else {
        		return ok(login.render("Ошибка аутентификации", 
        				"Пользователь с такой комбинацией логина и пароля не найден!"));
            }
        	return redirect(routes.Application.index());
    	} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
    	}
    }
    
    public static Result ajaxLoginForm() {
		return ok(loginForm.render());
    }
    
    public static Result loginForm() {
    	return ok(login.render("Вход", 
    			"Для работы с закрытыми разделами сайта вам нужно осуществить вход, используя логин и пароль."));
    }
    
    public static Result logout() {
    	try {
        	ClientSession cs = ClientSession.find(session().get("ssid"));
        	if (null != cs) {
        		cs.delete();
        	}
        	session().clear();
        	return redirect(routes.Application.index());
    	} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
    	}
    }
}
