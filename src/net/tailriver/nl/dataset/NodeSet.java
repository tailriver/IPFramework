package net.tailriver.nl.dataset;

import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.science.CylindricalPoint;

public class NodeSet extends NodeId {
	private final CylindricalPoint p;

	public NodeSet(NodeId nid, CylindricalPoint p) {
		super(nid);
		this.p = p;
	}

	public CylindricalPoint p() {
		return p;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + super.toString() + "," + p + "}";
	}
}
