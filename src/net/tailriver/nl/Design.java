package net.tailriver.nl;

import java.sql.*;
import java.util.Deque;

import net.tailriver.nl.parser.DesignParser;
import net.tailriver.nl.parser.Parser;
import net.tailriver.nl.parser.ParserException;
import net.tailriver.nl.sql.SQLiteUtil;
import net.tailriver.nl.util.TaskIncompleteException;

public class Design implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String inputfile;

	@Override
	public void pop(Deque<String> args) {
		try {
			dbname    = args.pop();
			inputfile = args.pop();
		} finally {
			Task.printPopLog(getClass(), "DB", dbname);
			Task.printPopLog(getClass(), "< design:", inputfile);
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
