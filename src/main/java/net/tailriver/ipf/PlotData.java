package net.tailriver.ipf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import net.tailriver.ipf.dataset.DesignSet;
import net.tailriver.ipf.dataset.ElementSet;
import net.tailriver.ipf.id.DesignId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.sql.ConstantTable;
import net.tailriver.ipf.sql.DesignTable;
import net.tailriver.ipf.sql.ElementTable;
import net.tailriver.ipf.sql.NodeTable;
import net.tailriver.ipf.sql.SQLiteUtil;
import net.tailriver.java.science.Point3D;
import net.tailriver.java.task.TaskIncompleteException;
import net.tailriver.java.task.TaskTarget;
import net.tailriver.java.task.TaskUtil;

public class PlotData implements TaskTarget {
	private Connection conn;
	private String dbname;
	private String deltaFile;
	private String plotDataFile;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname = args.remove();
			deltaFile = args.remove();
			plotDataFile = TaskUtil.outputFileCheck( args.remove() );
		} finally {
			TaskUtil.printPopLog("DB", dbname);
			TaskUtil.printPopLog("< delta file:", deltaFile);
			TaskUtil.printPopLog("> plot data:", plotDataFile);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		try {
			conn = SQLiteUtil.getConnection(dbname);
			generatePlotData();
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

	private void generatePlotData() throws IOException, SQLException {
		NodeTable     nt = new NodeTable(conn);
		ConstantTable ct = new ConstantTable(conn);
		ElementTable  et = new ElementTable(conn);
		DesignTable   dt = new DesignTable(conn);

		Map<NodeId, Double> deltaMap = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(deltaFile));
		String line = "";
		int designIdNum = 1;
		while ((line = br.readLine()) != null) {
			DesignId did = new DesignId(designIdNum);
			Double delta = Double.valueOf(line);
			for (DesignSet ds : dt.select(did))
				deltaMap.put(ds.node(), delta);
			designIdNum++;
		}
		br.close();

		double R = ct.select("radius", ConstantTable.DEFAULT_RADIUS);

		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(plotDataFile)));
		pw.println("# Database: " + dbname);
		pw.println("# Delta File: " + deltaFile);
		pw.println();
		for (ElementSet es : et.selectAll()) {
			NodeId[] elementNodes = es.nodes();
			for (int i : new int[]{ 4, 5, 6, 7, 4 } ) {
				NodeId nid = elementNodes[i];
				Point3D p = nt.selectPoint(nid).toPoint3D();
				double x = R * p.x() / Model.MAP_AMPLITUDE;
				double y = R * p.y() / Model.MAP_AMPLITUDE;
				Double z = deltaMap.containsKey(nid) ? deltaMap.get(nid) : p.z();
				pw.printf("%.3e\t%.3e\t%.3e", x, y, z);
				pw.println();
			}
			pw.println();
			pw.println();
		}
		pw.close();
	}
}
