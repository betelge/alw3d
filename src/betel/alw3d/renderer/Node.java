package betel.alw3d.renderer;

import java.util.HashSet;
import java.util.Set;

import betel.alw3d.math.Transform;

// TODO: extend with new class DirectNode, that "ignores" transform.

public class Node {
	private Node parent = null;
	private Set<Node> children = new HashSet<Node>();

	private Transform transform;

	public Node() {
		this(new Transform());
	}

	public Node(Transform transform) {
		this.transform = transform;
	}

	public Transform getTransform() {
		return transform;
	}
	
	public void setTransform(Transform transform) {
		this.transform = transform;
	}
	
	public Transform getAbsoluteTransform(){
		if(parent != null && parent.parent != null) // Check if self or parent is root. Ignores root transform.
			return parent.getAbsoluteTransform().mult(getTransform());
		else
			return getTransform();
	}

	public void attach(Node node) {
		synchronized (this) {
			children.add(node);
			node.parent = this;
		}
	}

	public void detachFromParent() {
		parent.children.remove(this);
		parent = null;
	}

	public Set<Node> getChildren() {
		return children;
	}
}
