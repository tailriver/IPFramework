public class Id<T extends Identifiable> {
	private final int id;

	public Id(int id) {
		this.id = id;
	}

	public Id(Id<T> idn) {
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
		if (obj instanceof Id<?>)
			return id == ((Id<?>) obj).id;
		return false;
	}

	public boolean equals(Id<T> obj) {
		return id == obj.id;
	}
}
