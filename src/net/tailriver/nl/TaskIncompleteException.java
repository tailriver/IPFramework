package net.tailriver.nl;

@SuppressWarnings("serial")
public class TaskIncompleteException extends Exception {
	public TaskIncompleteException() {
		super();
	}

	public TaskIncompleteException(String message) {
		super(message);
	}
}
