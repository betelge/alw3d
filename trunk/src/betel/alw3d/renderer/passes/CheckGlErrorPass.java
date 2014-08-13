package betel.alw3d.renderer.passes;

public class CheckGlErrorPass extends RenderPass {
	private boolean causeException;

	public CheckGlErrorPass(boolean causeException) {
		super();
		this.causeException = causeException;
	}

	public boolean isCauseException() {
		return causeException;
	}
	
}
