package models.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.ModelException;
import models.Role;
import models.Utils;
import play.db.DB;

/**
 * Класс клиентской сессии
 * 
 * @author Воронин Леонид
 *
 */
public class ClientSession {
	private final static String ERROR = "Ошибка при работе с таблицей ClientSessions ";
	private final static String READ = "при чтении записи. ";
	private final static String SAVE = "при сохранении записи. ";
	private final static String DELETE = "при удалении записи. ";
	private final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	private final static String SQL = "SQLException: ";

	private int id;
	private int account;
	public String ipaddr;
	public String ssid;

	private void getHash() throws ModelException {
		long timestamp = (new Date()).getTime();
		ssid = Utils.md5(ipaddr + timestamp);
	}

	private void readFields(ResultSet rs) throws SQLException {
		account = rs.getInt("cls_acocode");
		ipaddr = rs.getString("cls_ipaddr");
		ssid = rs.getString("cls_ssid");
	}

	public ClientSession(String ipAddress) throws ModelException {
		id = 0;
		this.ipaddr = ipAddress;
		getHash();
	}

	public ClientSession(final int id, String ipAddress) throws ModelException {
		this.id = id;
		this.ipaddr = ipAddress;
		getHash();
	}

	public static ClientSession find(String ssid) throws ModelException {
		if (ssid == null)
			return null;
		try (Connection con = DB.getConnection()) {
			ClientSession result = null;
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM getClientSession(?);",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, ssid);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new ClientSession(rs.getInt("cls_pcode"),
						rs.getString("cls_ipaddr"));
				result.readFields(rs);
			}
			rs.close();
			statement.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public static Account getAccount(final String ssid) {
		try {
			ClientSession cs = ClientSession.find(ssid);
			if (cs != null) {
				return cs.getAccount();
			}
			return null;
		} catch (ModelException e) {
			return null;
		}
	}

	public static boolean isAdmin(final String ssid) {
		Account ac = getAccount(ssid);
		return ((ac != null) && (ac.role == Role.ADMIN.ordinal()));
	}
	
	public static boolean isAdminOrInList(final String ssid, final Role... roles) {
		if ((null == ssid) || (ssid.isEmpty()))
			return false;
		Account ac = getAccount(ssid);
		if (null == ac)
			return false;
		if (ac.isAdmin())
			return true;
		for (Role role: roles) {
			if (ac.role == role.ordinal()) {
				return true;
			}
		}
		return false;		
	}

	public static List<ClientSession> fetchAll() throws ModelException {
		try (Connection con = DB.getConnection()) {
			List<ClientSession> result = new ArrayList<>();
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM clientsessions;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				ClientSession item = new ClientSession(rs.getInt("cls_pcode"),
						rs.getString("cls_ipaddr"));
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
			if (id == 0) {
				PreparedStatement statement = con
						.prepareStatement("INSERT INTO clientsessions (cls_acocode, cls_ipaddr, cls_ssid) VALUES (?, ?, ?);");
				statement.setInt(1, account);
				statement.setString(2, ipaddr);
				statement.setString(3, ssid);
				int count = statement.executeUpdate();
				if (count < 1) {
					throw new ModelException(ERROR + SAVE + NO_RECORDS);
				}
				if (id == 0) {
					id = Utils.getId(con);
				}
				statement.close();
			}
		} catch (SQLException e) {
			throw new ModelException(ERROR + SAVE + SQL + e.getMessage());
		}
	}

	public void delete() throws ModelException {
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con
					.prepareStatement("DELETE FROM clientsessions WHERE (cls_pcode=?);");
			statement.setInt(1, id);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + DELETE + NO_RECORDS);
			}
			statement.close();
		} catch (SQLException e) {
			throw new ModelException(ERROR + DELETE + SQL + e.getMessage());
		}
	}

	public Account getAccount() throws ModelException {
		return Account.get(account);
	}

	public void setAccount(Account ac) {
		account = ac.getId();
	}

	public int getId() {
		return id;
	}
}
