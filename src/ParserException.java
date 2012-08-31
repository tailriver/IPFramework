/**
 * Exception class for file reading and processing in {@link NLDatabaseInput}.
 * @author tailriver
 */
@SuppressWarnings("serial")
class ParserException extends Exception {
	public ParserException() {
		super();
	}

	public ParserException(String str) {
		super(str);
	}
}
