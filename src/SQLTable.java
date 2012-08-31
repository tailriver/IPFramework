import java.sql.*;
import java.util.*;

public class SQLTable {
	protected Connection conn;
	protected String tableName;
	private Map<String, String> columns;
	private List<String> properties;

	public SQLTable(Connection conn, String tableName) {
		this.conn = conn;
		this.tableName = tableName;
		columns = new LinkedHashMap<String, String>();
		properties = new ArrayList<String>();
	}

	public void create() throws SQLException {
		Iterator<String> it = columns.keySet().iterator();
		List<String> scheme = new ArrayList<String>();
		while (it.hasNext()) {
			String key = it.next();
			scheme.add(key + " " + columns.get(key));
		}
		scheme.addAll(properties);

		StringBuilder sb = new StringBuilder("CREATE TABLE ");
		sb.append(tableName).append(" (").append(Util.join(", ", scheme)).append(")");
		execute(scheme.toString());
	}

	public void drop() throws SQLException {
		execute("DROP TABLE IF EXISTS " + tableName);
	}

	protected void addColumn(String key, String value) {
		columns.put(key, value);
	}

	protected void addProperty(String property) {
		properties.add(property);
	}

	protected boolean execute(String sql) throws SQLException {
		return conn.createStatement().execute(sql);
	}
}
