package betel.alw3d;

import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.Node;

public abstract class Volume extends Node {

	// Returns true if the two volumes overlap.
	abstract public boolean isCollidedWith(Volume volume);
	
	// Returns true if teh two volumes overlap and sets point to the relative collision point.
	abstract public boolean isCollidedWith(Volume volume, Vector3f point);
	
}
