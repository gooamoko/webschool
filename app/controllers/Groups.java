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
		return TODO;
	}
	
	public static Result edit(final int depCode, final int id) {
		return TODO;
	}
	
	public static Result save(final int depCode, final int id) {
		return TODO;
	}
	
	public static Result confirm(final int depCode, final int id) {
		return TODO;
	}
	
	public static Result delete(final int depCode, final int id) {
		return TODO;
	}
}
