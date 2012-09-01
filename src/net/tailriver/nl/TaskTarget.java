package net.tailriver.nl;

import java.util.Deque;

import net.tailriver.nl.util.TaskIncompleteException;

public interface TaskTarget {
	public void pop(Deque<String> args);
	public void run() throws TaskIncompleteException;
}
