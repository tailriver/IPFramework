import java.sql.*;
import java.util.*;


class NodeTable implements Identifiable {
	static final Coordinate NODE_COORDINATE_SYSTEM = Coordinate.Cylindrical;
	private Connection conn;

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

	public void insert(Point p) throws SQLException {
		if (!p.equals(NODE_COORDINATE_SYSTEM))
			throw new IllegalArgumentException("Incompatible coordinate system");

		PreparedStatement ps =
				conn.prepareStatement("INSERT OR IGNORE INTO node (r,t,z) VALUES (?,?,?)");
		for (int i = 0; i < NODE_COORDINATE_SYSTEM.getDimension(); i++)
			ps.setDouble(i+1, p.x(i));
		ps.execute();
	}

	public Id<NodeTable> select(Point p) throws SQLException {
		if (!p.equals(NODE_COORDINATE_SYSTEM))
			throw new IllegalArgumentException("Incompatible coordinate system");

		PreparedStatement ps =
				conn.prepareStatement("SELECT num FROM node WHERE r=? AND t=? AND z=?");
		for (int i = 0; i < NODE_COORDINATE_SYSTEM.getDimension(); i++)
			ps.setDouble(i+1, p.x(i));
		ResultSet rs = ps.executeQuery();

		try {
			int n = rs.getInt("num");
			return new Id<NodeTable>(n);
		} catch (SQLException e) {
			throw new SQLException("node not found");			
		}
	}

	public List<NodeSet> selectAll() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM node");

		List<NodeSet> rows = new ArrayList<NodeSet>();
		while (rs.next()) {
			int n    = rs.getInt("num");
			double r = rs.getDouble("r");
			double t = rs.getDouble("t");
			double z = rs.getDouble("z");

			Id<NodeTable> num = new Id<NodeTable>(n);
			Point p = new Point(Coordinate.Cylindrical, r, t, z);
			rows.add(new NodeSet(num, p));
		}

		return rows;
	}
}
