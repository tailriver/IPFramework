package net.tailriver.ipf.id;

public class FactorId extends Id {
	public FactorId(int id) {
		super(id);
	}

	public FactorId(FactorId fid) {
		super(fid);
	}

	@Override
	public String toString() {
		return "F#" + id;
	}
}
