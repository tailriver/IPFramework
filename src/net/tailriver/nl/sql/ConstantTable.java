package net.tailriver.nl.sql;

import java.sql.*;


public class ConstantTable extends Table {
	public static final double DEFAULT_RADIUS    = 1;
	public static final double DEFAULT_THICKNESS = 1;
	public static final double DEFAULT_MAX_CYCLE_DEGREE = 180;

	public ConstantTable(Connection conn) {
		super(conn, "constant");
		addColumn("key", "STRING PRIMARY KEY");
		addColumn("value", "REAL");
	}

	public void insert(String key, double value) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?)");
		ps.setString(1, key);
		ps.setDouble(2, value);
		ps.execute();
		ps.close();

		if (isDebugMode)
			System.out.println(key + ": " + value);
	}

	public double select(String key, double defaultValue) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE key=?");
		ps.setString(1, key);
		ResultSet rs = ps.executeQuery();

		try {
			return rs.getDouble("value");
		} catch (SQLException e) {
			return defaultValue;
		} finally {
			rs.close();
			ps.close();
		}
	}
}
