package net.tailriver.ipf.sql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tailriver.ipf.dataset.NodeSet;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.java.science.AngleType;
import net.tailriver.java.science.CylindricalPoint;
import net.tailriver.java.science.Point;
import net.tailriver.java.science.Point3D;


public class NodeTable extends Table {
	public NodeTable(Connection conn) throws SQLException {
		super(conn, "node");
		addColumn("num", "INTEGER PRIMARY KEY");
		addColumn("r", "REAL");
		addColumn("t", "REAL");
		addColumn("z", "REAL");
		addTableConstraint("UNIQUE(r,t,z)");

		SQLiteUtil.createSinFunction(conn);
		SQLiteUtil.createCosFunction(conn);
	}

	public int insert(CylindricalPoint p) throws SQLException {
		return insert(Collections.singletonList(p))[0];
	}

	public int[] insert(List<CylindricalPoint> points) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT OR IGNORE INTO " + tableName + " (r,t,z) VALUES (?,?,?)");
			for (CylindricalPoint p : points) {
				ps.setDouble(1, p.r());
				ps.setDouble(2, p.tDegree());
				ps.setDouble(3, p.z());
				ps.addBatch();
			}
			return ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public int maxNodeId() throws SQLException {
		return executeQueryAndGetInt1("SELECT max(num) FROM " + tableName);
	}

	public NodeId select(CylindricalPoint point) throws SQLException {
		return select(Collections.singletonList(point)).get(0);
	}

	public List<NodeId> select(List<CylindricalPoint> points) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT num FROM node WHERE r=? AND t=? AND z=?");
			List<NodeId> nodes = new ArrayList<NodeId>();
			for (CylindricalPoint p : points) {
				ps.setDouble(1, p.r());
				ps.setDouble(2, p.tDegree());
				ps.setDouble(3, p.z());

				ResultSet rs = ps.executeQuery();
				try {
					int n = rs.getInt("num");
					nodes.add(new NodeId(n));
				} catch (SQLException e) {
					System.err.println("node not found: " + p);
					nodes.add(null);
				}				
			}
			return nodes;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public CylindricalPoint selectPoint(NodeId nid) throws SQLException {
		return selectPoint(Collections.singletonList(nid)).get(0);
	}

	public List<CylindricalPoint> selectPoint(List<NodeId> nodes) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE num=?");
			List<CylindricalPoint> points = new ArrayList<>();
			for (NodeId nid : nodes) {
				ps.setInt(1, nid.id());
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					double r = rs.getDouble("r");
					double t = rs.getDouble("t");
					double z = rs.getDouble("z");
					points.add(new CylindricalPoint(r, t, z, AngleType.DEGREE));
				}
			}
			return points;
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public NodeId selectNearest(Point3D point) throws SQLException {
		return selectNearest(Collections.singletonList(point)).get(0);
	}

	public List<NodeId> selectNearest(List<Point3D> points) throws SQLException {
		Map<NodeId, Point> plainNode = new HashMap<>();
		for (NodeSet ns : selectAll()) {
			if (ns.p().z() == 0)
				plainNode.put(ns.node(), ns.p().toPoint());
		}

		List<NodeId> nearests = new ArrayList<>(points.size());
		for (Point3D p : points) {
			if (p.toPolar().r() > 1e2)
				nearests.add(null);
			else {
				NodeId nearestNodeId = null;
				Double nearestDistance = Double.POSITIVE_INFINITY;
				for (Map.Entry<NodeId, Point> e : plainNode.entrySet()) {
					Double distance = p.getDistance(e.getValue());
					if (nearestDistance.compareTo(distance) > 0) {
						nearestNodeId = e.getKey();
						nearestDistance = distance;
					}
				}
				nearests.add(nearestNodeId);
			}
		}
		return nearests;
	}

	public List<NodeSet> selectAll() throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);

			List<NodeSet> rows = new ArrayList<NodeSet>();
			while (rs.next()) {
				int n    = rs.getInt("num");
				double r = rs.getDouble("r");
				double t = rs.getDouble("t");
				double z = rs.getDouble("z");

				NodeId num = new NodeId(n);
				CylindricalPoint p = new CylindricalPoint(r, t, z, AngleType.DEGREE);
				rows.add(new NodeSet(num, p));
			}
			return rows;			
		}
		finally {
			if (st != null)
				st.close();
		}
	}
}
