package betel.alw3d.renderer;

import java.util.HashSet;
import java.util.Set;

import betel.alw3d.Volume;
import betel.alw3d.math.Transform;

// TODO: extend with new class DirectNode, that "ignores" transform.

public class Node {
	protected Node parent = null;
	private Set<Node> children = new HashSet<Node>();
	
	private Volume volume = null;

	private Transform transform;
	
	private Transform cachedAbsoluteTransform = new Transform();
	/* TODO: Add isModified to check if cachedAbsoluteTransform
		needs to be updated.
		When isModified is set to true it should be passed down
		to the children.
		When cachedAbsoluteTransform is updated isModified should
		be set to false.
	*/
	// private boolean isModified = true;

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
		// Check if self or parent is root. Ignores root transform.
		if(parent != null && parent.parent != null) {
			cachedAbsoluteTransform.set(parent.getAbsoluteTransform());
			cachedAbsoluteTransform.multThis(getTransform());
			return cachedAbsoluteTransform;
		}
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
		if(parent != null) {
			synchronized (parent) {
				parent.children.remove(this);
			}
		}
		parent = null;
	}
	
	public void setVolume(Volume volume) {
		
		if(volume.parent != null)
			volume.parent.volume = null;
		
		if(this.volume != null)
			this.volume.parent = null;
		
		this.volume = volume;
		volume.parent = this;
	}
	
	public void detachVolume() {
		volume.parent = null;
		volume = null;
	}
	
	public Volume getVolume() {
		return volume;
	}

	public Set<Node> getChildren() {
		return children;
	}
}
