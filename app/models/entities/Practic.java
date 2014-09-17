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

public class Practic {

	final static String ERROR = "Ошибка при работе с таблицей практик ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";
	
	private int id;
	public String name;

	public int getId() {
		return id;
	}
	
	public Practic() {
		id = 0;
	}
	
	public Practic(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	public static Practic get(final int id) throws ModelException {
		Practic result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("SELECT * FROM practics WHERE (prc_pcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Practic(id, rs.getString("prc_name"));
			} else {
				throw new ModelException(ERROR + READ + NOT_FOUND);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public void updateFrom(Practic newPractic) {
		this.name = newPractic.name;
	}
	
	public void save() throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("INSERT INTO practics(prc_name) VALUES(?);");
			if (id > 0) {
				statement = con.prepareStatement("UPDATE practics SET prc_name=? WHERE (prc_pcode=?);");
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
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("DELETE FROM practics WHERE (prc_pcode=?);");
			statement.setInt(1, id);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + DELETE + NO_RECORDS);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + DELETE + SQL + e.getMessage());
		}
	}
	
	public static Practic find(final String name) throws ModelException {
		Practic result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("SELECT * FROM practics WHERE (prc_name LIKE ?) LIMIT 1;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, name + "%");
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Practic(rs.getInt("prc_pcode"), rs.getString("prc_name"));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public static List<Practic> fetchAll() throws ModelException {
		List<Practic> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("SELECT * FROM practics ORDER BY prc_name;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				result.add(new Practic(rs.getInt("prc_pcode"), rs.getString("prc_name")));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public static List<Practic> findByName(String name) throws ModelException {
		List<Practic> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement("SELECT * FROM practics WHERE (prc_name LIKE ?) ORDER BY prc_name;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, name + "%");
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				result.add(new Practic(rs.getInt("prc_pcode"), rs.getString("prc_name")));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}	
}
