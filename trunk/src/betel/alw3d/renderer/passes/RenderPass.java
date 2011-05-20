package betel.alw3d.renderer.passes;

import betel.alw3d.renderer.FBO;

public class RenderPass {
	private FBO fbo = null;

	public void setFbo(FBO fbo) {
		this.fbo = fbo;
	}

	public FBO getFbo() {
		return fbo;
	}
}
