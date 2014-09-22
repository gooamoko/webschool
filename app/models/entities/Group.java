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

public class Group {

	final static String ERROR = "Ошибка при работе с таблицей groups ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String LIST = "при получении списка записей. ";	
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";

	private int id;
	@Constraints.Required
	public String name;
	@Constraints.Required
	public int createYear;
	@Constraints.Required
	public int course;
	@Constraints.Required
	public int depCode;
	@Constraints.Required
	public int spcCode;
	@Constraints.Required
	public String master;
	@Constraints.Required
	public boolean archived;
	@Constraints.Required
	public boolean extramural;

	private void readFields(ResultSet rs) throws SQLException {
		name = rs.getString("grp_name");
		createYear = rs.getInt("grp_year");
		course = rs.getInt("grp_course");
		depCode = rs.getInt("grp_depcode");
		spcCode = rs.getInt("grp_spccode");
		master = rs.getString("grp_master");
		archived = rs.getBoolean("grp_archived");
		extramural = rs.getBoolean("grp_extramural");
	}
	
	private static List<Group> makeList(PreparedStatement statement) throws SQLException {
		List<Group> result = new ArrayList<>();
		ResultSet rs = statement.executeQuery();
		while(rs.next()) {
			Group item = new Group(rs.getInt("grp_pcode"));
			item.readFields(rs);
			result.add(item);
		}
		rs.close();
		statement.close();
		return result;
	}

	public Group() {
		id = 0;
		archived = false;
	}
	
	public Group(final int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public void setDepartment(final Department dep) {
		depCode = dep.getId();
	}

	public void setSpeciality(final Speciality spc) {
		spcCode = spc.getId();
	}

	public void updateFrom(Group grp) {
		name = grp.name;
		master = grp.master;
		createYear = grp.createYear;
		depCode = grp.depCode;
		spcCode = grp.spcCode;
		archived = grp.archived;
	}

	public static Group get(int id) throws ModelException {
		try (Connection con = DB.getConnection()) {
			Group item;
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM groups WHERE (grp_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				item = new Group(rs.getInt("dpr_pcode"));
				item.readFields(rs);
			} else {
				throw new ModelException(ERROR + READ + NOT_FOUND);
			}
			rs.close();
			statement.close();
			return item;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}
	
	public static List<Group> fetchAll() throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM groups;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			return makeList(statement);
		} catch (SQLException e) {
			throw new ModelException(ERROR + LIST + SQL + e.getMessage());
		}
	}

	public static List<Group> findLike(final String name) throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM groups WHERE (grp_name=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, name);
			return makeList(statement);
		} catch (SQLException e) {
			throw new ModelException(ERROR + LIST + SQL + e.getMessage());
		}
	}
	
	public static List<Group> getForSpeciality(final Speciality spc) throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM groups WHERE (grp_spccode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, spc.getId());
			return makeList(statement);
		} catch (SQLException e) {
			throw new ModelException(ERROR + LIST + SQL + e.getMessage());
		}
	}
	
	public static List<Group> getForDepartment(final Department dep) throws ModelException {
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM groups WHERE (grp_depcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, dep.getId());
			return makeList(statement);
		} catch (SQLException e) {
			throw new ModelException(ERROR + LIST + SQL + e.getMessage());
		}
	}
	
	public void save() throws ModelException {
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO groups(grp_name, grp_year, grp_master, grp_depcode, grp_spccode, grp_archived) "
					+ "VALUES(?,?,?,?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement(
						"UPDATE groups SET grp_name=?, grp_year=?, grp_master=?, grp_depcode=?, grp_spccode=?, "
						+ "grp_archived=? WHERE (grp_pcode=?);");
				statement.setInt(7, id);
			}
			statement.setString(1, name);
			statement.setInt(2, createYear);
			statement.setString(3, master);
			statement.setInt(4, depCode);
			statement.setInt(5, spcCode);
			statement.setBoolean(6, archived);
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
					"DELETE FROM groups WHERE (grp_pcode=?);");
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
