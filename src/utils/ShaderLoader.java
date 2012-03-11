package utils;

import java.io.IOException;

import betel.alw3d.renderer.ShaderProgram;
import betel.alw3d.renderer.ShaderProgram.Shader;

public class ShaderLoader {
	static public ShaderProgram loadShaderProgram(int vertex, int fragment) {
		Shader vertexShader = new Shader(Shader.Type.VERTEX, 
				StringLoader.loadStringExceptionless(vertex));
		Shader fragmentShader = new Shader(Shader.Type.FRAGMENT, 
				StringLoader.loadStringExceptionless(fragment));
		
		return new ShaderProgram(vertexShader, fragmentShader);
	}
}
