package net.tailriver.nl.sql;

import java.sql.*;
import java.util.*;

import net.tailriver.nl.dataset.DesignSet;
import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.NodeId;


public class DesignTable extends Table {
	public DesignTable(Connection conn) {
		super(conn, "design");
		addColumn("id", "INTEGER");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("comp", "TEXT");
		addColumn("value", "REAL");
		addTableConstraint("PRIMARY KEY(id,node,comp)");
	}

	public void insert(DesignId did, NodeId node, DesignSet ds) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?)");
		ps.setInt(1, did.id());
		ps.setInt(2, node.id());
		ps.setString(3, ds.component().name());
		ps.setDouble(4, ds.value());
		ps.execute();
		ps.close();
	}

	public int maxDesignId() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT max(id) FROM " + tableName);
		int max = rs.getInt(1);

		rs.close();
		st.close();
		return max;
	}

	public List<DesignSet> selectAll() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);

		List<DesignSet> fsl = processSelectResult(rs);
		st.close();
		return fsl;
	}

	private List<DesignSet> processSelectResult(ResultSet rs) throws SQLException {
		List<DesignSet> fsl = new ArrayList<DesignSet>();
		while (rs.next()) {
			DesignId did = new DesignId(rs.getInt("id"));
			NodeId num = new NodeId(rs.getInt("node"));
			String c = rs.getString("comp");
			Double w = rs.getDouble("weight");
			fsl.add(new DesignSet(did, num, c, w));
		}
		rs.close();
		return fsl;
	}
}
