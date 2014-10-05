package tk.betelge.alw3d.renderer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tk.betelge.alw3d.Alw3d;
import tk.betelge.alw3d.Alw3dModel;
import tk.betelge.alw3d.Alw3dSimulator;
import tk.betelge.alw3d.managers.FBOManager;
import tk.betelge.alw3d.managers.GeometryManager;
import tk.betelge.alw3d.managers.RenderBufferManager;
import tk.betelge.alw3d.managers.ShaderManager;
import tk.betelge.alw3d.managers.TextureManager;
import tk.betelge.alw3d.managers.GeometryManager.AttributeInfo;
import tk.betelge.alw3d.managers.GeometryManager.GeometryInfo;
import tk.betelge.alw3d.math.Quaternion;
import tk.betelge.alw3d.math.Transform;
import tk.betelge.alw3d.math.Vector3f;
import tk.betelge.alw3d.renderer.passes.CheckGlErrorPass;
import tk.betelge.alw3d.renderer.passes.ClearPass;
import tk.betelge.alw3d.renderer.passes.RenderPass;
import tk.betelge.alw3d.renderer.passes.SceneRenderPass;
import tk.betelge.alw3d.renderer.passes.SetPass;
import tk.betelge.alw3d.renderer.passes.CheckGlErrorPass.OnGlErrorListener;
import tk.betelge.alw3d.renderer.passes.RenderPass.OnRenderPassFinishedListener;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class Alw3dRenderer implements Renderer{
	
	private Alw3dModel model;
	
	GeometryManager geometryManager;
	ShaderManager shaderManager;
	TextureManager textureManager;
	RenderBufferManager renderBufferManager;
	FBOManager fboManager;
	OnSurfaceChangedListener onSurfaceChangedListener;
	
	public interface OnSurfaceChangedListener {
		public void onSurfaceChanged(int w, int h);
	}
	
	// Set with resources that are requested to be preloaded
	Set<Object> preloadSet = new HashSet<Object>();

	// TODO: fix this in a better way
	// Current camera transform
	Transform cameraTransform = new Transform();

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

	public Alw3dRenderer(Alw3dModel model) {
		this.model = model;
		
		// Initialize model-view matrix
		modelViewMatrix = FloatBuffer.allocate(16);

		// Initialize normal matrix
		normalMatrix = FloatBuffer.allocate(9);

		// Allocate perspective matrix
		perspectiveMatrix = FloatBuffer.allocate(16);
	}
	
	static public void setPerspectiveMatrix( FloatBuffer m, 
			float aspect, float fov, float zNear, float zFar ) {
		// Assumes that perspectveMatrix has size >=16 and the position 0.
		
		m.clear();
				
		float h = 1f / (float) Math.tan(fov * (float) Math.PI / 360f);
		m.put(h / aspect);
		m.put(0f);
		m.put(0f);
		m.put(0f);
		
		m.put(0f);
		m.put(h);
		m.put(0f);
		m.put(0f);
	
		m.put(0f);
		m.put(0f);
		m.put( (zNear + zFar) / (zNear - zFar) );
		m.put(-1f);
	
		m.put(0f);
		m.put(0f);
		m.put( 2f * (zNear * zFar) / (zNear - zFar));
		m.put(0f);
		
		m.flip();
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
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, geometryInfo.count,
				GLES20.GL_UNSIGNED_SHORT, geometryInfo.indexOffset);

		if (fbo != null) {
			bindFBO(null);
			fboManager.generateMipmaps(fbo);
		}

	}

	private void bindGeometryInfo(GeometryInfo geometryInfo) {
		// Bind VBOs
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, geometryInfo.indexVBO);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, geometryInfo.dataVBO);
		
		//Log.w(Alw3d.LOG_TAG, "Binding indexVBO: " + geometryInfo.indexVBO);
		//Log.w(Alw3d.LOG_TAG, "Binding dataVBO: " + geometryInfo.dataVBO);
	}
	
	private void bindAttributes(List<AttributeInfo> attributeInfos, int shaderProgram) {	
		// Bind attribute pointers
	//	Iterator<AttributeInfo> it = attributeInfos.iterator();
	//	AttributeInfo attributeInfo;
	//	while (it.hasNext()) {
	//	attributeInfo = it.next();
		
		for(AttributeInfo attributeInfo : attributeInfos) {
			
			// TODO: Use a get to get the right index from OpenGL?
			int index = GLES20.glGetAttribLocation(shaderProgram, attributeInfo.name);
			// Ignore unused attributes
			// TODO: Print warnings.
			if(index != -1) {
				GLES20.glEnableVertexAttribArray(index);
				GLES20.glVertexAttribPointer(index,
						attributeInfo.size, attributeInfo.type
						.getType(), attributeInfo.normalized,
						0, attributeInfo.dataOffset); // This call used to be in a JNI workaround
				
				//Log.w(Alw3d.LOG_TAG, "Binding dataOffset: " + attributeInfo.dataOffset);
			}
		}
		
	}

	public void renderSceneNonOpenGL(Node rootNode, CameraNode cameraNode) {
		backRenderNodes.clear();
		//backRenderTransforms.clear();

		cameraNode.getAbsoluteTransform().getCameraTransform(cameraTransform);
		
		float aspect = cameraNode.getAspect();
		if(aspect == 0)
			aspect = (float) model.getWidth() / model.getHeight();
		
		setPerspectiveMatrix(perspectiveMatrix, aspect,
				cameraNode.getFov(), cameraNode.getzNear(), cameraNode.getzFar());
		
		time = System.nanoTime();

		ProcessNode(rootNode, 0,0,0, 0,0,0,1);

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
		
		// TODO: temp
		GLES20.glDisable(GLES20.GL_CULL_FACE);

	/*	Iterator<GeometryNode> it = renderNodes.iterator();
		Iterator<Transform> tit = renderTransforms.iterator();
		while (it.hasNext() && tit.hasNext()) {
			GeometryNode geometryNode = it.next();
			Transform transform = tit.next(); */
		
		int length = renderNodes.size();
		for(int i = 0; i < length; i++) {

			GeometryNode geometryNode = renderNodes.get(i);
			Transform transform = renderTransforms.get(i);
			
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
			if(material == null)
				material = Material.DEFAULT;

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

			/*modelViewMatrix.clear();
			modelViewMatrix.put(transform.toMatrix4());
			m
			odelViewMatrix.flip();*/
			transform.toMatrix4(modelViewMatrix);

			GLES20.glUniformMatrix4fv(modelViewMatrixLocation, 1,
					false, modelViewMatrix);
			GLES20.glUniformMatrix4fv(perspectiveMatrixLocation, 1,
					false, perspectiveMatrix);
			/*normalMatrix.clear();
			normalMatrix.put(transform.getRotation().toMatrix3());
			normalMatrix.flip();*/
			transform.getRotation().toMatrix3(normalMatrix);
			GLES20.glUniformMatrix3fv(normalMatrixLocation, 1,
					false, normalMatrix);
	
			// Bind vertex attributes to uniform names
			if (oldGeometryInfo != geometryInfo
					|| oldShaderProgramHandle != shaderProgramHandle)
				bindAttributes(geometryInfo.attributeInfos, shaderProgramHandle);
	
			// Draw
			GLES20.glDrawElements(geometry.getPrimitiveType().getValue(), geometryInfo.count,
					GLES20.GL_UNSIGNED_SHORT, geometryInfo.indexOffset);
			//Log.w(Alw3d.LOG_TAG, "Rendering with indexOffset: " + geometryInfo.indexOffset + "  and count: " + geometryInfo.count);
	
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

	private static Transform currentTransform = new Transform();
	private static Transform transform = new Transform();
	private static Transform tempTransform = new Transform();
	private void ProcessNode(Node node, float x, float y, float z, float a, float b, float c, float w) {
		transform.getPosition().set(x, y, z);
		transform.getRotation().set(a, b, c, w);
		currentTransform.set(Transform.UNIT);
		
		if (node instanceof Movable) {
			Movable movable = (Movable) node;
			float ratio = 0f;
			if (movable.getNextTime() != movable.getLastTime())
				ratio = (time - movable.getLastTime())
						/ (float)(movable.getNextTime() - movable.getLastTime());
			movable.getTransform().interpolate(movable.getNextTransform(), ratio,
					tempTransform);
			
			transform.mult(tempTransform, currentTransform);
		} else {
			 transform.mult(node.getTransform(), currentTransform);
		}

		if (node instanceof GeometryNode) {
			if(((GeometryNode)node).isVisible()) {
				backRenderNodes.add((GeometryNode) node);
				int index = backRenderNodes.size() - 1;
				//backRenderTransforms.add(cameraTransform.mult(currentTransform));
				if(backRenderTransforms.size() <= index)
					backRenderTransforms.add(new Transform());
				Transform trans = backRenderTransforms.get(index);
				trans.set(cameraTransform);
				trans.multThis(currentTransform);
			}
		}

		// TODO: Handle multiple lights
		if (node instanceof Light) {
			cameraTransform.mult(currentTransform, backLightTransform);
		}
		/*synchronized (node) {
			Iterator<Node> it = node.getChildren().iterator();
			while (it.hasNext()) {
				ProcessNode(it.next(), currentTransform);
			}
		}*/
		
		Vector3f pos = currentTransform.getPosition();
		Quaternion rot = currentTransform.getRotation();
		
		float px = pos.x;
		float py = pos.y;
		float pz = pos.z;
		float rx = rot.x;
		float ry = rot.y;
		float rz = rot.z;
		float rw = rot.w;
		
		
		if(!node.getChildren().isEmpty()) {
			synchronized(node) {
				for(Node child : node.getChildren()) {
					ProcessNode(child, px, py, pz, rx, ry, rz, rw);
				}
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
	
	
	// TODO: Is this stil needed?
	//private float lastRenderTime = 0;
	//private float lastSimTime = 0;

	@Override
	public void onDrawFrame(GL10 arg0) {
			
		Alw3dSimulator simulator = model.getSimulator();
		if(simulator != null)
			simulator.steps();
		
		List<RenderPass> renderPasses = model.getRenderPasses();
		processRenderPasses(renderPasses);
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int w, int h) {
		GLES20.glViewport(0, 0, w, h);
		model.setWidth(w);
		model.setHeight(h);
		
		if(onSurfaceChangedListener != null)
			onSurfaceChangedListener.onSurfaceChanged(w, h);
		
		synchronized (preloadSet) {
			Iterator<Object> it = preloadSet.iterator();
			while(it.hasNext())
				preload(it.next());
		}
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
		
		int[] range = {666,666};
		int[] precision = {666};
		
		GLES20.glGetShaderPrecisionFormat(GLES20.GL_FRAGMENT_SHADER, GLES20.GL_HIGH_FLOAT, range, 0, precision, 0);
		Log.i(Alw3d.LOG_TAG, "highp float fragment precision is " + precision[0]);
		GLES20.glGetShaderPrecisionFormat(GLES20.GL_FRAGMENT_SHADER, GLES20.GL_MEDIUM_FLOAT, range, 0, precision, 0);
		Log.i(Alw3d.LOG_TAG, "mediump float fragment precision is " + precision[0]);
		GLES20.glGetShaderPrecisionFormat(GLES20.GL_VERTEX_SHADER, GLES20.GL_HIGH_FLOAT, range, 0, precision, 0);
		Log.i(Alw3d.LOG_TAG, "highp float vertex precision is " + precision[0]);
		GLES20.glGetShaderPrecisionFormat(GLES20.GL_VERTEX_SHADER, GLES20.GL_MEDIUM_FLOAT, range, 0, precision, 0);
		Log.i(Alw3d.LOG_TAG, "mediump float vertex precision is " + precision[0]);
		
	}
	
	private void processRenderPasses(List<RenderPass> renderPasses) {
	//	Iterator<RenderPass> it = renderPasses.iterator();
	//	while(it.hasNext()) {
		CheckGlErrorPass checkPass = null;
		synchronized (renderPasses) {
	
			for(RenderPass renderPass : renderPasses) {
				
				if(renderPass.isSilent()) continue;
				
			//	RenderPass renderPass = it.next();
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
					renderScene/*NonOpenGL*/(
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
				else if(renderPass instanceof CheckGlErrorPass) {
					/*CheckGlErrorPass errPass = (CheckGlErrorPass)renderPass;
					checkGlError(errPass.isCauseException(), errPass.getOnGlErrorListener());*/
					checkPass = (CheckGlErrorPass) renderPass;
				}
				
				if(renderPass.isOneTime()) renderPass.setSilent(true);
				
				OnRenderPassFinishedListener onRenderPassFinishedListener =
						renderPass.getOnRenderPassFinishedListener();
				if(onRenderPassFinishedListener != null)
					onRenderPassFinishedListener.onRenderPassFinished(renderPass);
			}
		}
		// Do the onGlError call-back outside of the synchronized block 
		if(checkPass != null) {
			checkGlError(checkPass.isCauseException(), checkPass.getOnGlErrorListener());
		}
	}
	
	private void checkGlError(boolean causeException, OnGlErrorListener errL) {
		int error = 0;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(Alw3d.LOG_TAG, "Check: glError " + error);
			if(errL != null)
				errL.onGlError(error);
		}
		if(causeException && error != 0)
			throw new RuntimeException("Check: glError " + error);
	}

	public void forgetOpenGLContext() {
		geometryManager.reset();
		shaderManager.reset();
		textureManager.reset();
		renderBufferManager.reset();
		fboManager.reset();
	}
	
	private boolean preload(Object obj) {
		if(obj instanceof Geometry)
			return (geometryManager.getGeometryInfo((Geometry) obj) != null);
		else if(obj instanceof ShaderProgram)
			return (shaderManager.getShaderProgramHandle((ShaderProgram) obj) != 0);
		else if(obj instanceof Texture)
			return (textureManager.getTextureHandle((Texture) obj) != 0);
		else if(obj instanceof FBO)
			return (fboManager.getFBOHandle((FBO) obj) != 0);
		else if(obj instanceof Material) {
			for(Entry<String, Texture> texEntry : ((Material)obj).getTextures().entrySet()) {
				preload(texEntry.getValue());
			}
			return preload(((Material)obj).getShaderProgram());
		}
		else if(obj instanceof Node) {
			if(obj instanceof GeometryNode) {
				preload(((GeometryNode)obj).getGeometry());
				preload(((GeometryNode)obj).getMaterial());
			}
			for( Node node : ((Node)obj).getChildren()) {
				preload(node);
			}
			return false;
		}
		else
			return false;
	}
	
	public void requestPreload(Object obj) {
		synchronized (preloadSet) {
			preloadSet.add(obj);
		}
	}

	public OnSurfaceChangedListener getOnSurfaceChangedListener() {
		return onSurfaceChangedListener;
	}

	public void setOnSurfaceChangedListener(
			OnSurfaceChangedListener onSurfaceChangedListener) {
		this.onSurfaceChangedListener = onSurfaceChangedListener;
	}
}
