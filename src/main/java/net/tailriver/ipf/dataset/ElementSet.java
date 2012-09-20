package net.tailriver.ipf.dataset;


import net.tailriver.ipf.id.ElementId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.java.Util;

public class ElementSet {
	final ElementId eid;
	final NodeId[] nodes;

	public ElementSet(ElementId eid, NodeId[] nodes) {
		this.eid = eid;
		this.nodes = nodes;
	}

	public final ElementId element() {
		return eid;
	}

	public final NodeId[] nodes() {
		return nodes;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + eid + "," + Util.join(",", nodes) + "}";
	}
}
