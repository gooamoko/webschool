package models.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.ModelException;
import models.Utils;
import play.data.validation.Constraints;
import play.db.DB;

public class Department {
	final static String ERROR = "Ошибка при работе с таблицей departments ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";

	private int id;
	@Constraints.Required
	public String name;
	@Constraints.Required
	public String boss;
	@Constraints.Required
	public String secretar;
	
	private void readFields(final ResultSet rs) throws SQLException {
		this.name = rs.getString("dep_name");
		this.boss = rs.getString("dep_boss");
		this.secretar = rs.getString("dep_secretar");
	}
	
	public Department() {
		id = 0;
	}
	
	public Department(final int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void updateFrom(final Department other) {
		this.name = other.name;
		this.boss = other.boss;
		this.secretar = other.secretar;
	}
	
	public static Department get(final int id) throws ModelException {
		try(Connection con = DB.getConnection()) {
			Department item;
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM departments WHERE (dep_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				item = new Department(rs.getInt("dep_pcode"));
				item.readFields(rs);
			} else {
				throw new ModelException (ERROR + READ + NOT_FOUND);
			}
			rs.close();
			statement.close();
			return item;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public static List<Department> fetchAll() throws ModelException {
		try (Connection con = DB.getConnection()) {
			List<Department> result = new ArrayList<>();
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM departments ORDER BY dep_name;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				Department item = new Department(rs.getInt("dep_pcode"));
				item.readFields(rs);
				result.add(item);
			}
			rs.close();
			statement.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public static Map<String, String> getMap() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try {
			for (Department d : fetchAll()) {
				result.put(d.getId() + "", d.name);
			}
		} catch (ModelException e) {
			Utils.logError(e.getMessage());
		}
		return result;
	}
	
	public void save() throws ModelException {
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO departments(dep_name, dep_boss, dep_secretar) VALUES(?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement(
						"UPDATE departments SET dep_name=?, dep_boss=?, dep_secretar=? WHERE (dep_pcode=?);");
				statement.setInt(4, id);
			}
			statement.setString(1, name);
			statement.setString(2, boss);
			statement.setString(3, secretar);
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
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"DELETE FROM departments WHERE (dep_pcode=?);");
			statement.setInt(1, id);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + SAVE + NO_RECORDS);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + SAVE + SQL + e.getMessage());
		}
	}	
}
