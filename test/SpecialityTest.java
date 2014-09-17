import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import models.ModelException;
import models.entities.Speciality;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;

public class SpecialityTest {
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
	private Speciality makeItem() {
		Speciality item = new Speciality();
		item.name = "Name";
		item.shortName = "SN";
		item.key = "Key";
		item.specialization = "Specialization";
		item.kvalification = "Kvalification";
		item.length = "Length";
		return item;
	}

	private Speciality makeNewItem() {
		// Новая специальность
		Speciality item = new Speciality();
		item.name = "NewName";
		item.shortName = "NSN";
		item.key = "NewKey";
		item.specialization = "NewSpecialization";
		item.kvalification = "NewKvalification";
		item.length = "NewLength";
		return item;
	}

	/**
	 * Проверим, выбросит ли исключение класс при заведомо неправильном ID
	 * 
	 * @throws ModelException
	 */
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		Speciality result = Speciality.get(0);
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
			Speciality s = makeItem();
			// Insert (save method)
			s.save();
			// findLike method
			Speciality fs = Speciality.findLike(s);
			assertNotNull(fs);
			// get method
			Speciality result = Speciality.get(fs.getId());
			// Проверяем соответствие
			assertNotNull(result);
			assertEquals(result.name, fs.name);
			assertEquals(result.shortName, fs.shortName);
			assertEquals(result.key, fs.key);
			assertEquals(result.kvalification, fs.kvalification);
			assertEquals(result.specialization, fs.specialization);
			assertEquals(result.kvalification, fs.kvalification);
			assertEquals(result.length, fs.length);
			assertEquals(result.getId(), fs.getId());
			// Создаем новую запись
			Speciality ns = makeNewItem();
			// обновляем старую
			fs.updateFrom(ns);
			// Проверяем соответствие
			assertNotNull(fs);
			assertEquals(ns.name, fs.name);
			assertEquals(ns.shortName, fs.shortName);
			assertEquals(ns.key, fs.key);
			assertEquals(ns.kvalification, fs.kvalification);
			assertEquals(ns.specialization, fs.specialization);
			assertEquals(ns.kvalification, fs.kvalification);
			assertEquals(ns.length, fs.length);
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
			Speciality fs = Speciality.findLike(makeNewItem());
			assertNull(fs);
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}

	@Test(expected = ModelException.class)
	public void testDeleteWrong() throws ModelException {
		Speciality s = makeItem();
		s.delete();
	}
	
	@Test
	public void testFetchAll() {
		try {
			List<Speciality> list = Speciality.fetchAll();
			assertNotNull(list);
			if (list.isEmpty()) {
				fail("Список не содержит ни одного элемента.");
			}
			for(Speciality s: list) {
				assertNotNull(s);
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
}
