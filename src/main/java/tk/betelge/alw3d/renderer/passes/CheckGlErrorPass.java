package tk.betelge.alw3d.renderer.passes;

public class CheckGlErrorPass extends RenderPass {
	private boolean causeException;
	private OnGlErrorListener onGlErrorListener = null;

	public CheckGlErrorPass(boolean causeException) {
		super();
		this.causeException = causeException;
	}

	public boolean isCauseException() {
		return causeException;
	}
	
	public OnGlErrorListener getOnGlErrorListener() {
		return onGlErrorListener;
	}

	public void setOnGlErrorListener(OnGlErrorListener onGlErrorListener) {
		this.onGlErrorListener = onGlErrorListener;
	}

	public static interface OnGlErrorListener {
		void onGlError(int error);
	}
}
