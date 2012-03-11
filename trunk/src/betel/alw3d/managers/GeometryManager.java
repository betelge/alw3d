package betel.alw3d.managers;

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
import android.util.Log;
import static fix.android.opengl.GLES20.glVertexAttribPointer;
import betel.alw3d.Alw3d;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Geometry.Type;
import betel.alw3d.renderer.UpdatableGeometry;

public class GeometryManager {

	//final private RendererMode rendererMode;
	
	private boolean isGeometryManagerInitialized = false;

	private int indexVBOHandle = 0;
	private int dataVBOHandle = 0;
	private int indexOffset = 0;
	private int dataOffset = 0;
	
	// Used for GroupGeometries
	/*private int groupIndexVBOHandle = 0;
	private int groupIndexOffset = 0;
	private int groupDataOffset = 0;*/
	
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
		public String name;
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
		 * Add new buffer objects dynamically and change these sizes back to
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
		Geometry.QUAD.setIndices(indices);
		Geometry.QUAD.setAttributes(lat);
		
		isGeometryManagerInitialized = true;
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
		while(!isGeometryManagerInitialized) Thread.yield();
				
		if (geometry == null)
			geometry = Geometry.QUAD;
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
	
	/*final private void uploadGeometry(UpdatableGeometry geometry) {
		GeometryInfo geometryInfo = new GeometryInfo();
		AttributeInfo positions = new AttributeInfo();
		// Is this the first group geometry?
		if(geometry instanceof GroupGeometry && groupIndexVBOHandle == 0) {
			// Create the special index VBO
			GLES20.glGenBuffers(1, handleBuffer);
			groupIndexVBOHandle = handleBuffer.get(0);
						
			GLES20.glBindBuffer(
					GLES20.GL_ELEMENT_ARRAY_BUFFER,
					groupIndexVBOHandle);
			GLES20.glBufferData(
					GLES20.GL_ELEMENT_ARRAY_BUFFER, 4 * 1024 * 1024,
					null, GLES20.GL_STATIC_DRAW);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, dataVBOHandle);
			GLES20.glBufferSubData(
					GLES20.GL_ARRAY_BUFFER,
					dataOffset, ((FloatBuffer) GroupGeometry.getPositions().buffer).capacity()*4,
					(FloatBuffer) GroupGeometry.getPositions().buffer);
			groupDataOffset = dataOffset;
			dataOffset += GroupGeometry.getPositions().buffer.capacity() * 4;
			
			GLES20.glEnableVertexAttribArray(0);
			glVertexAttribPointer(0,
					3, Type.FLOAT.getType(), false,
					0, dataOffset);

		}
		
		
		// TODO: this is specific for GroupGeometry. Make it handle general UpdatableGeometries and handle GroupGeometry as a special case.
		Attribute at = GroupGeometry.getPositions();
		
		positions.name = at.name;
		positions.type = at.type;
		positions.size = at.size;
		positions.normalized = at.normalized;
		positions.dataOffset = groupDataOffset;
		
		geometryInfo.indexVBO = indexVBOHandle;
		geometryInfo.dataVBO = dataVBOHandle;
		geometryInfo.count = geometry.getIndexCount();
		geometryInfo.attributeInfos = new ArrayList<AttributeInfo>();
		geometryInfo.attributeInfos.add(positions);
		
		// This one is new so we put it at the end
		geometryInfo.indexOffset = groupIndexOffset;
		int size = GroupGeometry.getSize();
		groupIndexOffset += size * size * 6 * 4;
		
		geometryInfos.put(geometry, geometryInfo);
		
		updateGeometryIndices(geometry);
	}*/
	
	final private void updateGeometryIndices(UpdatableGeometry geometry) {
		GeometryInfo geometryInfo = geometryInfos.get(geometry);

		synchronized(geometry) {
			geometryInfo.count = geometry.getIndexCount();
			
			GLES20.glBindBuffer(
					GLES20.GL_ELEMENT_ARRAY_BUFFER,
					geometryInfo.indexVBO);
			// Upload index data to the index VBO
			GLES20.glBufferSubData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
					geometryInfo.indexOffset, geometryInfo.count*4, geometry.getIndices());
			
			geometry.needsUpdate = false;
		}
			
		Log.w(Alw3d.LOG_TAG, "Updating indexes: " + geometryInfo.count);
	}

	/* TODO: Give Geometries or UpdatableGeometries the ability
	 * to share one buffer but not an other.
	 */
	
	final private boolean tryToUpload(Geometry geometry) {
		
		while(!isGeometryManagerInitialized) Thread.yield();
		
		/*if (geometry instanceof GroupGeometry) {
			if (!((GroupGeometry)geometry).needsUpdate) {
				if (geometryInfos.containsKey(geometry))
					// Nothing to do.
					return true;
				else {
					uploadGeometry((GroupGeometry)geometry);
					return true;
				}
			}
			else {
				// GroupGeometry needs update
				if (geometryInfos.containsKey(geometry)) {
					// Already existing
					updateGeometryIndices((GroupGeometry)geometry);
					return true;
				}				
				else {
					// First upload
					uploadGeometry((GroupGeometry)geometry);
					return true;
				}
			}
		}*/
		
		if (geometryInfos.containsKey(geometry)) {
			if(geometry instanceof UpdatableGeometry) {
				if( ((UpdatableGeometry)geometry).needsUpdate ) {
					updateGeometryIndices(((UpdatableGeometry)geometry));
				}
			}
			return true;
		}
		else {
			
			int count;
			if(geometry instanceof UpdatableGeometry)
				count = ((UpdatableGeometry)geometry).getIndexCount();
			else
				count = geometry.getIndexCount();
			Log.w(Alw3d.LOG_TAG, "Count: " + count);
			
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
					indexOffset, count*4, geometry.getIndices());
			
			Log.w(Alw3d.LOG_TAG, "indexOffset: " + indexOffset);

			// Set and update the index VBO offset
			geometryInfo.indexOffset = indexOffset;
			if(geometry instanceof UpdatableGeometry)
				indexOffset += ((UpdatableGeometry)geometry).getMaxCount()*4;
			else
				indexOffset += count*4;
			
			Log.w(Alw3d.LOG_TAG, "indexOffset changed to  " + indexOffset);

			geometryInfo.count = count;

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
							dataOffset, ((ByteBuffer) geometryAttribute.buffer).limit(),
							(ByteBuffer) geometryAttribute.buffer);
					break;
				case FLOAT:
					GLES20.glBufferSubData(
							GLES20.GL_ARRAY_BUFFER,
							dataOffset, ((FloatBuffer) geometryAttribute.buffer).limit()*4,
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
				
				Log.w(Alw3d.LOG_TAG, "dataOffset: " + dataOffset);


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
				dataOffset += geometryAttribute.buffer.limit() * 4;
				Log.w(Alw3d.LOG_TAG, "dataOffset changed to: " + dataOffset);
				
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

	public void reset() {
		indexVBOHandle = 0;
		dataVBOHandle = 0;
		indexOffset = 0;
		dataOffset = 0;
		
		/*groupIndexVBOHandle = 0;
		groupIndexOffset = 0;
		groupDataOffset = 0;*/
		
		geometryInfos.clear();
	}
}
