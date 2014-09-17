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
public class GosMark {

	final static String ERROR = "Ошибка при работе с таблицей оценок за гос. экзамены ";
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
	public int mark = 0;

	private void readFields(final ResultSet rs) throws SQLException {
		subCode = rs.getInt("gmk_subcode");
		crdCode = rs.getInt("gmk_crdcode");
		mark = rs.getInt("gmk_mark");
		subject = rs.getString("sub_name");
	}
	
	public int getId() {
		return id;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setCard(final Card c) {
		this.crdCode = c.getId();
	}
	
	public void setSubject(final Subject s) {
		this.subject = s.name;
		this.subCode = s.getId();
	}
	
	public GosMark() {
		id = 0;
	}

	public GosMark(final int id) {
		this.id = id;
	}
	
	public void updateFrom(final GosMark newInstance) {
		this.subCode = newInstance.subCode;
		this.mark = newInstance.mark;
	}
	
	public static GosMark get(final int id) throws ModelException {
		GosMark result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT gmarks.*, sub_name FROM gmarks, subjects "
					+ "WHERE (sub_pcode = gmk_subcode) AND (gmk_pcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new GosMark(id);
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

	public static List<GosMark> getForCard(Card c) throws ModelException {
		List<GosMark> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT gmarks.*, sub_name  FROM gmarks, subjects "
					+ "WHERE (sub_pcode = gmk_subcode) AND (gmk_crdcode=?) ORDER BY sub_name;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, c.getId());
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				GosMark item = new GosMark(rs.getInt("gmk_pcode"));
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
					"INSERT INTO gmarks(gmk_crdcode, gmk_subcode, gmk_mark) VALUES(?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement("UPDATE gmarks SET gmk_crdcode=?, gmk_subcode=?, gmk_mark=? "
						+ "WHERE (gmk_pcode=?);");
				statement.setInt(4, id);
			}
			statement.setInt(1, crdCode);
			statement.setInt(2, subCode);
			statement.setInt(3, mark);			
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
					"DELETE FROM gmarks WHERE (gmk_pcode=?);");
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
