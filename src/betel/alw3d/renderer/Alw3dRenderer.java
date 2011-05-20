package betel.alw3d.renderer;

import static fix.android.opengl.GLES20.glVertexAttribPointer;
import static fix.android.opengl.GLES20.glDrawElements;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import betel.alw3d.Alw3d;
import betel.alw3d.Alw3dSimulator;
import betel.alw3d.Model;
import betel.alw3d.managers.FBOManager;
import betel.alw3d.managers.GeometryManager;
import betel.alw3d.managers.RenderBufferManager;
import betel.alw3d.managers.ShaderManager;
import betel.alw3d.managers.TextureManager;
import betel.alw3d.managers.GeometryManager.AttributeInfo;
import betel.alw3d.managers.GeometryManager.GeometryInfo;
import betel.alw3d.math.Transform;
import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.passes.ClearPass;
import betel.alw3d.renderer.passes.RenderPass;
import betel.alw3d.renderer.passes.SceneRenderPass;
import betel.alw3d.renderer.passes.SetPass;

public class Alw3dRenderer implements Renderer{
	
	private Model model;
	
	GeometryManager geometryManager;
	ShaderManager shaderManager;
	TextureManager textureManager;
	RenderBufferManager renderBufferManager;
	FBOManager fboManager;

	// TODO: fix this in a better way
	// Current camera transform
	Transform cameraTransform;

	// TODO: Create matrix class?
	FloatBuffer modelViewMatrix;
	FloatBuffer perspectiveMatrix;
	FloatBuffer normalMatrix;

	List<GeometryNode> renderNodes = new ArrayList<GeometryNode>();
	List<Transform> renderTransforms = new ArrayList<Transform>();
	Transform lightTransform = new Transform();

	// Function as a "backbuffer" for the procesnode to write to. Are then
	// swapped with the "front"
	List<GeometryNode> backRenderNodes = new ArrayList<GeometryNode>();
	List<Transform> backRenderTransforms = new ArrayList<Transform>();
	Transform backLightTransform = new Transform();
	
	// Current states
	FBO oldFBO = null;

	long time = 0;

	public Alw3dRenderer(Model model) {
		this.model = model;
		
		// Initialize model-view matrix
		modelViewMatrix = FloatBuffer.allocate(16);

		// Initialize normal matrix
		normalMatrix = FloatBuffer.allocate(9);

		// Allocate perspective matrix
		perspectiveMatrix = FloatBuffer.allocate(16);
	}
	
	static public void setPerspectiveMatrix( FloatBuffer perspectiveMatrix, 
			float aspect, float fov, float zNear, float zFar ) {
		// Assumes that perspectveMatrix has size >=16 and the position 0.
		
		float[] floats = getPerspectiveMatrix(aspect, fov, zNear, zFar);
		perspectiveMatrix.put(floats);
		perspectiveMatrix.flip();
	}
	
	static public float[] getPerspectiveMatrix( 
			float aspect, float fov, float zNear, float zFar ) {
		// Assumes that perspectveMatrix has size >=16 and the position 0.
		
		float[] floats = new float[16]; 
				
		float h = 1f / (float) Math.tan(fov * (float) Math.PI / 360f);
		floats[0] = h / aspect;
		floats[1] = 0f;
		floats[2] = 0f;
		floats[3] = 0f;
		
		floats[4] = 0f;
		floats[5] = h;
		floats[6] = 0f;
		floats[7] = 0f;
	
		floats[8] = 0f;
		floats[9] = 0f;
		floats[10] = (zNear + zFar) / (zNear - zFar);
		floats[11] = -1f;
	
		floats[12] = 0f;
		floats[13] = 0f;
		floats[14] = 2f * (zNear * zFar) / (zNear - zFar);
		floats[15] = 0f;
		
		return floats;
	}
	
	public void setState(SetPass.State state, boolean set) {
		switch(state) {
		case DEPTH_WRITE:
			GLES20.glDepthMask(set);
			break;
		default:	
			if(set)
				GLES20.glEnable(state.getValue());
			else
				GLES20.glDisable(state.getValue());
		}
	}
	
	public void clear(int bufferBits) {
		clear(bufferBits, null);
	}
	
	public void clear(int bufferBits, FBO fbo) {
		// Bind FBO
		bindFBO(fbo);

		// Clear color and depth buffers
		GLES20.glClear(bufferBits);
	}

	public void renderQuad(Material material) {
		renderQuad(material, null);
	}

