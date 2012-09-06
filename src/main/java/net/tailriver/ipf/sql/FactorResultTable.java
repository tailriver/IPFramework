package net.tailriver.ipf.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import net.tailriver.ipf.dataset.AnsysResultSet;
import net.tailriver.ipf.dataset.DesignSet;
import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.OrthogonalTensor2;
import net.tailriver.ipf.science.Stress;


public class FactorResultTable extends Table {
	public FactorResultTable(Connection conn) {
		super(conn, "factor_result");
		addColumn("factor", "INTEGER REFERENCES factor(id)");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn(S(OrthogonalTensor2.XX), "REAL");
		addColumn(S(OrthogonalTensor2.YY), "REAL");
		addColumn(S(OrthogonalTensor2.XY), "REAL");
		addTableConstraint("PRIMARY KEY(factor,node)");
	}

	public int[] insert(Collection<AnsysResultSet> arsArray) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?,?)");
			for (AnsysResultSet ars : arsArray) {
				ps.setInt(1, ars.id());
				ps.setInt(2, ars.node().id());
				ps.setDouble(3, ars.stress(OrthogonalTensor2.XX));
				ps.setDouble(4, ars.stress(OrthogonalTensor2.YY));
				ps.setDouble(5, ars.stress(OrthogonalTensor2.XY));
				ps.addBatch();
			}
			return ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public AnsysResultSet select(FactorId fid, NodeId nid) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = selectPreparedStatement();
			ps.setInt(1, fid.id());
			ps.setInt(2, nid.id());
			ResultSet rs = ps.executeQuery();

			Stress s = new Stress();
			s.put(OrthogonalTensor2.XX, rs.getDouble(S(OrthogonalTensor2.XX)));
			s.put(OrthogonalTensor2.YY, rs.getDouble(S(OrthogonalTensor2.YY)));
			s.put(OrthogonalTensor2.XY, rs.getDouble(S(OrthogonalTensor2.XY)));
			return new AnsysResultSet(fid, nid, s);
		}
		finally {
			if (ps != null)
				ps.close();
		}
	}

	public double getMatrixValue(FactorId fid, Collection<DesignSet> dsl) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = selectPreparedStatement();
			ps.setInt(1, fid.id());
			double total = 0;
			for (DesignSet ds : dsl) {
				ps.setInt(2, ds.node().id());
				ResultSet rs = ps.executeQuery();

				for (Map.Entry<OrthogonalTensor2, Double> stress : ds.stress().entrySet())
					total += rs.getDouble(S(stress.getKey())) * stress.getValue();
			}
			return total;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	private final PreparedStatement selectPreparedStatement() throws SQLException {
		return conn.prepareStatement("SELECT * FROM " + tableName + " WHERE factor=? AND node=?");
	}

	private final String S(OrthogonalTensor2 t) {
		return "S".concat(t.name());
	}
}
