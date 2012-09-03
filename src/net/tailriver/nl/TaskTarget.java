package net.tailriver.nl;

import java.util.Queue;

/**
 * Interface for the tasks executed by {@link Task#getTask(String)} in {@link Main#main(String[])}}.
 * @author tailriver
 *
 */
public interface TaskTarget {
	public static final String CLASS_TOKEN = "--";

	/**
	 * You have to fetch arguments here to {@link #run()}.
	 * @param args - Arguments passed by {@link Main#main(String[])}.
	 * @see Task#outputFileCheck(String)
	 */
	public void pop(Queue<String> args);

	/**
	 * 
	 * @throws TaskIncompleteException if you want to interrupt this and following tasks.
	 */
	public void run() throws TaskIncompleteException;
}
