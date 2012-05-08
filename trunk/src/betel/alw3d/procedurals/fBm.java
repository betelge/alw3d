package betel.alw3d.procedurals;

import betel.alw3d.math.Noise;
import betel.alw3d.math.Vector3f;


public class fBm implements Procedural {
	Noise noise;
	int maxOctaves = 32;
	
	public fBm(long seed) {
		noise = new Noise(seed);
	}

	@Override
	public double getValue(double x, double y, double z, double resolution) {
		return getValueNormal(x, y, z, resolution, null);
	}

	@Override
	public double getValueNormal(double x, double y, double z,
			double resolution, Vector3f gradient) {
		if(maxOctaves < 1f/resolution)
			resolution = 1f/maxOctaves;
		
		gradient.set(0, 0, 0);
		
		Vector3f gradientAcc = new Vector3f();
		
		float value = 0;
		
		for(int i = 1; i < 1f/resolution; i *= 2) {
			value += 1f/i * noise.getValueNormal(i*x, i*y, i*z, resolution, gradientAcc);
			gradient.addMultThis(gradientAcc, 1f/i);
		}
		
		return value;
	}

}
