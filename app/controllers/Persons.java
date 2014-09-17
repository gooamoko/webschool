package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Person;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.persons.details;
import views.html.persons.edit;
import views.html.persons.index;

public class Persons extends Controller {
	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	private static final Form<Person> personForm = Form.form(Person.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.RECEPTION)) {
				return ok(index.render(Person.fetchAll()));
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
					Role.RECEPTION)) {
				String key = request().body().asFormUrlEncoded().get("name")[0];
				return ok(index.render(Person.findByName(key)));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result details(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.RECEPTION)) {
				final Person person = Person.get(id);
				return ok(details.render(person));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
				Role.RECEPTION)) {
			return ok(edit.render(personForm, 0));
		}
		return ok(errorPage.render(ACCESS_DENIED));
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.RECEPTION)) {
				Form<Person> form = personForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", DATA_ERROR);
					return badRequest(edit.render(form, id));
				}
				Person person = form.get();
				if (id == 0) {
					// Добавляем запись
					person.save();
				} else {
					// Изменяем запись
					Person old = Person.get(id);
					old.updateFrom(person);
					old.save();
					flash("success", String.format(
							"Успешно сохранена персона %s, %d",
							old.getFullName(), old.getId()));
				}
				return redirect(routes.Persons.index());
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
					Role.RECEPTION)) {
				final Person person = Person.get(id);
				return ok(edit.render(personForm.fill(person), person.getId()));
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
					Role.RECEPTION)) {
				final Person item = Person.get(id);
				String message = "Информация о персоне \"" + item.getFullName()
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message,
						routes.Persons.delete(id).url(), routes.Persons.index()
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
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT,
					Role.RECEPTION)) {
				final Person person = Person.get(id);
				person.delete();
				return redirect(routes.Persons.index());
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
