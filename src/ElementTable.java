import java.sql.*;
import java.util.*;


class ElementTable implements Identifiable {
	private Connection conn;

	public ElementTable(Connection conn) {
		this.conn = conn;
	}

	public void create() throws SQLException {
		Statement s = conn.createStatement();
		s.addBatch("DROP TABLE IF EXISTS element");
		s.addBatch("CREATE TABLE element (" +
				"id INTEGER PRIMARY KEY," +
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

	public void insert(List<Id<NodeTable>> nodes) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT INTO element (p,q,r,s,t,u,v,w) VALUES (?,?,?,?,?,?,?,?)");
		for (int i = 0; i < 2 * ModelParser.PLANE_NODE_SIZE; i++)
			ps.setInt(i+1, nodes.get(i).id());
		ps.execute();
	}

	public List<ElementSet> selectAll() throws SQLException {
		Statement st = conn.createStatement();

		String[] elementLabels = new String[]{"p", "q", "r", "s", "t", "u", "v", "w"};
		List<ElementSet> rows = new ArrayList<ElementSet>();

		ResultSet rs = st.executeQuery("SELECT * FROM element");
		while (rs.next()) {
			Id<ElementTable> eid = new Id<ElementTable>(rs.getInt("id"));
			@SuppressWarnings("unchecked")
			Id<NodeTable>[] nodes = new Id[elementLabels.length];
			for (int i = 0; i < elementLabels.length; i++)
				nodes[i] = new Id<NodeTable>(rs.getInt(elementLabels[i]));
			rows.add(new ElementSet(eid, nodes));
		}

		return rows;
	}
}
