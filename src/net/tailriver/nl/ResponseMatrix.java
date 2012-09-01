package net.tailriver.nl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;

import net.tailriver.nl.dataset.DesignSet;
import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.sql.DesignTable;
import net.tailriver.nl.sql.FactorResultTable;
import net.tailriver.nl.sql.FactorTable;
import net.tailriver.nl.sql.SQLiteUtil;

public class ResponseMatrix {
	Connection conn;

	ResponseMatrix(Connection conn) {
		this.conn = conn;
	}

	public void run(String filename) throws IOException, SQLException {
		DesignTable dt = new DesignTable(conn);
		FactorTable ft = new FactorTable(conn);
		FactorResultTable frt = new FactorResultTable(conn);

		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		int nRow = dt.maxDesignId();
		int nCol = ft.maxFactorId();
		for (int r = 1; r <= nRow; r++) {
			DesignId did = new DesignId(r);
			for (int c = 1; c <= nCol; c++) {
				FactorId fid = new FactorId(c);
				List<DesignSet> dsl = dt.select(did);

				// (r,c) component of matrix
				double rc = frt.getMatrixValue(fid, dsl);
				pw.printf("%.8E\t", rc);
			}
			pw.println();

			// progress
			if ((int)(10d*r/nRow) > (int)(10d*(r-1)/nRow))
				System.out.println((int)(100d*r/nRow) + "% ...");
		}
		pw.close();
	}

	private static void usage() {
		System.err.println("Required just one arguments");
		System.err.println("Usage:");
		System.err.println("	java ResponseMatrix [dbname]");
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			usage();
			System.exit(1);
		}

		String dbname    = args[0];

		Connection conn = null;
		try {
			conn = SQLiteUtil.getConnection(dbname);
			ResponseMatrix m = new ResponseMatrix(conn);

			m.run("responsematrix.txt");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			SQLiteUtil.closeConnection(conn);
		}
	}
}
