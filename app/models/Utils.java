package models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import models.entities.Card;
import models.entities.Practic;
import models.entities.School;
import models.entities.Speciality;
import play.Logger;
import play.db.DB;

/**
 * Класс для размещения функций, которые планируется использовать в нескольких
 * классах
 * 
 * @author Воронин Леонид
 * 
 */
public class Utils {
	

	public static void logError(String message) {
		Logger.error(message);
	}

	/**
	 * Возвращает MD5 хэш строки
	 * @param text строка
	 * @return хэш в виде строки
	 */
	public static String md5(final String text) throws ModelException {
		try {
			// "Солим" строку
			String message = text;
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(message.getBytes("UTF-8"));
			// converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			throw new ModelException(e.toString());
		}
	}
	
	public static String getDateString(Date date) {
		try {
			if (date == null) {
				return "null";
			}
			final String[] months = { "января", "февраля", "марта", "апреля",
					"мая", "июня", "июля", "августа", "сентября", "октября",
					"ноября", "декабря" };
			Calendar c = new GregorianCalendar();
			c.setTime(date);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			int year = c.get(Calendar.YEAR);
			return day + " " + months[month] + " " + year;
		} catch (Exception e) {
			return "exception";
		}
	}

	public static int getYear(Date date) {
		if (date == null) {
			return -1;
		}
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		return c.get(Calendar.YEAR);
	}

	public static String getMarkString(int mark) {
		String result = " ";
		switch (mark) {
		case 0:
			result = "x";
			break;
		case 1:
			result = "неудовлетворительно";
			break;
		case 2:
			result = "неудовлетворительно";
			break;
		case 3:
			result = "удовлетворительно";
			break;
		case 4:
			result = "хорошо";
			break;
		case 5:
			result = "отлично";
			break;
		case 13:
			result = "зачтено";
			break;
		default:
			result = " ";
		}
		return result;
	}

	public static String getLenString(final float length) {
		int ilength = (int) length;
		if (ilength > 20) {
			ilength %= 10;
		}
		String prefix = " недель";
		if (ilength == 1) {
			prefix = " неделя";
		} else if (ilength < 5) {
			prefix = " недели";
		}
		return String.format("%2.1f %s", length, prefix);
	}

	public static Map<String, String> specialities() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try {
			for (Speciality s : Speciality.fetchAll()) {
				result.put(s.getId() + "", s.getOutputName());
			}
		} catch (ModelException e) {
			logError(e.getMessage());
		}
		return result;
	}

	public static Map<String, String> schools() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try {
			for (School s : School.fetchAll()) {
				result.put(s.getId() + "", s.name);
			}

		} catch (ModelException e) {
			logError(e.getMessage());
		}
		return result;
	}

	/**
	 * Метод, который будет возвращать значение lastval СУБД
	 * 
	 * @return целое число
	 * @throws SQLException
	 */
	public static int getId(Connection con) throws SQLException {
		int result = 0;
		PreparedStatement st = con.prepareStatement("SELECT lastval() AS id;",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = st.executeQuery();
		if (rs.first()) {
			result = rs.getInt("id");
		}
		rs.close();
		st.close();
		con.close();
		return result;
	}

	public static Map<String, String> fmarkSubjects() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement st = con.prepareStatement(
					"SELECT * FROM subjects ORDER BY sub_name;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			result = getSubjectList(st.executeQuery());
			con.close();
		} catch (SQLException | NullPointerException e) {
			logError(e.getMessage());
		}
		return result;
	}

	public static Map<String, String> fmarkModules() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement st = con.prepareStatement(
					"SELECT * FROM subjects ORDER BY sub_name;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			result = getSubjectList(st.executeQuery());
			con.close();
		} catch (SQLException | NullPointerException e) {
			logError(e.getMessage());
		}
		return result;
	}

	public static Map<String, String> cmarkSubjects(final Card c) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement st = con.prepareStatement(
					"SELECT * FROM subjects WHERE (sub_pcode IN (SELECT fmk_subcode "
							+ "FROM fmarks WHERE (fmk_crdcode=?)))"
							+ " ORDER BY sub_name;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			st.setInt(1, c.getId());
			result = getSubjectList(st.executeQuery());
			con.close();
		} catch (SQLException | NullPointerException e) {
			logError(e.getMessage());
		}
		return result;
	}

	public static Map<String, String> gmarkSubjects(final Card c) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try (Connection con = DB.getConnection()) {
			PreparedStatement st = con.prepareStatement(
					"SELECT * FROM subjects WHERE (sub_pcode IN (SELECT fmk_subcode FROM fmarks "
							+ "WHERE (fmk_crdcode=?))) ORDER BY sub_name;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			st.setInt(1, c.getId());
			result = getSubjectList(st.executeQuery());
			con.close();
		} catch (SQLException | NullPointerException e) {
			logError(e.getMessage());
		}
		return result;
	}

	private static LinkedHashMap<String, String> getSubjectList(ResultSet rs) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try {
			while (rs.next()) {
				result.put(rs.getString("sub_pcode"), rs.getString("sub_name"));
			}
		} catch (SQLException | NullPointerException e) {
			logError(e.getMessage());
		}
		return result;
	}

	public static Map<String, String> practics() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		try {
			for (Practic p : Practic.fetchAll()) {
				result.put(p.getId() + "", p.name);
			}
		} catch (ModelException e) {
			logError(e.getMessage());
		}
		return result;
	}
}
