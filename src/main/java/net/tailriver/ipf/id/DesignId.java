package net.tailriver.ipf.id;

public class DesignId extends Id {
	public DesignId(int id) {
		super(id);
	}

	public DesignId(DesignId did) {
		super(did);
	}

	@Override
	public String toString() {
		return "D#" + id;
	}
}
