package tk.betelge.alw3d.procedurals;

import tk.betelge.alw3d.math.Vector3f;

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
			double resolution, Vector3f gradient) {
		
		float value1 = (float) procedural1.getValueNormal(x, y, z, resolution, gradient);
		
		float nx = gradient.x;
		float ny = gradient.y;
		float nz = gradient.z;
		
		float value2 = (float) procedural2.getValueNormal(x, y, z, resolution, gradient);
		
		gradient.multThis(weight2);
		gradient.addMultThis(nx, ny, nz, weight1);
		
		return weight1*value1 + weight2*value2;
	}

}
