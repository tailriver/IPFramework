import java.io.*;
import java.sql.*;

class SampleParserMain {
	SQLiteConnector sqlc;

	public SampleParserMain(String dbname) {
		sqlc = new SQLiteConnector(dbname);
	}

	public void load(String file) {
		Connection conn = sqlc.getConnection();
		try {
			SampleParser sp = new SampleParser();
			sp.load(file);
			sp.create(conn);
			sp.save(conn);
		} catch (IOException e) {
			System.err.println("Parse error in SampleParser");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("SQL Error in SampleParser");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Required just two arguments.");
			System.err.println("Usage:");
			System.err.println("	java SampleParserMain [dbname] [inputfile]");
			System.exit(1);
		}

		String dbname    = args[0];
		String inputfile = args[1];

		SampleParserMain spm = new SampleParserMain(dbname);
		spm.load(inputfile);
	}
}
