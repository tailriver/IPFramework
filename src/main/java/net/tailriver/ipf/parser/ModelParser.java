package net.tailriver.ipf.parser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.sql.ConstantTable;
import net.tailriver.ipf.sql.ConstantTableKey;
import net.tailriver.ipf.sql.ElementTable;
import net.tailriver.ipf.sql.NodeTable;
import net.tailriver.java.FieldArrayList;
import net.tailriver.java.science.AngleType;
import net.tailriver.java.science.CylindricalPoint;
import net.tailriver.java.science.PolarPoint;


public class ModelParser extends Parser {
	protected static final Pattern planeNodePattern = Pattern.compile("([\\d.]+)\\s+([\\d.]+)");

	protected Map<ConstantTableKey, Double> constantMap;
	protected List<FieldArrayList<PolarPoint, Double>> unitPlaneList;
	protected double currentCycleDegree;
	private boolean isPlaneContext;

	public ModelParser() {
		constantMap = new HashMap<>();
		unitPlaneList = new ArrayList<>();
		currentCycleDegree = ConstantTable.getDefaultValue(ConstantTableKey.MAX_CYCLE_DEGREE);
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
			String keyString = constantMatcher.group(1).toUpperCase();
			ConstantTableKey key = ConstantTableKey.valueOf(keyString);
			Double value = Double.valueOf(constantMatcher.group(2));
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
			FieldArrayList<PolarPoint, Double> currentUnitPlane;
			if (!isPlaneContext) {
				// 頂点定義の文脈でなければ新しく要素を作り、それを対象とする
				currentUnitPlane = new FieldArrayList<>();
				currentUnitPlane.set(currentCycleDegree);
				unitPlaneList.add(currentUnitPlane);
			}
			else {
				// 頂点定義の文脈であれば（前の行でも頂点を定義しているなら）最後の要素が対象
				currentUnitPlane = unitPlaneList.get(unitPlaneList.size() - 1);
			}

			Double r = Double.valueOf(planeNodeMatcher.group(1));
			Double t = Double.valueOf(planeNodeMatcher.group(2));
			PolarPoint p = new PolarPoint(r, t, AngleType.DEGREE);
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
		double maxCycleDegree = ct.select(ConstantTableKey.MAX_CYCLE_DEGREE);

		for (FieldArrayList<PolarPoint, Double> wof : unitPlaneList) {
			for (int cycle = 0; cycle < maxCycleDegree / wof.get(); cycle++) {
				NodeId[] elementNodes = new NodeId[ElementTable.ELEMENT_LABELS.length];

				int i = 0;
				for (PolarPoint op : wof) {
					PolarPoint temp = op.clone();
					temp.rotate(cycle * wof.get(), AngleType.DEGREE);

					List<CylindricalPoint> points = new ArrayList<CylindricalPoint>();
					points.add(new CylindricalPoint(temp, 0));
					points.add(new CylindricalPoint(temp, 1));
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
}
