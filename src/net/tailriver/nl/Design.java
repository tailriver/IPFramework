package net.tailriver.nl;

import java.sql.*;

import net.tailriver.nl.parser.DesignParser;
import net.tailriver.nl.parser.ParserException;
import net.tailriver.nl.sql.SQLiteUtil;

public class Design {
	Connection conn;
	DesignParser p;

	Design(Connection conn) {
		this.conn = conn;
		p = new DesignParser();
		p.setParserStackTrace(true);
	}

	public void run(String filename) throws ParserException, SQLException {
		p.parse(filename);
		p.save(conn);
	}

	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java Design [dbname] [inputfile]");
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			System.exit(1);
		}

		String dbname    = args[0];
		String inputfile = args[1];

		Connection conn = null;
		try {
			conn = SQLiteUtil.getConnection(dbname);
			Design m = new Design(conn);

			m.run(inputfile);
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
