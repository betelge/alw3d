package betel.alw3d;

import java.util.LinkedList;
import java.util.List;

import betel.alw3d.renderer.passes.RenderPass;

public class Model {
	private int width, height;
	
	List<RenderPass> renderPasses;
	
	private Alw3dSimulator simulator;

	public List<RenderPass> getRenderPasses() {
		return renderPasses;
	}
	
	public void setRenderPasses(List<RenderPass> renderPasses) {
		this.renderPasses = renderPasses;
	}
	
	public void addRenderPass(RenderPass renderPass) {
		if(this.renderPasses == null)
			renderPasses = new LinkedList<RenderPass>();
		this.renderPasses.add(renderPass);
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setSimulator(Alw3dSimulator simulator) {
		this.simulator = simulator;
	}

	public Alw3dSimulator getSimulator() {
		return simulator;
	}
	
}
