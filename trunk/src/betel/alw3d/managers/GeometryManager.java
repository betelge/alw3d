package betel.alw3d.managers;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.opengl.GLES20;
import static fix.android.opengl.GLES20.glVertexAttribPointer;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Geometry.Type;

public class GeometryManager {

	//final private RendererMode rendererMode;

	private int indexVBOHandle;
	private int dataVBOHandle;
	private int indexOffset = 0;
	private int dataOffset = 0;
	
	// Direct buffer for communication with OpenGL
	IntBuffer handleBuffer = ByteBuffer.allocateDirect(8).asIntBuffer();

	public class GeometryInfo {
		// Can't use VAO
		//public int VAO = 0;
		
		public int dataVBO, indexVBO;

		public int indexOffset;

		public int count;

		// TODO: fix mode enum
		/*
		 * public enum Mode { TRIANGLES(GL11.GL_TRIANGLES),
		 * QUADS(GL11.GL_QUADS); // ... }
		 */

		public List<AttributeInfo> attributeInfos;
	}
	
	public class AttributeInfo {
		String name;
		public Type type;
		public int size;
		public boolean normalized;
		public int dataOffset;
	}

	Map<Geometry, GeometryInfo> geometryInfos = new HashMap<Geometry, GeometryInfo>();

	public GeometryManager(/*RendererMode rendererMode*/) {
		//this.rendererMode = rendererMode;

		GLES20.glGenBuffers(1, handleBuffer);
		indexVBOHandle = handleBuffer.get(0);
		GLES20.glBindBuffer(
				GLES20.GL_ELEMENT_ARRAY_BUFFER,
				indexVBOHandle);
		GLES20.glBufferData(
				GLES20.GL_ELEMENT_ARRAY_BUFFER, 4 * 1024 * 1024,
				null, GLES20.GL_STATIC_DRAW);

		/* TODO:
		 * Add new buffer objects dynamically and change these sizez back to
		 * 1 MiB and 4 MiB.
		 * 
		 *  Add a Set or Map of VBOs.
		 */ 
		
		GLES20.glGenBuffers(1, handleBuffer);
		dataVBOHandle = handleBuffer.get(0);
		GLES20.glBindBuffer(
				GLES20.GL_ARRAY_BUFFER, dataVBOHandle);
		GLES20.glBufferData(
				GLES20.GL_ARRAY_BUFFER, 32 * 1024 * 1024, null,
				GLES20.GL_STATIC_DRAW);

		// Initialize the QUAD
		IntBuffer indices = ByteBuffer.allocateDirect(6*8).asIntBuffer();
		indices.put(0);
		indices.put(1);
		indices.put(2);
		indices.put(2);
		indices.put(3);
		indices.put(0);
		indices.flip();
		
		Geometry.Attribute at = new Geometry.Attribute();
		at.name = "position";
		at.size = 3;
		at.type = Geometry.Type.FLOAT;
		at.buffer = ByteBuffer.allocateDirect(3*4*16).asFloatBuffer();
		((FloatBuffer) at.buffer).put(-1);
		((FloatBuffer) at.buffer).put(-1);
		((FloatBuffer) at.buffer).put(0);

		((FloatBuffer) at.buffer).put(1);
		((FloatBuffer) at.buffer).put(-1);
		((FloatBuffer) at.buffer).put(0);

		((FloatBuffer) at.buffer).put(1);
		((FloatBuffer) at.buffer).put(1);
		((FloatBuffer) at.buffer).put(0);

		((FloatBuffer) at.buffer).put(-1);
		((FloatBuffer) at.buffer).put(1);
		((FloatBuffer) at.buffer).put(0);
		at.buffer.flip();
		
		Geometry.Attribute at2 = new Geometry.Attribute();
		at2.name = "textureCoord";
		at2.size = 2;
		at2.type = Geometry.Type.FLOAT;
		at2.buffer = ByteBuffer.allocateDirect(2*4*16).asFloatBuffer();
		((FloatBuffer) at2.buffer).put(0);
		((FloatBuffer) at2.buffer).put(0);

		((FloatBuffer) at2.buffer).put(1);
		((FloatBuffer) at2.buffer).put(0);

		((FloatBuffer) at2.buffer).put(1);
		((FloatBuffer) at2.buffer).put(1);

		((FloatBuffer) at2.buffer).put(0);
		((FloatBuffer) at2.buffer).put(1);
		at2.buffer.flip();
		
		List<Geometry.Attribute> lat = new ArrayList<Geometry.Attribute>();
		lat.add(at);
		lat.add(at2);
		Geometry.QUAD = new Geometry(indices, lat);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		IntBuffer buffs = ByteBuffer.allocateDirect(2*8).asIntBuffer();
		buffs.put(indexVBOHandle);
		buffs.put(dataVBOHandle);
		buffs.flip();
		GLES20.glDeleteBuffers(buffs.capacity(),buffs);
	}

