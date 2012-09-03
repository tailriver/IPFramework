package net.tailriver.nl.parser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.sql.FactorTable;
import net.tailriver.nl.sql.NodeTable;
import net.tailriver.nl.util.ArrayListWOF;
import net.tailriver.nl.util.Force;
import net.tailriver.nl.util.Point;
import net.tailriver.nl.util.Point.Coordinate;


public class FactorParser extends Parser {
	static final Pattern factorPattern =
			Pattern.compile("([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([XYZ])\\s+([-\\d.]+)");

	protected List<ArrayListWOF<FactorParserSet, FactorId>> factors;
	protected boolean isSameIdContext;

	public FactorParser() {
		factors = new ArrayList<ArrayListWOF<FactorParserSet,FactorId>>();
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
			ArrayListWOF<FactorParserSet, FactorId> factorSub;
			if (!isSameIdContext) {
				// 頂点定義の文脈でなければ新しく要素を作り、それを対象とする
				FactorId fid = new FactorId(factors.size() + 1);
				factorSub = new ArrayListWOF<FactorParserSet, FactorId>(fid);
				factors.add(factorSub);
			}
			else {
				// 頂点定義の文脈であれば（前の行でも頂点を定義しているなら）最後の要素が対象
				factorSub = factors.get(factors.size() - 1);
			}

			Double r = Double.valueOf(factorMatcher.group(1));
			Double t = Double.valueOf(factorMatcher.group(2));
			Double z = Double.valueOf(factorMatcher.group(3));
			String direction = factorMatcher.group(4).toUpperCase();
			Double value = Double.valueOf(factorMatcher.group(5));

			Point p = new Point(Coordinate.Cylindrical, r, t, z);
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

		for (ArrayListWOF<FactorParserSet, FactorId> wof : factors) {
			FactorId fid = wof.value();
			for  (FactorParserSet f : wof) {
				NodeId nid = nt.select(f.p);
				ft.insert(fid, nid, f.f);
			}
		}
		conn.commit();
	}
}


class FactorParserSet {
	final Point p;
	final Force f;
	FactorParserSet(Point p, Force f) {
		this.p = p;
		this.f = f;
	}
}
