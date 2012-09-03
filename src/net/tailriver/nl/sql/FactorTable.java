package net.tailriver.nl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.tailriver.nl.dataset.FactorSet;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.ArrayListWOF;


public class FactorTable extends Table {
	public FactorTable(Connection conn) {
		super(conn, "factor");
		addColumn("id", "INTEGER");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("comp", "TEXT");
		addColumn("value", "REAL");
		addTableConstraint("PRIMARY KEY(id,node,comp)");
	}

	public boolean insert(FactorId fid, NodeId node, FactorSet fs) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?)");
			ps.setInt(1, fid.id());
			ps.setInt(2, node.id());
			ps.setString(3, fs.direction().name());
			ps.setDouble(4, fs.value());
			return ps.execute();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public int maxFactorId() throws SQLException {
		return executeQueryAndGetInt1("SELECT max(id) FROM " + tableName);
	}

	public List<ArrayListWOF<FactorSet, FactorId>> selectAllByFactorNum() throws SQLException {
		int maxFactorId = maxFactorId();

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?");
			List<ArrayListWOF<FactorSet, FactorId>> whole =
					new ArrayList<ArrayListWOF<FactorSet, FactorId>>();
			for (int id = 1; id <= maxFactorId; id++) {
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				FactorId fid = new FactorId(id);
				ArrayListWOF<FactorSet, FactorId> wof = new ArrayListWOF<FactorSet, FactorId>(fid);
				wof.addAll(processSelectResult(rs));
				whole.add(wof);
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
		List<FactorSet> fsl = new ArrayList<FactorSet>();
		while (rs.next()) {
			FactorId fid = new FactorId(rs.getInt("id"));
			NodeId num = new NodeId(rs.getInt("node"));
			String d = rs.getString("comp");
			Double v = rs.getDouble("value");
			fsl.add(new FactorSet(fid, num, d, v));
		}
		return fsl;
	}
}
