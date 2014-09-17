package models.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.ModelException;
import models.Utils;
import play.db.DB;

public class Renaming {

	final static String ERROR = "Ошибка при работе с таблицей переименований ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";

	private int id;
	public Date renamingDate;
	public String oldName;
	public String newName;
	
	private void readFields(final ResultSet rs) throws SQLException {
		renamingDate = rs.getDate("ren_date");
		oldName = rs.getString("ren_oldname");
		newName = rs.getString("ren_newname");
	}

	public Renaming() {
		id = 0;
	}

	public Renaming(final int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static Renaming get(final int id) throws ModelException {
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM renamings WHERE (ren_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				Renaming result = new Renaming(rs.getInt("ren_pcode"));
				result.readFields(rs);
				return result;
			} else {
				throw new ModelException(ERROR + READ + NOT_FOUND);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public void updateFrom(final Renaming renaming) {
		this.renamingDate = renaming.renamingDate;
		this.oldName = renaming.oldName;
		this.newName = renaming.newName;
	}

	public static List<Renaming> find(final Date begin, final Date end) {
		List<Renaming> result = new ArrayList<>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con
					.prepareStatement(
							"SELECT * FROM renamings WHERE (ren_date >= ?) AND (ren_date <= ?);",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			statement.setDate(1, new java.sql.Date(begin.getTime()));
			statement.setDate(2, new java.sql.Date(end.getTime()));
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Renaming item = new Renaming(rs.getInt("ren_pcode"));
				item.readFields(rs);
				result.add(item);
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			return result;
		}
	}

	public static List<Renaming> fetchAll() throws ModelException {
		List<Renaming> result = new ArrayList<>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM renamings;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Renaming item = new Renaming(rs.getInt("ren_pcode"));
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
					.prepareStatement("INSERT INTO renamings(ren_date, ren_oldname, ren_newname) VALUES(?,?,?);");
			if (id > 0) {
				statement = con
						.prepareStatement("UPDATE renamings SET ren_date=?, ren_oldname=?, ren_newname=? WHERE (ren_pcode=?);");
				statement.setInt(4, id);
			}
			statement.setDate(1, new java.sql.Date(renamingDate.getTime()));
			statement.setString(2, oldName);
			statement.setString(3, newName);
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
			PreparedStatement statement = con
					.prepareStatement("DELETE FROM renamings WHERE (ren_pcode=?);");
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
