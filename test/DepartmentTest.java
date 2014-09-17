import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import models.ModelException;
import models.entities.Department;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;


public class DepartmentTest {
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
	 * Создает тестовый класс для проверки добавления, поиска, 
	 * изменения и удаления записей в базе данных.
	 * @return экземпляр требуемого класса
	 */
	private Department makeItem() {
		Department item = new Department();
		item.name = "Name";
		item.boss = "Boss";
		item.secretar = "Secretar";
		return item;
	}
	
	private Department makeNewItem() {
		Department item = new Department();
		item.name = "NewName";
		item.boss = "NewBoss";
		item.secretar = "NewSecretar";
		return item;
	}
	
	private void assertItemNotNull(Department item) {
		assertNotNull(item.boss);
		assertNotNull(item.name);
		assertNotNull(item.secretar);
	}
	
	private void assertItemEquals(Department src, Department dst) {
		assertEquals(src.boss, dst.boss);
		assertEquals(src.name, dst.name);
		assertEquals(src.secretar, dst.secretar);
	}

	/**
	 * Проверим, выбросит ли исключение класс при заведомо неправильном ID
	 * @throws ModelException
	 */
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		Department result = Department.get(0);
		if (result != null) {
			fail("Вместо того чтобы выбросить ModelException, возвращается какой-то объект!");
		} else {
			fail("Вместо того чтобы выбросить ModelException, возвращается NULL!");
		}
	}
	
	@Test
	public void testOperations() {
		try {
			Department d = makeItem();
			// Insert (save method)
			d.save();
			// get method
			Department result = Department.get(d.getId());
			// Проверяем соответствие
			assertItemNotNull(result);
			assertItemEquals(result, d);
			assertEquals(result.getId(), d.getId());
			// fetchAll method
			List<Department> resultList = Department.fetchAll();
			assertNotNull(resultList);
			for (Department item: resultList) {
				assertItemNotNull(item);
			}
			// Создаем новую персону
			Department nd = makeNewItem();
			// обновляем старую
			d.updateFrom(nd);
			// Проверяем соответствие
			assertItemNotNull(d);
			assertItemEquals(d, nd);
			// Update (save method)
			d.save();
			// delete method
			d.delete();
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected=ModelException.class)
	public void testDeleteWrong() throws ModelException {
			Department d = makeItem();
			d.delete();
	}
}
