import java.sql.*;
import java.util.*;

public class SQLTable {
	protected Connection conn;
	protected String tableName;
	private Map<String, String> columnDefs;
	private List<String> constraints;
	protected boolean isDebugMode;

	public SQLTable(Connection conn, String tableName) {
		this.conn = conn;
		this.tableName = tableName;
		columnDefs = new LinkedHashMap<String, String>();
		constraints = new ArrayList<String>();
		isDebugMode = false;
	}

	public void create() throws SQLException {
		Iterator<String> it = columnDefs.keySet().iterator();
		List<String> scheme = new ArrayList<String>();
		while (it.hasNext()) {
			String key = it.next();
			scheme.add(key + " " + columnDefs.get(key));
		}
		scheme.addAll(constraints);

		StringBuilder sb = new StringBuilder("CREATE TABLE ");
		sb.append(tableName).append(" (").append(Util.join(", ", scheme)).append(")");
		execute(sb.toString());
	}

	public void drop() throws SQLException {
		execute("DROP TABLE IF EXISTS " + tableName);
	}

	public void setDebugMode(boolean b) {
		isDebugMode = b;
	}

	protected void addColumn(String name, String constraint) {
		columnDefs.put(name, constraint);
	}

	protected void addTableConstraint(String constraint) {
		constraints.add(constraint);
	}

	protected boolean execute(String sql) throws SQLException {
		if (isDebugMode)
			System.out.println(sql);

		Statement st = conn.createStatement();
		boolean r = st.execute(sql);
		st.close();
		return r;
	}
}
