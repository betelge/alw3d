package betel.alw3d.procedurals;

import betel.alw3d.math.Vector3f;

public class AddProcedural implements Procedural {
	
	private Procedural procedural1, procedural2;
	private float weight1, weight2;
	
	public AddProcedural(Procedural procedural1, Procedural procedural2, float weight1, float weight2) {
		this.procedural1 = procedural1;
		this.procedural2 = procedural2;
		this.weight1 = weight1;
		this.weight2 = weight2;
	}
	
	public AddProcedural(Procedural procedural1, Procedural procedural2) {
		this(procedural1, procedural2, 1f, 1f);
	}

	@Override
	public double getValue(double x, double y, double z, double resolution) {
		return getValueNormal(x, y, z, resolution, null);
	}

	@Override
	public double getValueNormal(double x, double y, double z,
			double resolution, Vector3f normal) {
		
		float value1 = (float) procedural1.getValueNormal(x, y, z, resolution, normal);
		
		float nx = normal.x;
		float ny = normal.y;
		float nz = normal.z;
		
		float value2 = (float) procedural2.getValueNormal(x, y, z, resolution, normal);
		
		normal.multThis(weight2);
		normal.addMultThis(nx, ny, nz, weight1);
		normal.normalizeThis();
		
		return weight1*value1 + weight2*value2;
	}

}
