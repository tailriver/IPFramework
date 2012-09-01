package net.tailriver.nl;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import net.tailriver.nl.util.TaskIncompleteException;

public class Main {
	private static void usage() {
		System.out.println("Usage: (stub)");
	}

	public static void main(String[] args) {
		Deque<String> q = new LinkedList<String>(Arrays.asList(args));

		// default arguments
		if (q.size() == 0) {
			q.add("--Task");
			q.add("task.txt");
		}

		while (!q.isEmpty()) {
			String arg = q.pop();

			// skip if empty line (typical for *.txt) or start with #
			if (arg.isEmpty() || arg.startsWith("#"))
				continue;

			// first argument must be start with double hyphens
			if (!arg.startsWith("--")) {
				usage();
				System.exit(1);
			}

			try {
				TaskTarget t = getTask(arg);
				t.pop(q);
				t.run();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (NoSuchElementException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (TaskIncompleteException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.println("Complete all tasks!");
	}

	private static TaskTarget getTask(String s) throws ClassNotFoundException {
		String c = Main.class.getPackage().getName() + "." + s.substring(2);
		try {
			Object o = Class.forName(c).newInstance();
			if (o instanceof TaskTarget)
				return (TaskTarget)o;
			throw new ClassNotFoundException("Tasks must implements TaskTarget interface.");
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ClassNotFoundException(e.getMessage());
		}
	}
}
