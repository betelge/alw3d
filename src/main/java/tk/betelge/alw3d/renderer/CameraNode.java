package tk.betelge.alw3d.renderer;

import tk.betelge.alw3d.FrustumVolume;
import tk.betelge.alw3d.math.Vector3f;

public class CameraNode extends MovableNode {
	float fov, aspect, zNear, zFar;
	
	public CameraNode(float fov, float aspect, float zNear, float zFar) {
		this.fov = fov;
		this.aspect = aspect;
		this.zNear = zNear;
		this.zFar = zFar;
	}
	
	public FrustumVolume generateFrustumVolume() {
		Vector3f[] normals = new Vector3f[6];
		float[] distances = new float[6];
		
		// Vertical fov in radians
		float rfov = fov * (float)Math.PI/180;
		
		// Top
		normals[0] = new Vector3f(0f, -(float) Math.cos(rfov/2), -(float) Math.sin(rfov/2));
		distances[0] = 0;
		
		// Bottom
		normals[1] = new Vector3f(0f, (float) Math.cos(-rfov/2), (float) Math.sin(-rfov/2));
		distances[1] = 0;
		
		// Near
		normals[2] = new Vector3f(0f, 0f, -1f);
		distances[2] = zNear;
		
		// Far
		normals[3] = new Vector3f(0f, 0f, 1f);
		distances[3] = -zFar;
		
		// Horizontal fov in radians
		// TODO: aspect is unknown here
		float hfov = 2*(float) Math.cos(/*aspect*/(float)Math.acos(rfov/2) / 2);
		
		// Left
		normals[4] = new Vector3f((float) Math.cos(hfov/2), 0f,-(float) Math.sin(hfov/2));
		distances[4] = 0;
		
		// Right
		normals[5] = new Vector3f(-(float) Math.cos(-hfov/2), 0f,(float) Math.sin(-hfov/2));
		distances[5] = 0;
		
		return new FrustumVolume(normals, distances);
	}

	public float getFov() {
		return fov;
	}

	public void setFov(float fov) {
		this.fov = fov;
	}

	public float getAspect() {
		return aspect;
	}

	public void setAspect(float aspect) {
		this.aspect = aspect;
	}

	public float getzNear() {
		return zNear;
	}

	public void setzNear(float zNear) {
		this.zNear = zNear;
	}

	public float getzFar() {
		return zFar;
	}

	public void setzFar(float zFar) {
		this.zFar = zFar;
	}
}
