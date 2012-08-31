package net.tailriver.nl.id;


public abstract class Id {
	private final int id;

	public Id(int id) {
		this.id = id;
	}

	public Id(Id idn) {
		this.id = idn != null ? idn.id : -1;
	}

	public int id() {
		return id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Id)
			return id == ((Id) obj).id;
		return false;
	}
}
