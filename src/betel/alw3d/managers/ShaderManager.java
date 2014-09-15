package betel.alw3d.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.opengl.GLES20;
import android.util.Log;
import betel.alw3d.Alw3d;
import betel.alw3d.renderer.ShaderProgram;
import betel.alw3d.renderer.ShaderProgram.Shader;

public class ShaderManager {
	
	Map<ShaderProgram, Integer> shaderProgramHandles = new HashMap<ShaderProgram, Integer>();
	// Tracks shader handles attached to a shader program handle
	Map<Integer, Set<Integer>> shaderShaderHandles = new HashMap<Integer, Set<Integer>>();
	
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
	
	// TODO: Binaries? Better validation?
	private boolean tryToUpload(ShaderProgram shaderProgram) {
		if(shaderProgramHandles.containsKey(shaderProgram))
			return true;
		
		int shaderProgramHandle = GLES20.glCreateProgram();
		Set<Integer> shaderHandles = new HashSet<Integer>();
		
		Iterator<ShaderProgram.Shader> it = shaderProgram.getShaders().iterator();
		while(it.hasNext()) {
			Shader shader = it.next();
			/*if(shader.type == Type.VERTEX && rendererMode != RendererMode.SHADERS)
				continue;*/
			
			int shaderHandle = GLES20.glCreateShader(shader.type.getValue());
			shaderHandles.add(shaderHandle);
						
			/*if(rendererMode == RendererMode.FIXED_VERTEX && shader.source_ff != null)
				GLES20.glShaderSourceARB(shaderHandle, shader.source_ff);
			else*/
			
			GLES20.glShaderSource(shaderHandle, shader.source);
			GLES20.glCompileShader(shaderHandle);
			
			int[] length = new int[1];
			int[] status = new int[1];
			
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_INFO_LOG_LENGTH, length, 0);
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, status, 0);
			
			Log.w(Alw3d.LOG_TAG, "logShad, Length: " + length[0] +" Status: " + status[0] + " " +
					GLES20.glGetShaderInfoLog(shaderHandle));
			
			GLES20.glAttachShader(shaderProgramHandle, shaderHandle);
			
		}
		
		GLES20.glLinkProgram(shaderProgramHandle);
		
		Log.w(Alw3d.LOG_TAG, "logProg: " + 
				GLES20.glGetProgramInfoLog(shaderProgramHandle));
		
		// TODO: validate
		GLES20.glValidateProgram(shaderProgramHandle);
		int[] validStatus = new int[1]; 
		GLES20.glGetProgramiv(shaderProgramHandle,
			GLES20.GL_VALIDATE_STATUS, validStatus, 0);
		Log.w(Alw3d.LOG_TAG, ((Integer) validStatus[0]).toString());
		
		shaderShaderHandles.put(shaderProgramHandle, shaderHandles);
				
	//	if(validStatus[0] == GLES20.GL_TRUE)
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
	
	public void freeVMEM() {
		for(ShaderProgram program : shaderProgramHandles.keySet()) {
			
			int programHandle = shaderProgramHandles.get(program);
			for(Integer shaderHandle : shaderShaderHandles.get(programHandle)) {
				GLES20.glDeleteShader(shaderHandle);
			}
			GLES20.glDeleteProgram(programHandle);
		}
		reset();
	}

	public void reset() {
		shaderShaderHandles.clear();
		shaderProgramHandles.clear();
	}

}
