package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Card;
import models.entities.FinalMark;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.fmarks.edit;
import views.html.fmarks.index;

public class FinalMarks extends Controller {

	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";
	private static final Form<FinalMark> fmarkForm = Form.form(FinalMark.class);

	@Security.Authenticated(Secured.class)
	public static Result add(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Card card = Card.get(cardId);
				return ok(edit.render(fmarkForm, card, 0));
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
				return ok(index.render(card.getFinalMarks(), card));
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
				final FinalMark mark = FinalMark.get(id);
				return ok(edit.render(fmarkForm.fill(mark), mark.getCard(),
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
				final FinalMark item = FinalMark.get(id);
				String message = "Оценка по дисциплине \"" + item.getSubject()
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message, routes.FinalMarks
						.delete(personId, cardId, id).url(), routes.FinalMarks
						.index(personId, cardId).url()));
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
				final FinalMark mark = FinalMark.get(id);
				mark.delete();
				return redirect(routes.FinalMarks.index(personId, cardId));
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
				Form<FinalMark> form = fmarkForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", "При заполнении формы были допущены ошибки");
					return badRequest(edit.render(form, card, id));
				}
				FinalMark mark = form.get();
				mark.setCard(card);
				if (id == 0) {
					// Добавляем запись
					mark.save();
				} else {
					// Изменяем запись
					FinalMark old = FinalMark.get(id);
					old.updateFrom(mark);
					old.save();
					flash("success", String.format(
							"Успешно сохранена итоговая оценка %s",
							old.getSubject()));
				}
				return redirect(routes.FinalMarks.index(personId, cardId));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
