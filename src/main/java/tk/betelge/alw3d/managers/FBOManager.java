package tk.betelge.alw3d.managers;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import tk.betelge.alw3d.renderer.FBO;
import tk.betelge.alw3d.renderer.FBOAttachable;
import tk.betelge.alw3d.renderer.RenderBuffer;
import tk.betelge.alw3d.renderer.Texture;

import android.opengl.GLES20;

public class FBOManager {

	Map<FBO, Integer> FBOHandles = new HashMap<FBO, Integer>();

	final TextureManager textureManager;
	final RenderBufferManager renderBufferManager;
	
	// Direct buffer for communication with OpenGL
	IntBuffer handleBuffer = ByteBuffer.allocateDirect(8).asIntBuffer();

	public FBOManager(TextureManager textureManager,
			RenderBufferManager renderBufferManager) {
		this.textureManager = textureManager;
		this.renderBufferManager = renderBufferManager;
	}

	public int getFBOHandle(FBO fbo) {
		if (tryToUpload(fbo))
			return FBOHandles.get(fbo);
		else
			return 0;
	}

	private boolean tryToUpload(FBO fbo) {
		if (FBOHandles.containsKey(fbo))
			return true;
		
		// Allocate texture and mipmap space
		generateMipmaps(fbo);
		
		GLES20.glGenFramebuffers(1, handleBuffer);
		int handle = handleBuffer.get(0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handle);

		// TODO: Check if sizes match
		for (int i = 0; i < fbo.getAttachables().length && i <= 15; i++) {
			attach(GLES20.GL_COLOR_ATTACHMENT0 + i, fbo
					.getAttachables()[i]);
		}
		if (fbo.getDepthBuffer() != null)
			attach(GLES20.GL_DEPTH_ATTACHMENT, fbo
					.getDepthBuffer());
		else System.out.println("No depth buffer.");
		if (fbo.getStencilBuffer() != null)
			attach(GLES20.GL_STENCIL_ATTACHMENT, fbo
					.getStencilBuffer());
		else System.out.println("No stencil buffer.");
		
		FBOHandles.put(fbo, handle);

		return true;
	}

	private void attach(int attachPoint, FBOAttachable attachable) {
		if (attachable == null)
			GLES20.glFramebufferRenderbuffer(
					GLES20.GL_FRAMEBUFFER, attachPoint,
					GLES20.GL_RENDERBUFFER, GLES20.GL_NONE);
		else if (attachable instanceof Texture) {
			GLES20.glFramebufferTexture2D(
					GLES20.GL_FRAMEBUFFER, attachPoint,
					GLES20.GL_TEXTURE_2D, textureManager
							.getTextureHandle((Texture) attachable), 0);

		} else if (attachable instanceof RenderBuffer) {
			GLES20.glFramebufferRenderbuffer(
					GLES20.GL_FRAMEBUFFER, attachPoint,
					GLES20.GL_RENDERBUFFER,
					renderBufferManager
							.getRenderBufferHandle((RenderBuffer) attachable));

		}
	}

	public void generateMipmaps(FBO fbo) {
		for (int i = 0; i < fbo.getAttachables().length && i <= 15; i++) {
			if (fbo.getAttachables()[i] instanceof Texture) {
				Texture tex = (Texture) fbo.getAttachables()[i];
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureManager
						.getTextureHandle(tex));
				Texture.Filter filter = tex.getFilter();
				if(filter != Texture.Filter.LINEAR && filter != Texture.Filter.NEAREST)
					GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
			}
			if (fbo.getAttachables()[i] instanceof RenderBuffer)
				GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferManager
						.getRenderBufferHandle((RenderBuffer) fbo.getAttachables()[i]));
		}
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public boolean isComplete(FBO fbo) {
		tryToUpload(fbo);
		int fboStatus = GLES20.glCheckFramebufferStatus(
				GLES20.GL_FRAMEBUFFER);
		switch(fboStatus) {
		case GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
			System.out.println("FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			break;
		case GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
			System.out.println("FRAMEBUFFER_INCOMPLETE_DIMENSIONS");
			break;
		case GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
			System.out.println("FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			break;
		case GLES20.GL_FRAMEBUFFER_UNSUPPORTED:
			System.out.println("FRAMEBUFFER_UNSUPPORTED");
			break;
		}
		return (fboStatus == GLES20.GL_FRAMEBUFFER_COMPLETE);
	}

	public void reset() {
		FBOHandles.clear();
	}
}
