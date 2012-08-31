import java.sql.*;
import java.util.*;


public class FactorTable extends SQLTable implements Identifiable {
	public FactorTable(Connection conn) {
		super(conn, "factor");
		addColumn("id", "INTEGER");
		addColumn("node", "INTEGER REFERENCES node");
		addColumn("comp", "TEXT");
		addColumn("value", "REAL");
		addTableConstraint("PRIMARY KEY(id,node,comp)");
	}

	public void insert(Id<FactorTable> num, Id<NodeTable> node, FactorSet fs) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT INTO " + tableName + " (id,node,comp,value) VALUES (?,?,?,?)");
		ps.setInt(1, num.id());
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

	public List<FactorList<FactorSet>> selectAllByFactorNum() throws SQLException {
		int maxFactorId = maxFactorId();

		List<FactorList<FactorSet>> whole = new ArrayList<FactorList<FactorSet>>();
		PreparedStatement ps =
				conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?");
		for (int id = 1; id <= maxFactorId; id++) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			FactorList<FactorSet> fl = new FactorList<FactorSet>(id);
			fl.addAll(processSelectResult(rs));
			whole.add(fl);
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
			Id<FactorTable> fid = new Id<FactorTable>(rs.getInt("id"));
			Id<NodeTable> num = new Id<NodeTable>(rs.getInt("node"));
			String d = rs.getString("comp");
			Double v = rs.getDouble("value");
			fsl.add(new FactorSet(fid, num, d, v));
		}
		rs.close();
		return fsl;
	}
}
