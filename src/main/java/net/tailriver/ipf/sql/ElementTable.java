package net.tailriver.ipf.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.tailriver.ipf.dataset.ElementSet;
import net.tailriver.ipf.id.ElementId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.java.Util;


public class ElementTable extends Table {
	public static final String[] ELEMENT_LABELS = {"p", "q", "r", "s", "t", "u", "v", "w"};

	public ElementTable(Connection conn) {
		super(conn, "element");
		addColumn("id", "INTEGER PRIMARY KEY");
		for (String label : ELEMENT_LABELS)
			addColumn(label, "INTEGER REFERENCES node");
	}

	public boolean insert(NodeId[] nodes) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(
					"INSERT INTO " + tableName +
					" ("+ Util.join(",", ELEMENT_LABELS) + ") VALUES (?,?,?,?,?,?,?,?)");
			for (int i = 0; i < ELEMENT_LABELS.length; i++)
				ps.setInt(i+1, nodes[i].id());
			return ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public List<ElementSet> select(NodeId nid) throws SQLException {
		List<String> where = new ArrayList<>();
		for (String label : ELEMENT_LABELS)
			where.add(label.concat("=?"));

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(
					"SELECT * FROM " + tableName + " WHERE " + Util.join(" OR ", where)
					);
			for (int i = 1; i <= ELEMENT_LABELS.length; i++)
				ps.setInt(i, nid.id());

			ResultSet rs = ps.executeQuery();
			return processResult(rs);
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public ElementSet select(ElementId eid) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?");
			ps.setInt(1, eid.id());

			ResultSet rs = ps.executeQuery();
			return processResult(rs).get(0);
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
			return processResult(rs);
		} finally {
			if (st != null)
				st.close();
		}
	}

	private List<ElementSet> processResult(ResultSet rs) throws SQLException {
		List<ElementSet> rows = new ArrayList<>();
		while (rs.next()) {
			ElementId eid = new ElementId(rs.getInt("id"));
			NodeId[] nodes = new NodeId[ELEMENT_LABELS.length];
			for (int i = 0; i < ELEMENT_LABELS.length; i++)
				nodes[i] = new NodeId(rs.getInt(ELEMENT_LABELS[i]));
			rows.add(new ElementSet(eid, nodes));
		}
		return rows;
	}
}
