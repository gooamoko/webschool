import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import models.ModelException;
import models.entities.Account;
import models.entities.ClientSession;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;

/**
 * Проверка работоспособности класса клиентской сессии
 * @author Воронин Леонид
 *
 */
public class ClentSessionTest {
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
	 * Метод для создания персоны для проверки добавления, поиска, изменения и
	 * удаления записей в базе данных.
	 * 
	 * @return экземпляр класса Person
	 */
	private ClientSession makeItem() throws ModelException {
		ClientSession item = new ClientSession("127.0.0.1");
		item.setAccount(Account.get(1)); // код учетки админа
		return item;
	}

	@Test
	public void testOperations() {
		try {
			ClientSession cs = makeItem();
			// Insert (save method)
			cs.save();
			// find with wrong param
			ClientSession fs = ClientSession.find("test");
			assertNull(fs);
			// find with right param
			fs = ClientSession.find(cs.ssid);
			assertNotNull(fs);
			assertEquals(cs.ssid, fs.ssid);
			assertEquals(cs.ipaddr, fs.ipaddr);
			assertNotNull(fs.getAccount());
			// delete method
			fs.delete();
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}

	@Test(expected = ModelException.class)
	public void testDeleteWrong() throws ModelException {
		ClientSession cs = makeItem();
		cs.delete();
	}

	@Test
	public void testFetchAll() {
		try {
			List<ClientSession> list = ClientSession.fetchAll();
			assertNotNull(list);
			for (ClientSession cs : list) {
				assertNotNull(cs);
				assertNotNull(cs.getAccount());
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
}
