import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.ModelException;
import models.entities.Renaming;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;

public class RenamingTest {
	private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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
	private Renaming makeItem() {
		Renaming item = new Renaming();
		item.renamingDate = new Date();
		item.newName = "NewName";
		item.oldName = "OldName";
		return item;
	}

	private Renaming makeNewItem() {
		// Новое переименование
		Renaming item = new Renaming();
		item.renamingDate = new Date();
		item.renamingDate.setTime((new Date()).getTime() / 2);
		item.newName = "NewNewName";
		item.oldName = "NewOldName";
		return item;
	}

	private void checkEquals(Renaming src, Renaming dst) {
		assertEquals(src.newName, dst.newName);
		assertEquals(src.oldName, dst.oldName);
		assertEquals(sdf.format(src.renamingDate), sdf.format(dst.renamingDate));
	}

	/**
	 * Проверим, выбросит ли исключение класс при заведомо неправильном ID
	 * 
	 * @throws ModelException
	 */
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		Renaming result = Renaming.get(0);
		if (result != null) {
			fail("Вместо того чтобы выбросить ModelException, возвращается какой-то объект!");
		} else {
			fail("Вместо того чтобы выбросить ModelException, возвращается NULL!");
		}
	}

	@Test
	public void testOperations() {
		try {
			Renaming r = makeItem();
			// Insert (save method)
			r.save();
			// get method
			Renaming result = Renaming.get(r.getId());
			// Проверяем соответствие
			assertNotNull(result);
			checkEquals(result, r);
			assertEquals(result.getId(), r.getId());
			// Создаем новую персону
			Renaming nr = makeNewItem();
			// обновляем старую
			r.updateFrom(nr);
			// Проверяем соответствие
			assertNotNull(r);
			checkEquals(r, nr);
			// Update (save method)
			r.save();
			result = Renaming.get(r.getId());
			assertNotNull(result);
			checkEquals(result, r);
			assertEquals(result.getId(), r.getId());
			// delete method
			r.delete();
			result = null;
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}

	@Test(expected = ModelException.class)
	public void testDeleteWrong() throws ModelException {
		Renaming r = makeItem();
		r.delete();
	}
	
	@Test
	public void testFetchAll() {
		try {
			List<Renaming> list = Renaming.fetchAll();
			assertNotNull(list);
			if (list.isEmpty()) {
				fail("Список не содержит ни одного элемента.");
			}
			for(Renaming r: list) {
				assertNotNull(r);
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
	//TODO Добавить тесты для поиска по интервалу.
}
