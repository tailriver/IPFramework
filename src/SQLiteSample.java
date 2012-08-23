import java.sql.*;

public class SQLiteSample {
	public static void main(String[] args) {
		String dbname = args.length > 0 ? args[0] : ":memory:";
		SQLiteConnector sqlc = new SQLiteConnector(dbname);
		Connection con = sqlc.getConnection();

		try {
			// create (and drop) table
			Statement sth = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.execute("DROP TABLE IF EXISTS test");
			sth.execute("CREATE TABLE test (id INTEGER PRIMARY KEY, value REAL)");

			// insert
			PreparedStatement ps = con.prepareStatement("INSERT INTO test (value) VALUES (?)");
			for (int i = 0; i < 100; i++) {
				ps.setDouble(1, Math.random());
				ps.addBatch();
			}
			ps.executeBatch();

			// select
			ResultSet rs = sth.executeQuery("SELECT * FROM test WHERE value < 0.4");
			while (rs.next())
				System.out.println(rs.getInt("id") + ": " + rs.getDouble("value"));
			System.out.println();

			// update
			//		You may say that you change statement type to UPDATABLE.
			//		However, current version of SQLite JDBC HAS NOT IMPLEMENT THE TYPE!
			rs = sth.executeQuery("SELECT * FROM test WHERE value > 0.7");
			ps = con.prepareStatement("UPDATE test SET value=? WHERE id=?");
			while (rs.next()) {
				ps.setDouble(1, 1.0 + rs.getDouble("value"));
				ps.setInt(2, rs.getInt("id"));
				ps.addBatch();
			}
			ps.executeBatch();

			// delete
			sth.execute("DELETE FROM test WHERE value > 0.4 AND value < 0.7");

			// select all
			rs = sth.executeQuery("SELECT * FROM test");
			while (rs.next())
				System.out.println(rs.getInt("id") + ": " + rs.getDouble("value"));
			System.out.println();

			// select: sort and limit
			rs = sth.executeQuery("SELECT * FROM test ORDER BY value ASC LIMIT 5");
			while (rs.next())
				System.out.println(rs.getInt("id") + ": " + rs.getDouble("value"));			
			System.out.println();

			// select: offset
			rs = sth.executeQuery("SELECT * FROM test ORDER BY value ASC LIMIT 5 OFFSET 3");
			while (rs.next())
				System.out.println(rs.getInt("id") + ": " + rs.getDouble("value"));			

			// commit (save)
			//		If you want to abort above changes, use rollback()
			con.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			sqlc.close();
		}
	}
}
