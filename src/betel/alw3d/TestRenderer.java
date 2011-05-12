package betel.alw3d;

import static fix.android.opengl.GLES20.glVertexAttribPointer;
import static fix.android.opengl.GLES20.glDrawElements;

import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import betel.alw3d.managers.FBOManager;
import betel.alw3d.managers.GeometryManager;
import betel.alw3d.managers.RenderBufferManager;
import betel.alw3d.managers.ShaderManager;
import betel.alw3d.managers.TextureManager;
import betel.alw3d.managers.GeometryManager.AttributeInfo;
import betel.alw3d.managers.GeometryManager.GeometryInfo;
import betel.alw3d.renderer.FBO;
import betel.alw3d.renderer.Geometry;

import android.content.Context;
import android.opengl.GLES20;

import android.opengl.GLSurfaceView.Renderer;

public class TestRenderer implements Renderer {

	Model model;
	
	GeometryManager geometryManager;
	ShaderManager shaderManager;
	TextureManager textureManager;
	RenderBufferManager renderBufferManager;
	FBOManager fboManager;
	
	// Current states (TODO: Move to Model?)
	FBO currentFBO = null;
	GeometryInfo currentGeometryInfo = null;
	
	
	TestRenderer(Context context, Model model) {
		this.model = model;
	}
	
	@Override
	public void onDrawFrame(GL10 arg0) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
	//	ShaderProgram shaderProgram = model.shaderProgram;
		
		Geometry geometry = model.geometry;
		GeometryInfo geometryInfo = geometryManager.getGeometryInfo(geometry);
		
		if(geometryInfo != currentGeometryInfo)
		{
			// Bind VBOs
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, geometryInfo.indexVBO);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, geometryInfo.dataVBO);
			
			// Bind attribute pointers
			Iterator<AttributeInfo> it = geometryInfo.attributeInfos.iterator();
			int i = 0;
			AttributeInfo attributeInfo;
			while (it.hasNext()) {
				attributeInfo = it.next();
				
				// TODO: Use a get to get the right index from OpenGL?
				GLES20.glEnableVertexAttribArray(i);
				glVertexAttribPointer(i,
						attributeInfo.size, attributeInfo.type
						.getType(), attributeInfo.normalized,
						0, attributeInfo.dataOffset);
			}
			
			currentGeometryInfo = geometryInfo;
		}
		
		// TODO: Material
		// TODO: Transformt
		
		// Draw
		glDrawElements(geometry.getPrimitiveType().getValue(), geometryInfo.count,
				GLES20.GL_UNSIGNED_INT, geometryInfo.indexOffset);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int w, int h) {
		GLES20.glViewport(0, 0, w, h);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
		// TODO: These will forget everything, right?
		geometryManager = new GeometryManager();
		shaderManager = new ShaderManager();
		textureManager = new TextureManager();
		renderBufferManager = new RenderBufferManager();
		fboManager = new FBOManager(textureManager, renderBufferManager);
	}
	
	private void bindFBO(FBO fbo) {
		if(fbo != currentFBO && fbo != null)
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboManager.getFBOHandle(fbo));
		else if(fbo == null && currentFBO != null)
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		
		currentFBO = fbo;
	}

}
