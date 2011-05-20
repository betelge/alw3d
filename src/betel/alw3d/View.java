package betel.alw3d;

import betel.alw3d.renderer.Alw3dRenderer;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class View extends GLSurfaceView {
	
	Model model;
	Alw3dRenderer renderer;

	public View(Context context, Model model) {
		super(context);
		this.model = model;
		
        this.setEGLContextClientVersion(2);
		renderer = new Alw3dRenderer(model);
		setRenderer(renderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}
}
