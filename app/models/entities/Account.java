package models.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.ModelException;
import models.Role;
import models.Utils;
import play.db.DB;

/**
 * Класс для хранения учетных данных
 * 
 * @author Воронин Леонид
 *
 */
public class Account {

	private final static String ERROR = "Ошибка при работе с таблицей Accounts ";
	private final static String READ = "при чтении записи. ";
	private final static String SAVE = "при сохранении записи. ";
	private final static String DELETE = "при удалении записи. ";
	private final static String NOT_FOUND = "Запрос не вернул ни одной записи! ";
	private final static String NO_RECORDS = "Ни одна из записей не подверглась изменению! ";
	private final static String SQL = "SQLException: ";
	private static final String SALT = "#%&24563fGRt123SKDTfce#234";

	private int id;
	public String login;
	public String description;
	private String password;
	public int role = Role.DEPARTMENT.ordinal();

	public Account() {
		id = 0;
	}

	public Account(final int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean isAdmin() {
		return (role == Role.ADMIN.ordinal());
	}

	public boolean valid() {
		if ((login == null) || (login.isEmpty()))
			return false;
		if ((description == null) || (description.isEmpty()))
			return false;
		if ((password == null) || (password.isEmpty()))
			return false;
		return true;
	}

	public Map<String, String> getRoles() {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		for (Role val: Role.values()) {
			result.put(val.ordinal() + "", val.getDescription());
		}
		return result;
	}

	public String getPassword() {
		return new String();
	}

	public void setPassword(final String password) throws ModelException {
		this.password = Utils.md5(password + SALT);
	}

	public void updateFrom(final Account account) {
		this.login = account.login;
		this.description = account.description;
	}

	public static Account auth(final String login, final String password)
			throws ModelException {
		Account result = null;
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con
					.prepareStatement(
							"SELECT * FROM accounts WHERE (aco_login = ?) AND (aco_password = ?) LIMIT 1;",
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, login);
			statement.setString(2, Utils.md5(password + SALT));
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Account(rs.getInt("aco_pcode"));
				result.readFIelds(rs);
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
		return result;
	}

	public static Account get(int id) throws ModelException {
		try (Connection con = DB.getConnection()) {
			Account result = null;
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM accounts WHERE (aco_pcode = ?) LIMIT 1;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = new Account(rs.getInt("aco_pcode"));
				result.readFIelds(rs);
			} else {
				throw new ModelException(ERROR + READ + NOT_FOUND);
			}
			rs.close();
			statement.close();
			return result;
		} catch (SQLException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public static List<Account> fetchAll() throws ModelException {
		try (Connection con = DB.getConnection()) {
			List<Account> result = new ArrayList<>();
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM accounts;",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Account item = new Account(rs.getInt("aco_pcode"));
				item.readFIelds(rs);
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
			PreparedStatement statement = con
					.prepareStatement("INSERT INTO accounts(aco_login, aco_description, aco_role, aco_password) VALUES(?, ?, ?, ?);");
			if (id > 0) {
				statement = con
						.prepareStatement("UPDATE accounts SET aco_login=?, aco_description=?, aco_role=?, aco_password=? WHERE (aco_pcode=?);");
				statement.setInt(5, id);
			}
			statement.setString(1, login);
			statement.setString(2, description);
			statement.setInt(3, role);
			statement.setString(4, password);
			int count = statement.executeUpdate();
			if (count < 1) {
				throw new ModelException(ERROR + SAVE + NO_RECORDS);
			}
			if (id == 0) {
				id = Utils.getId(con);
			}
			statement.close();
		} catch (SQLException | NullPointerException e) {
			throw new ModelException(ERROR + READ + SQL + e.getMessage());
		}
	}

	public void delete() throws ModelException {
		try (Connection con = DB.getConnection()) {
			PreparedStatement statement = con
					.prepareStatement("DELETE FROM accounts WHERE (aco_pcode=?);");
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

	private void readFIelds(final ResultSet rs) throws SQLException {
		login = rs.getString("aco_login");
		description = rs.getString("aco_description");
		password = rs.getString("aco_password");
		role = rs.getInt("aco_role");
	}

}
