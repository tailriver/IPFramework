package net.tailriver.nl.util;

import java.util.ArrayList;

/** ArrayList w/ one field. */
@SuppressWarnings("serial")
public class ArrayListWOF<E, T> extends ArrayList<E> {
	private final T v;

	public ArrayListWOF(T v) {
		super();
		this.v = v;
	}

	public T value() {
		return v;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{v=" + v + ";" + super.toString() + "}";
	}
}
