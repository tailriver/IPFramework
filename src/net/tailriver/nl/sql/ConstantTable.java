package net.tailriver.nl.sql;

import java.sql.*;
import java.util.Collections;
import java.util.Map;


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
		insert(Collections.singletonMap(key, value));
	}

	public void insert(Map<String, Double> map) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?)");
			for (Map.Entry<String, Double> e : map.entrySet()) {
				ps.setString(1, e.getKey());
				ps.setDouble(2, e.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			if(ps != null)
				ps.close();
		}
	}

	public double select(String key, double defaultValue) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE key=?");
			ps.setString(1, key);
			ResultSet rs = ps.executeQuery();
			return rs.getDouble("value");
		} catch (SQLException e) { 
			return defaultValue;
		} finally {
			if (ps != null)
				ps.close();
		}
	}
}
