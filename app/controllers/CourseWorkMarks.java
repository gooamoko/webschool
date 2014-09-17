package controllers;

import static models.entities.ClientSession.isAdminOrInList;
import models.ModelException;
import models.Role;
import models.entities.Card;
import models.entities.CourseWorkMark;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.errorPage;
import views.html.ajax.confirmDelete;
import views.html.cmarks.edit;
import views.html.cmarks.index;

public class CourseWorkMarks extends Controller {

	public final static String ACCESS_DENIED = "У вас недостаточно прав для выполнения этой операции.";
	private static final Form<CourseWorkMark> cmarkForm = Form.form(CourseWorkMark.class);

	@Security.Authenticated(Secured.class)
	public static Result add(int personId, int cardId) {
		try {
			if (isAdminOrInList(session().get("ssid"), Role.DEPARTMENT)) {
			Card card = Card.get(cardId);
			return ok(edit.render(cmarkForm, card, 0));
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
			return ok(index.render(card.getCourseWorkMarks(), card));
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
			final CourseWorkMark mark = CourseWorkMark.get(id);
			return ok(edit.render(cmarkForm.fill(mark), mark.getCard(), mark.getId()));
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
			final CourseWorkMark item = CourseWorkMark.get(id);
			String message = "Оценка за курсовой проект по дисциплине \"" +
					item.getSubject() + "\" будет удалена!";
			return ok(confirmDelete.render(message, routes.CourseWorkMarks.delete(personId, cardId, id).url(),
					routes.CourseWorkMarks.index(personId, cardId).url()));
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
			final CourseWorkMark mark = CourseWorkMark.get(id);
			mark.delete();
			return redirect(routes.CourseWorkMarks.index(personId, cardId));
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
			Form<CourseWorkMark> form = cmarkForm.bindFromRequest();
			if (form.hasErrors()) {
				flash("error", "При заполнении формы были допущены ошибки");
				return badRequest(edit.render(form, card, id));
			}
			CourseWorkMark mark = form.get();
			mark.setCard(card);
			if (id == 0) {
				// Добавляем запись
				mark.save();
			} else {
				// Изменяем запись
				CourseWorkMark old = CourseWorkMark.get(id);
				old.updateFrom(mark);
				old.save();
				flash("success", String.format("Успешно сохранен курсовой проект %s",
						old.getSubject()));
			}
			return redirect(routes.CourseWorkMarks.index(personId, cardId));
			}
			return ok(errorPage.render(ACCESS_DENIED));
		} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
		}
	}
}
