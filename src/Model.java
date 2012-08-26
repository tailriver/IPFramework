import java.io.*;
import java.sql.*;
import java.util.*;

class Model {
	private static void usage() {
		System.err.println("Required just two arguments.");
		System.err.println("Usage:");
		System.err.println("	java Model [dbname] [inputfile]");
	}

	public static void generateAnsysInput(Connection conn) throws SQLException {
		PreparedStatement psSelectConstant =
				conn.prepareStatement("SELECT * FROM constant WHERE key=?");

		Map<String, Double> constant = new HashMap<String, Double>();
		for (String key : new String[]{"radius", "thickness"}) {
			psSelectConstant.setString(1, key);
			ResultSet rs = psSelectConstant.executeQuery();
			double value = rs.getDouble("value");
			constant.put(key, value);
		}

		PrintWriter pw = null;
		try {
			Statement st = conn.createStatement();
			pw = new PrintWriter(new BufferedWriter(new FileWriter("model.ansys.txt")));

			// write node information
			pw.println("CSYS,1");
			ResultSet sqlNodes = st.executeQuery("SELECT * FROM node");
			final double radius = constant.get("radius");
			final double thickness = constant.get("thickness");
			while (sqlNodes.next()) {
				int num  = sqlNodes.getInt("num");
				double r = sqlNodes.getDouble("r");
				double t = sqlNodes.getDouble("t");
				double z = sqlNodes.getDouble("z");
				pw.printf("N,%d,%.4e,%s,%.4e\n", num, r * radius * 1e-5, t, z * thickness * 1e-3);
			}

			// write element information
			pw.println("ET,1,SOLID185");
			ResultSet sqlElements = st.executeQuery("SELECT * FROM element");
			final String[] elementLabels = new String[]{"p", "q", "r", "s", "t", "u", "v", "w"};
			while (sqlElements.next()) {
				int num  = sqlNodes.getInt("num");
				Integer[] elementNodes = new Integer[elementLabels.length];
				for (int i = 0; i < elementLabels.length; i++) {
					elementNodes[i] = sqlElements.getInt(elementLabels[i]);
				}
				pw.printf("EN,%d,%s\n", num, Util.<Integer>join(",", elementNodes));
			}
			pw.println("ALLSEL");
			pw.println("NSEL,S,LOC,Y,0");
			pw.println("NSEL,A,LOC,Y,180");
			pw.println("DSYM,SYMM,Y");
			pw.println("ALLSEL");
			pw.println("NSEL,S,LOC,Z,0");
			pw.println("DSYM,SYMM,Z");
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
			generateAnsysInput(conn);

			// TODO output for gnuplot?
		} catch (ParserException e) {
			System.err.println(e.getMessage());
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		} finally {
			sqlc.close();
		}
	}
}
