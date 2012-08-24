import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;


class ModelParser extends AbstractParser {
	static final int PLANE_NODE_SIZE = 4;
	static final Pattern constantPattern  = Pattern.compile("^##\\s*(\\w+):\\s*([\\d.]+).*");
	static final Pattern cyclePattern     = Pattern.compile("^##\\s*([\\d.]+).*");
	static final Pattern commentPattern   = Pattern.compile("^#.*");
	static final Pattern planeNodePattern = Pattern.compile("^([\\d.]+)\\s+([\\d.]+).*");

	class PlaneNode {
		public double r;	// r [%]
		public double t;	// theta [degree]
	}

	class UnitPlane {
		public double cycleDegree;
		public List<PlaneNode> nodes = new ArrayList<PlaneNode>(PLANE_NODE_SIZE);
	}

	Map<String, Double> constantMap = new HashMap<String, Double>();
	List<UnitPlane> unitPlaneList = new ArrayList<UnitPlane>();
	double currentCycleDegree = 360;
	boolean isPlaneContext = false;

	@Override
	protected void parseLoopHook(String line) throws ParserException {
		try {
			if (line.isEmpty()) {
				setIsPlaneContext(false);
				return;
			}

			final Matcher constantMatcher = constantPattern.matcher(line);
			if (constantMatcher.matches()) {
				String key = constantMatcher.group(1).toLowerCase();
				double value = Double.valueOf(constantMatcher.group(2));
				constantMap.put(key, value);
				setIsPlaneContext(false);
				return;
			}

			final Matcher cycleMatcher = cyclePattern.matcher(line);
			if (cycleMatcher.matches()) {
				currentCycleDegree = Double.valueOf(cycleMatcher.group(1));
				setIsPlaneContext(false);
				return;
			}

			final Matcher commentMatcher = commentPattern.matcher(line);
			if (commentMatcher.matches()) {
				// should not change the state of isPlaneContext
				return;
			}

			final Matcher planeNodeMatcher = planeNodePattern.matcher(line);
			if (planeNodeMatcher.matches()) {
				UnitPlane currentUnitPlane;
				if (!isPlaneContext) {
					currentUnitPlane = new UnitPlane();
					currentUnitPlane.cycleDegree = currentCycleDegree;
					unitPlaneList.add(currentUnitPlane);
				}
				else {
					// fetch from last of the array
					currentUnitPlane = unitPlaneList.get(unitPlaneList.size() - 1);
				}

				PlaneNode pn = new PlaneNode();
				pn.r = Double.valueOf(planeNodeMatcher.group(1));
				pn.t = Double.valueOf(planeNodeMatcher.group(2));
				currentUnitPlane.nodes.add(pn);

				setIsPlaneContext(true);
				return;
			}

			throw new ParserException("Cannot parse: [" + line + "]");
		} catch (ParserException e) {
			throw e;
		} catch (Exception e) {
			throw new ParserException(e.toString());
		}
	}

	@Override
	protected void parseAfterHook() throws ParserException {
		// TODO expand cycle
	}

	@Override
	public void create(Connection conn) throws SQLException {
		// TODO
	}

	@Override
	public void save(Connection conn) throws SQLException {
		// TODO
	}

	protected void setIsPlaneContext(boolean newState) throws ParserException {
		if (!unitPlaneList.isEmpty() && isPlaneContext) {
			int nodeSize = unitPlaneList.get(unitPlaneList.size() - 1).nodes.size();

			// true -> false (lost context)
			if (!newState && nodeSize < PLANE_NODE_SIZE)
				throw new ParserException("The number of nodes per unit plane element should be four.");

			// true -> true (keep context)
			if (newState && nodeSize > PLANE_NODE_SIZE)
				throw new ParserException("The number of nodes per unit plane element should be four.");
		}
		isPlaneContext = newState;
	}

	@Override
	public String toString() {
		String n = "\n";
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTANT TABLE").append(n);
		for (Entry<String, Double> e : constantMap.entrySet()) {
			sb.append(e.getKey()).append(" : ").append(e.getValue()).append(n);
		}
		sb.append(n);
		sb.append("ELEMENT TABLE").append(n);
		for (UnitPlane up : unitPlaneList) {
			sb.append("Cycle: ").append(up.cycleDegree).append(n);
			for (PlaneNode pn : up.nodes) {
				sb.append("  ").append(pn.r).append(", ").append(pn.t).append(n);
			}
		}
		return sb.toString();
	}
}
