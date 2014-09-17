import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.ModelException;
import models.entities.Card;
import models.entities.CourseWorkMark;
import models.entities.FinalMark;
import models.entities.GosMark;
import models.entities.Person;
import models.entities.Practic;
import models.entities.PracticMark;
import models.entities.School;
import models.entities.Speciality;
import models.entities.Subject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Helpers;


public class CardTest {
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
	private Card makeItem() throws ModelException {
		Card item = new Card();
		// Персону, учебное заведение и специальность возьмем реальные
		item.setPerson(Person.get(87));
		item.setSchool(School.get(141));
		item.setSpeciality(Speciality.get(161));
		item.beginDate = new Date();
		item.beginDate.setTime(item.beginDate.getTime()/2);
		item.endDate = new Date();
		item.documentDate = item.beginDate;
		item.documentName = "documentName";
		item.documentOrganization = "documentOrganization";
		item.remanded = true;
		item.remandReason = "remandReason";
		item.remandCommand = "remandCommand";
		item.diplomeNumber = "diplomeNumber";
		item.appendixNumber = "appendixNumber";
		item.registrationNumber = "regNumber";
		item.diplomeDate = item.endDate;
		item.comissionDirector = "comissionDirector";
		item.comissionDate = item.endDate;
		item.diplomeLength = 9.5f;
		item.diplomeTheme = "diplomeTheme";
		item.diplomeMark = 5;
		item.gosExam = true;
		item.red = true;
		item.extramural = true;		
		return item;
	}
	
	private Card makeNewItem() throws ModelException {
		Card item = new Card();
		// Персону, учебное заведение и специальность возьмем реальные
		item.setPerson(Person.get(87));
		item.setSchool(School.get(141));
		item.setSpeciality(Speciality.get(161));
		item.beginDate = new Date();
		item.beginDate.setTime(item.beginDate.getTime()/4);
		item.endDate = new Date();
		item.endDate.setTime(item.beginDate.getTime()/2);
		item.documentDate = item.beginDate;
		item.documentName = "newdocumentName";
		item.documentOrganization = "newdocumentOrganization";
		item.remanded = false;
		item.remandReason = "newremandReason";
		item.remandCommand = "newremandCommand";
		item.diplomeNumber = "newdiplomeNumber";
		item.appendixNumber = "newappendixNumber";
		item.registrationNumber = "newregNumber";
		item.diplomeDate = item.endDate;
		item.comissionDirector = "newcomissionDirector";
		item.comissionDate = item.endDate;
		item.diplomeLength = 8.3f;
		item.diplomeTheme = "newdiplomeTheme";
		item.diplomeMark = 4;
		item.gosExam = false;
		item.red = false;
		item.extramural = false;		
		return item;
	}
	
	private void assertCardEquals(final Card src, final Card dst) throws ModelException {
		assertEquals(sdf.format(src.beginDate), sdf.format(dst.beginDate));
		assertEquals(sdf.format(src.endDate), sdf.format(dst.endDate));
		assertEquals(sdf.format(src.comissionDate), sdf.format(dst.comissionDate));
		assertEquals(sdf.format(src.diplomeDate), sdf.format(dst.diplomeDate));
		assertEquals(sdf.format(src.documentDate), sdf.format(dst.documentDate));
		assertEquals(src.documentName, dst.documentName);
		assertEquals(src.documentOrganization, dst.documentOrganization);
		assertEquals(src.remandReason, dst.remandReason);
		assertEquals(src.remandCommand, dst.remandCommand);
		assertEquals(src.appendixNumber, dst.appendixNumber);
		assertEquals(src.diplomeNumber, dst.diplomeNumber);
		assertEquals(src.registrationNumber, dst.registrationNumber);
		assertEquals(src.diplomeTheme, dst.diplomeTheme);
		assertEquals(src.comissionDirector, dst.comissionDirector);
		
		// одинаковы ли школы?
		School srcscl = src.getSchool();
		School dstscl = dst.getSchool();
		assertNotNull(srcscl);
		assertNotNull(dstscl);
		assertEquals(srcscl.director, dstscl.director);
		assertEquals(srcscl.name, dstscl.name);
		assertEquals(srcscl.place, dstscl.place);
		assertEquals(srcscl.shortName, dstscl.shortName);
		assertEquals(srcscl.getId(), dstscl.getId());
		
		Speciality srcspc = src.getSpeciality();
		Speciality dstspc = dst.getSpeciality();
		assertNotNull(srcspc);
		assertNotNull(dstspc);
		assertEquals(srcspc.key, dstspc.key);
		assertEquals(srcspc.kvalification, dstspc.kvalification);
		assertEquals(srcspc.length, dstspc.length);
		assertEquals(srcspc.name, dstspc.name);
		assertEquals(srcspc.shortName, dstspc.shortName);
		assertEquals(srcspc.specialization, dstspc.specialization);

		Person srcpsn = src.getPerson();
		Person dstpsn = dst.getPerson();
		assertNotNull(srcpsn);
		assertNotNull(dstpsn);
		assertEquals(sdf.format(srcpsn.birthDate), sdf.format(dstpsn.birthDate));
		assertEquals(srcpsn.birthPlace, dstpsn.birthPlace);
		assertEquals(srcpsn.firstName, dstpsn.firstName);
		assertEquals(srcpsn.middleName, dstpsn.middleName);
		assertEquals(srcpsn.lastName, dstpsn.lastName);
		assertEquals(srcpsn.isForeign, dstpsn.isForeign);
		assertEquals(srcpsn.isMale, dstpsn.isMale);

		assertEquals(src.remanded, dst.remanded);
		assertEquals(src.red, dst.red);
		assertEquals(src.gosExam, dst.gosExam);
		assertEquals(src.extramural, dst.extramural);
		assertTrue(src.diplomeLength == dst.diplomeLength);
		assertTrue(src.diplomeMark == dst.diplomeMark);		
	}
	
