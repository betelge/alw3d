package tk.betelge.alw3d.renderer;

import java.util.ArrayList;
import java.util.List;

import tk.betelge.alw3d.renderer.passes.RenderPass;


public abstract class RenderMultiPass extends RenderPass {

	protected List<RenderPass> renderPasses = new ArrayList<RenderPass>();

	public List<RenderPass> getRenderPasses() {
		return renderPasses;
	}
	
}
