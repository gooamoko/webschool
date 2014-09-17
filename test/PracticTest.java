import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import models.ModelException;
import models.entities.Practic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;

public class PracticTest {
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
	 * Метод для создания объекта для проверки добавления, поиска, изменения и
	 * удаления записей в базе данных.
	 * 
	 */
	private Practic makeItem() {
		Practic item = new Practic();
		item.name = "Name";
		return item;
	}

	private Practic makeNewItem() {
		// Новая дисциплина
		Practic item = new Practic();
		item.name = "NewName";
		return item;
	}

	/**
	 * Проверим, выбросит ли исключение класс при заведомо неправильном ID
	 * 
	 * @throws ModelException
	 */
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		Practic result = Practic.get(0);
		if (result != null) {
			fail("Вместо того чтобы выбросить ModelException, возвращается какой-то объект!");
		} else {
			fail("Вместо того чтобы выбросить ModelException, возвращается NULL!");
		}
	}

	/**
	 * Тест для проверки основных операций по работе с СУБД.
	 */
	@Test
	public void testOperations() {
		try {
			Practic p = makeItem();
			// Insert (save method)
			p.save();
			// findLike method
			Practic fp = Practic.find(p.name);
			assertNotNull(fp);
			// get method
			Practic result = Practic.get(fp.getId());
			// Проверяем соответствие
			assertNotNull(result);
			assertEquals(result.name, fp.name);
			assertEquals(result.getId(), fp.getId());
			// Создаем новую запись
			Practic ns = makeNewItem();
			// обновляем старую
			fp.updateFrom(ns);
			// Проверяем соответствие
			assertNotNull(fp);
			assertEquals(ns.name, fp.name);
			// Update (save method)
			fp.save();
			// delete method
			fp.delete();
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFindWrong() {
		try {
			Practic fp = Practic.find(makeNewItem().name);
			assertNull(fp);
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}

	@Test(expected = ModelException.class)
	public void testDeleteWrong() throws ModelException {
		Practic p = makeItem();
		p.delete();
	}

	@Test
	public void testFetchAll() {
		try {
			List<Practic> list = Practic.fetchAll();
			assertNotNull(list);
			if (list.isEmpty()) {
				fail("Список не содержит ни одного элемента.");
			}
			for (Practic p : list) {
				assertNotNull(p);
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
}
