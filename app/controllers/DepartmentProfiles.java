package controllers;

import static models.entities.ClientSession.isAdmin;
import models.ModelException;
import models.entities.DepartmentProfile;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class DepartmentProfiles extends Controller {

	private static final Form<DepartmentProfile> itemForm = Form.form(DepartmentProfile.class);
	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdmin(session().get("ssid"))) {
				return ok(index.render(DepartmentProfile.fetchAll()));
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
				Form<DepartmentProfile> form = itemForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", "При заполнении формы были допущены ошибки");
					return badRequest(edit.render(form, id));
				}
				DepartmentProfile item = form.get();
				if (id == 0) {
					// Добавляем запись
					item.save();
				} else {
					// Изменяем запись
					DepartmentProfile old = DepartmentProfile.get(id);
					old.updateFrom(item);
					old.save();
					flash("success", "Запись успешно сохранена.");
				}
				return redirect(routes.DepartmentProfiles.index());
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
				final DepartmentProfile item = DepartmentProfile.get(id);
				return ok(edit.render(itemForm.fill(item));
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
				final DepartmentProfile item = DepartmentProfile.get(id);
				String message = "Профиль отделения \"" + item.department
						+ "\" по специальности \"" + item.speciality
						+ "\" будет удален!";
				return ok(confirmDelete.render(message, routes.DepartmentProfiles
						.delete(id).url(), routes.DepartmentProfiles.index().url()));
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
				final DepartmentProfile item = DepartmentProfile.get(id);
				item.delete();
				return redirect(routes.DepartmentProfiles.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}







