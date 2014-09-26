package tk.betelge.alw3d.renderer;

import java.nio.ByteBuffer;

import android.opengl.GLES20;

public class Texture extends FBOAttachable{
	
	private ByteBuffer textureData;
	private final TextureType textureType;
	private final TexelType texelType;
	private final Filter filter; // TODO: Separate filters?
	private final WrapMode wrapMode; // TODO: Separate wrap modes?
	
	// -1 if unspecified
	private float mipmapLevel = -1;

	// width and height are in the super class
	private final int depth;

	public enum TextureType {
		TEXTURE_2D(GLES20.GL_TEXTURE_2D);

		private int type;

		private TextureType(int type) {
			this.type = type;
		}

		public int getValue() {
			return type;
		}
	}

	public enum TexelType {
		FLOAT(GLES20.GL_FLOAT), INT(GLES20.GL_INT), UINT(GLES20.GL_UNSIGNED_INT), BYTE(
				GLES20.GL_BYTE), UBYTE(GLES20.GL_UNSIGNED_BYTE), SHORT(
				GLES20.GL_SHORT), USHORT(GLES20.GL_UNSIGNED_SHORT);

		private int type;

		TexelType(int type) {
			this.type = type;
		}

		public int getValue() {
			return type;
		}
	}



	public enum Filter {
		NEAREST(GLES20.GL_NEAREST),
		LINEAR(GLES20.GL_LINEAR),
		NEAREST_MIPMAP_NEAREST(GLES20.GL_NEAREST_MIPMAP_NEAREST, GLES20.GL_NEAREST),
		NEAREST_MIPMAP_LINEAR(GLES20.GL_NEAREST_MIPMAP_LINEAR, GLES20.GL_NEAREST),
		LINEAR_MIPMAP_NEAREST(GLES20.GL_LINEAR_MIPMAP_NEAREST, GLES20.GL_LINEAR),
		LINEAR_MIPMAP_LINEAR(GLES20.GL_LINEAR_MIPMAP_LINEAR, GLES20.GL_LINEAR);

		private int filter;
		private int noMipMapFilter;
		
		private Filter(int filter) {
			this(filter, filter);
		}

		private Filter(int filter, int noMipMapFilter) {
			this.filter = filter;
			this.noMipMapFilter = noMipMapFilter;
		}

		public int getValue() {
			return filter;
		}
		
		public int getNoMipMapValue() {
			return noMipMapFilter;
		}
	}

	public enum WrapMode {
		REPEAT(GLES20.GL_REPEAT), CLAMP_TO_EDGE(
				GLES20.GL_CLAMP_TO_EDGE);

		private int mode;

		private WrapMode(int mode) {
			this.mode = mode;
		}

		public int getValue() {
			return mode;
		}
	}
	
	public Texture(ByteBuffer textureData, TextureType textureType, int width,
			int height, int depth, TexelType texelType, Format format, Filter filter,
			WrapMode wrapMode) {
		super(format, width, height);
		this.textureData = textureData;
		this.textureType = textureType;
		this.depth = depth;
		this.texelType = texelType;
		this.filter = filter;
		this.wrapMode = wrapMode;
	}

	public Texture(ByteBuffer textureData, TextureType textureType, int width,
			int height, TexelType texelType, Format format, Filter filter,
			WrapMode wrapMode) {
		this(textureData, textureType, width, height, 1, texelType, format,
				filter, wrapMode);
	}

	public TextureType getTextureType() {
		return textureType;
	}

	public ByteBuffer getTextureData() {
		return textureData;
	}

	public int getDepth() {
		return depth;
	}

	public TexelType getTexelType() {
		return texelType;
	}

	public Filter getFilter() {
		return filter;
	}

	public WrapMode getWrapMode() {
		return wrapMode;
	}

	public float getMipmapLevel() {
		return mipmapLevel;
	}

	public void setMipmapLevel(float mipmapLevel) {
		this.mipmapLevel = mipmapLevel;
	}

}
