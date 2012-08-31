package net.tailriver.nl.sql;

import java.sql.*;
import java.util.*;

import net.tailriver.nl.dataset.FactorSet;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.*;


public class FactorTable extends Table {
	public FactorTable(Connection conn) {
		super(conn, "factor");
		addColumn("id", "INTEGER");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("comp", "TEXT");
		addColumn("value", "REAL");
		addTableConstraint("PRIMARY KEY(id,node,comp)");
	}

	public void insert(FactorId fid, NodeId node, FactorSet fs) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?)");
		ps.setInt(1, fid.id());
		ps.setInt(2, node.id());
		ps.setString(3, fs.direction().name());
		ps.setDouble(4, fs.value());
		ps.execute();
		ps.close();
	}

	public int maxFactorId() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT max(id) FROM " + tableName);
		int max = rs.getInt(1);

		rs.close();
		st.close();
		return max;
	}

	public List<ArrayListWOF<FactorSet, FactorId>> selectAllByFactorNum() throws SQLException {
		int maxFactorId = maxFactorId();

		List<ArrayListWOF<FactorSet, FactorId>> whole =
				new ArrayList<ArrayListWOF<FactorSet, FactorId>>();
		PreparedStatement ps =
				conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?");
		for (int id = 1; id <= maxFactorId; id++) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			FactorId fid = new FactorId(id);
			ArrayListWOF<FactorSet, FactorId> wof = new ArrayListWOF<FactorSet, FactorId>(fid);
			wof.addAll(processSelectResult(rs));
			whole.add(wof);
		}
		ps.close();
		return whole;
	}

	public List<FactorSet> selectAll() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);

		List<FactorSet> fsl = processSelectResult(rs);
		st.close();
		return fsl;
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
		rs.close();
		return fsl;
	}
}
