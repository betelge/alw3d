package tk.betelge.alw3d.renderer.passes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import tk.betelge.alw3d.renderer.FBO;
import tk.betelge.alw3d.renderer.Material;
import tk.betelge.alw3d.renderer.QuadRenderPass;
import tk.betelge.alw3d.renderer.RenderMultiPass;
import tk.betelge.alw3d.renderer.ShaderProgram;
import tk.betelge.alw3d.renderer.Texture;
import tk.betelge.alw3d.renderer.Uniform;
import tk.betelge.alw3d.renderer.FBOAttachable.Format;
import tk.betelge.alw3d.renderer.ShaderProgram.Shader;
import tk.betelge.alw3d.renderer.ShaderProgram.Shader.Type;
import tk.betelge.alw3d.renderer.Texture.Filter;
import tk.betelge.alw3d.renderer.Texture.TexelType;
import tk.betelge.alw3d.renderer.Texture.TextureType;
import tk.betelge.alw3d.renderer.Texture.WrapMode;
import utils.StringLoader;


public class BloomPass extends RenderMultiPass {
	final int width = 512;
	final int height = 512;
	final float mipmapLevel = 2f;
	
	final int vertexFile = 0;// R.raw.direct_v;
/*	final int bloomFragmentFile = "/bloom.fragment";
	final int clampFragmentFile = "/clamp.fragment";
	final int blendFragmentFile = "/blend.fragment";*/

	
	FBO[] fbos = new FBO[2];

	Texture[] textures = new Texture[2];
	
	Material[] materials = new Material[4];
	
	Shader[] shaders = new Shader[4];
	
	Set<Shader> bShaders = new HashSet<Shader>();
	Set<Shader> cShaders = new HashSet<Shader>();
	Set<Shader> dShaders = new HashSet<Shader>();
	
	ShaderProgram shaderProgram;
	ShaderProgram clampShaderProgram;
	ShaderProgram blendShaderProgram;
	
	public BloomPass(Texture texture, int width, int height) {
		this(texture, width, height, null);
	}

	public BloomPass(Texture texture, int width, int height, FBO fbo) {
		setFbo(fbo);
		
		try {
			shaders[0] = new Shader(Type.VERTEX, StringLoader.loadString(vertexFile));
		/*	shaders[1] = new Shader(Type.FRAGMENT, StringLoader.loadString(bloomFragmentFile));
			shaders[2] = new Shader(Type.FRAGMENT, StringLoader.loadString(clampFragmentFile));
			shaders[3] = new Shader(Type.FRAGMENT, StringLoader.loadString(blendFragmentFile));*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bShaders.add(shaders[0]);
		bShaders.add(shaders[1]);
		shaderProgram = new ShaderProgram(bShaders);
		
		cShaders.add(shaders[0]);
		cShaders.add(shaders[2]);
		clampShaderProgram = new ShaderProgram(cShaders);
		
		dShaders.add(shaders[0]);
		dShaders.add(shaders[3]);
		blendShaderProgram = new ShaderProgram(dShaders);
		
		for (int i = 0; i < textures.length; i++) {
			textures[i] = new Texture(null, TextureType.TEXTURE_2D, width, height,
					TexelType.UBYTE, Format.GL_RGB565,
					Filter.LINEAR_MIPMAP_NEAREST, WrapMode.CLAMP_TO_EDGE);
			textures[i].setMipmapLevel(mipmapLevel);
		}

		for (int i = 0; i < fbos.length; i++)
			fbos[i] = new FBO(textures[i], width, height);
		
		materials[2] = new Material(clampShaderProgram);
		materials[2].addUniform(new Uniform("clampValue", 0.8f));
		materials[2].addTexture("source", texture);
		
		materials[0] = new Material(shaderProgram);
		materials[1] = new Material(shaderProgram);
		
		float offset = 1.2f * (float)Math.pow(2, mipmapLevel);
		materials[0].addUniform(new Uniform("offset", offset/(width), 0f));
		materials[1].addUniform(new Uniform("offset", 0f, offset/(height)));
		
		materials[3] = new Material(blendShaderProgram);
		materials[3].addTexture("source", textures[0]);
		materials[3].addTexture("source2", texture);
		
		// Draw from the input texture to fbo0, and do clamping.
		renderPasses.add(new QuadRenderPass(materials[2], fbos[0]));
		
		// Draw from fb0 to fbo1 and do horizontal blur.
		materials[0].addTexture("source", textures[0]);
		renderPasses.add(new QuadRenderPass(materials[0], fbos[1]));
		
		// Draw from fb01 to fbo0 and do vertical blur.
		materials[1].addTexture("source", textures[1]);
		renderPasses.add(new QuadRenderPass(materials[1], fbos[0]));
		
		// Blend fbo0 with original texture and draw to the fbo
		renderPasses.add(new QuadRenderPass(materials[3], fbo));
	}
}
