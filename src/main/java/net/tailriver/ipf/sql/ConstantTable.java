package net.tailriver.ipf.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


public class ConstantTable extends Table {
	public ConstantTable(Connection conn) {
		super(conn, "constant");
		addColumn("key", "STRING PRIMARY KEY");
		addColumn("value", "REAL");
	}

	public int[] insert(Map<ConstantTableKey, Double> map) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?)");
			for (Map.Entry<ConstantTableKey, Double> e : map.entrySet()) {
				ps.setString(1, e.getKey().name());
				ps.setDouble(2, e.getValue());
				ps.addBatch();
			}
			return ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public double select(ConstantTableKey key) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE key=?");
			ps.setString(1, key.name());
			ResultSet rs = ps.executeQuery();
			return rs.getDouble("value");
		} catch (SQLException e) { 
			return getDefaultValue(key);
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public static double getDefaultValue(ConstantTableKey key) {
		return key.defaultValue;
	}
}
