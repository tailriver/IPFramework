import java.sql.*;

class Model {
	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java Model [dbname] [inputfile]");
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			System.exit(1);
		}

		String dbname    = args[0];
		String inputfile = args[1];

		SQLiteConnector sqlc = new SQLiteConnector(dbname);
		Connection conn = sqlc.getConnection();
		NLDatabaseInput nldi = new ModelParser();
		try {
			// parse
			nldi.parse(inputfile);
			nldi.create(conn);
			nldi.save(conn);

			// TODO output for ANSYS
			// TODO output for gnuplot?
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			sqlc.close();
		}
	}
}
