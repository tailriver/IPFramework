package net.tailriver.nl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

import net.tailriver.nl.parser.DesignParser;
import net.tailriver.nl.parser.Parser;
import net.tailriver.nl.parser.ParserException;
import net.tailriver.nl.sql.HistoryTable;
import net.tailriver.nl.sql.SQLiteUtil;

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
			Task.printPopLog("DB", dbname);
			Task.printPopLog("< design:", inputfile);
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
