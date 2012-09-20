package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.Force;

public class FactorSet {
	final FactorId fid;
	final NodeId nid;
	final Force force;

	public FactorSet(FactorId fid, NodeId nid, Force force) {
		this.fid = fid;
		this.nid = nid;
		this.force = force;
	}

	public final FactorId factor() {
		return fid;
	}

	public final NodeId node() {
		return nid;
	}

	public final Force force() {
		return force;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + fid + "," + nid + "," + force + "}";
	}
}
