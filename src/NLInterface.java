import java.sql.*;

/**
 * What is NL?
 * <ul>
 * <li>テキストファイルから読み込み＆処理</li>
 * <li>SQLデータベースに読み込んだデータを書き込む</li>
 * </ul>
 * @author tailriver
 */
interface NLInterface {
	/**
	 * ファイルからデータを読み込む
	 * @param filename 読み込むファイル
	 * @throws ParserException
	 */
	public void load(String filename) throws ParserException;

	/**
	 * データベースにテーブルを作成する<br>
	 * {@code DROP TABLE IF EXISTS}も含まれることが望ましい
	 * @param conn {@link Connection}
	 * @throws SQLException
	 */
	public void create(Connection conn) throws SQLException;

	/**
	 * データベースにデータを書き込む
	 * @param conn {@link Connection}
	 * @throws SQLException
	 */
	public void save(Connection conn) throws SQLException;
}
