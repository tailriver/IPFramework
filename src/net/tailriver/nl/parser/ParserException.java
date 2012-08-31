package net.tailriver.nl.parser;
/**
 * Exception class for file reading and processing in {@link Parser} and derived.
 * @author tailriver
 */
@SuppressWarnings("serial")
public class ParserException extends Exception {
	public ParserException() {
		super();
	}

	public ParserException(String str) {
		super(str);
	}
}
