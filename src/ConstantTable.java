import java.sql.*;


class ConstantTable {
	Connection conn;

	public ConstantTable(Connection conn) {
		this.conn = conn;
	}

	public void create() throws SQLException {
		Statement st = conn.createStatement();
		st.addBatch("DROP TABLE IF EXISTS constant");
		st.addBatch("CREATE TABLE constant (key STRING PRIMARY KEY, value REAL)");
		st.executeBatch();
	}

	public void insert(String key, double value) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO constant VALUES (?,?)");
		ps.setString(1, key);
		ps.setDouble(2, value);
		ps.execute();
	}

	public double select(String key, double defaultValue) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM constant WHERE key=?");
		ps.setString(1, key);
		ResultSet rs = ps.executeQuery();

		try {
			return rs.getDouble("value");
		} catch (SQLException e) {
			return defaultValue;
		}
	}
}
