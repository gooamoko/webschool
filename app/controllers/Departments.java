package controllers;

import static models.entities.ClientSession.isAdmin;
import models.ModelException;
import models.entities.Department;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.departments.edit;
import views.html.departments.index;

public class Departments extends Controller {

	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	private static final Form<Department> itemForm = Form.form(Department.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdmin(session().get("ssid"))) {
				return ok(index.render(Department.fetchAll()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdmin(session().get("ssid"))) {
			return ok(edit.render(itemForm, 0));
		}
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
		try {
			if (isAdmin(session().get("ssid"))) {
				Form<Department> form = itemForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", DATA_ERROR);
					return badRequest(edit.render(form, id));
				}
				Department item = form.get();
				if (id == 0) {
					// Добавляем запись
					item.save();
				} else {
					// Изменяем запись
					Department old = Department.get(id);
					old.updateFrom(item);
					old.save();
					flash("success", String.format(
							"Успешно сохранено отделение %s, %d", old.name,
							old.getId()));
				}
				return redirect(routes.Departments.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(int id) {
		try {
			if (isAdmin(session().get("ssid"))) {
				final Department item = Department.get(id);
				Form<Department> form = itemForm.fill(item);
				return ok(edit.render(form, item.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result confirm(int id) {
		try {
			if (isAdmin(session().get("ssid"))) {
			final Department item = Department.get(id);
			String message = "Отделение \"" + item.name
					+ "\" будет удалено!";
			return ok(confirmDelete.render(message, routes.Departments.delete(id)
					.url(), routes.Departments.index().url()));
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
			final Department item = Department.get(id);
			item.delete();
			return redirect(routes.Departments.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
