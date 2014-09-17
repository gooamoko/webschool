package models.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.ModelException;
import models.Utils;
import play.data.validation.Constraints;
import play.db.DB;

/**
 * Класс, инкапсулирующий дисциплину. По-сути, это просто каталожное имя
 * дисциплины.
 * 
 * @author Воронин Леонид
 */
public class Subject {

	final static String ERROR = "Ошибка при работе с таблицей Subjects ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";

	private int id;
	@Constraints.Required
	public String name;

	public int getId() {
		return id;
	}

	public Subject() {
		id = 0;
	}

	public Subject(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Обновляет все поля кроме первичного ключа
	 * 
	 * @param newSubject
	 */
	public void updateFrom(Subject newSubject) {
		this.name = newSubject.name;
	}

	public static Subject get(final int id) throws ModelException {
		Subject result = null;
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM subjects WHERE (sub_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Subject(id, rs.getString("sub_name"));
			} else {
				throw new ModelException(ERROR + READ + NOT_FOUND);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public static Subject find(String name) throws ModelException {
		Subject result = null;
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM subjects WHERE (sub_name LIKE ?) LIMIT 1;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, name + "%");
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Subject(rs.getInt("sub_pcode"), rs.getString("sub_name"));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public static List<Subject> fetchAll() throws ModelException {
		List<Subject> result = new ArrayList<>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM subjects ORDER BY sub_name;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(new Subject(rs.getInt("sub_pcode"), rs.getString("sub_name")));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public static List<Subject> findByName(String name) throws ModelException {
		List<Subject> result = new ArrayList<>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM subjects WHERE (sub_name LIKE ?) ORDER BY sub_name;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, name + "%");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(new Subject(rs.getInt("sub_pcode"), rs.getString("sub_name")));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	
	public void save() throws ModelException {
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con
					.prepareStatement("INSERT INTO subjects(sub_name) VALUES(?);");
			if (id > 0) {
				statement = con
						.prepareStatement("UPDATE subjects SET sub_name=? WHERE (sub_pcode=?);");
				statement.setInt(2, id);
			}
			statement.setString(1, name);
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
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con
					.prepareStatement("DELETE FROM subjects WHERE (sub_pcode=?);");
			statement.setInt(1, id);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + DELETE + NO_RECORDS);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + DELETE + SQL + e.getMessage());
		}
	}
}
