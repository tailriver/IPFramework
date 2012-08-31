package net.tailriver.nl.parser;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

import net.tailriver.nl.dataset.*;
import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.sql.*;
import net.tailriver.nl.util.*;
import net.tailriver.nl.util.Point.*;


public class DesignParser extends Parser {
	class DesignParserSet extends DesignSet {
		public final Point p;
		DesignParserSet(DesignId did, Point p, String d, Double v) {
			super(did, null, d, v);
			this.p = p;
		}
	}

	static final Pattern designPattern =
			Pattern.compile("^([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([XYZ]{1,2})\\s+([-\\d.]+).*");

	private String filename;
	protected List<ArrayListWOF<DesignParserSet, Double>> designNodes;
	protected Double currentCycleDegree;
	protected boolean isPackedContext;

	public DesignParser() {
		designNodes = new ArrayList<ArrayListWOF<DesignParserSet, Double>>();
		currentCycleDegree = ConstantTable.DEFAULT_MAX_CYCLE_DEGREE;
		isPackedContext = false;
	}

	@Override
	protected void parseBeforeHook(String filename) throws Exception {
		this.filename = filename;
	}

	@Override
	protected boolean parseLoopHook(String line) throws Exception {
		if (line.isEmpty()) {
			isPackedContext = false;
			return true;
		}

		final Matcher cycleMatcher = Parser.CYCLE_PATTERN.matcher(line);
		if (cycleMatcher.matches()) {
			currentCycleDegree = Double.valueOf(cycleMatcher.group(1));
			isPackedContext = false;
			return true;
		}

		final Matcher commentMatcher = Parser.COMMENT_PATTERN.matcher(line);
		if (commentMatcher.matches()) {
			// 一行だけ変えたいという需要があるかもしれないので isPlaneContext は変更しない
			return true;
		}

		final Matcher factorMatcher = designPattern.matcher(line);
		if (factorMatcher.matches()) {
			ArrayListWOF<DesignParserSet, Double> factorSub;
			if (!isPackedContext) {
				factorSub = new ArrayListWOF<DesignParserSet, Double>(currentCycleDegree);
				designNodes.add(factorSub);
			}
			else {
				factorSub = designNodes.get(designNodes.size() - 1);
			}

			Double r = Double.valueOf(factorMatcher.group(1));
			Double t = Double.valueOf(factorMatcher.group(2));
			Double z = Double.valueOf(factorMatcher.group(3));
			String component = factorMatcher.group(4).toUpperCase();
			Double weight = Double.valueOf(factorMatcher.group(5));

			// normalize abbreviated tensor notation (X to XX, Y to YY, Z to ZZ)
			if (component.length() == 1)
				component += component;

			Point p = new Point(Coordinate.Cylindrical, r, t, z);
			factorSub.add(new DesignParserSet(null, p, component, weight));

			isPackedContext = true;
			return true;
		}

		return false;
	}

	/**
	 * モデル定義をデータベースに保存するメソッド<br>
	 * {@code constant}, {@code factor}テーブルを編集する
	 */
	@Override
	public void save(Connection conn) throws SQLException {
		ConstantTable ct = new ConstantTable(conn);
		NodeTable     nt = new NodeTable(conn);
		DesignTable   dt = new DesignTable(conn);

		// 定数の追加
		ct.insert("AUTO:DESIGN:" + filename, 0d);

		// テーブルの作成
		dt.drop();
		dt.create();

		double maxCycleDegree = ct.select("max_cycle_degree", ConstantTable.DEFAULT_MAX_CYCLE_DEGREE);

		int designIdNum = 1;
		for (ArrayListWOF<DesignParserSet, Double> wof : designNodes) {
			// Note: See condition of the 'for' statement below.
			//       Here is used [<=] sign although it is [<] sign in ModelParser.
			//       ModelParser is for element definition; DesignParser want to get node information
			for (int cycle = 0; cycle <= maxCycleDegree / wof.value(); cycle++) {
				DesignId did = new DesignId(designIdNum);
				for (DesignParserSet dps : wof) {
					double r = dps.p.x(0);
					double t = dps.p.x(1) + cycle * wof.value();
					double z = dps.p.x(2); // TODO relative? absolute?

					Point p = new Point(dps.p.coordinate(), r, t, z);
					NodeId node = nt.select(p);
					dt.insert(did, node, dps);
				}
				designIdNum++;
			}
		}
		conn.commit();
	}
}
