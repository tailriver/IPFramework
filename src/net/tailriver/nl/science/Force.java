package net.tailriver.nl.science;

import net.tailriver.java.science.TensorQuantity;


public class Force extends TensorQuantity<OrthogonalTensor1> {
	/** Constructs writable object. */
	public Force() {
		super(OrthogonalTensor1.class);
	}

	/** Constructs Read-only singleton object. */
	public Force(OrthogonalTensor1 component, Double value) {
		super(component, value);
	}

	/** Constructs Read-only singleton object. */
	public Force(String component, Double value) {
		this(OrthogonalTensor1.valueOf(component), value);
	}
}
