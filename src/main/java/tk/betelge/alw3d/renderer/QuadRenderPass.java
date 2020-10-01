package tk.betelge.alw3d.renderer;

import tk.betelge.alw3d.renderer.passes.RenderPass;

public class QuadRenderPass extends RenderPass {
	private Material material;
	private boolean useBigTriangle;
		
	public QuadRenderPass(Material material) {
		this(material, null, false);
	}

	public QuadRenderPass(Material material, boolean useBigTriangle) {
		this(material, null, useBigTriangle);
	}
	
	public QuadRenderPass(Material material, FBO fbo) {
		this(material, fbo, false);
	}

	public QuadRenderPass(Material material, FBO fbo, boolean useBigTriangle) {
		this.material = material;
		setFbo(fbo);
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	public boolean isUseBigTriangle() {
		return useBigTriangle;
	}
}
