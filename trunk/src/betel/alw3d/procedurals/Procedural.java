package betel.alw3d.procedurals;

import betel.alw3d.math.Vector3f;

public interface Procedural {
	public double getValue(double x, double y, double z, double resolution);
	public double getValueNormal(double x, double y, double z, double resolution, Vector3f normal);
}
