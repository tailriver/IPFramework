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
	 * @return processed ({@code true}), or not ({@code false})<br>
	 * 処理できない場合は{@link ParserException}が呼ばれる
	 * @throws Exception re-throw as a {@link ParserException} in {@link #parse(String)}
	 */
	protected abstract boolean parseLoopHook(String line) throws Exception;

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * ファイルが開かれ、最初の行の読み込みが始まる直前に読み込まれる。<br>
	 * デフォルトは何もしないので、必要であれば Override する。
	 * @throws Exception re-throw as a {@link ParserException} in {@link #parse(String)}

	 */
	protected void parseBeforeHook() throws Exception {}

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * ファイルの終端まで読み込まれ、ファイルを閉じる直前に読み込まれる。<br>
	 * デフォルトは何もしないので、必要であれば Override する。
	 * @throws Exception re-throw as a {@link ParserException} in {@link #parse(String)}
	 */
	protected void parseAfterHook() throws Exception {}

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

			String line = "";
			int lineNum = 0;
			try {
				parseBeforeHook();
				while ((line = br.readLine()) != null) {
					lineNum++;
					boolean matched = parseLoopHook(line);
					if (!matched)
						throw new ParserException("not matched to any expressions");
				}
				parseAfterHook();
			} catch (Exception e) {
				String s = e.getMessage() + " in " + filename + " at line " + lineNum + ".\n";
				s += "> " + line;
				throw new ParserException(s);
			}
		} catch (FileNotFoundException e) {
			throw new ParserException(e.toString());
		} catch (IOException e) {
			throw new ParserException(e.toString());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				throw new ParserException("Fail to close " + filename);
			}
		}
	}
}
