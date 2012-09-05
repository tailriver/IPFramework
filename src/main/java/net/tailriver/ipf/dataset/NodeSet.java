package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.CylindricalPoint;

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
