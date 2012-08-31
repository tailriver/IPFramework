package net.tailriver.nl.sql;
import java.sql.*;


public class SQLiteUtil {
	public static Connection getConnection(String dbname) throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbname);

			// initialize
			conn.setAutoCommit(false);
			conn.createStatement().execute("PRAGMA foreign_keys = ON");

			return conn;
		} catch (ClassNotFoundException e) {
			System.err.println("SQLite JDBC Not Found");
			throw new SQLException(e.getMessage());
		} catch (SQLException e) {
			System.err.println("Fail to open database...");
			throw e;
		}
	}

	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println("Fail to close database...");
			}
		}
	}
}
