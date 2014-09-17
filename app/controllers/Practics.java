package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Practic;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.practics.edit;
import views.html.practics.index;

public class Practics extends Controller {
	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	private static final Form<Practic> practicForm = Form.form(Practic.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST))
				return ok(index.render(Practic.fetchAll()));
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
				return ok(index.render(Practic.findByName(key)));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
				Role.METHODIST))
			return ok(edit.render(practicForm, 0));
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.METHODIST)) {
				Form<Practic> form = practicForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", DATA_ERROR);
					return badRequest(edit.render(form, id));
				}
				Practic practic = form.get();
				if (id == 0) {
					// Добавляем запись
					practic.save();
				} else {
					// Изменяем запись
					Practic old = Practic.get(id);
					old.updateFrom(practic);
					old.save();
					flash("success", String.format(
							"Успешно сохранена практика %s, %d", old.name,
							old.getId()));
				}
				return redirect(routes.Practics.index());
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
				final Practic practic = Practic.get(id);
				Form<Practic> form = practicForm.fill(practic);
				return ok(edit.render(form, practic.getId()));
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
				final Practic item = Practic.get(id);
				String message = "Практика \"" + item.name
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message,
						routes.Practics.delete(id).url(), routes.Practics
								.index().url()));
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
				final Practic practic = Practic.get(id);
				practic.delete();
				return redirect(routes.Practics.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
