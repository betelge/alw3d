package tk.betelge.alw3d.renderer;

import tk.betelge.alw3d.renderer.passes.RenderPass;

public class QuadRenderPass extends RenderPass {
	private Material material;
		
	public QuadRenderPass(Material material) {
		this(material, null);
	}
	
	public QuadRenderPass(Material material, FBO fbo) {
		this.material = material;
		setFbo(fbo);
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}
}
