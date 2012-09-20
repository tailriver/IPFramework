package net.tailriver.ipf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import net.tailriver.ipf.dataset.ElementSet;
import net.tailriver.ipf.dataset.NodeSet;
import net.tailriver.ipf.dataset.XYMapSet;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.parser.ModelParser;
import net.tailriver.ipf.parser.Parser;
import net.tailriver.ipf.parser.ParserException;
import net.tailriver.ipf.sql.ConstantTable;
import net.tailriver.ipf.sql.ElementTable;
import net.tailriver.ipf.sql.HistoryTable;
import net.tailriver.ipf.sql.ConstantTableKey;
import net.tailriver.ipf.sql.NodeTable;
import net.tailriver.ipf.sql.SQLiteUtil;
import net.tailriver.ipf.sql.XYMapTable;
import net.tailriver.java.Util;
import net.tailriver.java.science.CylindricalPoint;
import net.tailriver.java.science.Point;
import net.tailriver.java.science.Point3D;
import net.tailriver.java.science.PolarPoint;
import net.tailriver.java.task.TaskIncompleteException;
import net.tailriver.java.task.TaskTarget;
import net.tailriver.java.task.TaskUtil;

public class Model implements TaskTarget {
	public static final int MAP_RESOLUTION = 200;
	private static final double epsilon = 1e-8;
	private Connection conn;
	private String dbname;
	private boolean shouldMakeMap;
	private String inputfile;
	private String ansysModelFile;

	@Override
	public void pop(Queue<String> args) {
		try {
			dbname         = args.remove();
			shouldMakeMap  = Boolean.parseBoolean(args.remove());
			inputfile      = args.remove();
			ansysModelFile = TaskUtil.outputFileCheck( args.remove() );
		} finally {
			TaskUtil.printPopLog("DB", dbname);
			TaskUtil.printPopLog("< model: ", inputfile);
			TaskUtil.printPopLog("> Ansys model file:", ansysModelFile);
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

			if (shouldMakeMap)
				generateXYMap();

			// save history
			HistoryTable ht = new HistoryTable(conn);
			ht.insert(inputfile);
			conn.commit();

			// for ANSYS
			generateAnsysInput();
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

			double radius    = ct.select(ConstantTableKey.RADIUS);
			double thickness = ct.select(ConstantTableKey.THICKNESS);

			pw = new PrintWriter(new BufferedWriter(new FileWriter(ansysModelFile)));

			// node information
			pw.println("CSYS,1");
			for (NodeSet ns : nt.selectAll()) {
				CylindricalPoint p = ns.p();
				double r = p.r() * radius * 1e-5;
				double t = p.tDegree();
				double z = p.z() * thickness * calculateDepth(p) * 1e-3;
				pw.printf("N,%d,%.4e,%s,%.4e\n", ns.node().id(), r, t, z);
			}

			// element information
			pw.println("ET,1,SOLID185");
			for (ElementSet r : et.selectAll())
				pw.printf("EN,%d,%s\n", r.element().id(), Util.join(",", r.nodes()));

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

	private void generateXYMap() throws SQLException {
		NodeTable    nt = new NodeTable(conn);
		ElementTable et = new ElementTable(conn);

		List<Point3D> opoints = new ArrayList<>();
		for (int x = -MAP_RESOLUTION; x <= MAP_RESOLUTION; x++)
			for (int y = 0; y <= MAP_RESOLUTION; y++)
				opoints.add(new Point3D(x, y, 0).scale(1e2 / MAP_RESOLUTION));

		System.out.println("Find nearest nodes...");
		List<NodeId> nearestNodes = nt.selectNearest(opoints);
		List<XYMapSet> xyMap = new ArrayList<>();
		for (int i = 0; i < nearestNodes.size(); i++) {
			Point p0 = opoints.get(i);
			Point mapPoint = p0.clone().scale(1e-2);

			NodeId nearestNid = nearestNodes.get(i);
			if (nearestNid == null) {
				xyMap.add(new XYMapSet(mapPoint, null));
				continue;
			}

			// surrounded Element id from NodeId
			for (ElementSet es : et.select(nearestNid)) {
				List<Point> pi = new ArrayList<>();
				for (CylindricalPoint p : nt.selectPoint(Arrays.asList(es.nodes()))) {
					if (p.z() == 0)
						pi.add(p.toPoint());
				}
				pi.add(pi.get(0));

				// Law of cosines
				double innerAngleTotal = 0;		// in radian
				for (int j = 0; j < pi.size() - 1; j++) {
					double a = pi.get(j).getDistance(pi.get(j+1));
					double b = p0.getDistance(pi.get(j));
					double c = p0.getDistance(pi.get(j+1));
					if (b < epsilon || c < epsilon)
						innerAngleTotal += 2 * Math.PI;
					else if (b+c-a < epsilon)
						innerAngleTotal += Math.PI;
					else
						innerAngleTotal += Math.acos((b*b+c*c-a*a)/(2*b*c));
				}
				if (innerAngleTotal / (2 * Math.PI) > 1 - epsilon) {
					xyMap.add(new XYMapSet(mapPoint, es.element()));
					break;
				}
			}
		}

		XYMapTable mt = new XYMapTable(conn);
		mt.drop();
		mt.create();
		mt.insert(xyMap);
	}

	/**
	 * 高さ計算用メソッド Overrideすることで曲面を作成可能<br>
	 * デフォルトでは、どのような入力に対しても単位高さ (1) を返す
	 * @param r 無次元半径方向座標 [0,100] (%)
	 * @param t 周方向座標 [0,360) (degree)
	 * @return z 無次元軸方向座標
	 */
	private double calculateDepth(PolarPoint p) {
		return 1;
	}
}
