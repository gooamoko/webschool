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

public class DepartmentProfile {
	final static String ERROR = "Ошибка при работе с таблицей departmentProfiles ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";

	private int id;
	@Constraints.Required
	public boolean extramural;
	@Constraints.Required
	public int departmentCode;
	@Constraints.Required
	public int specialityCode;
	
	private void readFields(ResultSet rs) throws SQLException {
		extramural = rs.getBoolean("dpr_extramural");
		departmentCode = rs.getInt("dpr_depcode");
		specialityCode = rs.getInt("dpr_spccode");
	}
	
	public DepartmentProfile() {
		id = 0;
		extramural = false;
	}
	
	public DepartmentProfile(final int id) {
		this.id = id;
	}
	
	public void setDepartment(final Department dep) {
		departmentCode = dep.getId();
	}
	
	public int getId() {
		return id;
	}

	public static DepartmentProfile get(final int id) throws ModelException {
		try(Connection con = DB.getConnection()) {
			DepartmentProfile item;
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM departmentprofiles WHERE (dpr_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				item = new DepartmentProfile(rs.getInt("dpr_pcode"));
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
	
	public static List<DepartmentProfile> getForDepartment(final int departmentCode) throws ModelException {
		try (Connection con = DB.getConnection()) {
			List<DepartmentProfile> result = new ArrayList<>();
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM departmentprofiles WHERE (dpr_depcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, departmentCode);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				DepartmentProfile item = new DepartmentProfile(rs.getInt("dpr_pcode"));
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
	
	public void save() throws ModelException {
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO departmentprofiles(dpr_depcode, dpr_spccode, dpr_extramural) VALUES(?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement(
						"UPDATE departmentprofiles SET dpr_depcode=?, dpr_spccode=?, dpr_extramural=? WHERE (dpr_pcode=?);");
				statement.setInt(4, id);
			}
			statement.setInt(1, departmentCode);
			statement.setInt(2, specialityCode);
			statement.setBoolean(3, extramural);
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
					"DELETE FROM departmentprofiles WHERE (dpr_pcode=?);");
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
