import java.sql.*;
import java.util.*;
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
	protected boolean parseLoopHook(String line) throws Exception {
		if (line.isEmpty()) {
			setIsPlaneContext(false);
			return true;
		}

		final Matcher constantMatcher = constantPattern.matcher(line);
		if (constantMatcher.matches()) {
			String key = constantMatcher.group(1).toLowerCase();
			double value = Double.valueOf(constantMatcher.group(2));
			constantMap.put(key, value);
			setIsPlaneContext(false);
			return true;
		}

		final Matcher cycleMatcher = cyclePattern.matcher(line);
		if (cycleMatcher.matches()) {
			currentCycleDegree = Double.valueOf(cycleMatcher.group(1));
			setIsPlaneContext(false);
			return true;
		}

		final Matcher commentMatcher = commentPattern.matcher(line);
		if (commentMatcher.matches()) {
			// 一行だけ変えたいという需要があるかもしれないので isPlaneContext は変更しない
			return true;
		}

		final Matcher planeNodeMatcher = planeNodePattern.matcher(line);
		if (planeNodeMatcher.matches()) {
			UnitPlane currentUnitPlane;
			if (!isPlaneContext) {
				// 頂点定義の文脈でなければ新しく要素を作り、それを対象とする
				currentUnitPlane = new UnitPlane();
				currentUnitPlane.cycleDegree = currentCycleDegree;
				unitPlaneList.add(currentUnitPlane);
			}
			else {
				// 頂点定義の文脈であれば（前の行でも頂点を定義しているなら）最後の要素が対象
				currentUnitPlane = unitPlaneList.get(unitPlaneList.size() - 1);
			}

			PlaneNode pn = new PlaneNode();
			pn.r = Double.valueOf(planeNodeMatcher.group(1));
			pn.t = Double.valueOf(planeNodeMatcher.group(2));
			currentUnitPlane.nodes.add(pn);

			setIsPlaneContext(true);
			return true;
		}

		return false;
	}

	@Override
	public void create(Connection conn) throws SQLException {
		Statement s = conn.createStatement();
		s.addBatch("DROP TABLE IF EXISTS constant");
		s.addBatch("DROP TABLE IF EXISTS node");
		s.addBatch("DROP TABLE IF EXISTS element");

		// TODO constantテーブルはModelParserから分離した方がよい？
		s.addBatch("CREATE TABLE constant (" +
				"key STRING PRIMARY KEY," +
				"value REAL" +
				")");
		s.addBatch("CREATE TABLE node (" +
				"num INTEGER PRIMARY KEY," +
				"r REAL," +
				"t REAL," +
				"z REAL," +
				"UNIQUE(r, t, z)" +
				")");
		s.addBatch("CREATE TABLE element (" +
				"num INTEGER PRIMARY KEY," +
				"p REFERENCES node," +
				"q REFERENCES node," +
				"r REFERENCES node," +
				"s REFERENCES node," +
				"t REFERENCES node," +
				"u REFERENCES node," +
				"v REFERENCES node," +
				"w REFERENCES node" +
				")");
		s.executeBatch();
	}

	/**
	 * モデル定義をデータベースに保存するメソッド<br>
	 * {@code constant}, {@code node}, {@code element}テーブルを編集する
	 */
	@Override
	public void save(Connection conn) throws SQLException {
		PreparedStatement psInsertConstant =
				conn.prepareStatement("INSERT INTO constant (key,value) VALUES (?,?)");
		PreparedStatement psInsertNode =
				conn.prepareStatement("INSERT OR IGNORE INTO node (r,t,z) VALUES (?,?,?)");
		PreparedStatement psSelectNode =
				conn.prepareStatement("SELECT num FROM node WHERE r=? AND t=? AND z=?");
		PreparedStatement psInsertElement =
				conn.prepareStatement("INSERT INTO element (p,q,r,s,t,u,v,w) VALUES (?,?,?,?,?,?,?,?)");

		// 定数の追加
		for (Map.Entry<String, Double> e : constantMap.entrySet()) {
			psInsertConstant.setString(1, e.getKey());
			psInsertConstant.setDouble(2, e.getValue());
			psInsertConstant.addBatch();
		}
		psInsertConstant.executeBatch();

		// 繰り返し角度の拡張
		double maxCycleDegree =
				constantMap.containsKey("max_cycle_degree") ? constantMap.get("max_cycle_degree") : 180;

				for (UnitPlane up : unitPlaneList) {
					for (int cycle = 0; cycle < maxCycleDegree / up.cycleDegree; cycle++) {
						List<Integer> lowerNodes = new ArrayList<Integer>();
						List<Integer> upperNodes = new ArrayList<Integer>();

						for (PlaneNode pn : up.nodes) {
							double r = pn.r;
							double t = pn.t + cycle * up.cycleDegree;
							psInsertNode.setDouble(1, r);
							psInsertNode.setDouble(2, t);
							psInsertNode.setDouble(3, 0);
							psInsertNode.execute();

							// TODO lastrowid をどこかで使えるはず
							psSelectNode.setDouble(1, r);
							psSelectNode.setDouble(2, t);
							psSelectNode.setDouble(3, 0);
							ResultSet rsLower = psSelectNode.executeQuery();
							lowerNodes.add(rsLower.getInt(1));

							double z = calculateDepth(r, t);
							psInsertNode.setDouble(1, r);
							psInsertNode.setDouble(2, t);
							psInsertNode.setDouble(3, z);
							psInsertNode.execute();

							psSelectNode.setDouble(1, r);
							psSelectNode.setDouble(2, t);
							psSelectNode.setDouble(3, z);
							ResultSet rsUpper = psSelectNode.executeQuery();
							upperNodes.add(rsUpper.getInt(1));
						}

						for (int i = 0; i < PLANE_NODE_SIZE; i++) {
							psInsertElement.setInt(i+1, lowerNodes.get(i));					
							psInsertElement.setInt(i+5, upperNodes.get(i));					
						}
						psInsertElement.addBatch();
					}
				}
				psInsertElement.executeBatch();
				conn.commit();
	}

	/**
	 * 要素の頂点定義中にパースエラーをチェックするメソッド（内部用）<br>
	 * isPlaneContextの値は直接変更せずに、このメソッドで変更しよう
	 * @param newState
	 * @throws ParserException
	 */
	protected void setIsPlaneContext(boolean newState) throws ParserException {
		if (!unitPlaneList.isEmpty() && isPlaneContext) {
			int nodeSize = unitPlaneList.get(unitPlaneList.size() - 1).nodes.size();

			// true -> false (lost context)
			if (!newState && nodeSize < PLANE_NODE_SIZE)
				throw new ParserException("The number of nodes in a element must be " + PLANE_NODE_SIZE);

			// true -> true (keep context)
			if (newState && nodeSize > PLANE_NODE_SIZE)
				throw new ParserException("The number of nodes in a element must be " + PLANE_NODE_SIZE);
		}
		isPlaneContext = newState;
	}

	/**
	 * 高さ計算用メソッド Overrideすることで曲面を作成可能<br>
	 * デフォルトでは、どのような入力でも単位高さ (1) を返す
	 * @param r 半径方向座標 [%] （無次元）
	 * @param t 周方向座標 [degree]
	 * @return z 軸方向座標（無次元）
	 */
	protected double calculateDepth(double r, double t) {
		return 1;
	}

	/**
	 * ファイルをきちんと読み込めているか<br>
	 * {@link #parse(String)}後に呼び出すと、定数と頂点の定義を一覧表示する
	 */
	@Override
	public String toString() {
		String n = "\n";
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTANT TABLE").append(n);
		for (Map.Entry<String, Double> e : constantMap.entrySet()) {
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
