package net.tailriver.nl.dataset;
import net.tailriver.nl.id.*;

public class FactorSet extends FactorId {
	public enum Direction {X, Y, Z};

	private final NodeId n;
	private final Direction d;
	private final double v;

	public FactorSet(FactorId fid, NodeId node, String direction, double value) {
		super(fid);
		this.n = node;
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
}
