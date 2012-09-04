package net.tailriver.nl.parser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tailriver.java.FieldArrayList;
import net.tailriver.nl.id.DesignId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.science.CylindricalPoint;
import net.tailriver.nl.science.CylindricalTensor1;
import net.tailriver.nl.science.Stress;
import net.tailriver.nl.sql.ConstantTable;
import net.tailriver.nl.sql.DesignTable;
import net.tailriver.nl.sql.NodeTable;


public class DesignParser extends Parser {
	static final Pattern designPattern =
			Pattern.compile("([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([XYZ]{1,2})\\s+([-\\d.]+)");

	protected List<FieldArrayList<DesignParserSet, Double>> designNodes;
	protected Double currentCycleDegree;
	protected boolean isPackedContext;

	public DesignParser() {
		designNodes = new ArrayList<>();
		currentCycleDegree = ConstantTable.DEFAULT_MAX_CYCLE_DEGREE;
		isPackedContext = false;
	}

	@Override
	protected boolean parseLoopHook(String line) throws Exception {
		if (line.isEmpty()) {
			isPackedContext = false;
			return true;
		}

		final Matcher cycleMatcher = Parser.CYCLE_PATTERN.matcher(line);
		if (cycleMatcher.lookingAt()) {
			currentCycleDegree = Double.valueOf(cycleMatcher.group(1));
			isPackedContext = false;
			return true;
		}

		final Matcher commentMatcher = Parser.COMMENT_PATTERN.matcher(line);
		if (commentMatcher.lookingAt()) {
			// 一行だけ変えたいという需要があるかもしれないので isPlaneContext は変更しない
			return true;
		}

		final Matcher factorMatcher = designPattern.matcher(line);
		if (factorMatcher.lookingAt()) {
			FieldArrayList<DesignParserSet, Double> factorSub;
			if (!isPackedContext) {
				factorSub = new FieldArrayList<>();
				factorSub.set(currentCycleDegree);
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

			CylindricalPoint p = new CylindricalPoint(r, t, z);
			Stress s = new Stress(component, weight);
			factorSub.add(new DesignParserSet(p, s));

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

		// テーブルの作成
		dt.drop();
		dt.create();

		double maxCycleDegree = ct.select("max_cycle_degree", ConstantTable.DEFAULT_MAX_CYCLE_DEGREE);

		int designIdNum = 1;
		for (FieldArrayList<DesignParserSet, Double> wof : designNodes) {
			if (wof.get() == maxCycleDegree)
				System.err.println("Warning: cycle_degree is equals to its maximum (really expected?)");

			// Note: See condition of the 'for' statement below.
			//       Here is used [<=] sign although it is [<] sign in ModelParser.
			//       ModelParser is for element definition; DesignParser want to get node information
			for (int cycle = 0; cycle <= maxCycleDegree / wof.get(); cycle++) {
				DesignId did = new DesignId(designIdNum);
				for (DesignParserSet dps : wof) {
					double r = dps.p.get(CylindricalTensor1.R);
					double t = dps.p.get(CylindricalTensor1.T) + cycle * wof.get();
					double z = dps.p.get(CylindricalTensor1.Z);

					CylindricalPoint p = new CylindricalPoint(r, t, z);
					NodeId node = nt.select(p);
					dt.insert(did, node, dps.s);
				}
				designIdNum++;
			}
		}
		conn.commit();
	}
}


class DesignParserSet {
	final CylindricalPoint p;
	final Stress s;
	DesignParserSet(CylindricalPoint p, Stress s) {
		this.p = p;
		this.s = s;
	}
}
