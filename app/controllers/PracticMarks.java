package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Card;
import models.entities.PracticMark;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.pmarks.edit;
import views.html.pmarks.index;

public class PracticMarks extends Controller {

	private static final Form<PracticMark> pmarkForm = Form
			.form(PracticMark.class);
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";

	@Security.Authenticated(Secured.class)
	public static Result add(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Card card = Card.get(cardId);
				return ok(edit.render(pmarkForm, card, 0));
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
				return ok(index.render(card.getPracticMarks(), card));
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
				final PracticMark mark = PracticMark.get(id);
				return ok(edit.render(pmarkForm.fill(mark), mark.getCard(),
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
				final PracticMark item = PracticMark.get(id);
				String message = "Оценка по практике \"" + item.getPractic()
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message, routes.PracticMarks
						.delete(personId, cardId, id).url(),
						routes.PracticMarks.index(personId, cardId).url()));
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
				final PracticMark mark = PracticMark.get(id);
				mark.delete();
				return redirect(routes.PracticMarks.index(personId, cardId));
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
				Form<PracticMark> form = pmarkForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", "При заполнении формы были допущены ошибки");
					return badRequest(edit.render(form, card, id));
				}
				PracticMark mark = form.get();
				mark.setCard(card);
				if (id == 0) {
					// Добавляем запись
					mark.save();
				} else {
					// Изменяем запись
					PracticMark old = PracticMark.get(id);
					old.updateFrom(mark);
					old.save();
					flash("success", String.format(
							"Успешно сохранена оценка по практике %s",
							old.getPractic()));
				}
				return redirect(routes.PracticMarks.index(card.getPerson()
						.getId(), card.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
