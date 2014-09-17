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
 * Класс для хранения информации об оценке за курсовой проект.
 * 
 * @author Воронин Леонид
 */
public class CourseWorkMark {

	final static String ERROR = "Ошибка при работе с таблицей оценок за курсовые ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";
	
	private int id;
	private int crdCode;
	private String subject;
	public int subCode;
	public String theme;
	public int mark = 0;

	private void readFields(final ResultSet rs) throws SQLException {
		subCode = rs.getInt("cmk_subcode");
		crdCode = rs.getInt("cmk_crdcode");
		mark = rs.getInt("cmk_mark");
		theme = rs.getString("cmk_theme");
		subject = rs.getString("sub_name");
	}
	
	public int getId() {
		return id;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setCard(Card c) {
		crdCode = c.getId();
	}
	
	public void setSubject(Subject s) {
		subCode = s.getId();
		subject = s.name;
	}

	public CourseWorkMark() {
		id = 0;
	}

	public CourseWorkMark(int id) {
		this.id = id;
	}
	
	public void updateFrom(CourseWorkMark newInstance) {
		this.subject = newInstance.subject;
		this.theme = newInstance.theme;
		this.mark = newInstance.mark;
	}
	
	public static CourseWorkMark get(final int id) throws ModelException {
		CourseWorkMark result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT cmarks.*, sub_name FROM cmarks, subjects "
					+ "WHERE (sub_pcode = cmk_subcode) AND (cmk_pcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new CourseWorkMark(id);
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
	
	public static List<CourseWorkMark> getForCard(Card c) throws ModelException {
		List<CourseWorkMark> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT cmarks.*, sub_name  FROM cmarks, subjects "
					+ "WHERE (sub_pcode = cmk_subcode) AND (cmk_crdcode=?) ORDER BY sub_name;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, c.getId());
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				CourseWorkMark item = new CourseWorkMark(rs.getInt("cmk_pcode"));
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
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO cmarks(cmk_crdcode, cmk_subcode, cmk_theme, cmk_mark) VALUES(?,?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement("UPDATE cmarks SET cmk_crdcode=?, "
						+ "cmk_subcode=?, cmk_theme=?, cmk_mark=? WHERE (cmk_pcode=?);");
				statement.setInt(5, id);
			}
			statement.setInt(1, crdCode);
			statement.setInt(2, subCode);
			statement.setString(3, theme);
			statement.setInt(4, mark);			
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
					"DELETE FROM cmarks WHERE (cmk_pcode=?);");
			statement.setInt(1, id);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + DELETE + NO_RECORDS);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + DELETE + SQL + e.getMessage());
		}
	}
	
	public Card getCard() throws ModelException {
		return Card.get(crdCode);
	}		
}
