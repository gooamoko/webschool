package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Card;
import models.entities.GosMark;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.gmarks.edit;
import views.html.gmarks.index;

public class GosMarks extends Controller {

	private static final Form<GosMark> gmarkForm = Form.form(GosMark.class);
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	@Security.Authenticated(Secured.class)
	public static Result add(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Card card = Card.get(cardId);
				return ok(edit.render(gmarkForm, card, 0));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result index(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Card card = Card.get(cardId);
				return ok(index.render(card.getGosMarks(), card));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(int personId, int cardId, int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final GosMark mark = GosMark.get(id);
				return ok(edit.render(gmarkForm.fill(mark), mark.getCard(),
						mark.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result confirm(int personId, int cardId, int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final GosMark item = GosMark.get(id);
				String message = "Оценка за ГОС экзамен \"" + item.getSubject()
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message,
						routes.GosMarks.delete(personId, cardId, id).url(),
						routes.GosMarks.index(personId, cardId).url()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result delete(int personId, int cardId, int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final GosMark mark = GosMark.get(id);
				mark.delete();
				return redirect(routes.GosMarks.index(personId, cardId));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int personId, int cardId, int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Card card = Card.get(cardId);
				Form<GosMark> form = gmarkForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", "При заполнении формы были допущены ошибки");
					return badRequest(edit.render(form, card, id));
				}
				GosMark mark = form.get();
				mark.setCard(card);
				if (id == 0) {
					// Добавляем запись
					mark.save();
				} else {
					// Изменяем запись
					GosMark old = GosMark.get(id);
					old.updateFrom(mark);
					old.save();
					flash("success", String.format(
							"Успешно сохранен государственный экзамен %s",
							old.getSubject()));
				}
				return redirect(routes.GosMarks.index(card.getPerson().getId(),
						card.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
