import java.sql.*;

class SampleParserMain {
	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java SampleParserMain [dbname] [inputfile]");
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
		NLDatabaseInput nldi = new SampleParser();
		try {
			nldi.parse(inputfile);
			nldi.create(conn);
			nldi.save(conn);
		} catch (ParserException e) {
			System.err.println("Parse error in SampleParser");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("SQL Error in SampleParser");
			e.printStackTrace();
		} finally {
			sqlc.close();
		}
	}
}
