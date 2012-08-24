import java.sql.*;

/**
 * Interface for NL Database Input
 * <ul>
 * <li>テキストファイルから読み込み＆処理</li>
 * <li>SQLデータベースに読み込んだデータを書き込む</li>
 * </ul>
 * @author tailriver
 */
interface NLDatabaseInput {
	/**
	 * ファイルからデータを読み込む
	 * @param filename 読み込むファイル
	 * @throws ParserException
	 */
	public void parse(String filename) throws ParserException;

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
