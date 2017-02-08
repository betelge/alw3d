package tk.betelge.alw3d.math;

public class Matrix4 {
	private float[] floats = new float[16];
	
	public Matrix4(float[] floats) {
		this.floats = floats;
	}
	
	public void setFloats(float[] floats) {
		this.floats = floats;
	}
	
	public float[] getFloats() {
		return floats;
	}
	
	public void setFloat(int index, float value) {
		if(index >= 0 && index < 16)
			floats[index] = value;
	}
	
	public float getFloat(int index) {
		if(index >= 0 && index < 16)
			return floats[index];
		else
			return 0;
	}
	
	public void multThis(Matrix4 m) {
		float[] newF = new float[16];
		float[] argF = m.getFloats();
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				newF[4*i+j] = floats[4*i]*argF[j] + floats[4*i+1]*argF[j+4]
				+ floats[4*i+2]*argF[j+2*4] + floats[4*i+3]*argF[j+3*4];
			}
		}
		
		floats = newF;
	}
	
	public Matrix4 mult(Matrix4 m) {
		Matrix4 newMatrix = new Matrix4(floats);
		newMatrix.multThis(m);
		return newMatrix;
	}

	public Vector3f mult(Vector3f in) {
		Vector3f out = new Vector3f();
		this.mult(in, out);
		return out;
	}

	// TODO: Something seems wrong with this method
	public void mult(Vector3f in, Vector3f out) {
		float x = in.x;
		float y = in.y;
		float z = in.z;

		float tempw = floats[12] * x + floats[13] * y + floats[14] * z + floats[15];

		out.x = (floats[0] * x + floats[1] * y + floats[2] * z + floats[3]) / tempw;
		out.y = (floats[4] * x + floats[5] * y + floats[6] * z + floats[7]) / tempw;
		out.z = (floats[8] * x + floats[9] * y + floats[10] * z + floats[11]) / tempw / tempw; // TODO: Why twice??
	}
}
