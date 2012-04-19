package betel.alw3d;

import betel.alw3d.math.Vector3f;

public class SphereVolume extends Volume {
	
	private float radius = 0;
	
	public SphereVolume(float radius) {
		this.radius = radius;
	}
	
	@Override
	public boolean isCollidedWith(Volume volume) {
		if (volume == null)
			return false;
		
		// TODO: Take scaling into account.
		
		if(volume instanceof SphereVolume) {
			SphereVolume sphere = (SphereVolume) volume;
			
			float distance = this.getAbsoluteTransform().getPosition().getDistance(
					sphere.getAbsoluteTransform().getPosition());
			
			return (distance <= radius + sphere.radius);
		}
		else {
			float distance = this.getAbsoluteTransform().getPosition().getDistance(
					volume.getAbsoluteTransform().getPosition());
			
			return (distance <= radius);
		}
	}

	@Override
	public boolean isCollidedWith(Volume volume, Vector3f point) {
		if(isCollidedWith(volume)) {
			if(volume instanceof SphereVolume) {
				Vector3f thisPos = this.getAbsoluteTransform().getPosition();
				Vector3f spherePos = volume.getAbsoluteTransform().getPosition();
				float ratio = radius / (radius + ((SphereVolume)volume).radius);
				
				point.x += ratio*(spherePos.x - thisPos.x);
				point.y += ratio*(spherePos.y - thisPos.y);
				point.z += ratio*(spherePos.z - thisPos.z);
			}
			else {
				point.set(volume.getTransform().getPosition());
			}
			
			return true;
		}
		else return false;
	}

	public float getRadius() {
		return radius;
	}
	
	public void setRadius(float radius) {
		this.radius = radius;
	}
}
