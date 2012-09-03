package net.tailriver.nl.util;

import java.util.Collections;
import java.util.EnumMap;

@SuppressWarnings("serial")
public class Stress extends EnumMap<Tensor2, Double> {
	public Stress() {
		super(Tensor2.class);
	}

	public Stress(Tensor2 component, Double value) {
		super(Collections.singletonMap(component, value));
	}

	public Stress(String component, Double value) {
		this(Tensor2.valueOf(component), value);
	}
}
