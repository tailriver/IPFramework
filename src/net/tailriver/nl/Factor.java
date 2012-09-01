package net.tailriver.nl;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.List;

import net.tailriver.nl.dataset.FactorSet;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.parser.FactorParser;
import net.tailriver.nl.parser.Parser;
import net.tailriver.nl.parser.ParserException;
import net.tailriver.nl.sql.FactorTable;
import net.tailriver.nl.sql.SQLiteUtil;
import net.tailriver.nl.util.ArrayListWOF;
import net.tailriver.nl.util.TaskIncompleteException;

public class Factor implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String inputfile;
	private String ansysLoopFile;
	private String ansysConstFile;

	@Override
	public void pop(Deque<String> args) {
		try {
			dbname    = args.pop();
			inputfile = args.pop();
			ansysLoopFile  = Task.outputFileCheck( args.pop() );
			ansysConstFile = Task.outputFileCheck( args.pop() );
		} finally {
			Task.printPopLog(getClass(), "DB", dbname);
			Task.printPopLog(getClass(), "< factor:", inputfile);
			Task.printPopLog(getClass(), "> Ansys loop file:", ansysLoopFile);
			Task.printPopLog(getClass(), "> Ansys const file:", ansysConstFile);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		try {
			conn = SQLiteUtil.getConnection(dbname);

			// parse and save
			Parser p = new FactorParser();
			p.setParserStackTrace(true);
			p.parse(inputfile);
			p.save(conn);

			// for ANSYS
			generateAnsysInput();
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

	private void generateAnsysInput() throws SQLException {
		PrintWriter lpw = null;
		PrintWriter cpw = null;
		try {
			FactorTable et = new FactorTable(conn);
			List<ArrayListWOF<FactorSet, FactorId>> factors = et.selectAllByFactorNum();

			// case information
			lpw = new PrintWriter(new BufferedWriter(new FileWriter(ansysLoopFile)));
			for (ArrayListWOF<FactorSet, FactorId> wof : factors) {
				lpw.println("*IF,%FACTOR_ID%,EQ," + wof.value().id() + ",THEN");
				lpw.println("ALLSEL");
				for (FactorSet fs : wof) {
					lpw.printf("F,%d,F%s,%.5f\n", fs.node().id(), fs.direction(), fs.value());
				}
				lpw.println("*ENDIF");
				lpw.println();
			}

			// constant information
			cpw = new PrintWriter(new BufferedWriter(new FileWriter(ansysConstFile)));
			cpw.println("*SET,FACTOR_ID_MAX," + factors.size());
		} catch (IOException e) {
			// TODO
		} finally {
			if (lpw != null)
				lpw.close();
			if (cpw != null)
				cpw.close();
		}
	}
}
