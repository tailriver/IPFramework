package net.tailriver.nl.parser;

import java.io.*;
import java.sql.*;
import java.util.regex.Pattern;

/**
 * {@link #parse(String)}中に実行されるフックがいくつかあるので、必要なものを Override して使う。<br>
 * なお、以下のメソッドは未実装なので、継承クラスで実装する必要がある。
 * <ul>
 * <li>{@link #save}</li>
 * </ul>
 * @author tailriver
 */
public abstract class Parser {
	protected static final Pattern CONSTANT_PATTERN = Pattern.compile("^##\\s*(\\w+):\\s*([\\d.]+).*");
	protected static final Pattern CYCLE_PATTERN    = Pattern.compile("^##\\s*([\\d.]+).*");
	protected static final Pattern COMMENT_PATTERN  = Pattern.compile("^#.*");
	public boolean isPrintStackTrace = false;

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * 1行読み込むごとに呼び出される。<br>
	 * 抽象メソッドなので Override する必要がある。
	 * @param line 読み込まれた文字列
	 * @return processed ({@code true}), or not ({@code false})<br>
	 * 処理できない場合は{@link ParserException}が呼ばれる
	 * @throws Exception re-throw as a {@link ParserException} in {@link #parse(String)}
	 */
	abstract protected boolean parseLoopHook(String line) throws Exception;

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * ファイルが開かれ、最初の行の読み込みが始まる直前に読み込まれる。<br>
	 * デフォルトは何もしないので、必要であれば Override する。
	 * @throws Exception re-throw as a {@link ParserException} in {@link #parse(String)}

	 */
	protected void parseBeforeHook(String filename) throws Exception {}

	/**
	 * {@link #parse(String)}で定義されたフックの一つ。
	 * ファイルの終端まで読み込まれ、ファイルを閉じる直前に読み込まれる。<br>
	 * デフォルトは読み込んだファイルを表示する程度なので、必要であれば Override する。
	 * @throws Exception re-throw as a {@link ParserException} in {@link #parse(String)}
	 */
	protected void parseAfterHook(String filename) throws Exception {
		System.out.println(this.getClass().getName() + ": " + filename);
	}

	/**
	 * ファイルからの読み込みの実装例。
	 * メソッド中で次のフックを呼び出す。
	 * <ul>
	 *  <li>{@link #parseBeforeHook(String)}</li>
	 *  <li>{@link #parseLoopHook(String)}</li>
	 *  <li>{@link #parseAfterHook(String)}</li>
	 * </ul>
	 * @param filename このメソッドで読み込むファイルの名前
	 * @throws ParserException
	 */
	@SuppressWarnings("javadoc")
	public final void parse(String filename) throws ParserException {
		if (filename == null)
			throw new IllegalArgumentException();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));

			String line = "";
			int lineNum = 0;
			try {
				parseBeforeHook(filename);
				while ((line = br.readLine()) != null) {
					lineNum++;
					boolean matched = parseLoopHook(line);
					if (!matched)
						throw new ParserException("not matched to any expressions");
				}
				parseAfterHook(filename);
			} catch (Exception e) {
				if (isPrintStackTrace)
					e.printStackTrace();

				String s = e.getMessage() + " in " + filename + " at line " + lineNum + ".\n";
				s += "> " + line;
				throw new ParserException(s);
			}
		} catch (FileNotFoundException e) {
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

	abstract public void save(Connection conn) throws SQLException;
}
