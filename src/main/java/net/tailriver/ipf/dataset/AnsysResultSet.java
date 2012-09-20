package net.tailriver.ipf.dataset;

import net.tailriver.ipf.id.FactorId;
import net.tailriver.ipf.id.NodeId;
import net.tailriver.ipf.science.Stress;

public class AnsysResultSet {
	final FactorId fid;
	final NodeId nid;
	final Stress stress;

	public AnsysResultSet(FactorId fid, NodeId nid, Stress stress) {
		this.fid = fid;
		this.nid = nid;
		this.stress = stress;
	}

	public final FactorId factor() {
		return fid;
	}

	public final NodeId node() {
		return nid;
	}

	public final Stress stress() {
		return stress;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" + fid + "," + nid + "," + stress + "}";
	}
}
