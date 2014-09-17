package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Card;
import models.entities.Person;
import models.reports.DIplome;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.cards.details;
import views.html.cards.edit;
import views.html.cards.index;

public class Cards extends Controller {

	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";
	private static final Form<Card> itemForm = Form.form(Card.class);

	@Security.Authenticated(Secured.class)
	public static Result index(int personId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Person p = Person.get(personId);
				return ok(index.render(p.getCards(), p));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result add(int personId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Person p = Person.get(personId);
				return ok(edit.render(itemForm, p, 0));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result edit(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final Card card = Card.get(cardId);
				return ok(edit.render(itemForm.fill(card), card.getPerson(),
						card.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result save(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				Person p = Person.get(personId);
				Form<Card> form = itemForm.bindFromRequest();
				if (form.hasErrors()) {
					flash("error", "При заполнении формы были допущены ошибки");
					return badRequest(edit.render(form, p, cardId));
				}
				Card card = form.get();
				card.setPerson(p);
				if (cardId == 0) {
					// Добавляем запись
					card.save();
				} else {
					// Изменяем запись
					Card old = Card.get(cardId);
					old.updateFrom(card);
					old.save();
					flash("success", String.format(
							"Успешно сохранена карточка %s, %d",
							old.getTitle(), old.getId()));
				}
				return redirect(routes.Cards.index(p.getId()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result confirm(int personId, int id) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final Card item = Card.get(id);
				String message = "Личная карточка \"" + item.getTitle()
						+ "\" будет удалена!";
				return ok(confirmDelete.render(message,
						routes.Cards.delete(personId, id).url(), routes.Cards
								.index(personId).url()));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result delete(int personId, int cardId) {
		try {
			final Card card = Card.get(cardId);
			card.delete();
			return redirect(routes.Cards.index(personId));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result details(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final Card card = Card.get(cardId);
				return ok(details.render(card));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	private static Result getDiplome(final Card card, final boolean copy) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				DIplome rep = new DIplome();
				rep.build(card, copy);
				if (rep.isReady()) {
					response().setContentType("application/pdf");
					return ok(rep.getReportContent());
				}
				return ok(errorPage
						.render("Произошла неизвестная ошибка при генерации отчета."));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result diplome(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final Card card = Card.get(cardId);
				return getDiplome(card, false);
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result copy(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
				final Card card = Card.get(cardId);
				return getDiplome(card, true);
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
			return ok(errorPage.render(e.getMessage()));
		}
	}
}
