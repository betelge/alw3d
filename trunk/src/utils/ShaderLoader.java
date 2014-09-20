package utils;

import betel.alw3d.renderer.ShaderProgram;
import betel.alw3d.renderer.ShaderProgram.Shader;

public class ShaderLoader {
	static public ShaderProgram loadShaderProgram(int vertex, int fragment) {
		return loadShaderProgram(vertex, fragment, null, null);
	}
	
	static public ShaderProgram loadShaderProgram(int vertex, int fragment, int[] includes) {
		return loadShaderProgram(vertex, fragment, includes, includes);
	}
	
	static public ShaderProgram loadShaderProgram(int vertex, int fragment,
			int[] vertexIncludes, int[] fragmentIncludes) {
		
		Shader vertexShader = new Shader(Shader.Type.VERTEX, 
				StringLoader.loadStringExceptionless(vertex) + loadIncludes(vertexIncludes));
		Shader fragmentShader = new Shader(Shader.Type.FRAGMENT, 
				StringLoader.loadStringExceptionless(fragment) + loadIncludes(fragmentIncludes));
		
		return new ShaderProgram(vertexShader, fragmentShader);
	}
	
	static private String loadIncludes(int[] includes) {
		String includeSource = "";
		if(includes != null)
			for(int include : includes)
				includeSource += StringLoader.loadStringExceptionless(include);
		return includeSource;
	}
}
