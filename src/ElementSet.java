public class ElementSet extends Id<ElementTable> {
	private final Id<NodeTable>[] nodes;

	public ElementSet(Id<ElementTable> num, Id<NodeTable>[] nodes) {
		super(num);
		this.nodes = nodes;
	}

	public Id<NodeTable>[] nodes() {
		return nodes;
	}
}
