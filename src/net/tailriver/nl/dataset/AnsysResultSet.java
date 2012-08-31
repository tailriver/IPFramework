package net.tailriver.nl.dataset;
import net.tailriver.nl.id.*;
import net.tailriver.nl.util.Util;

public class AnsysResultSet extends FactorId {
	private NodeId n;
	private final Double sxx, syy, sxy;

	public AnsysResultSet(FactorId fid, NodeId node, Double sxx, Double syy, Double sxy) {
		super(fid);
		this.n = node;
		this.sxx = sxx;
		this.syy = syy;
		this.sxy = sxy;
	}

	public NodeId node() {
		return n;
	}

	public Double sxx() {
		return sxx;
	}

	public Double syy() {
		return syy;
	}

	public Double sxy() {
		return sxy;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("N").append(n.id()).append(" F").append(id()).append(";");
		sb.append(Util.<Double>join(", ", new Double[]{sxx, syy, sxy}));
		return sb.toString();
	}
}
