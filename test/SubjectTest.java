import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import models.ModelException;
import models.entities.Subject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;

public class SubjectTest {
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
	private Subject makeItem() {
		Subject item = new Subject();
		item.name = "Name";
		return item;
	}

	private Subject makeNewItem() {
		// Новая дисциплина
		Subject item = new Subject();
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
		Subject result = Subject.get(0);
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
			Subject s = makeItem();
			// Insert (save method)
			s.save();
			// findLike method
			Subject fs = Subject.find(s.name);
			assertNotNull(fs);
			// get method
			Subject result = Subject.get(fs.getId());
			// Проверяем соответствие
			assertNotNull(result);
			assertEquals(result.name, fs.name);
			assertEquals(result.getId(), fs.getId());
			// Создаем новую запись
			Subject ns = makeNewItem();
			// обновляем старую
			fs.updateFrom(ns);
			// Проверяем соответствие
			assertNotNull(fs);
			assertEquals(ns.name, fs.name);
			// Update (save method)
			fs.save();
			// delete method
			fs.delete();
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFindWrong() {
		try {
			Subject fs = Subject.find(makeNewItem().name);
			assertNull(fs);
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}

	@Test(expected = ModelException.class)
	public void testDeleteWrong() throws ModelException {
		Subject s = makeItem();
		s.delete();
	}
	
	@Test
	public void testFetchAll() {
		try {
			List<Subject> list = Subject.fetchAll();
			assertNotNull(list);
			if (list.isEmpty()) {
				fail("Список не содержит ни одного элемента.");
			}
			for(Subject s: list) {
				assertNotNull(s);
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
}
