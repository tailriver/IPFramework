import java.sql.*;
import java.util.ArrayList;


class SampleParser extends AbstractParser {
	ArrayList<String>  s;
	ArrayList<Integer> i;

	public SampleParser() {
		s = new ArrayList<String>();
		i = new ArrayList<Integer>();
	}

	@Override
	protected void loadLoopHook(String line) {
		s.add(line);
		i.add(line.length());
	}

	@Override
	public void create(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.addBatch("DROP TABLE IF EXISTS string_count");
		stmt.addBatch("CREATE TABLE string_count" +
				"(" +
				"id INTEGER PRIMARY KEY, " +
				"string STRING, " +
				"count INTEGER" +
				")");
		stmt.executeBatch();
	}

	@Override
	public void save(Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO string_count (string,count) VALUES (?,?)"
				);
		for (int idx = 0; idx < s.size(); idx++) {
			ps.setString(1, s.get(idx));
			ps.setInt(2, i.get(idx));
			ps.addBatch();
		}
		ps.executeBatch();
		conn.commit();
	}
}
