import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import models.ModelException;
import models.entities.Account;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;

/**
 * Класс для проверки работоспособности класса "Персоны"
 * 
 * @author Воронин Леонид
 * 
 */
public class AccountTest {
	/**
	 * Запустим фейковое приложение перед всеми тестами
	 */
	@BeforeClass
	public static void prepareApplication() {
		Helpers.start(play.test.Helpers.fakeApplication());
	}

	/**
	 * Остановим фейковое приложение после всех тестов
	 */
	@AfterClass
	public static void stopApplication() {
		Helpers.stop(play.test.Helpers.fakeApplication());
	}
	
	/**
	 * Метод для создания персоны для проверки добавления, поиска, 
	 * изменения и удаления записей в базе данных.
	 * @return экземпляр класса Person
	 */
	private Account makeItem() throws ModelException {
		Account item = new Account();
		item.login = "login";
		item.description = "description";
		item.setPassword("password");
		return item;
	}
	
	private Account makeNewItem() throws ModelException {
		Account item = new Account();
		item.login = "newLogin";
		item.description = "newDescription";
		item.setPassword("newPassword");
		return item;
	}

	/**
	 * Проверим, выбросит ли исключение класс при заведомо неправильном ID
	 * @throws ModelException
	 */
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		Account account = Account.get(0);
		if (account != null) {
			fail("Вместо того чтобы выбросить ModelException, возвращается какой-то объект!");
		} else {
			fail("Вместо того чтобы выбросить ModelException, возвращается NULL!");
		}
	}
	
	@Test
	public void testOperations() {
		try {
			Account a = makeItem();
			// Insert (save method)
			a.save();
			// Проверим возможность смены пароля
			a.setPassword("anotherPassword");
			a.save();
			// Проверим возможность залогиниться
			Account la = Account.auth("login", "password");
			assertNull(la);
			la = Account.auth("login", "anotherPassword");
			assertNotNull(la);
			assertEquals(la.login, a.login);
			assertEquals(la.description, a.description);			
			// get method
			Account result = Account.get(la.getId());
			// Проверяем соответствие
			assertNotNull(result);
			assertEquals(result.login, la.login);
			assertEquals(result.description, la.description);
			assertEquals(result.getId(), la.getId());
			// Проверим возможность смены пароля
			a.setPassword("anotherPassword");
			a.save();
			// Создаем новую персону
			Account na = makeNewItem();
			// обновляем старую
			a.updateFrom(na);
			// Проверяем соответствие
			assertNotNull(a);
			assertEquals(na.login, a.login);
			assertEquals(na.description, a.description);
			// Update (save method)
			a.save();
			// delete method
			a.delete();
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testAuthWrong() {
		try {
			Account ac = Account.auth("test", "");
			assertNull(ac);
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected=ModelException.class)
	public void testDeleteWrong() throws ModelException {
			Account ac = makeItem();
			ac.delete();
	}
	
	@Test
	public void testFetchAll() {
		try {
			List<Account> list = Account.fetchAll();
			assertNotNull(list);
			if (list.isEmpty()) {
				fail("Список не содержит ни одного элемента.");
			}
			for(Account a: list) {
				assertNotNull(a);
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
}