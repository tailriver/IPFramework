package net.tailriver.nl.science;

import net.tailriver.java.science.TensorQuantity;

public class CylindricalPoint extends TensorQuantity<CylindricalTensor1> {
	public CylindricalPoint () {
		super(CylindricalTensor1.class);
	}

	public CylindricalPoint(Double r, Double t, Double z) {
		this();
		put(CylindricalTensor1.R, r);
		put(CylindricalTensor1.T, t);
		put(CylindricalTensor1.Z, z);
	}
}
