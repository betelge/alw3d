package tk.betelge.alw3d;

import tk.betelge.alw3d.renderer.Alw3dRenderer;
import tk.betelge.alw3d.renderer.Alw3dRenderer.OnSurfaceChangedListener;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class Alw3dView extends GLSurfaceView {
	
	Alw3dModel model;
	Alw3dRenderer renderer;

	public Alw3dView(Context context, Alw3dModel model) {
		this(context, model, 2);
	}

	public Alw3dView(Context context, Alw3dModel model, int glContextClientVersion) {
		super(context);
		this.model = model;
		
        this.setEGLContextClientVersion(glContextClientVersion);
		renderer = new Alw3dRenderer(model);
		setRenderer(renderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		/*try {
			Thread.sleep(1);
		} catch (InterruptedException e) {} // Not a problem*/
		return super.onTouchEvent(event);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Let renderer know that the OpenGL context is lost.
		// It later will upload new data as needed.
		renderer.forgetOpenGLContext();
	}
	
	// Tries to load Alw3d objects into memory
	public void requestPreload(Object obj) {
		renderer.requestPreload(obj);
	}
	
	public OnSurfaceChangedListener getOnSurfaceChangedListener() {
		return renderer.getOnSurfaceChangedListener();
	}

	public void setOnSurfaceChangedListener(
			OnSurfaceChangedListener onSurfaceChangedListener) {
		renderer.setOnSurfaceChangedListener(onSurfaceChangedListener);
	}

	// TODO: Should this be accessible?
	public Alw3dRenderer getRenderer() {
		return renderer;
	}
}
