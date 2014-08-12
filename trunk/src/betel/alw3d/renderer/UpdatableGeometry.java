package betel.alw3d.renderer;

import java.nio.ShortBuffer;
import java.util.List;

import android.util.Log;
import betel.alw3d.Alw3d;

public abstract class UpdatableGeometry extends Geometry {
	
	public boolean needsUpdate = true;
	
	// Actual count of indexes
	private int count;
	
	public UpdatableGeometry(PrimitiveType primitiveType, ShortBuffer indices,
			List<Attribute> attributes) {
		super(primitiveType, indices, attributes);
	}
	
	public UpdatableGeometry(ShortBuffer indices,
			List<Attribute> attributes) {
		super(indices, attributes);
	}
	
	/* To be overridden by subclass, returning a subclass specific static constant
	 * 
	 * Example:
	 * 
	 * static int MAX_COUNT = 100;
	 * @Override
	 * public int getMaxCount() {
	 * 		return MAX_COUNT;
	 * }
	 */
	abstract public int getMaxCount();

	public void setCount(int count) {
		Log.w(Alw3d.LOG_TAG, "Count set to: " + count);
		this.count = count;
	}

	@Override
	public int getIndexCount() {
		return count;
	}

}
