package betel.alw3d;

import betel.alw3d.renderer.Alw3dRenderer;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class Alw3dView extends GLSurfaceView {
	
	Alw3dModel model;
	Alw3dRenderer renderer;

	public Alw3dView(Context context, Alw3dModel model) {
		super(context);
		this.model = model;
		
        this.setEGLContextClientVersion(2);
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
}
