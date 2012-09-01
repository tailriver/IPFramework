package net.tailriver.nl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import net.tailriver.nl.util.TaskIncompleteException;
import net.tailriver.nl.util.Util;

public class Task implements TaskTarget {
	Deque<String> q;
	String taskFile;

	@Override
	public void pop(Deque<String> q) {
		this.q = q;
		try {
			taskFile = q.pop();
		} finally {
			Task.printPopLog(getClass(), "< task:", taskFile);
		}
	}

	@Override
	public void run() throws TaskIncompleteException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(taskFile));

			String line;
			Deque<String> buffer = new LinkedList<String>();
			while ((line = br.readLine()) != null)
				buffer.push(line);
			while (!buffer.isEmpty())
				q.addFirst(buffer.pop());
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

	public static String outputFileCheck(String candidate) {
		if (candidate.startsWith(">"))
			return candidate.substring(1);
		throw new NoSuchElementException("output file fail-safe checker detected: " + candidate);
	}

	public static void printPopLog(Class<?> c, String... s) {
		System.out.println("[" + c.getSimpleName() + "] " + Util.join(" ", s));
	}
}
