package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

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
import play.Logger;
import play.db.DB;

public class CardImport {

	public String pcode = ""; // Идентификатор группы
	private int cardCount = 0;
	private static final String NOT_FOUND = "Запрос не вернул ни одной записи.";
	private static final String NULL = "При обработке возникло NullPointerException.";
	private static final String ERROR = "Ошибка при импорте ";
	private static final String SCHOOL = " учебного заведения. ";
	private static final String CARD = " личной карточки. ";
	private static final String FMARK = " итоговой оценки. ";
	private static final String CMARK = " оценки за курсовой проект. ";
	private static final String PMARK = " оценки за практику. ";
	private static final String GMARK = " оценки за гос. экзамен. ";
	private static final String PERSON = " персоны. ";
	private static final String SPECIALITY = " специальности. ";
	private static final String SUBJECT = " дисциплины. ";
	private static final String PRACTIC = " практики. ";

	private void logAndThrow(String message) throws ModelException {
		Logger.error(message);
		throw new ModelException(message);
	}

	private void logOnly(String message) {
		Logger.info(message);
	}

	public int getCardCount() {
		return cardCount;
	}

	/**
	 * Получаем специальность по коду группы
	 * 
	 * @param groupCode
	 *            код группы
	 * @return
	 */
	private Speciality getSpeciality(String groupCode) throws ModelException {
		Speciality result = null;
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con
					.prepareStatement(
							"SELECT * FROM Specialities, Groups WHERE (sp_pcode = gr_speccode) AND (gr_pcode=?);",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, groupCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.first()) {
				// Что-то нашли, выбираем данные
				result = new Speciality();
				result.key = rs.getString("sp_Fullkey");
				result.name = rs.getString("sp_name");
				result.shortName = rs.getString("sp_shortName");
				result.kvalification = rs.getString("sp_kvalification");
				result.specialization = rs.getString("sp_specialization");
				result.length = rs.getString("gr_lernLength");
				// Если такая специальность уже есть, то будем использовать
				// существующую
				Speciality ex = Speciality.findLike(result);
				if (ex != null) {
					return ex;
				} else {
					result.save();
				}
			} else {
				logAndThrow(ERROR + SPECIALITY + NOT_FOUND);
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + SPECIALITY + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + SPECIALITY + NULL);
		}
		return result;
	}

	private Subject getSubject(final String subjectCode) throws ModelException {
		Subject result = null;
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con.prepareStatement(
					"SELECT * FROM subjects WHERE (sb_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, subjectCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.first()) {
				result = new Subject();
				result.name = rs.getString("sb_Name");
				Subject exSubject = Subject.find(result.name);
				if (exSubject != null) {
					return exSubject;
				} else {
					result.save();
				}
			} else {
				logAndThrow(ERROR + SUBJECT + NOT_FOUND);
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + SUBJECT + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + SUBJECT + NULL);
		}
		return result;
	}

	private Practic getPractic(final String practicCode) throws ModelException {
		Practic result = null;
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con.prepareStatement(
					"SELECT * FROM FnPractics WHERE (fpk_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, practicCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.first()) {
				result = new Practic();
				result.name = rs.getString("fpk_Title");
				Practic exPractic = Practic.find(result.name);
				if (exPractic != null) {
					return exPractic;
				} else {
					result.save();
				}
			} else {
				logAndThrow(ERROR + PRACTIC + NOT_FOUND);
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + PRACTIC + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + PRACTIC + NULL);
		}
		return result;
	}

	private void getFinalMarks(final String personCode, final Card card)
			throws ModelException {
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con
					.prepareStatement(
							"SELECT * FROM FnMarks WHERE (fnm_mark > 0) AND (fnm_stcode=?);",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, personCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				FinalMark fm = new FinalMark();
				fm.setSubject(getSubject(rs.getString("fnm_sbcode")));
				fm.auditoryLoad = rs.getInt("fnm_AHours");
				fm.maximumLoad = rs.getInt("fnm_Hours");
				fm.mark = rs.getInt("fnm_mark");
				fm.setCard(card);
				// Сохраняем запись
				fm.save();
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + FMARK + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + FMARK + NULL);
		}
	}

	private void getCourseWorkMarks(final String personCode, final Card card)
			throws ModelException {
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con.prepareStatement(
					"SELECT * FROM CwMarks, CourseWorks WHERE (cwm_cwcode = cw_pcode)"
							+ " AND (cwm_mark > 0) AND (cwm_stcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, personCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				CourseWorkMark cwm = new CourseWorkMark();
				cwm.setSubject(getSubject(rs.getString("cw_sbcode")));
				cwm.theme = rs.getString("cwm_Theme");
				cwm.mark = rs.getInt("cwm_mark");
				cwm.setCard(card);
				cwm.save();
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + CMARK + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + CMARK + NULL);
		}
	}

	private void getGosMarks(final String personCode, final Card card)
			throws ModelException {
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con
					.prepareStatement(
							"SELECT * FROM GOSMarks WHERE (gsm_mark > 0) AND (gsm_stcode=?);",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, personCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				GosMark gsm = new GosMark();
				gsm.setSubject(getSubject(rs.getString("gsm_sbcode")));
				gsm.mark = rs.getInt("gsm_mark");
				gsm.setCard(card);
				gsm.save();
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + GMARK + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + GMARK + NULL);
		}
	}

	private void getPracticMarks(final String personCode, final Card card)
			throws ModelException {
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con
					.prepareStatement(
							"SELECT * FROM FpMarks, FnPractics WHERE (fpm_fpcode = fpk_pcode) "
									+ "AND (fpm_mark > 0) AND (fpm_stcode=?) ORDER BY fpk_number;",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, personCode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				PracticMark pm = new PracticMark();
				pm.setPractic(getPractic(rs.getString("fpm_fpcode")));
				pm.length = rs.getFloat("fpk_length");
				pm.mark = rs.getInt("fpm_mark");
				pm.setCard(card);
				pm.save();
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + PMARK + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + PMARK + NULL);
		}
	}

	private Person getPerson(String personCode) throws ModelException {
		Person result = null;
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con.prepareStatement(
					"SELECT * FROM students WHERE (st_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, personCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.first()) {
				result = new Person();
				result.birthDate = rs.getDate("st_birthDate");
				result.birthPlace = rs.getString("st_birthPlace");
				result.firstName = rs.getString("st_FName");
				result.middleName = rs.getString("st_MName");
				result.lastName = rs.getString("st_LName");
				result.isMale = rs.getBoolean("st_ismale");
				result.isForeign = false;
				Person ex = Person.findLike(result);
				if (ex != null) {
					return ex;
				} else {
					result.save();
				}
			} else {
				logAndThrow(ERROR + PERSON + NOT_FOUND);
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + PERSON + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + PERSON + NULL);
		}
		return result;
	}

	private School getSchool(String personCode) throws ModelException {
		School result = null;
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con
					.prepareStatement(
							"SELECT * FROM students, schools WHERE (st_sccode = sc_pcode) AND (st_pcode=?);",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, personCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.first()) {
				result = new School();
				result.name = rs.getString("sc_Name");
				result.shortName = rs.getString("sc_SHortName");
				result.place = rs.getString("sc_place");
				result.director = rs.getString("sc_DName");
				School ex = School.findLike(result);
				if (ex != null) {
					return ex;
				} else {
					result.save();
				}
			} else {
				logAndThrow(ERROR + SCHOOL + NOT_FOUND);
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + SCHOOL + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + SCHOOL + NULL);
		}
		return result;
	}

	private boolean prepareCard(String personCode) throws ModelException {
		boolean result = false;
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con.prepareStatement(
					"SELECT * FROM students, comissions, sessions WHERE (ss_stcode = st_pcode) "
							+ "AND (ss_comcode = com_pcode) AND (st_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, personCode);
			ResultSet rs = stmt.executeQuery();
			if (rs.first()) {
				logOnly("Обнаружена карточка!");
				Card card = new Card();
				Person psn = getPerson(personCode);
				card.setPerson(psn);
				logOnly("-->" + psn.getFullName());
				School scl = getSchool(personCode);
				card.setSchool(scl);
				logOnly("-->" + scl.name);
				Speciality spc = getSpeciality(rs.getString("st_grcode"));
				card.setSpeciality(spc);
				logOnly("-->" + spc.shortName);
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				card.beginDate = sdf
						.parse("01.09." + rs.getString("st_inYear"));
				card.endDate = sdf.parse("30.06." + rs.getString("st_outYear"));
				card.remanded = false;
				card.red = rs.getBoolean("st_isRed");
				card.comissionDirector = rs.getString("com_PDirector");
				card.comissionDate = rs.getDate("com_Date");
				card.documentDate = sdf.parse("25.06."
						+ rs.getString("st_documentsYear").trim());
				card.documentName = rs.getString("st_documents");
				card.documentOrganization = ""; // Ну нет сведений в старой
												// базе!
				card.diplomeNumber = rs.getString("st_DiplNum");
				card.registrationNumber = rs.getString("st_DiplRegNum");
				card.diplomeDate = rs.getDate("st_diplGetDate");
				String theme = rs.getString("st_DProject");
				card.gosExam = true;
				card.diplomeMark = rs.getInt("st_GOSMark");
				if ((null != theme) && (!theme.isEmpty())) {
					card.diplomeTheme = theme;
					card.gosExam = false;
					card.diplomeMark = rs.getInt("st_DMark");
				}
				card.extramural = rs.getBoolean("st_isOutZaoch");
				// А вдруг есть старые карточки? Seek and destroy!
				for (Card old : psn.getCards()) {
					deleteCard(old);
				}
				// Сохраняем новую карточку
				card.save();
				// Сохраняем информацию об оценках
				getFinalMarks(personCode, card);
				getPracticMarks(personCode, card);
				getGosMarks(personCode, card);
				getCourseWorkMarks(personCode, card);
				result = true;
			} else {
				logOnly(ERROR + CARD + NOT_FOUND);
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow(ERROR + CARD + e.getMessage());
		} catch (ParseException e) {
			logAndThrow(ERROR + CARD + e.getMessage());
		} catch (NullPointerException e) {
			logAndThrow(ERROR + CARD + NULL);
		}
		return result;
	}

	/**
	 * Удаляет существующую карточку из базы
	 * 
	 * @param card
	 *            карточка, которую надо удалить
	 */
	private void deleteCard(Card card) throws ModelException {
		// TODO Возможно стоить упростить удаление карточки 
		if (card.getFinalMarks() != null) {
			for (FinalMark fm : card.getFinalMarks()) {
				fm.delete();
			}
		}
		if (card.getGosMarks() != null) {
			for (GosMark gm : card.getGosMarks()) {
				gm.delete();
			}
		}
		if (card.getCourseWorkMarks() != null) {
			for (CourseWorkMark cm : card.getCourseWorkMarks()) {
				cm.delete();
			}
		}
		if (card.getPracticMarks() != null) {
			for (PracticMark pm : card.getPracticMarks()) {
				pm.delete();
			}
		}
		card.delete();
	}

	/**
	 * Импортирует карточки из одной базы данных в другую.
	 * 
	 * @return количество успешно импортированных карточек
	 * @throws ModelException
	 */
	public int importCards() throws ModelException {
		int count = 0;
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con
					.prepareStatement(
							"SELECT st_pcode FROM students WHERE (st_Attributes = 0) AND (st_grcode=?) ORDER BY st_FullName;",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, pcode);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				cardCount += 1;
				if (prepareCard(rs.getString("st_pcode"))) {
					count += 1;
				}
			}
			rs.close();
			con.close();
		} catch (SQLException e) {
			logAndThrow("Исключение при подготовке списка для импорта. "
					+ e.getMessage());
		}
		return count;
	}

	/**
	 * Возвращает список групп для выбора
	 * 
	 * @return
	 */
	public static Map<String, String> importGroups() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try (Connection con = DB.getConnection("old")) {
			PreparedStatement stmt = con
					.prepareStatement("SELECT gr_pcode, gr_Name, COUNT(st_pcode) AS gr_students FROM Groups, Students "
							+ "WHERE (st_Attributes = 0) AND (st_grcode = gr_pcode) AND (gr_Attributes = 0) "
							+ "GROUP BY gr_pcode, gr_Name  ORDER BY gr_Name;");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				result.put(rs.getString("gr_pcode"), rs.getString("gr_Name")
						+ " (студентов: " + rs.getString("gr_students") + ")");
			}
			con.close();
		} catch (SQLException e) {
			Logger.error("Exception " + e);
			result.put("Exception", e.getMessage());
		}
		return result;
	}
}
