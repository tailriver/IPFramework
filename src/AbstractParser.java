import java.io.*;

/**
 * <h1>{@link NLDatabaseInput}を一部実装した抽象クラス</h1>
 * {@link #parse(String)}中に実行されるフックがいくつかあるので、必要なものを Override して使う。<br>
 * なお、SQLデータベースに関連する以下のメソッドは未実装なので、継承クラスで実装する必要がある。
 * <ul>
 * <li>{@link NLDatabaseInput#create}</li>
 * <li>{@link NLDatabaseInput#save}</li>
 * </ul>
 * @author tailriver
 */
abstract class AbstractParser implements NLDatabaseInput {
	public AbstractParser() {}

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * 1行読み込むごとに呼び出される。<br>
	 * 抽象メソッドなので Override する必要がある。
	 * @param line 読み込まれた文字列
	 * @throws ParserException
	 */
	protected abstract void parseLoopHook(final String line) throws ParserException;

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * ファイルが開かれ、最初の行の読み込みが始まる直前に読み込まれる。<br>
	 * デフォルトは何もしないので、必要であれば Override する。
	 * @throws ParserException
	 */
	protected void parseBeforeHook() throws ParserException {}

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * ファイルの終端まで読み込まれ、ファイルを閉じる直前に読み込まれる。<br>
	 * デフォルトは何もしないので、必要であれば Override する。
	 * @throws ParserException
	 */
	protected void parseAfterHook() throws ParserException {}

	/**
	 * ファイルからの読み込みの実装例。
	 * メソッド中で次のフックを呼び出す。
	 * <ul>
	 *  <li>{@link #parseBeforeHook()}</li>
	 *  <li>{@link #parseLoopHook(String)}</li>
	 *  <li>{@link #parseAfterHook()}</li>
	 * </ul>
	 * @param filename このメソッドで読み込むファイルの名前
	 * @throws ParserException
	 */
	public final void parse(String filename) throws ParserException {
		if (filename == null)
			throw new IllegalArgumentException();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line;

			parseBeforeHook();
			while ((line = br.readLine()) != null)
				parseLoopHook(line);
			parseAfterHook();
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + filename);
			throw new ParserException(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new ParserException(e.toString());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				System.err.println("Fail to close: " + filename);
				e.printStackTrace();
				throw new ParserException();
			}
		}
	}
}
