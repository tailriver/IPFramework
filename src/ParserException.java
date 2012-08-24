import java.io.*;

/**
 * Exception class for file reading and processing in {@link NLDatabaseInput}.
 * It is an <i>extend</i> class of {@link IOException}. (In fact, IS-A?)
 * @author tailriver
 */
class ParserException extends IOException {
	private static final long serialVersionUID = 1L;

	public ParserException() {
		super();
	}

	public ParserException(String str) {
		super(str);
	}

}
