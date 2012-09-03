package net.tailriver.nl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;


public class Main {
	private static int MAX_QUEUE_SIZE = 2000;

	private static void usage() {
		System.out.println("Usage: (stub)");
	}

	public static void main(String[] args) {
		Queue<String> q = new LinkedList<String>(Arrays.asList(args));

		// default arguments
		if (q.size() == 0) {
			q.add("--Task");
			q.add("task.txt");
		}

		while (!q.isEmpty()) {
			String arg = q.poll();

			// skip if empty line (typical for *.txt)
			if (arg.isEmpty())
				continue;

			// first argument must be start with double hyphens
			if (!arg.startsWith(TaskTarget.CLASS_TOKEN)) {
				usage();
				System.exit(1);
			}

			// stop program when 
			if (q.size() > MAX_QUEUE_SIZE) {
				System.err.println("abort due to too long task queue. (infinite loop?)");
				System.exit(2);
			}

			String taskName = arg.substring(TaskTarget.CLASS_TOKEN.length());
			try {
				TaskTarget t = Task.getTask(taskName);
				t.pop(q);
				t.run();
			} catch (ClassNotFoundException e) {
				// throws in TaskTarget#getTask
				e.printStackTrace();
				System.err.println("fail to get class and/or its instance in " + taskName);
				System.exit(4);
			} catch (NoSuchElementException e) {
				// throws in TaskTarget#pop
				e.printStackTrace();
				System.err.println("fail to fetch argument(s) in " + taskName);
				System.exit(8);
			} catch (TaskIncompleteException e) {
				// throws in TaskTarget#run
				e.printStackTrace();
				System.exit(16);
			}
		}
		System.out.println("Complete all tasks!");
	}
}
