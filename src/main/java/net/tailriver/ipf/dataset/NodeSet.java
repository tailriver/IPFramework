package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.NodeId;
import net.tailriver.java.science.CylindricalPoint;

public class NodeSet {
	final NodeId nid;
	final CylindricalPoint p;

	public NodeSet(NodeId nid, CylindricalPoint p) {
		this.nid = nid;
		this.p = p;
	}

	public NodeId node() {
		return nid;
	}

	public CylindricalPoint p() {
		return p;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + nid + "," + p + "}";
	}
}
