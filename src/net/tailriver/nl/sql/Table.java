package net.tailriver.nl.sql;
import java.sql.*;
import java.util.*;

import net.tailriver.nl.util.Util;

class Table {
	protected Connection conn;
	protected String tableName;
	private Map<String, String> columnDefs;
	private List<String> constraints;

	public Table(Connection conn, String tableName) {
		this.conn = conn;
		this.tableName = tableName;
		columnDefs = new LinkedHashMap<String, String>();
		constraints = new ArrayList<String>();
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

	@Deprecated
	public void setDebugMode(boolean b) {
	}

	protected void addColumn(String name, String constraint) {
		columnDefs.put(name, constraint);
	}

	protected void addTableConstraint(String constraint) {
		constraints.add(constraint);
	}

	protected boolean execute(String sql) throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			return st.execute(sql);
		} finally {
			if (st != null)
				st.close();
		}
	}
}
