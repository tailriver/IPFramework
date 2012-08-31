import java.sql.*;
import java.util.List;


public class FactorResultTable extends SQLTable implements Identifiable {
	public FactorResultTable(Connection conn) {
		super(conn, "factor_result");
		addColumn("factor", "INTEGER REFERENCES factor(id)");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("sxx", "REAL");
		addColumn("syy", "REAL");
		addColumn("sxy", "REAL");
		addTableConstraint("PRIMARY KEY(factor,node)");
	}

	public void insert(List<AnsysResultSet> arsList) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?,?)");
		for (AnsysResultSet ars : arsList) {
			ps.setInt(1, ars.id());
			ps.setInt(2, ars.node().id());
			ps.setDouble(3, ars.sxx());
			ps.setDouble(4, ars.syy());
			ps.setDouble(5, ars.sxy());
			ps.addBatch();

			if (isDebugMode)
				System.out.println(ars.toString());
		}
		ps.executeBatch();
		ps.close();
	}

	public AnsysResultSet select(Id<FactorTable> fid, Id<NodeTable> node) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("SELECT * FROM " + tableName + " WHERE factor=? AND node=?");
		ps.setInt(1, fid.id());
		ps.setInt(2, node.id());
		ResultSet rs = ps.executeQuery();

		Double sxx = Double.valueOf(rs.getInt("sxx"));
		Double syy = Double.valueOf(rs.getInt("syy"));
		Double sxy = Double.valueOf(rs.getInt("sxy"));

		rs.close();
		ps.close();
		return new AnsysResultSet(fid, node, sxx, syy, sxy);
	}
}
