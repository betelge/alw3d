package tk.betelge.alw3d.renderer.passes;

import tk.betelge.alw3d.renderer.FBO;

public class RenderPass {
	private FBO fbo = null;
	private boolean isSilent = false;
	private boolean isOneTime = false;
	private OnRenderPassFinishedListener onRenderPassFinishedListener = null;
	
	public interface OnRenderPassFinishedListener {
		public void onRenderPassFinished(RenderPass pass);
	}
	
	public void setFbo(FBO fbo) {
		this.fbo = fbo;
	}

	public FBO getFbo() {
		return fbo;
	}

	public boolean isSilent() {
		return isSilent;
	}

	public void setSilent(boolean isOneTime) {
		this.isSilent = isOneTime;
	}

	public boolean isOneTime() {
		return isOneTime;
	}

	public void setOneTime(boolean isOneTime) {
		this.isOneTime = isOneTime;
	}

	public OnRenderPassFinishedListener getOnRenderPassFinishedListener() {
		return onRenderPassFinishedListener;
	}

	public void setOnRenderPassFinishedListener(
			OnRenderPassFinishedListener onRenderPassFinishedListener) {
		this.onRenderPassFinishedListener = onRenderPassFinishedListener;
	}
}
