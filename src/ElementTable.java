import java.sql.*;
import java.util.*;


class ElementTable extends SQLTable implements Identifiable {
	public static final String[] ELEMENT_LABELS = new String[]{"p", "q", "r", "s", "t", "u", "v", "w"};

	public ElementTable(Connection conn) {
		super(conn, "element");
		addColumn("id", "INTEGER PRIMARY KEY");
		for (String label : ELEMENT_LABELS)
			addColumn(label, "INTEGER REFERENCES node");
	}

	public void insert(List<Id<NodeTable>> nodes) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement(
						"INSERT INTO " + tableName
						+ " ("+ Util.join(",", ELEMENT_LABELS) + ") VALUES (?,?,?,?,?,?,?,?)");
		for (int i = 0; i < 2 * ModelParser.PLANE_NODE_SIZE; i++)
			ps.setInt(i+1, nodes.get(i).id());
		ps.execute();
		ps.close();
	}

	public List<ElementSet> selectAll() throws SQLException {
		Statement st = conn.createStatement();
		List<ElementSet> rows = new ArrayList<ElementSet>();

		ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);
		while (rs.next()) {
			Id<ElementTable> eid = new Id<ElementTable>(rs.getInt("id"));
			@SuppressWarnings("unchecked")
			Id<NodeTable>[] nodes = new Id[ELEMENT_LABELS.length];
			for (int i = 0; i < ELEMENT_LABELS.length; i++)
				nodes[i] = new Id<NodeTable>(rs.getInt(ELEMENT_LABELS[i]));
			rows.add(new ElementSet(eid, nodes));
		}

		rs.close();
		st.close();
		return rows;
	}
}
