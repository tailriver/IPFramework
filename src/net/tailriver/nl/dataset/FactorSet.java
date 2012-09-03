package net.tailriver.nl.dataset;

import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;
import net.tailriver.nl.util.Force;

public class FactorSet extends FactorId {
	private final NodeId nid;
	private final Force force;

	public FactorSet(FactorId fid, NodeId nid, Force force) {
		super(fid);
		this.nid = nid;
		this.force = force;
	}

	public NodeId node() {
		return nid;
	}

	public Force force() {
		return force;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" +
				super.toString() + "," + nid + "," + force + "}";
	}
}
