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
 * Класс для инкапсуляции данных об итоговых оценках.
 * 
 * @author Воронин Леонид
 */
public class FinalMark {

	final static String ERROR = "Ошибка при работе с таблицей итоговых оценок ";
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
	public int moduleCode;
	public boolean isModule = false;
	public int mark = 0;
	public int auditoryLoad = 0;
	public int maximumLoad = 0;

	private void readFields(final ResultSet rs) throws SQLException {
		subCode = rs.getInt("fmk_subcode");
		crdCode = rs.getInt("fmk_crdcode");
		moduleCode = rs.getInt("fmk_module");
		isModule = rs.getBoolean("fmk_ismodule");
		mark = rs.getInt("fmk_mark");
		auditoryLoad = rs.getInt("fmk_audload");
		maximumLoad = rs.getInt("fmk_maxload");
		subject = rs.getString("sub_name");
	}
	
	public int getId() {
		return id;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public Subject getModule() throws ModelException {
		return Subject.get(moduleCode);
	}
	
	public void setCard(Card c) {
		crdCode = c.getId();
	}
	
	public void setSubject(Subject s) {
		subCode = s.getId();
		subject = s.name;
	}
	
	public FinalMark() {
		id = 0;
	}

	public FinalMark(final int id) {
		this.id = id;
	}
	
	public static FinalMark get(final int id) throws ModelException {
		FinalMark result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT fmarks.*, sub_name FROM fmarks, subjects "
					+ "WHERE (sub_pcode = fmk_subcode) AND (fmk_pcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new FinalMark(id);
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
	
	public static List<FinalMark> getForCard(Card c) throws ModelException {
		List<FinalMark> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT fmarks.*, sub_name  FROM fmarks, subjects "
					+ "WHERE (sub_pcode = fmk_subcode) AND (fmk_crdcode=?) "
					+ "ORDER BY fmk_module DESC, fmk_ismodule DESC, sub_name;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, c.getId());
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				FinalMark item = new FinalMark(rs.getInt("fmk_pcode"));
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
					"INSERT INTO fmarks(fmk_crdcode, fmk_subcode, fmk_audload, "
					+ "fmk_maxload, fmk_mark, fmk_module, fmk_ismodule) VALUES(?,?,?,?,?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement("UPDATE fmarks SET fmk_crdcode=?, "
						+ "fmk_subcode=?, fmk_audload=?, fmk_maxload=?, fmk_mark=?, "
						+ "fmk_module=?, fmk_ismodule=? WHERE (fmk_pcode=?);");
				statement.setInt(8, id);
			}
			statement.setInt(1, crdCode);
			statement.setInt(2, subCode);
			statement.setInt(3, auditoryLoad);
			statement.setInt(4, maximumLoad);
			statement.setInt(5, mark);
			if (moduleCode > 0) {
				statement.setInt(6, moduleCode);
			} else {
				statement.setNull(6, java.sql.Types.INTEGER);
			}
			statement.setBoolean(7, isModule);
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
					"DELETE FROM fmarks WHERE (fmk_pcode=?);");
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

	public void updateFrom(FinalMark newFinalMark) {
		this.subCode = newFinalMark.subCode;
		this.mark = newFinalMark.mark;
		this.auditoryLoad = newFinalMark.auditoryLoad;
		this.maximumLoad = newFinalMark.maximumLoad;
		this.isModule = newFinalMark.isModule;
		this.moduleCode = newFinalMark.moduleCode;
	}
}
