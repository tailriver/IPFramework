package net.tailriver.nl.util;

import java.util.Collections;
import java.util.EnumMap;

@SuppressWarnings("serial")
public class Force extends EnumMap<Tensor1, Double> {
	/** Constructs writable object. */
	public Force() {
		super(Tensor1.class);
	}

	/** Constructs Read-only singleton object. */
	public Force(Tensor1 component, Double value) {
		super(Collections.singletonMap(component, value));
	}

	/** Constructs Read-only singleton object. */
	public Force(String component, Double value) {
		this(Tensor1.valueOf(component), value);
	}
}
