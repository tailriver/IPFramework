package net.tailriver.nl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.tailriver.java.FieldArrayList;
import net.tailriver.nl.dataset.FactorSet;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.science.Force;
import net.tailriver.nl.science.OrthogonalTensor1;


public class FactorTable extends Table {
	public FactorTable(Connection conn) {
		super(conn, "factor");
		addColumn("id", "INTEGER");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("comp", "TEXT");
		addColumn("value", "REAL");
		addTableConstraint("PRIMARY KEY(id,node,comp)");
	}

	public int[] insert(FactorId fid, NodeId nid, Force force) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?)");
			ps.setInt(1, fid.id());
			ps.setInt(2, nid.id());
			for (Map.Entry<OrthogonalTensor1, Double> f : force.entrySet()) {
				ps.setString(3, f.getKey().name());
				ps.setDouble(4, f.getValue());
				ps.addBatch();
			}
			return ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public int maxFactorId() throws SQLException {
		return executeQueryAndGetInt1("SELECT max(id) FROM " + tableName);
	}

	public List<FieldArrayList<FactorSet, FactorId>> selectAllByFactorNum() throws SQLException {
		int maxFactorId = maxFactorId();

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?");
			List<FieldArrayList<FactorSet, FactorId>> whole = new ArrayList<>();
			for (int id = 1; id <= maxFactorId; id++) {
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				FactorId fid = new FactorId(id);
				FieldArrayList<FactorSet, FactorId> fal = new FieldArrayList<>();
				fal.set(fid);
				fal.addAll(processSelectResult(rs));
				whole.add(fal);
			}
			return whole;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public List<FactorSet> selectAll() throws SQLException {
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

	private List<FactorSet> processSelectResult(ResultSet rs) throws SQLException {
		List<FactorSet> list = new ArrayList<>();
		while (rs.next()) {
			FactorId fid = new FactorId(rs.getInt("id"));
			NodeId nid = new NodeId(rs.getInt("node"));
			Force f = new Force(rs.getString("comp"), rs.getDouble("value"));
			list.add(new FactorSet(fid, nid, f));
		}
		return list;
	}
}
