package betel.alw3d;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.CameraNode;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Light;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.Movable;
import betel.alw3d.renderer.MovableGeometryNode;
import betel.alw3d.renderer.Node;
import betel.alw3d.renderer.passes.ClearPass;
import betel.alw3d.renderer.passes.SceneRenderPass;
import utils.GeometryLoader;
import utils.StringLoader;
import android.app.Activity;
import android.os.Bundle;

public class Alw3d extends Activity{
	
	public static final String LOG_TAG = "alw3d";
	
	private Alw3dModel model;
	private Alw3dView surface;
	private Alw3dSimulator simulator;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Pass context so that /res/raw can accessed.
        // TODO: What happens on resume?
        StringLoader.setContext(this);
        GeometryLoader.setContext(this);
        
        model = new Alw3dModel();
        surface = new Alw3dView(this, model);
        setContentView(surface);
               
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	surface.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	surface.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(simulator != null)
    		simulator.exit();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	/*if(simulator != null)
    		simulator.exit();*/
    }
    
}