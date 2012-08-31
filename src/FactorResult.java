import java.io.File;
import java.sql.*;

class FactorResult {
	Connection conn;
	AnsysResultParser p;

	FactorResult(Connection conn) {
		this.conn = conn;
		p = new AnsysResultParser();
		p.isPrintStackTrace = true;
	}

	public void run(String dirname) throws ParserException, SQLException {
		FactorTable ft = new FactorTable(conn);
		int max = ft.maxFactorId();

		FactorResultTable frt = new FactorResultTable(conn);
		frt.drop();
		frt.create();

		for (int i = 1; i <= max; i++) {
			File file = new File(dirname + File.separator + i + ".txt");
			p.parse(file.getPath());
			p.save(conn);
		}

		ConstantTable ct = new ConstantTable(conn);
		ct.insert("AUTO:FACTOR_RESULT:" + dirname, 0d);
		conn.commit();
	}

	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java FactorResult [dbname] [inputdir]");
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			System.exit(1);
		}

		String dbname   = args[0];
		String inputdir = args[1];

		Connection conn = null;
		try {
			conn = SQLiteUtil.getConnection(dbname);
			FactorResult m = new FactorResult(conn);

			m.run(inputdir);
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			SQLiteUtil.closeConnection(conn);
		}
	}
}