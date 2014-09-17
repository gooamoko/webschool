package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Speciality;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.specialities.*;

public class Specialities extends Controller {

	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	private static final Form<Speciality> inputForm = Form
			.form(Speciality.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
				return ok(index.render(Speciality.fetchAll()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
			return ok(edit.render(inputForm, 0));
		}
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.METHODIST)) {
				Form<Speciality> form = inputForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", DATA_ERROR);
					return badRequest(edit.render(form, id));
				}
				Speciality item = form.get();
				if (id == 0) {
					// Добавляем запись
					item.save();
				} else {
					// Изменяем запись
					Speciality old = Speciality.get(id);
					old.updateFrom(item);
					old.save();
					flash("success", String.format(
							"Успешно сохранена запись %s", old.name));
				}
				return redirect(routes.Specialities.index());
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
				final Speciality item = Speciality.get(id);
				return ok(edit.render(inputForm.fill(item), item.getId()));
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
				final Speciality item = Speciality.get(id);
				String message = "Специальность \"" + item.name
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message, routes.Specialities
						.delete(id).url(), routes.Specialities.index().url()));
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
				final Speciality item = Speciality.get(id);
				item.delete();
				return redirect(routes.Specialities.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
