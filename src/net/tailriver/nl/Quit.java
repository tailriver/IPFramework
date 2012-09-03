package net.tailriver.nl;

import java.util.Queue;


public class Quit implements TaskTarget {
	@Override
	public void pop(Queue<String> args) {
		args.clear();
	}

	@Override
	public void run() throws TaskIncompleteException {
	}
}
