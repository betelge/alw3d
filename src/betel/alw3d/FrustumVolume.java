package betel.alw3d;

import betel.alw3d.math.Vector3f;

public class FrustumVolume extends Volume {
	
	// Inward pointing normals
	public Vector3f[] normals;
	// Intersection plane distance from origo
	public float[] distances;
	
	public FrustumVolume(Vector3f[] normals, float[] distances) {
		assert(normals.length == distances.length);
		
		this.normals = normals;
		this.distances = distances;
		
		for(Vector3f normal : normals) {
			normal.normalizeThis();
		}
	}

	Vector3f relPosition = new Vector3f();
	@Override
	public boolean isCollidedWith(Volume volume) {
		relPosition.set(volume.getAbsoluteTransform().getPosition());
		relPosition.subThis(this.getAbsoluteTransform().getPosition());
		
		if(volume instanceof SphereVolume) {			
			boolean intersecting = true;
			for(int i = 0; i < normals.length; i++) {
				// This can give false positives close to corners
				intersecting = intersecting && normals[i].dot(relPosition) - distances[i] + ((SphereVolume)volume).getRadius() > 0;
			}
			return intersecting;
		}
		else {
			boolean intersecting = true;
			for(int i = 0; i < normals.length; i++) {
				intersecting = intersecting && normals[i].dot(relPosition) - distances[i] > 0;
			}
			return intersecting;
		}
	}

	@Override
	public boolean isCollidedWith(Volume volume, Vector3f point) {
		// TODO Auto-generated method stub
		return false;
	}

}
