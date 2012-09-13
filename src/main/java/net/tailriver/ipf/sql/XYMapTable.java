package net.tailriver.ipf.sql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.tailriver.ipf.dataset.XYMapSet;
import net.tailriver.ipf.id.ElementId;
import net.tailriver.java.science.Point;


public class XYMapTable extends Table {
	public XYMapTable(Connection conn) throws SQLException {
		super(conn, "xymap");
		addColumn("x", "REAL");
		addColumn("y", "REAL");
		addColumn("eid", "INTEGER REFERENCES element(id)");
		addTableConstraint("UNIQUE(x,y)");

		SQLiteUtil.createSinFunction(conn);
		SQLiteUtil.createCosFunction(conn);
	}

	public int[] insert(Collection<XYMapSet> data) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?)");
			for (XYMapSet p : data) {
				ElementId eid = p.element();
				ps.setDouble(1, p.x());
				ps.setDouble(2, p.y());
				if (eid != null)
					ps.setInt(3, eid.id());
				else
					ps.setNull(3, Types.INTEGER);
				ps.addBatch();
			}
			return ps.executeBatch();
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	public List<XYMapSet> selectAll() throws SQLException {
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + " ORDER BY x,y ASC");

			List<XYMapSet> rows = new ArrayList<>();
			while (rs.next()) {
				double x = rs.getInt("x");
				double y = rs.getInt("y");
				int e = rs.getInt("eid");

				Point p = new Point(x, y);
				ElementId eid = new ElementId(e != 0 ? e : null);

				rows.add(new XYMapSet(p, eid));
			}
			return rows;			
		}
		finally {
			if (st != null)
				st.close();
		}
	}
}
