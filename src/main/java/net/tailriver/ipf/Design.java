package net.tailriver.ipf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

import net.tailriver.ipf.parser.DesignParser;
import net.tailriver.ipf.parser.Parser;
import net.tailriver.ipf.parser.ParserException;
import net.tailriver.ipf.sql.HistoryTable;
import net.tailriver.ipf.sql.SQLiteUtil;
import net.tailriver.java.task.TaskIncompleteException;
import net.tailriver.java.task.TaskTarget;
import net.tailriver.java.task.TaskUtil;

public class Design implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String inputfile;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname    = args.remove();
			inputfile = args.remove();
		} finally {
			TaskUtil.printPopLog("DB", dbname);
			TaskUtil.printPopLog("< design:", inputfile);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		try {
			conn = SQLiteUtil.getConnection(dbname);

			Parser p = new DesignParser();
			p.setParserStackTrace(true);
			p.parse(inputfile);
			p.save(conn);

			HistoryTable ht = new HistoryTable(conn);
			ht.insert(inputfile);
			conn.commit();
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			SQLiteUtil.closeConnection(conn);
		}
	}
}
