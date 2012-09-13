package net.tailriver.ipf.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlite.Function;

public class SQLiteUtil {
	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.println("SQLite JDBC Not Found");
			System.exit(1);
		}		
	}

	public static Connection getConnection(String dbname) throws SQLException {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbname);

			// initialize
			conn.setAutoCommit(false);

			Table t = new Table(conn, null);
			t.execute("PRAGMA foreign_keys = ON");

			return conn;
		} catch (SQLException e) {
			System.err.println("fail to connect/initialize database");
			if (conn != null)
				closeConnection(conn);
			throw e;
		}
	}

	public static void createSinFunction(Connection conn) throws SQLException {
		Function.create(conn, "sin", new Function() {
			@Override
			protected void xFunc() throws SQLException {
				result(Math.sin(value_double(0)));
			}
		});

		Function.create(conn, "sin360", new Function() {
			@Override
			protected void xFunc() throws SQLException {
				result(Math.sin(Math.toRadians(value_double(0))));
			}
		});
	}

	public static void createCosFunction(Connection conn) throws SQLException {
		Function.create(conn, "cos", new Function() {
			@Override
			protected void xFunc() throws SQLException {
				result(Math.cos(value_double(0)));
			}
		});

		Function.create(conn, "cos360", new Function() {
			@Override
			protected void xFunc() throws SQLException {
				result(Math.cos(Math.toRadians(value_double(0))));
			}
		});
	}

	public static void closeConnection(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			System.err.println("fail to close database");
		}
	}
}
