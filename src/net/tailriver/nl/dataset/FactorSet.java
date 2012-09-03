package net.tailriver.nl.dataset;

import net.tailriver.nl.id.FactorId;
import net.tailriver.nl.id.NodeId;

public class FactorSet extends FactorId {
	public enum Direction {X, Y, Z};

	private final NodeId n;
	private final Direction d;
	private final double v;

	public FactorSet(FactorId fid, NodeId nid, String direction, double value) {
		super(fid);
		this.n = nid;
		this.d = Direction.valueOf(direction);
		this.v = value;
	}

	public NodeId node() {
		return n;
	}

	public Direction direction() {
		return d;
	}

	public double value() {
		return v;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@{" +
				super.toString() + "," + n + ",d=" + d + ",v=" + v + "}";
	}
}
