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
 * Класс, инкапсулирующий информацию о специальности
 * 
 * @author Воронин Леонид
 */
public class Speciality {

	final static String ERROR = "Ошибка при работе с таблицей специальностей ";
	final static String READ = "при чтении записи. ";
	final static String SAVE = "при сохранении записи. ";
	final static String DELETE = "при удалении записи. ";
	final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	final static String SQL = "SQLException: ";
	
	private int id; // Уникальный идентификатор
	public String name; // Наименование специальности
	public String shortName; // Краткое наименование специальности
	public String key; // Код специальности (230105.51 например)
	public String kvalification; // Квалификация специальности
	public String specialization = "не предусмотрено"; // Специализация
	public String length; // Срок обучения

	private void readFields(final ResultSet rs) throws SQLException {
		this.key = rs.getString("spc_key");
		this.name = rs.getString("spc_name");
		this.shortName = rs.getString("spc_shortname");
		this.kvalification = rs.getString("spc_kvalification");
		this.specialization = rs.getString("spc_specialization");
		this.length = rs.getString("spc_length");
	}
	
	public int getId() {
		return id;
	}
	
	public Speciality() {
		id = 0;
	}

	public Speciality(final int id) {
		this.id = id;
	}
	
	public static Speciality get(final int id) throws ModelException {
		Speciality result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM specialities WHERE (spc_pcode=?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Speciality(id);
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
	
	public static List<Speciality> fetchAll() throws ModelException {
		List<Speciality> result = new ArrayList<>();
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM specialities ORDER BY spc_key, spc_name;",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Speciality item = new Speciality(rs.getInt("spc_pcode"));
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
					"INSERT INTO specialities(spc_key, spc_name, spc_shortname, "
					+ "spc_kvalification, spc_specialization, spc_length) VALUES(?,?,?,?,?,?);");
			if (id > 0) {
				statement = con.prepareStatement("UPDATE specialities SET spc_key=?, spc_name=?, "
						+ "spc_shortname=?, spc_kvalification=?, spc_specialization=?, spc_length=? WHERE (spc_pcode=?)");
				statement.setInt(7, id);
			}
			statement.setString(1, key);
			statement.setString(2, name);
			statement.setString(3, shortName);
			statement.setString(4, kvalification);
			statement.setString(5, specialization);
			statement.setString(6, length);
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
					"DELETE FROM specialities WHERE (spc_pcode=?)");
			statement.setInt(1, id);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + DELETE + NO_RECORDS);
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + DELETE + SQL + e.getMessage());
		}
	}
	
	public static Speciality findLike(final Speciality sample) throws ModelException {
		Speciality result = null;
		try(Connection con = DB.getConnection()) {
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM specialities WHERE (spc_key LIKE ?) AND (spc_name LIKE ?) "
					+ "AND (spc_specialization LIKE ?) AND (spc_kvalification LIKE ?) LIMIT 1;",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, sample.key + "%");
			statement.setString(2, sample.name + "%");
			statement.setString(3, sample.specialization + "%");
			statement.setString(4, sample.kvalification + "%");
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Speciality(rs.getInt("spc_pcode"));
				result.readFields(rs);				
			}
			rs.close();
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
		return result;
	}

	public void updateFrom(final Speciality newSpeciality) {
		this.name = newSpeciality.name;
		this.shortName = newSpeciality.shortName;
		this.key = newSpeciality.key;
		this.kvalification = newSpeciality.kvalification;
		this.specialization = newSpeciality.specialization;
		this.length = newSpeciality.length;
	}
	
	public String getOutputName() {
		return key + " (" + shortName + ")";
	}
	
	public String getFullName() {
		return key + " " + name;
	}
}
