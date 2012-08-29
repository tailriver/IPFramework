import java.sql.*;
import java.util.*;


class ElementTable {
	public class Row {
		public final int num;
		public final Integer[] nodes;

		Row(int num, Integer[] nodes) {
			this.num = num;
			this.nodes = nodes;
		}
	}

	Connection conn;

	public ElementTable(Connection conn) {
		this.conn = conn;
	}

	public void create() throws SQLException {
		Statement s = conn.createStatement();
		s.addBatch("DROP TABLE IF EXISTS element");
		s.addBatch("CREATE TABLE element (" +
				"num INTEGER PRIMARY KEY," +
				"p REFERENCES node," +
				"q REFERENCES node," +
				"r REFERENCES node," +
				"s REFERENCES node," +
				"t REFERENCES node," +
				"u REFERENCES node," +
				"v REFERENCES node," +
				"w REFERENCES node" +
				")");
		s.executeBatch();
	}

	public void insertElement(List<Integer> lowerNodes, List<Integer> upperNodes) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT INTO element (p,q,r,s,t,u,v,w) VALUES (?,?,?,?,?,?,?,?)");
		for (int i = 0; i < ModelParser.PLANE_NODE_SIZE; i++) {
			ps.setInt(i+1, lowerNodes.get(i));					
			ps.setInt(i+5, upperNodes.get(i));					
		}
		ps.execute();
	}

	public int selectNode(double r, double t, double z) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("SELECT num FROM node WHERE r=? AND t=? AND z=?");
		ps.setDouble(1, r);
		ps.setDouble(2, t);
		ps.setDouble(3, z);
		ResultSet rs = ps.executeQuery();

		if (rs.getFetchSize() != 1)
			throw new SQLException("node not found");

		return rs.getInt("num");
	}

	public List<ElementTable.Row> selectAllElements() throws SQLException {
		Statement st = conn.createStatement();

		String[] elementLabels = new String[]{"p", "q", "r", "s", "t", "u", "v", "w"};
		List<ElementTable.Row> rows = new ArrayList<ElementTable.Row>();

		ResultSet rs = st.executeQuery("SELECT * FROM element");
		while (rs.next()) {
			int num  = rs.getInt("num");
			Integer[] nodes = new Integer[elementLabels.length];
			for (int i = 0; i < elementLabels.length; i++) {
				nodes[i] = rs.getInt(elementLabels[i]);
			}
			rows.add(new Row(num, nodes));
		}

		return rows;
	}
}
