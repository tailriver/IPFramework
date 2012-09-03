package net.tailriver.nl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import net.tailriver.nl.dataset.AnsysResultSet;
import net.tailriver.nl.dataset.DesignSet;
import net.tailriver.nl.dataset.DesignSet.Component;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;


public class FactorResultTable extends Table {
	public FactorResultTable(Connection conn) {
		super(conn, "factor_result");
		addColumn("factor", "INTEGER REFERENCES factor(id)");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn(S(Component.XX), "REAL");
		addColumn(S(Component.YY), "REAL");
		addColumn(S(Component.XY), "REAL");
		addTableConstraint("PRIMARY KEY(factor,node)");
	}

	public int[] insert(Collection<AnsysResultSet> arsArray) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?,?)");
			for (AnsysResultSet ars : arsArray) {
				ps.setInt(1, ars.id());
				ps.setInt(2, ars.node().id());
				ps.setDouble(3, ars.s(Component.XX));
				ps.setDouble(4, ars.s(Component.YY));
				ps.setDouble(5, ars.s(Component.XY));
				ps.addBatch();
			}
			return ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public AnsysResultSet select(FactorId fid, NodeId node) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = selectPreparedStatement();
			ps.setInt(1, fid.id());
			ps.setInt(2, node.id());
			ResultSet rs = ps.executeQuery();

			Double sxx = rs.getDouble(S(Component.XX));
			Double syy = rs.getDouble(S(Component.YY));
			Double sxy = rs.getDouble(S(Component.XY));
			return new AnsysResultSet(fid, node, sxx, syy, sxy);
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

				double s = rs.getDouble(S(ds.component()));
				total += s * ds.weight();
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

	private final String S(Component c) {
		return "S".concat(c.name());
	}
}
