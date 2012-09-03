package net.tailriver.nl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tailriver.nl.util.Util;

/**
 * Task file parser and utility methods related execution of tasks.
 * @author tailriver
 */
public class Task implements TaskTarget {
	public static final Pattern variableDefinition =
			Pattern.compile("[!#]\\s*(\\w+)\\s*[:=]\\s*(.*?)\\s*$");
	public static final Pattern variableSubstring  = Pattern.compile("\\$\\((\\w+)\\)");
	public static final Pattern commentDefinition  = Pattern.compile("[!#]");
	public static final Map<String, String> variableTable = new HashMap<String, String>();
	public static final String outputFileCheckerPrefix = ">";
	private Queue<String> q;
	private String taskFile;

	/**
	 * It consumes just one argument.
	 * <ol>
	 * <li>The filename of Task file.</li>
	 * </ol>
	 */
	@Override
	public void pop(Queue<String> q) {
		this.q = q;
		try {
			taskFile = q.remove();
		} finally {
			Task.printPopLog("< task:", taskFile);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(taskFile));

			String line;
			Queue<String> buffer = new LinkedList<String>(q);
			q.clear();
			while ((line = br.readLine()) != null) {
				line.trim();

				// variable definition
				Matcher md = variableDefinition.matcher(line);
				if (md.lookingAt()) {
					variableTable.put(md.group(1), md.group(2));
					continue;
				}

				// comment skip
				Matcher mc = commentDefinition.matcher(line);
				if (mc.lookingAt())
					continue;

				// variable substitution
				Matcher ms = variableSubstring.matcher(line);
				StringBuffer sb = new StringBuffer();
				while (ms.find()) {
					String k = ms.group(1);
					String v = variableTable.containsKey(k) ? variableTable.get(k) : "";
					ms.appendReplacement(sb, v);
				}
				ms.appendTail(sb);
				line = sb.toString();

				q.add(line);
			}
			while (!buffer.isEmpty())
				q.add(buffer.poll());
		} catch (IOException e) {
			throw new TaskIncompleteException(e.getMessage());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				throw new TaskIncompleteException(e.getMessage());
			}
		}
	}

	public static TaskTarget getTask(String className) throws ClassNotFoundException {
		// adds package identifier if argument does not follow package name style
		if (!className.matches("^[A-Z]"))
			className = TaskTarget.class.getPackage().getName() + "." + className;
	
		try {
			Object o = Class.forName(className).newInstance();
			if (o instanceof TaskTarget)
				return (TaskTarget)o;
			throw new ClassNotFoundException("Tasks must implement TaskTarget interface");
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ClassNotFoundException(e.getMessage());
		}
	}

	/**
	 * It helps you from misplaced arguments.
	 * @param filename - The candidate file name you will output.
	 * @return filename (It already removed {@link #outputFileCheckerPrefix}).
	 * @throws NoSuchElementException if the candidate failed the test.
	 */
	public static String outputFileCheck(String filename) throws NoSuchElementException {
		if (filename.startsWith(outputFileCheckerPrefix))
			return filename.substring(outputFileCheckerPrefix.length());
		throw new NoSuchElementException("output file fail-safe checker detected: " + filename);
	}

	/**
	 * It makes you easy to print something to standard output, especially in {@link #pop(Queue)}.
	 * When you call this, you will see white-space concatenated messages with caller class name.
	 * @param s string(s)
	 */
	public static void printPopLog(String... s) {
		System.out.println("[" + Util.getCallerClass(3) + "] " + Util.join(" ", s));
	}
}
