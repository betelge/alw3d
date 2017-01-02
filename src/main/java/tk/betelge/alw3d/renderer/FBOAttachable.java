package tk.betelge.alw3d.renderer;

import android.opengl.GLES20;

public class FBOAttachable {
	private final int width;
	private final int height;
	private final Format format;

	public enum Format {
		GL_RGB(GLES20.GL_RGB, GLES20.GL_RGB, 2),
		GL_RGBA(GLES20.GL_RGBA, GLES20.GL_RGBA, 2),
		
		GL_RGB565(GLES20.GL_RGB, GLES20.GL_RGB565, 2),
		GL_RGBA4(GLES20.GL_RGBA, GLES20.GL_RGBA4, 2),
		GL_RGB5_A1(GLES20.GL_RGBA,GLES20.GL_RGB5_A1, 2),
				
		GL_DEPTH_COMPONENT(0, GLES20.GL_DEPTH_COMPONENT, 2),
		GL_STENCIL_INDEX(0, GLES20.GL_STENCIL_INDEX, 1),
				
		GL_DEPTH_COMPONENT16(GLES20.GL_DEPTH_COMPONENT, GLES20.GL_DEPTH_COMPONENT16, 4);

		private int externalFormatValue;
		private int internalFormatValue;
		private int texelSizeValue;

		private Format(int externalFormatValue, int internalFormatValue,
				int texelSizeValue) {
			this.internalFormatValue = internalFormatValue;
			this.externalFormatValue = externalFormatValue;
			this.texelSizeValue = texelSizeValue;
		}

		public int getExternalFormatValue() {
			return externalFormatValue;
		}

		public int getInternalFormatValue() {
			return internalFormatValue;
		}

		public int getTexelSizeValue() {
			return texelSizeValue;
		}
	};

	public FBOAttachable(Format format, int width, int height) {
		this.format = format;
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Format getFormat() {
		return format;
	}
}
