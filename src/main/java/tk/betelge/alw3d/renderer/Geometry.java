package tk.betelge.alw3d.renderer;

import java.nio.Buffer;
import java.nio.ShortBuffer;
import java.util.List;

import android.opengl.GLES20;

public class Geometry {
	static public Geometry QUAD = new Geometry(null, null);
	static public Geometry BIG_TRIANGLE = new Geometry(null, null);

	private ShortBuffer indices;
	private List<Attribute> attributes;
	public enum PrimitiveType {
		POINTS(GLES20.GL_POINTS), LINES(GLES20.GL_LINES), LINE_STRIP(GLES20.GL_LINE_STRIP),
		TRIANGLES(GLES20.GL_TRIANGLES), TRIANGLE_STRIP(GLES20.GL_TRIANGLE_STRIP);
		
		int value;
		
		PrimitiveType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	final PrimitiveType primitiveType;
	
	public enum Type {		
		BYTE(GLES20.GL_BYTE), UBYTE(GLES20.GL_UNSIGNED_BYTE), FLOAT(GLES20.GL_FLOAT);
		
		int type;
		
		Type(int type) {
			this.type = type;
		}
		
		public int getType() {
			return type;
		}
	}
	
	static public class Attribute {
		public String name;
		public Type type;
		public int size;
		public Buffer buffer;
		public boolean normalized = false;
	}
	
	public Geometry(PrimitiveType primitiveType, ShortBuffer indices, List<Attribute> attributes) {
		this.primitiveType = primitiveType;
		this.indices = indices;
		this.attributes = attributes;
	}
	
	public Geometry(ShortBuffer indices, List<Attribute> attributes) {
		this(PrimitiveType.TRIANGLES, indices, attributes);
	}
		
	public ShortBuffer getIndices() {
		return indices;
	}
	
	public void setIndices(ShortBuffer indices) {
		this.indices = indices;
	}
	
	public List<Attribute> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public PrimitiveType getPrimitiveType() {
		return primitiveType;
	}

	public int getIndexCount() {
		return indices.limit();
	}
}
