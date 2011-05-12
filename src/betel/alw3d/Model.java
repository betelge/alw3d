package betel.alw3d;

import utils.GeometryLoader;
import betel.alw3d.managers.ShaderManager;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.ShaderProgram;

public class Model {
	//private int width, height;
	
	//TODO: This fails. Handle it.
	public Geometry geometry = GeometryLoader.loadObj("/object.obj");
	//public String
	
}
