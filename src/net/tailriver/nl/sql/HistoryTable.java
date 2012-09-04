package net.tailriver.nl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import net.tailriver.java.Util;

public class HistoryTable extends Table {
	public HistoryTable(Connection conn) {
		super(conn, "history");
		addColumn("date", "TIMESTAMP DEFAULT(DATETIME('now','localtime'))");
		addColumn("class", "TEXT");
		addColumn("history", "TEXT");
	}

	@Override
	public boolean drop() {
		// noop
		return true;
	}

	public int insert(String logMessage) throws SQLException {
		return insert(Collections.singletonList(logMessage))[0];
	}

	public int[] insert(List<String> logMessages) throws SQLException {
		create();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " (class,history) VALUES (?,?)");
			ps.setString(1, Util.getCallerClass(4));
			for (String m : logMessages) {
				ps.setString(2, m);
				ps.addBatch();
			}
			return ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}
}
