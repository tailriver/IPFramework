package net.tailriver.nl.sql;

import java.sql.*;
import java.util.*;

import net.tailriver.nl.dataset.NodeSet;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.*;
import static net.tailriver.nl.util.Point.*;


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

	public void insert(Point p) throws SQLException {
		insert(new Point[]{p});
	}

	public void insert(Point[] points) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT OR IGNORE INTO " + tableName + " (r,t,z) VALUES (?,?,?)");
			for (Point p : points) {
				if (!p.equals(NODE_CSYS))
					throw new IllegalArgumentException("incompatible coordinate system");

				for (int i = 0; i < DIMENSION; i++)
					ps.setDouble(i+1, p.x(i));
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public int maxNodeId() throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT max(num) FROM " + tableName);
			int max = rs.getInt(1);
			return max;
		} finally {
			if (st != null)
				st.close();
		}
	}

	public NodeId select(Point p) throws SQLException {
		return select(new Point[]{p})[0];
	}

	public NodeId[] select(Point[] p) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT num FROM node WHERE r=? AND t=? AND z=?");
			NodeId[] nodes = new NodeId[p.length];
			for (int i = 0; i < p.length; i++) {
				if (!p[i].equals(NODE_CSYS))
					throw new IllegalArgumentException("Incompatible coordinate system");

				for (int j = 0; j < DIMENSION; j++)
					ps.setDouble(j+1, p[i].x(j));

				ResultSet rs = ps.executeQuery();
				try {
					int n = rs.getInt("num");
					nodes[i] = new NodeId(n);
				} catch (SQLException e) {
					System.err.println("node not found: " + p);
					nodes[i] = null;
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
