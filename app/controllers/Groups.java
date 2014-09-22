package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Department;
import models.entities.Group;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.groups.edit;

public class Groups extends Controller {
	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";
	private static final Form<Group> itemForm = Form.form(Group.class);

	@Security.Authenticated(Secured.class)
	public static Result index(final int depCode) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.RECEPTION)) {
				Department dep = Department.get(depCode);
				return ok(views.html.groups.index.render(Group.getForDepartment(dep), dep.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
	
	public static Result add(final int depCode) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.RECEPTION)) {
				Department dep = Department.get(depCode);
				return ok(edit.render(itemForm, dep, 0));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
	
	public static Result edit(final int depCode, final int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.RECEPTION)) {
				Department dep = Department.get(depCode);
				Group item = Group.get(id);
				return ok(edit.render(itemForm.fill(item), dep, id));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
	
	public static Result save(final int depCode, final int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.RECEPTION)) {
				Department dep = Department.get(depCode);
				Form<Group> form = itemForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", "При заполнении формы были допущены ошибки");
					return badRequest(edit.render(form, dep, id));
				}
				Group item = form.get();
				item.setDepartment(dep);
				if (id == 0) {
					// Добавляем запись
					item.save();
					flash("success", String.format(
							"Успешно добавлена группа %s",
							item.name));
				} else {
					// Изменяем запись
					Group old = Group.get(id);
					old.updateFrom(item);
					old.save();
					flash("success", String.format(
							"Успешно сохранена группа %s",
							old.name));
				}
				return redirect(routes.Groups.index(depCode));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
	
	public static Result confirm(final int depCode, final int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.RECEPTION)) {
				Group item = Group.get(id);
				String message = "Группа \"" + item.name
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message, routes.Groups.delete(depCode, id).url(), 
						routes.Groups.index(depCode).url()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
	
	public static Result delete(final int depCode, final int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT, Role.RECEPTION)) {
				Group item = Group.get(id);
				item.delete();
				return redirect(routes.Groups.index(depCode));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
