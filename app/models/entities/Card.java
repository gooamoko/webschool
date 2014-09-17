package models.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.ModelException;
import models.Utils;
import play.db.DB;

/**
 * Класс для инкапсуляции персональной карточки студента. Персональных карточек
 * может быть несколько, поэтому тут целесообразно хранить только то, что
 * связывает студента со специальностью.
 * 
 * @author Воронин Леонид
 */
public class Card {

	final static String ERROR = "Ошибка при работе с таблицей личных карточек ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String PRACTIC = "при подсчете практик. ";
	final static String FMARK = "при выборке списка итоговых оценок. ";	
	final static String CMARK = "при выборке списка курсовых. ";	
	final static String PMARK = "при выборке списка практик. ";	
	final static String GMARK = "при выборке списка гос. экзаменов. ";	
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";
	
	private int id;
	private int psnCode = 0;
	public int sclCode = 0;
	public int spcCode = 0;
	public boolean extramural; // заочник
	public Date beginDate; // дата начала обучения
	public Date endDate; // дата окончания обучения (дата комиссии)
	public Date documentDate; // Дата выдачи документа, на основе которого
								// поступил
	public String documentName; // Наименование документа на основании которого
								// поступил
	public String documentOrganization; // Организация, выдавшая документ
	public boolean remanded; // Был ли студент отчислен (не закончил обычным
								// образом)
	public String remandReason; // Причина окончания обучения (преимущественно
								// для академической справки)
	public String remandCommand; // Номер приказа на отчисление
	public String diplomeNumber; // Номер бланка диплома
	public String appendixNumber; // Номер бланка приложения к диплому
	public String registrationNumber; // регистрационный номер
	public String comissionDirector; // Председатель коммиссии
	public Date comissionDate; // Дата комиссии
	public boolean gosExam;   // Гос экзамен или дипломирование?
	public float diplomeLength; // Продолжительность итоговой гос. аттестации в неделях
	public String diplomeTheme; // Тема дипломного проекта
	public int diplomeMark; // Оценка за дипломный проект
	public Date diplomeDate; // дата выдачи диплома
	public boolean red;  // Красный диплом
	
	
	private void readFields(ResultSet rs) throws SQLException {
		psnCode = rs.getInt("crd_psncode");
		sclCode = rs.getInt("crd_sclcode");
		spcCode = rs.getInt("crd_spccode");
		beginDate = rs.getTimestamp("crd_bdate");
		endDate = rs.getTimestamp("crd_edate");
		documentDate = rs.getTimestamp("crd_docdate");
		documentName = rs.getString("crd_docname");
		documentOrganization = rs.getString("crd_docorganization");
		remanded = rs.getBoolean("crd_isremanded");
		remandReason = rs.getString("crd_remandreason");
		remandCommand = rs.getString("crd_remandcommand");
		diplomeNumber = rs.getString("crd_diplomenumber");
		appendixNumber = rs.getString("crd_appendixnumber");
		registrationNumber = rs.getString("crd_regnumber");
		diplomeDate = rs.getTimestamp("crd_diplomedate");
		comissionDirector = rs.getString("crd_comissiondirector");
		comissionDate = rs.getTimestamp("crd_comissiondate");
		diplomeLength = rs.getFloat("crd_diplomelength");
		diplomeTheme = rs.getString("crd_diplometheme");
		diplomeMark = rs.getInt("crd_diplomemark");
		gosExam = rs.getBoolean("crd_isgosexam");
		red = rs.getBoolean("crd_isred");
		extramural = rs.getBoolean("crd_isextramural");		
	}
	
