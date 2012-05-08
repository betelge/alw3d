package betel.alw3d.procedurals;

import betel.alw3d.math.Vector3f;

public class MultProcedural implements Procedural {
	
	private Procedural procedural1, procedural2;
	
	public MultProcedural(Procedural procedural1, Procedural procedural2) {
		this.procedural1 = procedural1;
		this.procedural2 = procedural2;
	}

	@Override
	public double getValue(double x, double y, double z, double resolution) {
		return getValueNormal(x, y, z, resolution, null);
	}

	@Override
	public double getValueNormal(double x, double y, double z,
			double resolution, Vector3f gradient) {
		
		float value1 = (float) procedural1.getValueNormal(x, y, z, resolution, gradient);
		
		float nx = gradient.x;
		float ny = gradient.y;
		float nz = gradient.z;
		
		float value2 = (float) procedural2.getValueNormal(x, y, z, resolution, gradient);
		
		gradient.multThis(value1);
		gradient.addMultThis(nx, ny, nz, value2);
		
		return value1 * value2;
	}

}
