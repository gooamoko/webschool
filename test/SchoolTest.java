import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import models.ModelException;
import models.entities.School;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;



public class SchoolTest {
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
	 * Метод для создания образовательного учреждения для проверки добавления, поиска, 
	 * изменения и удаления записей в базе данных.
	 * @return экземпляр класса School
	 */
	private School makeItem() {
		School item = new School();
		item.name = "SchoolName";
		item.shortName = "ShortName";
		item.place = "Place";
		item.director = "Director";
		return item;
	}
	
	private School makeNewItem() {
		// Новое учебное заведение
		School item = new School();
		item.name = "NewSchoolName";
		item.shortName = "NewShortName";
		item.place = "NewPlace";
		item.director = "NewDirector";
		return item;
	}

	/**
	 * Проверим, выбросит ли исключение класс при заведомо неправильном ID
	 * @throws ModelException
	 */
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		School result = School.get(0);
		if (result != null) {
			fail("Вместо того чтобы выбросить ModelException, возвращается какой-то объект!");
		} else {
			fail("Вместо того чтобы выбросить ModelException, возвращается NULL!");
		}
	}
	
	@Test
	public void testOperations() {
		try {
			School s = makeItem();
			// Insert (save method)
			s.save();
			// findLike method
			School fs = School.findLike(s);
			assertNotNull(fs);
			// get method
			School result = School.get(fs.getId());
			// Проверяем соответствие
			assertNotNull(result);
			assertEquals(result.name, fs.name);
			assertEquals(result.shortName, fs.shortName);
			assertEquals(result.place, fs.place);
			assertEquals(result.director, fs.director);
			assertEquals(result.getId(), fs.getId());
			// Создаем новую запись
			School ns = makeNewItem();
			// обновляем старую
			fs.updateFrom(ns);
			// Проверяем соответствие
			assertNotNull(fs);
			assertEquals(fs.name, ns.name);
			assertEquals(fs.shortName, ns.shortName);
			assertEquals(fs.place, ns.place);
			assertEquals(fs.director, ns.director);
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
			School fs = School.findLike(makeNewItem());
			assertNull(fs);
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected=ModelException.class)
	public void testDeleteWrong() throws ModelException {
			School s = makeItem();
			s.delete();
	}
	
	@Test
	public void testFetchAll() {
		try {
			List<School> list = School.fetchAll();
			assertNotNull(list);
			if (list.isEmpty()) {
				fail("Список не содержит ни одного элемента.");
			}
			for(School s: list) {
				assertNotNull(s);
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
}
