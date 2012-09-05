package net.tailriver.ipf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.tailriver.ipf.dataset.FactorSet;
import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.parser.FactorParser;
import net.tailriver.ipf.parser.Parser;
import net.tailriver.ipf.parser.ParserException;
import net.tailriver.ipf.science.OrthogonalTensor1;
import net.tailriver.ipf.sql.FactorTable;
import net.tailriver.ipf.sql.HistoryTable;
import net.tailriver.ipf.sql.SQLiteUtil;
import net.tailriver.java.FieldArrayList;
import net.tailriver.java.task.TaskIncompleteException;
import net.tailriver.java.task.TaskTarget;
import net.tailriver.java.task.TaskUtil;

public class Factor implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String inputfile;
	private String ansysLoopFile;
	private String ansysConstFile;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname    = args.remove();
			inputfile = args.remove();
			ansysLoopFile  = TaskUtil.outputFileCheck( args.remove() );
			ansysConstFile = TaskUtil.outputFileCheck( args.remove() );
		} finally {
			TaskUtil.printPopLog("DB", dbname);
			TaskUtil.printPopLog("< factor:", inputfile);
			TaskUtil.printPopLog("> Ansys loop file:", ansysLoopFile);
			TaskUtil.printPopLog("> Ansys const file:", ansysConstFile);
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

			// save history
			HistoryTable ht = new HistoryTable(conn);
			ht.insert(inputfile);
			conn.commit();

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
			List<FieldArrayList<FactorSet, FactorId>> factors = et.selectAllByFactorNum();

			// case information
			lpw = new PrintWriter(new BufferedWriter(new FileWriter(ansysLoopFile)));
			for (FieldArrayList<FactorSet, FactorId> wof : factors) {
				lpw.println("*IF,%FACTOR_ID%,EQ," + wof.get().id() + ",THEN");
				lpw.println("ALLSEL");
				for (FactorSet fs : wof) {
					for (Map.Entry<OrthogonalTensor1, Double> f : fs.force().entrySet())
						lpw.printf("F,%d,F%s,%.5f\n", fs.node().id(), f.getKey().name(), f.getValue());
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
