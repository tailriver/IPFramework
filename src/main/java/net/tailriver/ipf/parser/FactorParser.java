package net.tailriver.ipf.parser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.Force;
import net.tailriver.ipf.sql.FactorTable;
import net.tailriver.ipf.sql.NodeTable;
import net.tailriver.java.FieldArrayList;
import net.tailriver.java.science.CylindricalPoint;


public class FactorParser extends Parser {
	static final Pattern factorPattern =
			Pattern.compile("([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([XYZ])\\s+([-\\d.]+)");

	protected List<FieldArrayList<FactorParserSet, FactorId>> factors;
	protected boolean isSameIdContext;

	public FactorParser() {
		factors = new ArrayList<>();
		isSameIdContext = false;
	}

	@Override
	protected boolean parseLoopHook(String line) throws Exception {
		if (line.isEmpty()) {
			isSameIdContext = false;
			return true;
		}

		final Matcher commentMatcher = Parser.COMMENT_PATTERN.matcher(line);
		if (commentMatcher.lookingAt()) {
			// 一行だけ変えたいという需要があるかもしれないので isPlaneContext は変更しない
			return true;
		}

		final Matcher factorMatcher = factorPattern.matcher(line);
		if (factorMatcher.lookingAt()) {
			FieldArrayList<FactorParserSet, FactorId> factorSub;
			if (!isSameIdContext) {
				factorSub = new FieldArrayList<>();
				factorSub.set(new FactorId(factors.size() + 1));
				factors.add(factorSub);
			}
			else {
				factorSub = factors.get(factors.size() - 1);
			}

			Double r = Double.valueOf(factorMatcher.group(1));
			Double t = Double.valueOf(factorMatcher.group(2));
			Double z = Double.valueOf(factorMatcher.group(3));
			String direction = factorMatcher.group(4).toUpperCase();
			Double value = Double.valueOf(factorMatcher.group(5));

			CylindricalPoint p = new CylindricalPoint(r, t, z, false);
			Force f = new Force(direction, value);
			factorSub.add(new FactorParserSet(p, f));

			isSameIdContext = true;
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
		NodeTable     nt = new NodeTable(conn);
		FactorTable   ft = new FactorTable(conn);

		// テーブルの作成
		ft.drop();
		ft.create();

		for (FieldArrayList<FactorParserSet, FactorId> wof : factors) {
			FactorId fid = wof.get();
			for  (FactorParserSet f : wof) {
				NodeId nid = nt.select(f.p);
				ft.insert(fid, nid, f.f);
			}
		}
		conn.commit();
	}
}


class FactorParserSet {
	final CylindricalPoint p;
	final Force f;
	FactorParserSet(CylindricalPoint p, Force f) {
		this.p = p;
		this.f = f;
	}
}
