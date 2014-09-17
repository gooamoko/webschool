package controllers;

import models.CardImport;
import models.ModelException;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

public class Import extends Controller {

	public final static String DATA_ERROR = "Некорректно заполнены поля формы.";
	public final static String NOT_FOUND = "Запрашиваемая запись не найдена.";

	private static final Form<CardImport> itemForm = Form.form(CardImport.class);

	@Security.Authenticated(Secured.class)
	public static Result begin() {
		return ok(importcard.render(itemForm));
	}

	@Security.Authenticated(Secured.class)
	public static Result process() {
		Form<CardImport> form = itemForm.bindFromRequest();
		if (form.hasErrors()) {
			flash("error", DATA_ERROR);
			return badRequest(importcard.render(form));
		}
		// Получили код группы
		CardImport item = form.get();
		try {
			int count = item.importCards();
			if (count > 0) {
				flash("success", String.format(
						"Успешно импортировано %d личных карточек из %d потенциально возможных",
						count, item.getCardCount()));
				return redirect(routes.Application.index());
			} else {
				flash("error", "Ни одной карточки экспортировано не было!");
				return redirect(routes.Application.index());
			}
		} catch (ModelException e) {
    		return ok(errorPage.render(e.getMessage()));
		}
	}
}
