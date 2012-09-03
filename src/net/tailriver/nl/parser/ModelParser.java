package net.tailriver.nl.parser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.sql.ConstantTable;
import net.tailriver.nl.sql.ElementTable;
import net.tailriver.nl.sql.NodeTable;
import net.tailriver.nl.util.ArrayListWOF;
import net.tailriver.nl.util.Point;
import net.tailriver.nl.util.Point.Coordinate;
import net.tailriver.nl.util.Util;


public class ModelParser extends Parser {
	protected static final Pattern planeNodePattern = Pattern.compile("([\\d.]+)\\s+([\\d.]+)");

	protected Map<String, Double> constantMap;
	protected List<ArrayListWOF<Point, Double>> unitPlaneList;
	protected double currentCycleDegree;
	private boolean isPlaneContext;

	public ModelParser() {
		constantMap = new HashMap<String, Double>();
		unitPlaneList = new ArrayList<ArrayListWOF<Point, Double>>();
		currentCycleDegree = ConstantTable.DEFAULT_MAX_CYCLE_DEGREE;
		isPlaneContext = false;
	}

	@Override
	protected boolean parseLoopHook(String line) throws Exception {
		if (line.isEmpty()) {
			setIsPlaneContext(false);
			return true;
		}

		final Matcher constantMatcher = Parser.CONSTANT_PATTERN.matcher(line);
		if (constantMatcher.lookingAt()) {
			String key = constantMatcher.group(1).toLowerCase();
			double value = Double.valueOf(constantMatcher.group(2));
			constantMap.put(key, value);
			setIsPlaneContext(false);
			return true;
		}

		final Matcher cycleMatcher = Parser.CYCLE_PATTERN.matcher(line);
		if (cycleMatcher.lookingAt()) {
			currentCycleDegree = Double.valueOf(cycleMatcher.group(1));
			setIsPlaneContext(false);
			return true;
		}

		final Matcher commentMatcher = Parser.COMMENT_PATTERN.matcher(line);
		if (commentMatcher.lookingAt()) {
			// 一行だけ変えたいという需要があるかもしれないので isPlaneContext は変更しない
			return true;
		}

		final Matcher planeNodeMatcher = planeNodePattern.matcher(line);
		if (planeNodeMatcher.lookingAt()) {
			ArrayListWOF<Point, Double> currentUnitPlane;
			if (!isPlaneContext) {
				// 頂点定義の文脈でなければ新しく要素を作り、それを対象とする
				currentUnitPlane = new ArrayListWOF<Point, Double>(currentCycleDegree);
				unitPlaneList.add(currentUnitPlane);
			}
			else {
				// 頂点定義の文脈であれば（前の行でも頂点を定義しているなら）最後の要素が対象
				currentUnitPlane = unitPlaneList.get(unitPlaneList.size() - 1);
			}

			Double r = Double.valueOf(planeNodeMatcher.group(1));
			Double t = Double.valueOf(planeNodeMatcher.group(2));
			Point p = new Point(Coordinate.Polar, r, t);
			currentUnitPlane.add(p);

			setIsPlaneContext(true);
			return true;
		}

		return false;
	}

	/**
	 * モデル定義をデータベースに保存するメソッド<br>
	 * {@code constant}, {@code node}, {@code element}テーブルを編集する
	 */
	@Override
	public void save(Connection conn) throws SQLException {
		ConstantTable ct = new ConstantTable(conn);
		NodeTable     nt = new NodeTable(conn);
		ElementTable  et = new ElementTable(conn);

		// テーブルの作成
		ct.drop();
		nt.drop();
		et.drop();
		ct.create();
		nt.create();
		et.create();

		// 定数の追加
		ct.insert(constantMap);

		// 繰り返し角度の拡張
		double maxCycleDegree = ct.select("max_cycle_degree", ConstantTable.DEFAULT_MAX_CYCLE_DEGREE);

		for (ArrayListWOF<Point, Double> wof : unitPlaneList) {
			for (int cycle = 0; cycle < maxCycleDegree / wof.value(); cycle++) {
				NodeId[] elementNodes = new NodeId[ElementTable.ELEMENT_LABELS.length];

				int i = 0;
				for (Point p : wof) {
					double r = p.x(0);
					double t = p.x(1) + cycle * wof.value();
					double z = calculateDepth(r, t);

					List<Point> points = new ArrayList<Point>();
					points.add(new Point(Coordinate.Cylindrical, r, t, 0));
					points.add(new Point(Coordinate.Cylindrical, r, t, z));
					nt.insert(points);

					List<NodeId> nodes = nt.select(points);
					elementNodes[i]   = nodes.get(0);
					elementNodes[i+4] = nodes.get(1);
					i++;
				}
				et.insert(elementNodes);
			}
		}
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
			final int planeNodeSize = ElementTable.ELEMENT_LABELS.length / 2;
			int nodeSize = unitPlaneList.get(unitPlaneList.size() - 1).size();

			// true -> false (lost context)
			if (!newState && nodeSize < planeNodeSize)
				throw new ParserException("The number of nodes in a element must be " + planeNodeSize);

			// true -> true (keep context)
			if (newState && nodeSize > planeNodeSize)
				throw new ParserException("The number of nodes in a element must be " + planeNodeSize);
		}
		isPlaneContext = newState;
	}

	/**
	 * 高さ計算用メソッド Overrideすることで曲面を作成可能<br>
	 * デフォルトでは、どのような入力に対しても単位高さ (1) を返す
	 * @param r 無次元半径方向座標 [0,100] (%)
	 * @param t 周方向座標 [0,360) (degree)
	 * @return z 無次元軸方向座標
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
		for (ArrayListWOF<Point, Double> wof : unitPlaneList) {
			sb.append("Cycle: ").append(wof.value()).append(n);
			for (Point p : wof) {
				sb.append("  ").append(Util.<Double>join(", ", p.x())).append(n);
			}
		}
		return sb.toString();
	}
}
