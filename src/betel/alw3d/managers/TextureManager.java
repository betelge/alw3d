package betel.alw3d.managers;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import android.opengl.GLES20;
import betel.alw3d.renderer.Texture;

public class TextureManager {

	Map<Texture, Integer> textureHandles = new HashMap<Texture, Integer>();
	
	// Direct buffer for communication with OpenGL
	IntBuffer handleBuffer = ByteBuffer.allocateDirect(8).asIntBuffer();

	public int getTextureHandle(Texture texture) {
		if (tryToUpload(texture))
			return textureHandles.get(texture);
		else
			return 0;
	}

	private boolean tryToUpload(Texture texture) {
		if (textureHandles.containsKey(texture))
			return true;

		// Create a texture object
		GLES20.glGenTextures(1, handleBuffer);
		int textureHandle = handleBuffer.get(0);
		GLES20.glBindTexture(texture.getTextureType().getValue(), textureHandle);
		
		// Set both filters
		GLES20.glTexParameteri(texture.getTextureType().getValue(),
				GLES20.GL_TEXTURE_MIN_FILTER, texture.getFilter().getValue());
		GLES20.glTexParameteri(texture.getTextureType().getValue(),
				GLES20.GL_TEXTURE_MAG_FILTER, texture.getFilter().getNoMipMapValue());

		// Set wrap modes
		GLES20.glTexParameteri(texture.getTextureType().getValue(),
				GLES20.GL_TEXTURE_WRAP_S, texture.getWrapMode().getValue());
		GLES20.glTexParameteri(texture.getTextureType().getValue(),
				GLES20.GL_TEXTURE_WRAP_T, texture.getWrapMode().getValue());

		// Upload data
		switch (texture.getTextureType()) {
		case TEXTURE_2D:
			GLES20.glTexImage2D(texture.getTextureType().getValue(), 0, texture
					.getFormat().getInternalFormatValue(), texture.getWidth(),
					texture.getHeight(), 0, texture.getFormat()
							.getExternalFormatValue(), texture.getTexelType()
							.getValue(), texture.getTextureData());
			break;
		default:
			return false;
		}
		
		// Generate mipmaps (TODO: Take it easy with mipmaping on android?)
		GLES20.glGenerateMipmap(
				texture.getTextureType().getValue());

		textureHandles.put(texture, textureHandle);
		return true;
	}

	public void reset() {
		textureHandles.clear();
	}

}
