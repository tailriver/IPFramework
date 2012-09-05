package net.tailriver.ipf.science;

import net.tailriver.java.science.TensorQuantity;


public class  Stress extends TensorQuantity<OrthogonalTensor2> {
	public Stress() {
		super(OrthogonalTensor2.class);
	}

	public Stress(OrthogonalTensor2 component, Double value) {
		super(component, value);
	}

	public Stress(String component, Double value) {
		this(OrthogonalTensor2.valueOf(component), value);
	}
}
