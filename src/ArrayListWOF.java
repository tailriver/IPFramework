import java.util.ArrayList;

/** ArrayList w/ one field. */
@SuppressWarnings("serial")
public class ArrayListWOF<E, T> extends ArrayList<E> {
	private final T v;

	public ArrayListWOF(T v, int capacity) {
		super(capacity);
		this.v = v;
	}

	public ArrayListWOF(T v) {
		super();
		this.v = v;
	}

	public T value() {
		return v;
	}
}
