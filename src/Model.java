import java.io.*;
import java.sql.*;

class Model {
	static final double DEFAULT_RADIUS    = 1;
	static final double DEFAULT_THICKNESS = 1;
	static final double DEFAULT_MAX_CYCLE_DEGREE = 180;

	Connection conn;
	ModelParser p;

	Model(Connection conn) {
		this.conn = conn;
		p = new ModelParser();
	}

	public void parse(String filename) throws ParserException {
		p.parse(filename);
	}

	public void save(Connection conn) throws SQLException {
		p.save(conn);
	}

	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java Model [dbname] [inputfile]");
	}

	public void generateAnsysInput(String filename) throws SQLException {
		PrintWriter pw = null;
		try {
			ConstantTable ct = new ConstantTable(conn);
			NodeTable     nt = new NodeTable(conn);
			ElementTable  et = new ElementTable(conn);

			final double radius    = ct.select("radius", DEFAULT_RADIUS);
			final double thickness = ct.select("thickness", DEFAULT_THICKNESS);

			pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

			// node information
			pw.println("CSYS,1");
			for (NodeTable.Row r : nt.selectAll())
				pw.printf("N,%d,%.4e,%s,%.4e\n",
						r.num,
						r.get(Util.C3D.r) * radius * 1e-5,
						r.get(Util.C3D.t),
						r.get(Util.C3D.z) * thickness * 1e-3
						);

			// element information
			pw.println("ET,1,SOLID185");
			for (ElementTable.Row r : et.selectAll())
				pw.printf("EN,%d,%s\n", r.num, Util.<Integer>join(",", r.nodes));

			// constraint information
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

		Connection conn = null;
		try {
			conn = SQLiteUtil.getConnection(dbname);
			Model m = new Model(conn);

			// parse
			m.parse(inputfile);
			m.save(conn);

			// for ANSYS
			m.generateAnsysInput("model.ansys.txt");

			// TODO output for gnuplot?
		} catch (ParserException e) {
			System.err.println(e.getMessage());
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			SQLiteUtil.closeConnection(conn);
		}
	}
}
