package betel.alw3d;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import betel.ATest.R;
import betel.alw3d.math.Quaternion;
import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.CameraNode;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.GeometryNode;
import betel.alw3d.renderer.Light;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.Movable;
import betel.alw3d.renderer.MovableGeometryNode;
import betel.alw3d.renderer.Node;
import betel.alw3d.renderer.ShaderProgram.Shader;
import betel.alw3d.renderer.passes.ClearPass;
import betel.alw3d.renderer.passes.SceneRenderPass;
import utils.GeometryLoader;
import utils.StringLoader;
import android.app.Activity;
import android.os.Bundle;

public class Alw3d extends Activity{
	
	public static final String LOG_TAG = "alw3d";
	
	private Model model;
	private View surface;
	private Alw3dSimulator simulator;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Pass context so that /res/raw can accessed.
        // TODO: What happens on resume?
        StringLoader.setContext(this);
        GeometryLoader.setContext(this);
        
        model = new Model();
        surface = new View(this, model);
        setContentView(surface);
       
        setupLevel();
        
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
    
    private void setupLevel() {
    	Node rootNode = new Node();
    	CameraNode cameraNode = new CameraNode(60, 0, 0.01f, 100);

    	
    	Light light = new Light();
    	light.getTransform().setPosition(new Vector3f(-4f,3f, 0f));
    	
    	Geometry geometry = GeometryLoader.loadObj(R.raw.object);
    	
    	Random rand = new Random(74367246l);
    	
    	int numOfNodes = 2;
    	Node[] nodes = new Node[numOfNodes];
    	for(int i = 0; i < numOfNodes; i++) {
    		nodes[i] = new MovableGeometryNode(geometry, Material.DEFAULT);
    		nodes[i].getTransform().getPosition().set(
    				10*(rand.nextFloat()-0.5f), 10*(rand.nextFloat()-0.5f), 10*(rand.nextFloat()-0.5f)-15);
    		((Movable)nodes[i]).getMovement().getRotation().fromAngleAxis(rand.nextFloat()-0.5f,
    				new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
    		
        	rootNode.attach(nodes[i]);
    	}
    	
    	nodes[0].getTransform().getScale().multThis(5f);
    	
    	rootNode.attach(cameraNode);
    	rootNode.attach(light);
    	
    	SceneRenderPass sceneRenderPass = new SceneRenderPass(rootNode, cameraNode);
    	model.addRenderPass(new ClearPass(ClearPass.COLOR_BUFFER_BIT | ClearPass.DEPTH_BUFFER_BIT, null));
    	model.addRenderPass(sceneRenderPass);
    	
    	Set<Node> simNodes = new HashSet<Node>();
    	simNodes.add(rootNode);
    	simulator = new Alw3dSimulator(simNodes);
    	simulator.setSimulation(new Alw3dSimulation(500));
    	model.setSimulator(simulator);
    	simulator.start();
    }
}