	// итоговые оценки
	public List<FinalMark> getFinalMarks() {
		try {
			return FinalMark.getForCard(this);
		} catch (ModelException e) {
			Utils.logError(ERROR + FMARK + e.getMessage());
			return new ArrayList<FinalMark>();
		}
	}
	// оценки за курсовые
	public List<CourseWorkMark> getCourseWorkMarks() {
		try {
			return CourseWorkMark.getForCard(this);
		} catch (ModelException e) {
			Utils.logError(ERROR + CMARK + e.getMessage());
			return new ArrayList<CourseWorkMark>();
		}
	}
	// оценки за ГОСы
	public List<GosMark> getGosMarks() {
		try {
			return GosMark.getForCard(this);
		} catch (ModelException e) {
			Utils.logError(ERROR + GMARK + e.getMessage());
			return new ArrayList<GosMark>();
		}
	}
	// оценки за практику
	public List<PracticMark> getPracticMarks() {
		try {
			return PracticMark.getForCard(this);
		} catch (ModelException e) {
			Utils.logError(ERROR + PMARK + e.getMessage());
			return new ArrayList<PracticMark>();
		}
	}

	// Владелец карточки (студент)
	public Person getPerson() throws ModelException {
		return Person.get(psnCode);
	}
	
	public void setPerson(Person p) {
		psnCode = p.getId();
	}
	
	// Специальность по которой обучался студент
	public Speciality getSpeciality() throws ModelException {
		return Speciality.get(spcCode);
	}
	
	public void setSpeciality(Speciality s) {
		spcCode = s.getId();
	}
	
	// Образовательное учреждение, которое окончил
	public School getSchool() throws ModelException {
		return School.get(sclCode);
	}
	
	public void setSchool(School s) {
		sclCode = s.getId();
	}
	
	public Card() {
		// Новая карточка с нулевым идентификатором
		id = 0;
	}
	
	public Card(final int id) {
		this.id = id;
	}
	
	public static Card get(int id) throws ModelException {
		Card result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("SELECT * FROM cards WHERE (crd_pcode = ?)", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				// Запись есть, считываем поля
				result = new Card(id);
				result.readFields(rs);;
			} else {
				throw new ModelException(ERROR + READ + NOT_FOUND);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public static List<Card> forPerson(final Person p) throws ModelException {
		List<Card> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("SELECT * FROM cards WHERE (crd_psncode = ?) ORDER BY crd_edate;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, p.getId());
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Card item = new Card(rs.getInt("crd_pcode"));
				item.readFields(rs);
				result.add(item);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	// Сохраняет карточку в базу данных
	public void save() throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO cards(crd_psncode, crd_sclcode, crd_spccode, crd_bdate, crd_edate, crd_docdate, crd_docname, "
					+ "crd_docorganization, crd_isremanded, crd_remandreason, crd_remandcommand, crd_diplomenumber, crd_appendixnumber, "
					+ "crd_regnumber, crd_diplomedate, crd_comissiondirector, crd_comissiondate, crd_diplomelength, "
					+ "crd_diplometheme, crd_diplomemark, crd_isgosexam, crd_isred, crd_isextramural) "
					+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			if (id > 0) {
				// обновляем существующую запись
				statement = con.prepareStatement("UPDATE cards SET crd_psncode=?, crd_sclcode=?, crd_spccode=?, "
						+ "crd_bdate=?, crd_edate=?, crd_docdate=?, crd_docname=?, crd_docorganization=?, "
						+ "crd_isremanded=?, crd_remandreason=?, crd_remandcommand=?, crd_diplomenumber=?, "
						+ "crd_appendixnumber=?, crd_regnumber=?, crd_diplomedate=?, crd_comissiondirector=?, "
						+ "crd_comissiondate=?, crd_diplomelength=?, crd_diplometheme=?, crd_diplomemark=?, "
						+ "crd_isgosexam=?, crd_isred=?, crd_isextramural=? WHERE (crd_pcode = ?)");
				statement.setInt(24, id);
			}
				statement.setInt(1, psnCode);
				statement.setInt(2, sclCode);
				statement.setInt(3, spcCode);
				statement.setDate(4, new java.sql.Date(beginDate.getTime()));
				statement.setDate(5, new java.sql.Date(endDate.getTime()));
				statement.setDate(6, new java.sql.Date(documentDate.getTime()));
				statement.setString(7, documentName);
				statement.setString(8, documentOrganization);
				statement.setBoolean(9, remanded);
				statement.setString(10, remandReason);
				statement.setString(11, remandCommand);
				statement.setString(12, diplomeNumber);
				statement.setString(13, appendixNumber);
				statement.setString(14, registrationNumber);
				statement.setDate(15, new java.sql.Date(diplomeDate.getTime()));
				statement.setString(16, comissionDirector);
				statement.setDate(17, new java.sql.Date(comissionDate.getTime()));
				statement.setFloat(18, diplomeLength);
				statement.setString(19, diplomeTheme);
				statement.setInt(20, diplomeMark);
				statement.setBoolean(21, gosExam);
				statement.setBoolean(22, red);
				statement.setBoolean(23, extramural);
				int count = statement.executeUpdate();
				if (count < 1) {
					throw new ModelException(ERROR + SAVE + NO_RECORDS);
				} else {
					if (id == 0) {
						id = Utils.getId(con);
					}
				}
		} catch (SQLException e) {
			throw new ModelException(ERROR + SAVE + SQL + e.getMessage());
		}
	}
	
	public void delete() throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"DELETE FROM cards WHERE (crd_pcode=?);");
			statement.setInt(1, id);
			int recordCount = statement.executeUpdate();
			if (recordCount <= 0) {
				throw new ModelException(ERROR + DELETE + NO_RECORDS);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + DELETE + SQL + e.getMessage());
		}
	}
	
	
	public int getId() {
		return id;
	}

