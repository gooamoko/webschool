import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import models.ModelException;
import models.entities.Person;
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
public class PersonTest {
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
	 * Метод для создания персоны для проверки добавления, поиска, 
	 * изменения и удаления записей в базе данных.
	 * @return экземпляр класса Person
	 */
	private Person makeItem() {
		Person person = new Person();
		person.birthDate = new Date();
		person.birthPlace = "Birth Place";
		person.firstName = "firstName";
		person.middleName = "middleName";
		person.lastName = "lastName";
		person.isForeign = true;
		person.isMale = true;
		return person;
	}
	
	private Person makeNewItem() {
		// Новая персона
		Person newPerson = new Person();
		newPerson.birthDate = new Date();
		newPerson.birthDate.setTime(newPerson.birthDate.getTime()/2);
		newPerson.birthPlace = "New Birth Place";
		newPerson.firstName = "NewFirstName";
		newPerson.middleName = "NewMiddleName";
		newPerson.lastName = "NewLastName";
		newPerson.isForeign = false;
		newPerson.isMale = false;
		return newPerson;
	}

	/**
	 * Проверим, выбросит ли исключение класс при заведомо неправильном ID
	 * @throws ModelException
	 */
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		Person result = Person.get(0);
		if (result != null) {
			fail("Вместо того чтобы выбросить ModelException, возвращается какой-то объект!");
		} else {
			fail("Вместо того чтобы выбросить ModelException, возвращается NULL!");
		}
	}
	
	@Test
	public void testOperations() {
		try {
			Person p = makeItem();
			// Insert (save method)
			p.save();
			// findLike method
			Person fp = Person.findLike(p);
			assertNotNull(fp);
			// get method
			Person result = Person.get(fp.getId());
			// Проверяем соответствие
			assertNotNull(result);
			assertEquals(result.firstName, fp.firstName);
			assertEquals(result.middleName, fp.middleName);
			assertEquals(result.lastName, fp.lastName);
			assertEquals(result.birthPlace, fp.birthPlace);
			assertEquals(result.isMale, fp.isMale);
			assertEquals(result.isForeign, fp.isForeign);
			assertNotNull(result.birthDate);
			assertEquals(sdf.format(result.birthDate), sdf.format(fp.birthDate));
			assertEquals(result.getId(), fp.getId());
			// findByName method
			List<Person> resultList = Person.findByName(p.firstName);
			assertNotNull(resultList);
			for (Person item: resultList) {
				assertNotNull(item);
				assertEquals(p.firstName, item.firstName);
			}
			// Создаем новую персону
			Person np = makeNewItem();
			// обновляем старую
			fp.updateFrom(np);
			// Проверяем соответствие
			assertNotNull(fp);
			assertEquals(fp.firstName, np.firstName);
			assertEquals(fp.middleName, np.middleName);
			assertEquals(fp.lastName, np.lastName);
			assertEquals(fp.birthPlace, np.birthPlace);
			assertEquals(fp.isMale, np.isMale);
			assertEquals(fp.isForeign, np.isForeign);
			assertNotNull(fp.birthDate);
			assertEquals(sdf.format(fp.birthDate), sdf.format(np.birthDate));
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
			Person fp = Person.findLike(makeNewItem());
			assertNull(fp);
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected=ModelException.class)
	public void testDeleteWrong() throws ModelException {
			Person p = makeItem();
			p.delete();
	}
	
	@Test
	public void testFetchAll() {
		try {
			List<Person> list = Person.fetchAll();
			assertNotNull(list);
			if (list.isEmpty()) {
				fail("Список не содержит ни одного элемента.");
			}
			for(Person p: list) {
				assertNotNull(p);
			}
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
}