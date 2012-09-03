package net.tailriver.nl.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	public boolean create() throws SQLException {
		Iterator<String> it = columnDefs.keySet().iterator();
		List<String> scheme = new ArrayList<String>();
		while (it.hasNext()) {
			String key = it.next();
			scheme.add(key + " " + columnDefs.get(key));
		}
		scheme.addAll(constraints);

		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
		sb.append(tableName).append(" (").append(Util.join(", ", scheme)).append(")");
		return execute(sb.toString());
	}

	public boolean drop() throws SQLException {
		return execute("DROP TABLE IF EXISTS " + tableName);
	}

	protected String addColumn(String name, String constraint) {
		return columnDefs.put(name, constraint);
	}

	protected boolean addTableConstraint(String constraint) {
		return constraints.add(constraint);
	}

	public boolean execute(String sql) throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			return st.execute(sql);
		} finally {
			if (st != null)
				st.close();
		}
	}

	public int executeQueryAndGetInt1(String sql) throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			return rs.getInt(1);
		} finally {
			if (st != null)
				st.close();
		}
	}
}
