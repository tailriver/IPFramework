import java.sql.*;
import java.util.*;
import java.util.regex.*;


public class FactorParser extends Parser {
	class FactorParserSet extends FactorSet {
		public final Point p;
		FactorParserSet(Point p, String d, Double v) {
			super(d, v);
			this.p = p;
		}
	}

	static final Pattern factorPattern =
			Pattern.compile("^([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([XYZ])\\s+([-\\d.]+).*");

	private String filename;
	protected List<List<FactorParserSet>> factors;
	protected boolean isSameIdContext;

	FactorParser() {
		factors = new ArrayList<List<FactorParserSet>>();
		isSameIdContext = false;
	}

	@Override
	protected boolean parseLoopHook(String line) throws Exception {
		if (line.isEmpty()) {
			isSameIdContext = false;
			return true;
		}

		final Matcher commentMatcher = Util.COMMENT_PATTERN.matcher(line);
		if (commentMatcher.matches()) {
			// 一行だけ変えたいという需要があるかもしれないので isPlaneContext は変更しない
			return true;
		}

		final Matcher factorMatcher = factorPattern.matcher(line);
		if (factorMatcher.matches()) {
			List<FactorParserSet> currentUnitPlane;
			if (!isSameIdContext) {
				// 頂点定義の文脈でなければ新しく要素を作り、それを対象とする
				currentUnitPlane = new ArrayList<FactorParserSet>();
				factors.add(currentUnitPlane);
			}
			else {
				// 頂点定義の文脈であれば（前の行でも頂点を定義しているなら）最後の要素が対象
				currentUnitPlane = factors.get(factors.size() - 1);
			}

			Double r = Double.valueOf(factorMatcher.group(1));
			Double t = Double.valueOf(factorMatcher.group(2));
			Double z = Double.valueOf(factorMatcher.group(3));
			String direction = factorMatcher.group(4).toUpperCase();
			Double value = Double.valueOf(factorMatcher.group(5));

			Point p = new Point(Coordinate.Cylindrical, r, t, z);
			currentUnitPlane.add(new FactorParserSet(p, direction, value));

			isSameIdContext = true;
			return true;
		}

		return false;
	}

	@Override
	protected void parseAfterHook(String filename) throws Exception {
		this.filename = filename;
	}

	/**
	 * モデル定義をデータベースに保存するメソッド<br>
	 * {@code constant}, {@code factor}テーブルを編集する
	 */
	@Override
	public void save(Connection conn) throws SQLException {
		ConstantTable ct = new ConstantTable(conn);
		NodeTable     nt = new NodeTable(conn);
		FactorTable   ft = new FactorTable(conn);

		// 定数の追加
		ct.insert("FACTOR=" + filename, 0d);

		// テーブルの作成
		ft.create();

		Iterator<List<FactorParserSet>> fi = factors.iterator();
		for (int id = 1; fi.hasNext(); id++) {
			for  (FactorParserSet f : fi.next()) {
				Id<FactorTable> num = new Id<FactorTable>(id);
				Id<NodeTable> node = nt.select(f.p);
				ft.insert(num, node, f);
			}
		}
		conn.commit();
	}

	/**
	 * ファイルをきちんと読み込めているか<br>
	 * {@link #parse(String)}後に呼び出すと、定数と頂点の定義を一覧表示する
	 */
	@Override
	public String toString() {
		String n = "\n";
		String t = "\t";
		StringBuilder sb = new StringBuilder();
		sb.append("Factor Parser").append(n);
		for (List<FactorParserSet> up : factors) {
			for(FactorParserSet f : up) {
				sb.append(f.p).append(t).append(f.direction()).append(t).append(f.value()).append(n);
			}
			sb.append(n);
		}
		return sb.toString();
	}
}
