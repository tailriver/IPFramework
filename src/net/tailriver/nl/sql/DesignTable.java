package net.tailriver.nl.sql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.tailriver.nl.dataset.DesignSet;
import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.Stress;
import net.tailriver.nl.util.Tensor2;


public class DesignTable extends Table {
	public DesignTable(Connection conn) {
		super(conn, "design");
		addColumn("id", "INTEGER");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("comp", "TEXT");
		addColumn("weight", "REAL");
		addTableConstraint("PRIMARY KEY(id,node,comp)");
	}

	public int[] insert(DesignId did, NodeId node, Stress stress) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?)");
			ps.setInt(1, did.id());
			ps.setInt(2, node.id());
			for (Map.Entry<Tensor2, Double> s : stress.entrySet()) {
				ps.setString(3, s.getKey().name());
				ps.setDouble(4, s.getValue());
				ps.addBatch();
			}
			return ps.executeBatch();
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
			NodeId nid = new NodeId(rs.getInt("node"));
			Stress s = new Stress(rs.getString("comp"), rs.getDouble("weight"));
			fsl.add(new DesignSet(did, nid, s));
		}
		return fsl;
	}
}
