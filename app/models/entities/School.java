package models.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.ModelException;
import models.Utils;
import play.db.DB;

/**
 * Класс для инкапсуляции данных об учебном заведении.
 * 
 * @author Воронин Леонид
 */
public class School {
	final static String ERROR = "Ошибка при работе с таблицей образовательных учреждений ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";

	private int id;
	public String name;
	public String shortName;
	public String place;
	public String director;

	public int getId() {
		return id;
	}

	private void readFields(ResultSet rs) throws SQLException {
		this.shortName = rs.getString("scl_shortname");
		this.name = rs.getString("scl_name");
		this.place = rs.getString("scl_place");
		this.director = rs.getString("scl_director");
	}

	public School(final int id) {
		this.id = id;
	}

	public School() {
		id = 0;
	}

	public void updateFrom(School newSchool) {
		this.name = newSchool.name;
		this.shortName = newSchool.shortName;
		this.place = newSchool.place;
		this.director = newSchool.director;
	}

	public static School get(final int id) throws ModelException {
		School result = null;
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM schools WHERE (scl_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new School(rs.getInt("scl_pcode"));
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

	public static School findLike(final School sample) throws ModelException {
		School result = null;
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM schools WHERE (scl_name LIKE ?) AND (scl_shortname LIKE ?) "
							+ "AND (scl_place LIKE ?) LIMIT 1;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, sample.name + "%");
			statement.setString(2, sample.shortName + "%");
			statement.setString(3, sample.place + "%");
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new School(rs.getInt("scl_pcode"));
				result.readFields(rs);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public static List<School> fetchAll() throws ModelException {
		List<School> result = new ArrayList<>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM schools ORDER BY scl_shortname;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				School item = new School(rs.getInt("scl_pcode"));
				item.readFields(rs);
				result.add(item);
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
					.prepareStatement("INSERT INTO schools(scl_name, scl_shortname, scl_place, scl_director) VALUES(?,?,?,?)");
			if (id > 0) {
				statement = con
						.prepareStatement("UPDATE schools SET scl_name=?, scl_shortname=?, "
								+ "scl_place=?, scl_director=? WHERE (scl_pcode=?);");
				statement.setInt(5, id);
			}
			statement.setString(1, name);
			statement.setString(2, shortName);
			statement.setString(3, place);
			statement.setString(4, director);
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
					.prepareStatement("DELETE FROM schools WHERE (scl_pcode=?);");
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
