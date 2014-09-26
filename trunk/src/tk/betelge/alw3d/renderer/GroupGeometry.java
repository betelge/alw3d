package tk.betelge.alw3d.renderer;

import java.nio.ShortBuffer;
import java.util.List;

public class GroupGeometry extends UpdatableGeometry {
	
	public boolean needsUpdate = true;
	
	static private int size;
	static private Attribute positions;
	
	// Actual count of indexes
	private int count;
	
	public GroupGeometry() {
		super(ShortBuffer.allocate(size*size*6), null);
	}

	public GroupGeometry(ShortBuffer indices, List<Attribute> attributes) {
		super(indices, attributes);
	}

	public static void setSize(int size) {
		GroupGeometry.size = size;
	}

	public static int getSize() {
		return size;
	}

	public static void setPositions(Attribute positions) {
		GroupGeometry.positions = positions;
	}

	public static Attribute getPositions() {
		return positions;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	@Override
	public int getMaxCount() {
		return size*size*6;
	}

}
