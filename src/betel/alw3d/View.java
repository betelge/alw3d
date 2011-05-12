package betel.alw3d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class View extends GLSurfaceView {
	
	Model model;
	TestRenderer renderer;

	public View(Context context, Model model) {
		super(context);
		this.model = model;
		
        this.setEGLContextClientVersion(2);
		renderer = new TestRenderer(context, model);
		setRenderer(renderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}
}