	public void renderQuad(Material material, FBO fbo) {

		// Bind FBO
		bindFBO(fbo);

		// Disable depth test
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		
		// Disable alpha
		GLES20.glDisable(GLES20.GL_BLEND);

		// Bind quad geometry info
		GeometryInfo geometryInfo =geometryManager
			.getGeometryInfo(Geometry.QUAD);
		bindGeometryInfo(geometryInfo);

		// Set shader
		int shaderProgram = shaderManager.getShaderProgramHandle(material
				.getShaderProgram());
		GLES20.glUseProgram(shaderProgram);
		
		// Bind textures
		bindTextures(shaderProgram, material.getTextures());

		// Upload uniforms
		Uniform[] uniforms = material.getUniforms();
		uploadUniforms(shaderProgram, uniforms);

		// Bind vertex attributes to uniform names
		bindAttributes(geometryInfo.attributeInfos, shaderProgram);
		// Draw
		glDrawElements(GLES20.GL_TRIANGLES, geometryInfo.count,
				GLES20.GL_UNSIGNED_INT, geometryInfo.indexOffset);

		if (fbo != null) {
			bindFBO(null);
			fboManager.generateMipmaps(fbo);
		}

	}

	private void bindGeometryInfo(GeometryInfo geometryInfo) {
		// Bind VBOs
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, geometryInfo.indexVBO);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, geometryInfo.dataVBO);
	}
	
	private void bindAttributes(List<AttributeInfo> attributeInfos, int shaderProgram) {	
		// Bind attribute pointers
		Iterator<AttributeInfo> it = attributeInfos.iterator();
		AttributeInfo attributeInfo;
		while (it.hasNext()) {
			attributeInfo = it.next();
			
			// TODO: Use a get to get the right index from OpenGL?
			int index = GLES20.glGetAttribLocation(shaderProgram, attributeInfo.name);
			GLES20.glEnableVertexAttribArray(index);
			glVertexAttribPointer(index,
					attributeInfo.size, attributeInfo.type
					.getType(), attributeInfo.normalized,
					0, attributeInfo.dataOffset);
		}
	}

	public void renderSceneNonOpenGL(Node rootNode, CameraNode cameraNode) {
		backRenderNodes.clear();
		backRenderTransforms.clear();

		cameraTransform = cameraNode.getAbsoluteTransform().getCameraTransform();
		
		float aspect = cameraNode.getAspect();
		if(aspect == 0)
			aspect = (float) model.getWidth() / model.getHeight();
		
		setPerspectiveMatrix(perspectiveMatrix, aspect,
				cameraNode.getFov(), cameraNode.getzNear(), cameraNode.getzFar());
		
		time = System.nanoTime();
		Log.d(Alw3d.LOG_TAG, "renderTime: " + time);

		ProcessNode(rootNode, new Transform());

		List<GeometryNode> tempRenderNodes = backRenderNodes;
		List<Transform> tempRenderTransforms = backRenderTransforms;
		Transform tempLightTransform = backLightTransform;

		backRenderNodes = renderNodes;
		backRenderTransforms = renderTransforms;
		backLightTransform = lightTransform;

		renderNodes = tempRenderNodes;
		renderTransforms = tempRenderTransforms;
		lightTransform = tempLightTransform;
	}

	public void renderScene(Node rootNode, CameraNode cameraNode) {
		renderScene(rootNode, cameraNode, null, null);
	}
	
	public void renderScene(Node rootNode, CameraNode cameraNode, FBO fbo) {
		renderScene(rootNode, cameraNode, fbo, null);
	}

	public void renderScene(Node rootNode, CameraNode cameraNode, FBO fbo, Material overrideMaterial) {

		renderSceneNonOpenGL(rootNode, cameraNode);

		// Current objects. Used for performance.
		Geometry oldGeometry = null;
		GeometryInfo oldGeometryInfo = null;
		Map<String, Texture> oldTextures = null;
		int shaderProgramHandle = 0;
		int oldShaderProgramHandle = 0;
		ShaderProgram shaderProgram = null;
		ShaderProgram oldShaderProgram = null;
		Uniform[] uniforms = null;
		Uniform[] oldUniforms = null;
		int modelViewMatrixLocation = 0;
		int perspectiveMatrixLocation = 0;
		int normalMatrixLocation = 0;
		int lightPosVectorLocation = 0;

		GeometryInfo geometryInfo = null;

		// Bind FBO
		bindFBO(fbo);

		// Enable depth test
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		// Enable alpha
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		Iterator<GeometryNode> it = renderNodes.iterator();
		Iterator<Transform> tit = renderTransforms.iterator();
		while (it.hasNext() && tit.hasNext()) {
			GeometryNode geometryNode = it.next();
			Transform transform = tit.next();

			Geometry geometry = geometryNode.getGeometry();

			if (oldGeometry != geometry || geometry == null) {

				geometryInfo = geometryManager.getGeometryInfo(geometry);

				// Bind VAO
				bindGeometryInfo(geometryInfo);
			}
			
			Material material;
			if(overrideMaterial == null)
				material = geometryNode.getMaterial();
			else
				material = overrideMaterial;

			// Set shader
			Map<String, Texture> textures = null;
			shaderProgram = material.getShaderProgram();
			if (oldShaderProgram != shaderProgram) {
				shaderProgramHandle = shaderManager
						.getShaderProgramHandle(material.getShaderProgram());
				GLES20.glUseProgram(shaderProgramHandle);
			}

			// Bind textures
			textures = material.getTextures();
			if (oldTextures != textures
					|| oldShaderProgramHandle != shaderProgramHandle) {
				bindTextures(shaderProgramHandle, textures);
			}

			// Upload uniforms
			uniforms = material.getUniforms();
			if (oldUniforms != uniforms
					|| oldShaderProgramHandle != shaderProgramHandle) {
				// TODO: check for changes instead?
				uploadUniforms(shaderProgramHandle, uniforms);
			}

			if (oldShaderProgram != shaderProgram) {
				modelViewMatrixLocation = GLES20
						.glGetUniformLocation(shaderProgramHandle,
								"modelViewMatrix");
				perspectiveMatrixLocation = GLES20
						.glGetUniformLocation(shaderProgramHandle,
								"perspectiveMatrix");
				normalMatrixLocation = GLES20
						.glGetUniformLocation(shaderProgramHandle,
								"normalMatrix");
				lightPosVectorLocation = GLES20
						.glGetUniformLocation(shaderProgramHandle,
								"lightPos");
			}
			
			// TODO: lightPos seems to always be (0,0,0)
			Vector3f lightPosVector = lightTransform.getPosition();
			GLES20.glUniform3f(lightPosVectorLocation,
				lightPosVector.x, lightPosVector.y, lightPosVector.z);

			modelViewMatrix.clear();
			modelViewMatrix.put(transform.toMatrix4());
			modelViewMatrix.flip();

			GLES20.glUniformMatrix4fv(modelViewMatrixLocation, 1,
					false, modelViewMatrix);
			GLES20.glUniformMatrix4fv(perspectiveMatrixLocation, 1,
					false, perspectiveMatrix);
			normalMatrix.clear();
			normalMatrix.put(transform.getRotation().toMatrix3());
			normalMatrix.flip();
			GLES20.glUniformMatrix3fv(normalMatrixLocation, 1,
					false, normalMatrix);
	
			// Bind vertex attributes to uniform names
			if (oldGeometryInfo != geometryInfo
					|| oldShaderProgramHandle != shaderProgramHandle)
				bindAttributes(geometryInfo.attributeInfos, shaderProgramHandle);
	
			// Draw
			glDrawElements(geometry.getPrimitiveType().getValue(), geometryInfo.count,
					GLES20.GL_UNSIGNED_INT, geometryInfo.indexOffset);
	
			oldGeometry = geometry;
			oldGeometryInfo = geometryInfo;
			oldTextures = textures;
			oldShaderProgramHandle = shaderProgramHandle;
			oldShaderProgram = shaderProgram;
			oldUniforms = uniforms;
		}

		if (fbo != null) {
			bindFBO(null);
			fboManager.generateMipmaps(fbo);
		}
	}

	private void bindTextures(int shaderProgramHandle,
			Map<String, Texture> textures) {
		Iterator<String> it = textures.keySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			String name = it.next();
			Texture texture = textures.get(name);

			int textureLocation = GLES20.glGetUniformLocation(
					shaderProgramHandle, name);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
			GLES20.glBindTexture(texture.getTextureType().getValue(),
					textureManager.getTextureHandle(texture));
			GLES20.glUniform1i(textureLocation, i);

			i++;
		}
	}

	private void bindFBO(FBO fbo) {
		if (fbo == oldFBO) return;
		
		if (fbo != null) {

			if (!fboManager.isComplete(fbo))
				System.out.println("FBO not complete!");

			GLES20.glBindFramebuffer(
					GLES20.GL_FRAMEBUFFER, fboManager
							.getFBOHandle(fbo));

			GLES20.glViewport(0, 0, fbo.getWidth(), fbo.getHeight());
		} else {
			GLES20.glBindFramebuffer(
					GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glViewport(0, 0, model.getWidth(), model.getHeight());
		}
		
		oldFBO = fbo;
	}

	private void ProcessNode(Node node, Transform transform) {

		// System.out.println("Renderer processing node: " + node);

		Transform currentTransform = null;
		if (node instanceof Movable) {
			Movable movable = (Movable) node;
			float ratio = 0f;
			if (movable.getNextTime() != movable.getLastTime())
				ratio = (time - movable.getLastTime())
						/ (float)(movable.getNextTime() - movable.getLastTime());
			currentTransform = transform.mult(movable.getTransform()
					.interpolate(movable.getNextTransform(), ratio));
		} else {
			currentTransform = transform.mult(node.getTransform());
		}

		if (node instanceof GeometryNode) {
			backRenderNodes.add((GeometryNode) node);
			backRenderTransforms.add(cameraTransform.mult(currentTransform));
		}

		// TODO: Handle multiple lights
		if (node instanceof Light) {
			backLightTransform.set(cameraTransform.mult(currentTransform));
		}
		synchronized (node) {
			Iterator<Node> it = node.getChildren().iterator();
			while (it.hasNext()) {
				ProcessNode(it.next(), currentTransform);
			}
		}
	}

	private void uploadUniforms(int shaderProgram, Uniform[] uniforms) {
		if (uniforms != null) {
			for (int i = 0; i < uniforms.length; i++) {
				int uniformLocation = GLES20.glGetUniformLocation(
						shaderProgram, uniforms[i].getName());
				float[] floats = uniforms[i].getFloats();
				switch (uniforms[i].getType()) {
				case FLOAT:
					GLES20.glUniform1f(uniformLocation, floats[0]);
					break;
				case FLOAT2:
					GLES20.glUniform2f(uniformLocation, floats[0],
							floats[1]);
					break;
				case FLOAT3:
					GLES20.glUniform3f(uniformLocation, floats[0],
							floats[1], floats[2]);
					break;
				case FLOAT4:
					GLES20.glUniform4f(uniformLocation, floats[0],
							floats[1], floats[2], floats[3]);
					break;
				case INT:
					GLES20.glUniform1i(uniformLocation,
							(int) floats[0]);
					break;
				case INT2:
					GLES20.glUniform2i(uniformLocation,
							(int) floats[0], (int) floats[1]);
					break;
				case INT3:
					GLES20.glUniform3i(uniformLocation,
							(int) floats[0], (int) floats[1], (int) floats[2]);
					break;
				case INT4:
					GLES20.glUniform4i(uniformLocation,
							(int) floats[0], (int) floats[1], (int) floats[2],
							(int) floats[3]);
					break;
				case MATRIX2:
					GLES20.glUniformMatrix2fv(uniformLocation, 2, uniforms[i].isTranspose(), uniforms[i].getMatrix());
					break;
				case MATRIX3:
					GLES20.glUniformMatrix3fv(uniformLocation, 3, uniforms[i].isTranspose(), uniforms[i].getMatrix());
					break;
				case MATRIX4:
					GLES20.glUniformMatrix4fv(uniformLocation, 4, uniforms[i].isTranspose(), uniforms[i].getMatrix());
					break;
				default:
					System.out.println("Error: Unknown or unimplemented uniform type, " + uniforms[i].getName());
					break;
				}
			}
		}
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		Alw3dSimulator simulator = model.getSimulator();
		if(simulator != null)
			simulator.steps();
		
		List<RenderPass> renderPasses = model.getRenderPasses();
		synchronized (renderPasses) {
			processRenderPasses(renderPasses);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int w, int h) {
		GLES20.glViewport(0, 0, w, h);
		model.setWidth(w);
		model.setHeight(h);
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		// TODO: These will forget everything, right?
		geometryManager = new GeometryManager();
		shaderManager = new ShaderManager();
		textureManager = new TextureManager();
		renderBufferManager = new RenderBufferManager();
		fboManager = new FBOManager(textureManager, renderBufferManager);
		
		// Enable back-faces culling
		GLES20.glEnable(GLES20.GL_CULL_FACE);
	}
	
	private void processRenderPasses(List<RenderPass> renderPasses) {
		Iterator<RenderPass> it = renderPasses.iterator();
		while(it.hasNext()) {
			
			RenderPass renderPass = it.next();
			if(renderPass instanceof SetPass) {
				setState(((SetPass) renderPass).getState(),
						((SetPass) renderPass).isSet());
			}
			else if(renderPass instanceof ClearPass) {
				clear(((ClearPass) renderPass).getBufferBits(),
						renderPass.getFbo());
			}
			else if(renderPass instanceof SceneRenderPass) {
				synchronized (((SceneRenderPass) renderPass).getRootNode()) {
				renderScene(
						((SceneRenderPass) renderPass).getRootNode(),
						((SceneRenderPass) renderPass).getCameraNode(),
						renderPass.getFbo(),
						((SceneRenderPass) renderPass).getOverrideMaterial());
				}
			}
			else if(renderPass instanceof QuadRenderPass) {
				renderQuad(
						((QuadRenderPass) renderPass).getMaterial(),
						renderPass.getFbo());
			}
			else if(renderPass instanceof RenderMultiPass) {
				processRenderPasses(((RenderMultiPass) renderPass).getRenderPasses());
			}
		}
	}
}
