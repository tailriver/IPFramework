package net.tailriver.ipf;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

import net.tailriver.ipf.parser.AnsysResultParser;
import net.tailriver.ipf.parser.Parser;
import net.tailriver.ipf.parser.ParserException;
import net.tailriver.ipf.sql.FactorResultTable;
import net.tailriver.ipf.sql.FactorTable;
import net.tailriver.ipf.sql.HistoryTable;
import net.tailriver.ipf.sql.SQLiteUtil;
import net.tailriver.java.task.TaskIncompleteException;
import net.tailriver.java.task.TaskTarget;
import net.tailriver.java.task.TaskUtil;

public class FactorResult implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String inputdir;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname   = args.remove();
			inputdir = args.remove();
		} finally {
			TaskUtil.printPopLog("DB", dbname);
			TaskUtil.printPopLog("< directory:", inputdir);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		try {
			conn = SQLiteUtil.getConnection(dbname);

			parseDirectory();
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

	private void parseDirectory() throws ParserException, SQLException {
		FactorTable ft = new FactorTable(conn);
		int max = ft.maxFactorId();

		FactorResultTable frt = new FactorResultTable(conn);
		frt.drop();
		frt.create();

		Parser p = new AnsysResultParser();
		p.setParserStackTrace(true);

		for (int i = 1; i <= max; i++) {
			File file = new File(inputdir + File.separator + i + ".txt");
			p.parse(file.getPath());
			p.save(conn);
		}

		HistoryTable ht = new HistoryTable(conn);
		ht.insert(inputdir);
		conn.commit();
	}
}
