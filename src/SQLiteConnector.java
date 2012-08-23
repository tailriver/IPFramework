import java.sql.*;


class SQLiteConnector {
	Connection dbh;

	public SQLiteConnector(String dbname) {
		try {
			Class.forName("org.sqlite.JDBC");
			dbh = DriverManager.getConnection("jdbc:sqlite:" + dbname);

			// initialize
			dbh.setAutoCommit(false);

		} catch (ClassNotFoundException e) {
			System.err.println("SQLite JDBC Not Found");
			this.close();
		} catch (SQLException e) {
			System.err.println("Fail to open database...");
			e.printStackTrace();
			this.close();
		}
	}

	public Connection getConnection() {
		return dbh;
	}

	public void close() {
		try {
			if (dbh != null)
				dbh.close();
		} catch (SQLException e) {
			System.err.println("Fail to close database...");
			e.printStackTrace();
		}			
	}
}
