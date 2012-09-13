package net.tailriver.ipf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

import net.tailriver.ipf.dataset.DesignSet;
import net.tailriver.ipf.parser.DesignParser;
import net.tailriver.ipf.parser.Parser;
import net.tailriver.ipf.parser.ParserException;
import net.tailriver.ipf.science.OrthogonalTensor2;
import net.tailriver.ipf.sql.DesignTable;
import net.tailriver.ipf.sql.HistoryTable;
import net.tailriver.ipf.sql.NodeTable;
import net.tailriver.ipf.sql.SQLiteUtil;
import net.tailriver.java.science.CylindricalPoint;
import net.tailriver.java.task.TaskIncompleteException;
import net.tailriver.java.task.TaskTarget;
import net.tailriver.java.task.TaskUtil;

public class Design implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String inputfile;
	private String sigmaDesignFile;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname    = args.remove();
			inputfile = args.remove();
			sigmaDesignFile = TaskUtil.outputFileCheck( args.remove() );
		} finally {
			TaskUtil.printPopLog("DB", dbname);
			TaskUtil.printPopLog("< design:", inputfile);
			TaskUtil.printPopLog("> sigmadesign:", sigmaDesignFile);
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

			HistoryTable ht = new HistoryTable(conn);
			ht.insert(inputfile);
			conn.commit();

			generateSigmaDesign();
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			throw new TaskIncompleteException();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			throw new TaskIncompleteException();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			throw new TaskIncompleteException();
		} finally {
			SQLiteUtil.closeConnection(conn);
		}
	}

	private void generateSigmaDesign() throws IOException, SQLException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(sigmaDesignFile)));

			DesignTable dt = new DesignTable(conn);
			NodeTable   nt = new NodeTable(conn);

			for (DesignSet ds : dt.selectAll()) {
				CylindricalPoint p = nt.selectPoint(ds.node());
				double r = p.r() / 100;
				double t = p.tDegree();

				if (p.z() == 0)
					continue;

				if (ds.stress().containsKey(OrthogonalTensor2.XX))
					pw.println( -SigmaDesign.getSigmaNDiff(r, t) );
				else if (ds.stress().containsKey(OrthogonalTensor2.XY))
					pw.println( SigmaDesign.getSigmaS(r, t) );
			}
		} finally {
			if (pw != null)
				pw.close();
		}
	}
}

class SigmaDesign {
	private static double Anozzle = 120;
	private static double Aedge = 1.6;
	private static double Aflowx = 2.30;
	private static double Aflowy = 1.85;
	private static double tol = 1e-6;
	private static double C0 = 6e-12;
	private static double thickness = 2e-3;

	public static double getRetardation(double r, double t) {
		if (r > 1)
			throw new IllegalArgumentException("out of circle (r:" + r + ",t:" + t + ")");

		double x = r * Math.cos(Math.toRadians(t));
		double y = r * Math.sin(Math.toRadians(t));
		return Anozzle * pow(1 + pow(y, 2) / (x-1+tol) / (x+1+tol), 2) * pow(2, -3*(x+1d+tol))
				+ pow(Aedge * pow(y, 2) + Aedge * pow(x+0.05, 2), 8)
				+ pow(Aflowx * pow(x+1, 2) + Aflowy * pow(y, 2), 2);
	}

	public static double getFastAxis(double r, double t) {
		double x = r * Math.cos(Math.toRadians(t));
		double y = r * Math.sin(Math.toRadians(t));

		double dflowx = Aflowy * (x+1);
		double dflowy = Aflowx * y;

		return Math.atan2(dflowy, dflowx);
	}

	public static double getSigmaNDiff(double r, double t) {
		double s1_s2 = getRetardation(r, t) * 1e-9 / C0 / thickness;
		double phi = getFastAxis(r, t);
		return s1_s2 * Math.cos(2 * phi);
	}

	public static double getSigmaS(double r, double t) {
		double s1_s2 = getRetardation(r, t) * 1e-9 / C0 / thickness;
		double phi = getFastAxis(r, t);
		return .5 * s1_s2 * Math.sin(2 * phi);		
	}

	private static double pow(double a, double b) {
		return Math.pow(a, b);
	}
}
