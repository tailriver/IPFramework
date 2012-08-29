import java.io.*;
import java.sql.*;

class Model {
	static final double DEFAULT_RADIUS    = 1;
	static final double DEFAULT_THICKNESS = 1;

	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java Model [dbname] [inputfile]");
	}

	public static void generateAnsysInput(Connection conn, String filename) throws SQLException {
		PrintWriter pw = null;
		try {
			ConstantTable ct = new ConstantTable(conn);
			NodeTable     nt = new NodeTable(conn);
			ElementTable  et = new ElementTable(conn);

			final double radius    = ct.getValue("radius", DEFAULT_RADIUS);
			final double thickness = ct.getValue("thickness", DEFAULT_THICKNESS);

			pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

			// write node information
			pw.println("CSYS,1");
			ResultSet sqlNodes = nt.selectAllNodes();
			while (sqlNodes.next()) {
				int num  = sqlNodes.getInt("num");
				double r = sqlNodes.getDouble("r");
				double t = sqlNodes.getDouble("t");
				double z = sqlNodes.getDouble("z");
				pw.printf("N,%d,%.4e,%s,%.4e\n", num, r * radius * 1e-5, t, z * thickness * 1e-3);
			}

			// write element information
			pw.println("ET,1,SOLID185");
			for (ElementTable.Row r : et.selectAllElements())
				pw.printf("EN,%d,%s\n", r.num, Util.<Integer>join(",", r.nodes));
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

			// for ANSYS
			generateAnsysInput(conn, "model.ansys.txt");

			// TODO output for gnuplot?
		} catch (ParserException e) {
			System.err.println(e.getMessage());
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			sqlc.close();
		}
	}
}