	public int getIndexVBOHandle(Geometry geometry) {
		if (tryToUpload(geometry))
			return /* geometryInfos.get(geometry). */indexVBOHandle;
		else
			return 0;
	}

	public int getDataVBOHandle(Geometry geometry) {
		if (tryToUpload(geometry))
			return /* geometryInfos.get(geometry). */dataVBOHandle;
		else
			return 0;
	}

	final public GeometryInfo getGeometryInfo(Geometry geometry) {
		if (tryToUpload(geometry))
			return geometryInfos.get(geometry);
		else
			return null;
	}

	/*public int getVAO(Geometry geometry) {
		if (tryToUpload(geometry))
			return geometryInfos.get(geometry).VAO;
		else
			return 0;
	}*/

	private boolean tryToUpload(Geometry geometry) {
		if (geometryInfos.containsKey(geometry))
			return true;
		else {
			GeometryInfo geometryInfo = new GeometryInfo();

			// Can't use VAO
			//geometryInfo.VAO = ARBVertexArrayObject.glGenVertexArrays();
			//ARBVertexArrayObject.glBindVertexArray(geometryInfo.VAO);

			// Bind VBOs
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexVBOHandle);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBOHandle);
			geometryInfo.indexVBO = indexVBOHandle;
			geometryInfo.dataVBO = dataVBOHandle;

			// Upload index data to the index VBO
			GLES20.glBufferSubData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
					indexOffset, geometry.getIndices().capacity(), geometry.getIndices());

			// Set and update the index VBO offset
			geometryInfo.indexOffset = indexOffset;
			indexOffset += geometry.getIndices().capacity() * 4;

			geometryInfo.count = geometry.getIndices().capacity();

			geometryInfo.attributeInfos = new LinkedList<AttributeInfo>();

			// Iterate through the attributes
			Iterator<Geometry.Attribute> it = geometry.getAttributes()
					.iterator();
			int i = 0;
			Geometry.Attribute geometryAttribute;
			AttributeInfo attributeInfo;
			while (it.hasNext()) {
				geometryAttribute = it.next();

				// Upload attribute data to the data VBO
				switch (geometryAttribute.type) {
				case BYTE:
				case UBYTE:
					GLES20.glBufferSubData(
							GLES20.GL_ARRAY_BUFFER,
							dataOffset, ((ByteBuffer) geometryAttribute.buffer).capacity(),
							(ByteBuffer) geometryAttribute.buffer);
					break;
				case FLOAT:
					GLES20.glBufferSubData(
							GLES20.GL_ARRAY_BUFFER,
							dataOffset, ((ByteBuffer) geometryAttribute.buffer).capacity(), //TODO ok? float-byte
							(FloatBuffer) geometryAttribute.buffer);
					break;
				}

				// Fill out and add attribute info
				attributeInfo = new AttributeInfo();
				attributeInfo.type = geometryAttribute.type;
				attributeInfo.size = geometryAttribute.size;
				attributeInfo.normalized = geometryAttribute.normalized;
				attributeInfo.dataOffset = dataOffset;
				attributeInfo.name = geometryAttribute.name;
				geometryInfo.attributeInfos.add(attributeInfo);


				// Bind the attribute to the VAO
				// TODO: Use a get to get the right index from OpenGL?
				//if (rendererMode == RendererMode.SHADERS) {
					GLES20.glEnableVertexAttribArray(i);
					glVertexAttribPointer(i,
							geometryAttribute.size, geometryAttribute.type
									.getType(), geometryAttribute.normalized,
							0, dataOffset);

				/*} else {
					if (geometryAttribute.name.equals("position")) {
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						GL11.glVertexPointer(geometryAttribute.size,
								geometryAttribute.type.getType(), 0, dataOffset);
					}
					else if (geometryAttribute.name.equals("textureCoord")) {
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						GL11.glTexCoordPointer(geometryAttribute.size,
								geometryAttribute.type.getType(), 0, dataOffset);
					}
					else if (geometryAttribute.name.equals("normal")) {
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
						GL11.glNormalPointer(
								geometryAttribute.type.getType(), 0, dataOffset);
					}
					else if (geometryAttribute.name.equals("color")) {
						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
						GL11.glColorPointer(geometryAttribute.size,
								geometryAttribute.type.getType(), 0, dataOffset);
					}
					System.out.println("Vertex attribute name: " + geometryAttribute.name);
				}*/

				i++;

				// Update the data VBO offset
				dataOffset += geometryAttribute.buffer.capacity() * 4;
				
			}

			geometryInfos.put(geometry, geometryInfo);

			// Unbind the VAO
			//ARBVertexArrayObject.glBindVertexArray(0);

			return true;
		}
	}

	/*
	 * public ... getVertexAttributes(Geometry geometry) {
	 * 
	 * }
	 */

	public void register(Geometry geometry /* TODO: hints */) {

	}
}
