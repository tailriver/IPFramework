package net.tailriver.nl.sql;

import java.sql.*;
import java.util.*;

import net.tailriver.nl.dataset.ElementSet;
import net.tailriver.nl.id.ElementId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.*;


public class ElementTable extends Table {
	public static final String[] ELEMENT_LABELS = new String[]{"p", "q", "r", "s", "t", "u", "v", "w"};

	public ElementTable(Connection conn) {
		super(conn, "element");
		addColumn("id", "INTEGER PRIMARY KEY");
		for (String label : ELEMENT_LABELS)
			addColumn(label, "INTEGER REFERENCES node");
	}

	public void insert(NodeId[] nodes) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName 
					+ " ("+ Util.join(",", ELEMENT_LABELS) + ") VALUES (?,?,?,?,?,?,?,?)");
			for (int i = 0; i < ELEMENT_LABELS.length; i++)
				ps.setInt(i+1, nodes[i].id());
			ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public List<ElementSet> selectAll() throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);
			List<ElementSet> rows = new ArrayList<ElementSet>();
			while (rs.next()) {
				ElementId eid = new ElementId(rs.getInt("id"));
				NodeId[] nodes = new NodeId[ELEMENT_LABELS.length];
				for (int i = 0; i < ELEMENT_LABELS.length; i++)
					nodes[i] = new NodeId(rs.getInt(ELEMENT_LABELS[i]));
				rows.add(new ElementSet(eid, nodes));
			}
			return rows;
		} finally {
			if (st != null)
				st.close();
		}
	}
}
