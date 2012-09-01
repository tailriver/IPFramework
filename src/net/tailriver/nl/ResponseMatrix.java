package net.tailriver.nl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Deque;
import java.util.List;

import net.tailriver.nl.dataset.DesignSet;
import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.sql.DesignTable;
import net.tailriver.nl.sql.FactorResultTable;
import net.tailriver.nl.sql.FactorTable;
import net.tailriver.nl.sql.SQLiteUtil;
import net.tailriver.nl.util.TaskIncompleteException;

public class ResponseMatrix implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String matrixFile;

	@Override
	public void pop(Deque<String> args) {
		try {
			dbname = args.pop();
			matrixFile = Task.outputFileCheck( args.pop() );
		} finally {
			Task.printPopLog(getClass(), "DB", dbname);
			Task.printPopLog(getClass(), "> matrix file:", matrixFile);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		try {
			conn = SQLiteUtil.getConnection(dbname);
			generateResponseMatrix();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new TaskIncompleteException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new TaskIncompleteException(e.getMessage());
		} finally {
			SQLiteUtil.closeConnection(conn);
		}
	}

	private void generateResponseMatrix() throws IOException, SQLException {
		DesignTable dt = new DesignTable(conn);
		FactorTable ft = new FactorTable(conn);
		FactorResultTable frt = new FactorResultTable(conn);

		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(matrixFile)));
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
}
