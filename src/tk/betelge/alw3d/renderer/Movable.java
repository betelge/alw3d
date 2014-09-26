package tk.betelge.alw3d.renderer;

import tk.betelge.alw3d.math.Transform;

public interface Movable {
	
	public Transform getMovement();

	public void setMovement(Transform movement);

	public Transform getTransform();

	public void setTransform(Transform transform);
		
	public Transform getNextTransform();
	
	public long getLastTime();
	
	public void setLastTime(long time);
	
	public long getNextTime();
	
	public void setNextTime(long time);
}
