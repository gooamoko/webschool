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
 * Класс для хранения информации об оценке за практику.
 * 
 * @author Воронин Леонид
 */
public class PracticMark {

	final static String ERROR = "Ошибка при работе с таблицей оценок за практику ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";
	
	private int id;
	private String practic;
	private int crdCode;
	public int prcCode;
	public int mark = 0;
	public float length = 0;

	private void readFields(final ResultSet rs) throws SQLException {
		prcCode = rs.getInt("pmk_prccode");
		crdCode = rs.getInt("pmk_crdcode");
		mark = rs.getInt("pmk_mark");
		length = rs.getFloat("pmk_length");
		practic = rs.getString("prc_name");
	}
	
	public int getId() {
		return id;
	}
	
	public String getPractic() {
		return practic;
	}
	
	public void setCard(Card c) {
		crdCode = c.getId();
	}
	
	public void setPractic(Practic p) {
		prcCode = p.getId();
		practic = p.name;
	}
	
	public PracticMark() {
		id = 0;
	}

	public PracticMark(int id) {
		this.id = id;
	}

	public void updateFrom(PracticMark newPracticMark) {
		this.prcCode = newPracticMark.prcCode;
		this.mark = newPracticMark.mark;
		this.length = newPracticMark.length;
	}
	
	public static PracticMark get(final int id) throws ModelException {
		PracticMark result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT pmarks.*, prc_name FROM pmarks, practics "
					+ "WHERE (prc_pcode = pmk_prccode) AND (pmk_pcode=?);", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new PracticMark(id);
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
	
	public static List<PracticMark> getForCard(Card c) throws ModelException {
		List<PracticMark> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT pmarks.*, prc_name  FROM pmarks, practics "
					+ "WHERE (prc_pcode = pmk_prccode) AND (pmk_crdcode=?) ORDER BY prc_name;", 
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, c.getId());
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				PracticMark item = new PracticMark(rs.getInt("pmk_pcode"));
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
					"INSERT INTO pmarks(pmk_crdcode, pmk_prccode, pmk_length, pmk_mark) VALUES(?,?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement("UPDATE pmarks SET pmk_crdcode=?, "
						+ "pmk_prccode=?, pmk_length=?, pmk_mark=? WHERE (pmk_pcode=?);");
				statement.setInt(5, id);
			}
			statement.setInt(1, crdCode);
			statement.setInt(2, prcCode);
			statement.setFloat(3, length);
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
					"DELETE FROM pmarks WHERE (pmk_pcode=?);");
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
