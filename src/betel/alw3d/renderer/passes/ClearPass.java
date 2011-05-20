package betel.alw3d.renderer.passes;

import betel.alw3d.renderer.FBO;
import android.opengl.GLES20;

public class ClearPass extends RenderPass {
	public final static int COLOR_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT;
	public final static int DEPTH_BUFFER_BIT = GLES20.GL_DEPTH_BUFFER_BIT;
	public final static int STENCIL_BUFFER_BIT = GLES20.GL_STENCIL_BUFFER_BIT;
	
	private int bufferBits;
	
	public ClearPass(int bufferBits, FBO fbo) {
		this.setBufferBits(bufferBits);
		setFbo(fbo);
	}

	public void setBufferBits(int bufferBits) {
		this.bufferBits = bufferBits;
	}

	public int getBufferBits() {
		return bufferBits;
	}
}
