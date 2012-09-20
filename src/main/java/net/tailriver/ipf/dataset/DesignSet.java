package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.DesignId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.Stress;

public class DesignSet {
	final DesignId did;
	final NodeId nid;
	final Stress stress;

	public DesignSet(DesignId did, NodeId nid, Stress stress) {
		this.did = did;
		this.nid = nid;
		this.stress = stress;
	}

	public final DesignId design() {
		return did;
	}

	public final NodeId node() {
		return nid;
	}

	public final Stress stress() {
		return stress;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + did + "," + nid + "," + stress + "}";
	}
}
