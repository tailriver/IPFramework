/**
 * Exception class for file reading and processing in {@link NLDatabaseInput}.
 * @author tailriver
 */
class ParserException extends Exception {
	private static final long serialVersionUID = 1L;

	public ParserException() {
		super();
	}

	public ParserException(String str) {
		super(str);
	}
}
