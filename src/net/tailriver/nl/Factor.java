package net.tailriver.nl;

import java.io.*;
import java.sql.*;
import java.util.List;

import net.tailriver.nl.dataset.FactorSet;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.parser.FactorParser;
import net.tailriver.nl.parser.ParserException;
import net.tailriver.nl.sql.FactorTable;
import net.tailriver.nl.sql.SQLiteUtil;
import net.tailriver.nl.util.ArrayListWOF;

class Factor {
	Connection conn;
	FactorParser p;

	Factor(Connection conn) {
		this.conn = conn;
		p = new FactorParser();
		p.isPrintStackTrace = true;
	}

	public void parse(String filename) throws ParserException {
		p.parse(filename);
	}

	public void save(Connection conn) throws SQLException {
		p.save(conn);
	}

	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java Factor [dbname] [inputfile]");
	}

	public void generateAnsysInput(String loopfile, String constfile) throws SQLException {
		PrintWriter lpw = null;
		PrintWriter cpw = null;
		try {
			FactorTable et = new FactorTable(conn);
			List<ArrayListWOF<FactorSet, FactorId>> factors = et.selectAllByFactorNum();

			// case information
			lpw = new PrintWriter(new BufferedWriter(new FileWriter(loopfile)));
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
			cpw = new PrintWriter(new BufferedWriter(new FileWriter(constfile)));
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
			Factor m = new Factor(conn);

			// parse
			m.parse(inputfile);
			m.save(conn);

			// for ANSYS
			m.generateAnsysInput("factor.ansys.txt", "factor_max.ansys.txt");

			// TODO output for gnuplot?
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
