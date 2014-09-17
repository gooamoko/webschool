package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Renaming;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.renamings.edit;
import views.html.renamings.index;

public class Renamings extends Controller {

	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	private static final Form<Renaming> itemForm = Form.form(Renaming.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.METHODIST)) {
			return ok(index.render(Renaming.fetchAll()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.METHODIST)) {
		return ok(edit.render(itemForm, 0));
		}
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.METHODIST)) {
			Form<Renaming> form = itemForm.bindFromRequest();
			if (form.hasErrors()) {
				flash("error", DATA_ERROR);
				return badRequest(edit.render(form, id));
			}
			Renaming item = form.get();
			if (id == 0) {
				// Добавляем запись
				item.save();
			} else {
				// Изменяем запись
				Renaming old = new Renaming(id);
				old.updateFrom(item);
				old.save();
				flash("success", String.format(
						"Успешно сохранено переименование %s, %d", old.oldName,
						old.getId()));
			}
			return redirect(routes.Renamings.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.METHODIST)) {
			final Renaming item = Renaming.get(id);
			Form<Renaming> form = itemForm.fill(item);
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
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.METHODIST)) {
			final Renaming item = Renaming.get(id);
			String message = "Информация о переименовании \"" +
					item.oldName + "\" в \"" + item.newName + "\" будет удалена!";
			return ok(confirmDelete.render(message, routes.Renamings.delete(id).url(),
					routes.Renamings.index().url()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
		}
	}
	
	@Security.Authenticated(Secured.class)
	public static Result delete(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.METHODIST)) {
			final Renaming item = new Renaming(id);
			item.delete();
			return redirect(routes.Renamings.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
		}
	}
}
