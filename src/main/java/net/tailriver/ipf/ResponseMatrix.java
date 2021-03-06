package net.tailriver.ipf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Queue;

import net.tailriver.ipf.dataset.DesignSet;
import net.tailriver.ipf.id.DesignId;
import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.sql.DesignTable;
import net.tailriver.ipf.sql.FactorResultTable;
import net.tailriver.ipf.sql.FactorTable;
import net.tailriver.ipf.sql.SQLiteUtil;
import net.tailriver.java.task.TaskIncompleteException;
import net.tailriver.java.task.TaskTarget;
import net.tailriver.java.task.TaskUtil;

public class ResponseMatrix implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String matrixFile;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname = args.remove();
			matrixFile = TaskUtil.outputFileCheck( args.remove() );
		} finally {
			TaskUtil.printPopLog("DB", dbname);
			TaskUtil.printPopLog("> matrix file:", matrixFile);
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
