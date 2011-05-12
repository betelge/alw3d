package betel.alw3d.managers;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import android.opengl.GLES20;
import betel.alw3d.renderer.RenderBuffer;

public class RenderBufferManager {

	Map<RenderBuffer, Integer> renderBufferHandles = new HashMap<RenderBuffer, Integer>();
	
	// Direct buffer for communication with OpenGL
	IntBuffer handleBuffer = ByteBuffer.allocateDirect(8).asIntBuffer();

	public int getRenderBufferHandle(RenderBuffer renderBuffer) {
		if (tryToUpload(renderBuffer))
			return renderBufferHandles.get(renderBuffer);
		else
			return 0;
	}

	private boolean tryToUpload(RenderBuffer renderBuffer) {
		if (renderBufferHandles.containsKey(renderBuffer))
			return true;
		
		GLES20.glGenRenderbuffers(1, handleBuffer);
		int handle = handleBuffer.get(0); 
		GLES20.glBindRenderbuffer(
				GLES20.GL_RENDERBUFFER, handle);

		// Allocate space
		GLES20.glRenderbufferStorage(
				GLES20.GL_RENDERBUFFER, renderBuffer
						.getFormat().getInternalFormatValue(), renderBuffer
						.getWidth(), renderBuffer.getHeight());

		GLES20.glBindRenderbuffer(
				GLES20.GL_RENDERBUFFER, 0);
		
		renderBufferHandles.put(renderBuffer, handle);
		return true;
	}

}
