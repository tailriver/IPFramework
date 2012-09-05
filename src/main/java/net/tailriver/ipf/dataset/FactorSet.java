package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.Force;

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
