package controllers;

import models.ModelException;
import models.Role;
import static models.entities.ClientSession.*;
import models.entities.Subject;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.subjects.edit;
import views.html.subjects.index;

public class Subjects extends Controller {

	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	private static final Form<Subject> subjectForm = Form.form(Subject.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST)) {
				return ok(index.render(Subject.fetchAll()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result find() {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST)) {
				String key = request().body().asFormUrlEncoded().get("name")[0];
				return ok(index.render(Subject.findByName(key)));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
				Role.METHODIST)) {
			return ok(edit.render(subjectForm, 0));
		}
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST)) {
				Form<Subject> form = subjectForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", DATA_ERROR);
					return badRequest(edit.render(form, id));
				}
				Subject subject = form.get();
				if (id == 0) {
					// Добавляем запись
					subject.save();
				} else {
					// Изменяем запись
					Subject old = Subject.get(id);
					old.updateFrom(subject);
					old.save();
					flash("success", String.format(
							"Успешно сохранена дисциплина %s, %d", old.name,
							old.getId()));
				}
				return redirect(routes.Subjects.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST)) {
				final Subject subject = Subject.get(id);
				Form<Subject> form = subjectForm.fill(subject);
				return ok(edit.render(form, subject.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result confirm(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST)) {
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
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST)) {
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
