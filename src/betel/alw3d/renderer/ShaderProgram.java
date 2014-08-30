package betel.alw3d.renderer;

import java.util.HashSet;
import java.util.Set;

import betel.Alw3D.R;
import android.opengl.GLES20;

import utils.StringLoader;

public class ShaderProgram {
	
	// TODO: NullPointerException
	static public ShaderProgram DEFAULT = null;//new ShaderProgram(Shader.DEFAULT_VERTEX, Shader.DEFAULT_FRAGMENT);

	static public class Shader {
		
		static public Shader DEFAULT_VERTEX = null;
			//new Shader(Type.VERTEX, StringLoader.loadStringExceptionless(R.raw.default_v));
		
		static public Shader DEFAULT_FRAGMENT = null;
			//new Shader(Type.FRAGMENT, StringLoader.loadStringExceptionless(R.raw.default_f));
		
		final public String source;
		final public Type type;
		
		public Shader(Type type, String source) {
			this(type, source, null);
		}
		
		public Shader(Type type, String source, String source_ff) {
			this.type = type;
			this.source = source;
		}

		public enum Type {
			VERTEX(GLES20.GL_VERTEX_SHADER), FRAGMENT(
					GLES20.GL_FRAGMENT_SHADER);
					
			private int type;
					
			Type(int type) {
				this.type = type;
			}
			
			public int getValue() {
				return type;
			}
		}
	}

	final Set<Shader> shaders;
	
	public ShaderProgram(Shader vertexShader, Shader fragmentShader) {
		assert(vertexShader.type == Shader.Type.VERTEX &&
				fragmentShader.type == Shader.Type.FRAGMENT);
		Set<Shader> shaders = new HashSet<Shader>();
		shaders.add(vertexShader);
		shaders.add(fragmentShader);
		this.shaders = shaders;
	}

	public ShaderProgram(Set<Shader> shaders) {
		this.shaders = shaders;
	}

	public Set<Shader> getShaders() {
		return shaders;
	}
}
