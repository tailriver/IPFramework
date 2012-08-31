import java.sql.*;
import java.util.*;


public class FactorTable implements Identifiable {
	private Connection conn;

	public FactorTable(Connection conn) {
		this.conn = conn;
	}

	public void create() throws SQLException {
		Statement st = conn.createStatement();
		st.addBatch("DROP TABLE IF EXISTS factor");
		st.addBatch("CREATE TABLE factor (" +
				"id INTEGER," +
				"node INTEGER REFERENCES node," +
				"comp TEXT," +
				"value REAL," +
				"PRIMARY KEY(id, node, comp)" +
				")");
		st.executeBatch();
	}

	public void insert(Id<FactorTable> num, Id<NodeTable> node, FactorSet fs) throws SQLException {
		PreparedStatement ps =
				conn.prepareStatement("INSERT INTO factor (id,node,comp,value) VALUES (?,?,?,?)");
		ps.setInt(1, num.id());
		ps.setInt(2, node.id());
		ps.setString(3, fs.direction().name());
		ps.setDouble(4, fs.value());
		ps.execute();
	}

	public List<FactorList> selectAllByFactorNum() throws SQLException {
		// fetch max of factor.id
		ResultSet rsMaxId = conn.createStatement().executeQuery("SELECT max(id) FROM factor");
		int maxId = rsMaxId.getInt(1);

		List<FactorList> whole = new ArrayList<FactorList>();
		PreparedStatement ps =
				conn.prepareStatement("SELECT * FROM factor WHERE id=?");
		for (int id = 1; id <= maxId; id++) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			FactorList fl = new FactorList(id);
			fl.addAll(processSelectResult(rs));
			whole.add(fl);
		}
		return whole;
	}

	public List<FactorSet> selectAll() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM factor");

		return processSelectResult(rs);
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
		return fsl;
	}
}
