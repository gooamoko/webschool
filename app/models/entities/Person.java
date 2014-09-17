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
 * Класс для хранения информации о персоне.
 * 
 * @author Воронин Леонид
 */
public class Person {
	final static String ERROR = "Ошибка при работе с таблицей персон ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String CARD = "при выборке карточек. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";

	private int id; // Уникальный идентификатор
	public String firstName = ""; // имя
	public String middleName = ""; // отчество
	public String lastName = ""; // фамилия
	public Date birthDate; // дата рождения
	public String birthPlace; // место рождения
	public boolean isMale = true; // пол
	public boolean isForeign = false;// иностранный гражданин

	private void readFields(ResultSet rs) throws SQLException {
		firstName = rs.getString("psn_firstname");
		middleName = rs.getString("psn_middlename");
		lastName = rs.getString("psn_lastname");
		birthDate = rs.getDate("psn_birthdate");
		birthPlace = rs.getString("psn_birthplace");
		isMale = rs.getBoolean("psn_ismale");
		isForeign = rs.getBoolean("psn_isforeign");
	}
	
	public int getId() {
		return id;
	}
	
	public Person() {
		id = 0;
	}

	public Person(int id) {
		this.id = id;
	}
	
	public static Person get(final int id) throws ModelException {
		Person result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM persons WHERE (psn_pcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Person(id);
				result.readFields(rs);
			} else {
				throw new ModelException(ERROR + READ + NOT_FOUND);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public static Person findLike(final Person sample) throws ModelException {
		Person result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM persons WHERE (psn_firstname=?) AND (psn_middlename=?) "
					+ "AND (psn_lastname=?) AND (psn_birthdate=?) AND (psn_birthplace=?) LIMIT 1;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, sample.firstName);
			statement.setString(2, sample.middleName);
			statement.setString(3, sample.lastName);
			statement.setDate(4, new java.sql.Date(sample.birthDate.getTime()));
			statement.setString(5, sample.birthPlace);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Person(rs.getInt("psn_pcode"));
				result.readFields(rs);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public static List<Person> fetchAll() throws ModelException {
		List<Person> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM persons ORDER BY psn_firstname, psn_middlename, psn_lastname;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				Person item = new Person(rs.getInt("psn_pcode"));
				item.readFields(rs);
				result.add(item);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + CARD + SQL + e.getMessage());
		}
	}

	public static List<Person> findByName(String firstName) throws ModelException {
		List<Person> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM persons WHERE (psn_firstname LIKE ?) ORDER BY psn_firstname, psn_middlename, psn_lastname;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, firstName + "%");
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				Person item = new Person(rs.getInt("psn_pcode"));
				item.readFields(rs);
				result.add(item);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + CARD + SQL + e.getMessage());
		}
	}
	
	public void save() throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO persons(psn_firstname, psn_middlename, psn_lastname, "
					+ "psn_birthdate, psn_birthplace, psn_ismale, psn_isforeign) VALUES(?,?,?,?,?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement("UPDATE persons SET psn_firstname=?, "
						+ "psn_middlename=?, psn_lastname=?, psn_birthdate=?, psn_birthplace=?, "
						+ "psn_ismale=?, psn_isforeign=? WHERE (psn_pcode=?);");
				statement.setInt(8, id);
			}
			statement.setString(1, firstName);
			statement.setString(2, middleName);
			statement.setString(3, lastName);
			statement.setDate(4, new java.sql.Date(birthDate.getTime()));
			statement.setString(5, birthPlace);
			statement.setBoolean(6, isMale);
			statement.setBoolean(7, isForeign);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + SAVE + NO_RECORDS);
			} else {
				if (0 == id) {
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
					"DELETE FROM persons WHERE (psn_pcode=?);");
			statement.setInt(1, id);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + DELETE + NO_RECORDS);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + DELETE + SQL + e.getMessage());
		}
	}

	public List<Card> getCards() throws ModelException {
		return Card.forPerson(this);
	}
	
	public void updateFrom(Person newPerson) {
		this.firstName = newPerson.firstName;
		this.middleName = newPerson.middleName;
		this.lastName = newPerson.lastName;
		this.birthDate = newPerson.birthDate;
		this.birthPlace = newPerson.birthPlace;
		this.isMale = newPerson.isMale;
		this.isForeign = newPerson.isForeign;
	}
		
	public String getCitizenship() {
		return (isForeign) ? "другое" : "россия";
	}

	public String getGender() {
		return (isMale) ? "мужской" : "женский";
	}

	public String getFullName() {
		return firstName + " " + middleName + " " + lastName;
	}
	
	public String getBirthDateString() {
		String result = "Неизвестно";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			result = sdf.format(birthDate);
		} catch (NullPointerException e) {
			// DO nothing
		}
		return result;
	}
}