	private void assertCardNotNull(final Card item) throws ModelException {
		assertNotNull(item);
		assertNotNull(item.getPerson());
		assertNotNull(item.getSchool());
		assertNotNull(item.getSpeciality());
		assertNotNull(item.beginDate);
		assertNotNull(item.endDate);
		assertNotNull(item.documentDate);
		assertNotNull(item.documentName);
		assertNotNull(item.documentOrganization);
		assertNotNull(item.remandReason);
		assertNotNull(item.remandCommand);
		assertNotNull(item.diplomeNumber);
		assertNotNull(item.appendixNumber);
		assertNotNull(item.registrationNumber);
		assertNotNull(item.diplomeDate);
		assertNotNull(item.comissionDirector);
		assertNotNull(item.comissionDate);
		assertNotNull(item.diplomeTheme);
	}
	
	@Test(expected = ModelException.class)
	public void testGetWrong() throws ModelException {
		Card result = Card.get(0);
		if (result != null) {
			fail("Вместо того чтобы выбросить ModelException, возвращается какой-то объект!");
		} else {
			fail("Вместо того чтобы выбросить ModelException, возвращается NULL!");
		}
	}
	
	@Test
	public void testOperations() {
		try {
			Card c = makeItem();
			// Insert (save method)
			c.save();
			// forPerson method
			List<Card> cards = Card.forPerson(c.getPerson());
			assertNotNull(cards);
			for (Card item: cards) {
				assertCardNotNull(item);
			}
			// get method
			Card result = Card.get(c.getId());
			// Проверяем соответствие
			assertCardNotNull(result);
			assertCardEquals(c, result);
			assertEquals(result.getId(), c.getId());
			// Создаем новую карточку
			Card nc = makeNewItem();
			// обновляем старую
			c.updateFrom(nc);
			// Проверяем соответствие
			assertCardNotNull(c);
			assertCardEquals(c, nc);
			// Update (save method)
			c.save();
			// проверим возможность указания оценок.
			// Практика
			PracticMark pm = new PracticMark();
			pm.setCard(c);
			pm.setPractic(Practic.get(24));
			pm.length = 3;
			pm.mark = 4;
			pm.save();
			List<PracticMark> pmarks = c.getPracticMarks();
			assertNotNull(pmarks);
			assertFalse(pmarks.isEmpty());
			assertTrue(c.getPracticLoad() > 0);
			PracticMark npm = new PracticMark();
			npm.setCard(c);
			npm.setPractic(Practic.get(24));
			pm.length = 2.5f;
			pm.mark = 5;
			pm.updateFrom(npm);
			pm.save();
			pm.delete();
			// ГОСы
			GosMark gm = new GosMark();
			gm.setCard(c);
			gm.setSubject(Subject.get(11));
			gm.mark = 3;
			gm.save();
			List<GosMark> gmarks = c.getGosMarks();
			assertNotNull(gmarks);
			assertFalse(gmarks.isEmpty());
			GosMark ngm = new GosMark();
			ngm.setCard(c);
			ngm.setSubject(Subject.get(11));
			ngm.mark = 4;
			gm.updateFrom(ngm);
			gm.save();
			gm.delete();
			// Курсовые
			CourseWorkMark cm = new CourseWorkMark();
			cm.setCard(c);
			cm.setSubject(Subject.get(11));
			cm.mark = 3;
			cm.theme = "test";
			cm.save();
			List<CourseWorkMark> cmarks = c.getCourseWorkMarks();
			assertNotNull(cmarks);
			assertFalse(cmarks.isEmpty());
			CourseWorkMark ncm = new CourseWorkMark();
			ncm.setCard(c);
			ncm.setSubject(Subject.get(11));
			ncm.mark = 4;
			ncm.theme = "newTest";
			cm.updateFrom(ncm);
			cm.save();
			cm.delete();
			// Итоговые оценки
			FinalMark fm = new FinalMark();
			fm.setCard(c);
			fm.setSubject(Subject.get(11));
			fm.mark = 3;
			fm.isModule = false;
			fm.maximumLoad = 100;
			fm.auditoryLoad = 64;
			fm.save();
			List<FinalMark> fmarks = c.getFinalMarks();
			assertNotNull(fmarks);
			assertFalse(fmarks.isEmpty());
			FinalMark nfm = new FinalMark();
			nfm.setCard(c);
			nfm.setSubject(Subject.get(11));
			nfm.mark = 5;
			nfm.isModule = false;
			nfm.maximumLoad = 120;
			nfm.auditoryLoad = 80;
			fm.updateFrom(nfm);
			fm.save();
			fm.delete();
			assertNotNull(c.getTitle());
			// delete method
			c.delete();
		} catch (ModelException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected=ModelException.class)
	public void testDeleteWrong() throws ModelException {
			Card c = makeItem();
			c.delete();
	}
}