	public String getBeginDateString() {
		return Utils.getDateString(beginDate);
	}
	
	public String getEndDateString() {
		return Utils.getDateString(endDate);
	}
	
	public String getDiplomeDateString() {
		return Utils.getDateString(diplomeDate);
	}
	
	public String getComissionDateString() {
		return Utils.getDateString(comissionDate);
	}
	
	public String getDocumentDateString() {
		return Utils.getDateString(documentDate);
	}
	
	public void updateFrom(Card card) {
		sclCode = card.sclCode;
		spcCode = card.spcCode;
		beginDate = card.beginDate;
		endDate = card.endDate;
		documentDate = card.documentDate;
		documentName = card.documentName;
		documentOrganization = card.documentOrganization;
		remanded = card.remanded;
		remandReason = card.remandReason;
		remandCommand = card.remandCommand;
		diplomeNumber = card.diplomeNumber;
		appendixNumber = card.appendixNumber;
		registrationNumber = card.registrationNumber;
		diplomeDate = card.diplomeDate;
		comissionDirector = card.comissionDirector;
		comissionDate = card.comissionDate;
		diplomeLength = card.diplomeLength;
		diplomeTheme = card.diplomeTheme;
		diplomeMark = card.diplomeMark;
		gosExam = card.gosExam;
		red = card.red;
		extramural = card.extramural;
	}

	public String getRemandedString() {
		return (remanded) ? "да" : "нет";
	}
	
	public String getRedString() {
		return (red) ? "да" : "нет";
	}
	
	public String getExtramuralString() {
		return (extramural) ? "заочная" : "очная";
	}
	
	public String getGosExamString() {
		return (gosExam) ? "итоговый междисциплинарный гос. экзамен" : "защита дипломных проектов";
	}
	
	/**
	 * Возвращает суммарную продолжительность практик
	 * @return
	 */
	public float getPracticLoad() throws ModelException {
		float result = 0;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT SUM(pmk_length) AS length FROM pmarks WHERE (pmk_crdcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = rs.getFloat("length");
			} else {
				throw new ModelException(ERROR + PRACTIC + NOT_FOUND);
			}
			rs.close();
		} catch (SQLException e) {
			throw new ModelException(ERROR + PRACTIC + SQL + e.getMessage());
		}
		return result;
	}

	public String getTitle() throws ModelException {
		Speciality spc = getSpeciality();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		String spec = (spc != null) ? spc.getOutputName()
				: "Неизвестная специальность";
		String bdate = (beginDate != null) ? sdf.format(beginDate)
				: "неизвестно";
		String edate = (endDate != null) ? sdf.format(endDate) : "неизвестно";
		return spec + " с " + bdate + " по " + edate + " (" + getExtramuralString() + " форма обучения)";
	}
}