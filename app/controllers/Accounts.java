package controllers;

import static models.entities.ClientSession.getAccount;
import static models.entities.ClientSession.isAdmin;
import models.ModelException;
import models.entities.Account;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.accounts.edit;
import views.html.accounts.index;
import views.html.accounts.passwordForm;
import views.html.ajax.confirmDelete;

public class Accounts extends Controller {

	public static String ERROR = "Вы не имеете полномочий для выполнения этой операции.";

	@Security.Authenticated(Secured.class)
	public static Result newPassword(final int id) {
		try {
			Account ac = getAccount(session().get("ssid"));
			if (id == ac.getId()) {
				// Всё верно, рисуем страницу на смену пароля
				return ok(passwordForm.render(ac));
			}
			if (ac.isAdmin()) {
				// Отображаем страницу для смены пароля другому пользователю
				return ok(passwordForm.render(Account.get(id)));
			}
			return ok(errorPage.render(ERROR));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		} catch (NullPointerException e) {
			return ok(errorPage.render(ERROR));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result savePassword(final int id) {
		try {
			DynamicForm loginForm = Form.form().bindFromRequest();
			String password = loginForm.get("password");
			String confirm = loginForm.get("confirm");
			if (!password.contentEquals(confirm)) {
				flash("error", "Пароль и подтверждение не совпадают!");
				return redirect(routes.Application.index());
			}
			Account ac = getAccount(session().get("ssid"));
			if (id == ac.getId()) {
				ac.setPassword(password);
				ac.save();
				flash("success", "Пароль успешно изменен.");
				return redirect(routes.Application.index());
			}
			if (ac.isAdmin()) {
				Account user = Account.get(id);
				user.setPassword(password);
				user.save();
				flash("success", "Пароль для пользователя " + ac.login
						+ " изменен.");
				return redirect(routes.Application.index());
			}
			return ok(errorPage.render(ERROR));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		} catch (NullPointerException e) {
			return ok(errorPage.render(ERROR));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result save(final int id) {
		try {
			// Проверим, что сессия принадлежит админу
			if (isAdmin(session().get("ssid"))) {
				Account user = (id == 0) ? new Account() : Account.get(id);
				DynamicForm form = Form.form().bindFromRequest();
				user.role = Integer.parseInt(form.get("role"));
				user.login = form.get("login");
				user.description = form.get("description");
				String password = form.get("password");
				String confirm = form.get("confirm");
				// Обновляем пароль если он задан
				if ((null != password) && !password.isEmpty()) {
					if (!password.contentEquals(confirm)) {
						flash("error", "Пароль и подтверждение не совпадают!");
						return ok(edit.render(user));
					}
					user.setPassword(password);
				}
				// Проверяем данные на валидность
				if (!user.valid()) {
					flash("error", "Данные указаны неправильно!");
					return ok(edit.render(user));
				}
				// Обновляем данные в СУБД
				user.save();
				flash("success", "Учетная запись "
						+ ((id == 0) ? "добавлена." : "изменёна."));
				return redirect(routes.Accounts.index());
			}
			return ok(errorPage.render(ERROR));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(final int id) {
		try {
			// Проверим, что сессия принадлежит админу
			if (isAdmin(session().get("ssid"))) {
				// Рендерим страницу с формой
				return ok(edit.render(Account.get(id)));
			}
			return ok(errorPage.render(ERROR));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add() {
		// Проверим, что сессия принадлежит админу
		if (isAdmin(session().get("ssid"))) {
			// Рендерим страницу с формой
			return ok(edit.render(new Account()));
		}
		return ok(errorPage.render(ERROR));
	}

	@Security.Authenticated(Secured.class)
	public static Result confirm(int id) {
		try {
			final Account ac = Account.get(id);
			String message = "Учетная запись \"" + ac.login
					+ "\" будет удалена!";
			return ok(confirmDelete.render(message, routes.Accounts.delete(id)
					.url(), routes.Accounts.index().url()));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result delete(int id) {
		try {
			if (isAdmin(session().get("ssid"))) {
				final Account ac = Account.get(id);
				ac.delete();
				return redirect(routes.Accounts.index());
			}
			return ok(errorPage.render(ERROR));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result index() {
		try {
			if (isAdmin(session().get("ssid"))) {
				return ok(index.render(Account.fetchAll()));
			}
			return ok(errorPage.render(ERROR));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
