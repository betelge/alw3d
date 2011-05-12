package betel.alw3d.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.opengl.GLES20;
import betel.alw3d.renderer.ShaderProgram;
import betel.alw3d.renderer.ShaderProgram.Shader;

public class ShaderManager {
	
	Map<ShaderProgram, Integer> shaderProgramHandles = new HashMap<ShaderProgram, Integer>();
	
	/*final private RendererMode rendererMode;
	
	public ShaderManager(RendererMode rendererMode) {
		this.rendererMode = rendererMode;
	}*/
	
	public int getShaderProgramHandle(ShaderProgram shaderProgram) {
		if(tryToUpload(shaderProgram))
			return shaderProgramHandles.get(shaderProgram);
		else
			return 0;
	}
	
	private boolean tryToUpload(ShaderProgram shaderProgram) {
		if(shaderProgramHandles.containsKey(shaderProgram))
			return true;
		
		int shaderProgramHandle = GLES20.glCreateProgram();
		
		Iterator<ShaderProgram.Shader> it = shaderProgram.getShaders().iterator();
		while(it.hasNext()) {
			Shader shader = it.next();
			/*if(shader.type == Type.VERTEX && rendererMode != RendererMode.SHADERS)
				continue;*/
			
			int shaderHandle = GLES20.glCreateShader(shader.type.getValue());
			
			/*if(rendererMode == RendererMode.FIXED_VERTEX && shader.source_ff != null)
				GLES20.glShaderSourceARB(shaderHandle, shader.source_ff);
			else*/
			
			GLES20.glShaderSource(shaderHandle, shader.source);
			GLES20.glCompileShader(shaderHandle);
			
			GLES20.glAttachShader(shaderProgramHandle, shaderHandle);
			
			System.out.println("logShad: " +
					GLES20.glGetShaderInfoLog(shaderHandle));
			
		}
		
		GLES20.glLinkProgram(shaderProgramHandle);
		
		System.out.println("logProg: " + 
				GLES20.glGetShaderInfoLog(shaderProgramHandle));
		
		// TODO: validate
		/*GLES20.glValidateProgram(shaderProgramHandle);
		int validStatus = GLES20.glGetObjectParameteriARB(shaderProgramHandle,
				GLES20.GL_OBJECT_VALIDATE_STATUS_ARB);
		System.out.println("validProgStat: " + validStatus);*/
				
	//	if(validStatus == GLES20.GL_TRUE)
			shaderProgramHandles.put(shaderProgram, shaderProgramHandle);
	/*	else {
			/*System.out.println("Source:");
			Iterator<ShaderProgram.Shader> sit = shaderProgram.getShaders().iterator();
			while(sit.hasNext())
				System.out.println(sit.next().source);
			*/
	/*		if(shaderProgram == ShaderProgram.DEFAULT) {
				System.out.println("Default shader failed validation.");
				return false;
			}
			tryToUpload(ShaderProgram.DEFAULT);
			int defaultHandle = shaderProgramHandles.get(ShaderProgram.DEFAULT);
			shaderProgramHandles.put(shaderProgram, defaultHandle);
		}*/
		
		return true;
	}

}
