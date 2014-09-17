package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.School;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.schools.edit;
import views.html.schools.index;

public class Schools extends Controller {
	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	private static final Form<School> schoolForm = Form.form(School.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
				return ok(index.render(School.fetchAll()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
			return ok(edit.render(schoolForm, 0));
		}
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
				Form<School> form = schoolForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", DATA_ERROR);
					return badRequest(edit.render(form, id));
				}
				School school = form.get();
				if (id == 0) {
					// Добавляем запись
					school.save();
				} else {
					// Изменяем запись
					School old = School.get(id);
					old.updateFrom(school);
					old.save();
					flash("success", String.format(
							"Успешно сохранено учебное заведение %s, %d",
							old.name, old.getId()));
				}
				return redirect(routes.Schools.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
				final School school = School.get(id);
				return ok(edit.render(schoolForm.fill(school), school.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result confirm(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
				final School item = School.get(id);
				String message = "Учебное заведение \"" + item.name
						+ "\" будет удалено!";
				return ok(confirmDelete.render(message,
						routes.Schools.delete(id).url(), routes.Schools.index()
								.url()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result delete(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
				final School school = School.get(id);
				school.delete();
				return redirect(routes.Schools.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
