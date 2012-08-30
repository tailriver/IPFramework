import java.sql.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;


class NodeTable {
	@SuppressWarnings("serial")
	public class Row extends EnumMap<Util.C3D, Double> {
		public final int num;

		Row(int num, double r, double t, double z) {
			super(Util.C3D.class);
			this.num = num;
			put(Util.C3D.r, r);
			put(Util.C3D.t, t);
			put(Util.C3D.z, z);
		}
	}

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

	public void insert(double r, double t, double z) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT OR IGNORE INTO node (r,t,z) VALUES (?,?,?)");
		ps.setDouble(1, r);
		ps.setDouble(2, t);
		ps.setDouble(3, z);
		ps.execute();
	}

	public int select(double r, double t, double z) throws SQLException {
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

	public List<Row> selectAll() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM node");

		List<Row> rows = new ArrayList<Row>();
		while (rs.next()) {
			int num  = rs.getInt("num");
			double r = rs.getDouble("r");
			double t = rs.getDouble("t");
			double z = rs.getDouble("z");
			rows.add(new Row(num, r, t, z));
		}

		return rows;
	}
}
