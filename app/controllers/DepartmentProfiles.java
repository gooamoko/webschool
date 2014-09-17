package controllers;

import static models.entities.ClientSession.isAdmin;
import models.ModelException;
import models.entities.Subject;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;

public class DepartmentProfiles extends Controller {

	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	@Security.Authenticated(Secured.class)
	public static Result index(final int depId) {
			if (isAdmin(session().get("ssid"))) {
				return TODO;
			}
			return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdmin(session().get("ssid"))) {
			return TODO;
		}
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
			if (isAdmin(session().get("ssid"))) {
				return TODO;
			}
			return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(int id) {
			if (isAdmin(session().get("ssid"))) {
				return TODO;
			}
			return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result confirm(int id) {
		try {
			if (isAdmin(session().get("ssid"))) {
			final Subject subject = Subject.get(id);
			String message = "Дисциплина \"" + subject.name
					+ "\" будет удалена!";
			return ok(confirmDelete.render(message, routes.Subjects.delete(id)
					.url(), routes.Subjects.index().url()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result delete(int id) {
		try {
			if (isAdmin(session().get("ssid"))) {
			final Subject subject = Subject.get(id);
			subject.delete();
			return redirect(routes.Subjects.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
