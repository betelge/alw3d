package tk.betelge.alw3d.renderer;

public class GeometryNode extends Node {
	
	private Geometry geometry;
	private Material material;
	private boolean visible = true;

	public GeometryNode(Geometry geometry, Material material) {
		this.geometry = geometry;
		this.material = material;
	}
	
	public Geometry getGeometry() {
		return geometry;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
