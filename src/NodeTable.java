import java.sql.*;


class NodeTable {
	Connection conn;

	public NodeTable(Connection conn) {
		this.conn = conn;
	}

	public void create() throws SQLException {
		Statement st = conn.createStatement();
		st.addBatch("DROP TABLE IF EXISTS node");
		st.addBatch("CREATE TABLE node (" +
				"num INTEGER PRIMARY KEY, r REAL, t REAL, z REAL," +
				"UNIQUE(r,t,z)" +
				")");
		st.executeBatch();
	}

	public void insertNode(double r, double t, double z) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT OR IGNORE INTO node (r,t,z) VALUES (?,?,?)");
		ps.setDouble(1, r);
		ps.setDouble(2, t);
		ps.setDouble(3, z);
		ps.execute();
	}

	public int selectNode(double r, double t, double z) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("SELECT num FROM node WHERE r=? AND t=? AND z=?");
		ps.setDouble(1, r);
		ps.setDouble(2, t);
		ps.setDouble(3, z);
		ResultSet rs = ps.executeQuery();

		try {
			return rs.getInt("num");
		} catch (SQLException e) {
			throw new SQLException("node not found");			
		}
	}

	public ResultSet selectAllNodes() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM node");
		return rs;
	}
}
