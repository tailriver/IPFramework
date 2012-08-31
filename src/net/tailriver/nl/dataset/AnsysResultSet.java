package net.tailriver.nl.dataset;
import java.util.EnumMap;

import net.tailriver.nl.dataset.DesignSet.Component;
import net.tailriver.nl.id.*;
import net.tailriver.nl.util.Util;

public class AnsysResultSet extends FactorId {
	private NodeId n;
	private final EnumMap<Component, Double> s;

	public AnsysResultSet(FactorId fid, NodeId node, Double sxx, Double syy, Double sxy) {
		super(fid);
		this.n = node;
		this.s = new EnumMap<Component, Double>(Component.class);
		s.put(Component.XX, sxx);
		s.put(Component.YY, syy);
		s.put(Component.XY, sxy);
	}

	public NodeId node() {
		return n;
	}

	public Double s(Component c) {
		if (!s.containsKey(c))
			throw new IllegalArgumentException("unsupported component");
		return s.get(c);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("N").append(n.id()).append(" F").append(id()).append(";");
		sb.append(Util.<Double>join(", ", (Double[])s.values().toArray()));
		return sb.toString();
	}
}
