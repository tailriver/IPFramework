package net.tailriver.nl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

import net.tailriver.nl.dataset.ElementSet;
import net.tailriver.nl.dataset.NodeSet;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.parser.ModelParser;
import net.tailriver.nl.parser.Parser;
import net.tailriver.nl.parser.ParserException;
import net.tailriver.nl.sql.ConstantTable;
import net.tailriver.nl.sql.ElementTable;
import net.tailriver.nl.sql.HistoryTable;
import net.tailriver.nl.sql.NodeTable;
import net.tailriver.nl.sql.SQLiteUtil;
import net.tailriver.nl.util.Util;

public class Model implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String inputfile;
	private String ansysModelFile;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname         = args.remove();
			inputfile      = args.remove();
			ansysModelFile = Task.outputFileCheck( args.remove() );
		} finally {
			Task.printPopLog("DB", dbname);
			Task.printPopLog("< model:   ", inputfile);
			Task.printPopLog("> Ansys model file:", ansysModelFile);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		try {
			conn = SQLiteUtil.getConnection(dbname);

			// parse and save
			Parser p = new ModelParser();
			p.parse(inputfile);
			p.save(conn);

			// save history
			HistoryTable ht = new HistoryTable(conn);
			ht.insert(inputfile);
			conn.commit();

			// for ANSYS
			generateAnsysInput();

			// TODO output for gnuplot?
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			throw new TaskIncompleteException();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new TaskIncompleteException();
		} finally {
			SQLiteUtil.closeConnection(conn);
		}
	}

	private void generateAnsysInput() throws SQLException {
		PrintWriter pw = null;
		try {
			ConstantTable ct = new ConstantTable(conn);
			NodeTable     nt = new NodeTable(conn);
			ElementTable  et = new ElementTable(conn);

			double radius    = ct.select("radius", ConstantTable.DEFAULT_RADIUS);
			double thickness = ct.select("thickness", ConstantTable.DEFAULT_THICKNESS);

			pw = new PrintWriter(new BufferedWriter(new FileWriter(ansysModelFile)));

			// node information
			pw.println("CSYS,1");
			for (NodeSet r : nt.selectAll())
				pw.printf("N,%d,%.4e,%s,%.4e\n",
						r.id(), r.p(0) * radius * 1e-5, r.p(1), r.p(2) * thickness * 1e-3);

			// element information
			pw.println("ET,1,SOLID185");
			for (ElementSet r : et.selectAll())
				pw.printf("EN,%d,%s\n", r.id(), Util.<NodeId>join(",", r.nodes()));

			// constraint information
			pw.println("ALLSEL");
			pw.println("NSEL,S,LOC,Y,0");
			pw.println("NSEL,A,LOC,Y,180");
			pw.println("DSYM,SYMM,Y");
			pw.println("ALLSEL");
			pw.println("NSEL,S,LOC,Z,0");
			pw.println("DSYM,SYMM,Z");
			pw.println("ALLSEL");
			pw.println("NSEL,S,LOC,Y,180");
			pw.printf("NSEL,R,LOC,X,%.4e\n", radius * 1e-3);
			pw.println("D,ALL,UX,0");
		} catch (IOException e) {
			// TODO
		} finally {
			if (pw != null)
				pw.close();
		}
	}
}