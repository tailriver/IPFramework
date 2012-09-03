package net.tailriver.nl.sql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.tailriver.nl.dataset.DesignSet;
import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.NodeId;


public class DesignTable extends Table {
	public DesignTable(Connection conn) {
		super(conn, "design");
		addColumn("id", "INTEGER");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("comp", "TEXT");
		addColumn("weight", "REAL");
		addTableConstraint("PRIMARY KEY(id,node,comp)");
	}

	public boolean insert(DesignId did, NodeId node, DesignSet ds) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?)");
			ps.setInt(1, did.id());
			ps.setInt(2, node.id());
			ps.setString(3, ds.component().name());
			ps.setDouble(4, ds.weight());
			return ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public int maxDesignId() throws SQLException {
		return executeQueryAndGetInt1("SELECT max(id) FROM " + tableName);
	}

	public List<DesignSet> select(DesignId did) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?");
			ps.setInt(1, did.id());
			ResultSet rs = ps.executeQuery();	
			return processSelectResult(rs);
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public List<DesignSet> selectAll() throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);
			return processSelectResult(rs);
		} finally {
			if (st != null)
				st.close();
		}
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
		return fsl;
	}
}
