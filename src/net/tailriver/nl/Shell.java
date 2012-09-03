package net.tailriver.nl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.tailriver.nl.util.Util;


public class Shell implements TaskTarget {
	private Queue<String> q;
	private List<String> commands;

	@Override
	public void pop(Queue<String> args) {
		q = args;
		commands = new ArrayList<String>();
		while (!args.isEmpty() && !args.peek().isEmpty())
			commands.add(args.poll());
	}

	@Override
	public void run() throws TaskIncompleteException {
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectErrorStream(true);
		Map<String, String> env = pb.environment();
		env.put("NL_TASK_LIST", Util.<String>join(",", new ArrayList<String>(q)));
		env.put("NL_TASK_SIZE", String.valueOf(q.size()));

		Process pr = null;
		BufferedReader stdin = null;
		try {
			pr = pb.start();
			stdin = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line;
			while ((line = stdin.readLine()) != null)
				System.out.println(line);

			int exitValue = pr.waitFor();
			if (exitValue != 0)
				System.out.println("Exited with error code " + exitValue);
		} catch (IOException e) {
			e.printStackTrace();
			throw new TaskIncompleteException(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new TaskIncompleteException(e.getMessage());
		} finally {
			if (pr != null)
				pr.destroy();
			try {
				if (stdin != null)
					stdin.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new TaskIncompleteException(e.getMessage());
			}
		}
	}
}
