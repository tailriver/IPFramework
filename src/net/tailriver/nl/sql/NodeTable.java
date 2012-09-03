package net.tailriver.nl.sql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.tailriver.nl.dataset.NodeSet;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.Point;
import net.tailriver.nl.util.Point.Coordinate;


public class NodeTable extends Table {
	public static final Coordinate NODE_CSYS = Coordinate.Cylindrical;
	private static final int DIMENSION = NODE_CSYS.getDimension();

	public NodeTable(Connection conn) {
		super(conn, "node");
		addColumn("num", "INTEGER PRIMARY KEY");
		addColumn("r", "REAL");
		addColumn("t", "REAL");
		addColumn("z", "REAL");
		addTableConstraint("UNIQUE(r,t,z)");
	}

	public int insert(Point p) throws SQLException {
		return insert(Collections.singletonList(p))[0];
	}

	public int[] insert(Collection<Point> points) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT OR IGNORE INTO " + tableName + " (r,t,z) VALUES (?,?,?)");
			for (Point p : points) {
				if (!p.hasCoordinate(NODE_CSYS))
					throw new IllegalArgumentException("incompatible coordinate system");

				for (int i = 0; i < DIMENSION; i++)
					ps.setDouble(i+1, p.x(i));
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

	public NodeId select(Point point) throws SQLException {
		return select(Collections.singletonList(point)).get(0);
	}

	public List<NodeId> select(List<Point> points) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT num FROM node WHERE r=? AND t=? AND z=?");
			List<NodeId> nodes = new ArrayList<NodeId>();
			for (Point p : points) {
				if (!p.hasCoordinate(NODE_CSYS))
					throw new IllegalArgumentException("Incompatible coordinate system");

				for (int i = 0; i < DIMENSION; i++)
					ps.setDouble(i+1, p.x(i));

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
				Point p = new Point(Coordinate.Cylindrical, r, t, z);
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
