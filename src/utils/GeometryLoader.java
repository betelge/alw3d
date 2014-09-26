package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tk.betelge.alw3d.math.Vector3f;
import tk.betelge.alw3d.renderer.Geometry;
import tk.betelge.alw3d.renderer.Geometry.Attribute;
import tk.betelge.alw3d.renderer.Geometry.Type;


import android.content.Context;

public class GeometryLoader {
	
	static private Context context;
	
	static public Geometry loadObj(int resource) {

		ArrayList<Vector3f> v = new ArrayList<Vector3f>();
		ArrayList<Vector3f> vt = new ArrayList<Vector3f>();
		ArrayList<Vector3f> vn = new ArrayList<Vector3f>();

		ArrayList<Short> indices = new ArrayList<Short>();
		ArrayList<Float> v2 = new ArrayList<Float>();
		ArrayList<Float> vt2 = new ArrayList<Float>();
		ArrayList<Float> vn2 = new ArrayList<Float>();

		try {
			InputStream is = context.getResources().openRawResource(resource);
			if(is == null)
				System.out.println("Cant't load geometry: " + resource);
			else {
				System.out.println("Geometry reads ok");
			}
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			while (br.ready()) {
				String line = br.readLine().trim();

				String[] words = line.split(" ");
				if (words[0].equals("v")) {
					v.add(new Vector3f(Float.parseFloat(words[1]), Float
							.parseFloat(words[2]), Float.parseFloat(words[3])));
				} else if (words[0].equals("vt")) {
					vt.add(new Vector3f(Float.parseFloat(words[1]), Float
							.parseFloat(words[2]), 0f));
				} else if (words[0].equals("vn")) {
					vn.add(new Vector3f(Float.parseFloat(words[1]), Float
							.parseFloat(words[2]), Float.parseFloat(words[3])));
				} else if (words[0].equals("f")) {
					for (int j = 1; j < words.length && j < 5; j++) {
						String[] letters = words[j].split("/");

						// Is this the 4:th vertex of a quad?
						if (j == 4) {
							// Create an extra triangle
							// TODO: Check for short overflow?
							indices.add((short) (v2.size() / 3));
							indices.add((short) (v2.size() / 3 - 3));
							indices.add((short) (v2.size() / 3 - 1));
						} else
							indices.add((short) (v2.size() / 3));

						Vector3f vec;
						if (!v.isEmpty()) {
							vec = v.get(Integer.parseInt(letters[0]) - 1);
							v2.add(vec.x);
							v2.add(vec.y);
							v2.add(vec.z);
						}

						if (!vt.isEmpty()) {
							vec = vt.get(Integer.parseInt(letters[1]) - 1);
							vt2.add(vec.x);
							vt2.add(vec.y);
						}

						if (!vn.isEmpty()) {
							vec = vn.get(Integer.parseInt(letters[2]) - 1);
							vn2.add(vec.x);
							vn2.add(vec.y);
							vn2.add(vec.z);
						}
					}
				}
			}
			br.close();

		} catch (IOException e) {
			System.out.println("Can't read " + resource + ", using a quad instead.");
			return Geometry.QUAD;
		}

		List<Attribute> attributes = new ArrayList<Attribute>();
		ShortBuffer indexBuffer = ShortBuffer.allocate(indices.size());
		Iterator<Short> intIt = indices.iterator();
		while (intIt.hasNext())
			indexBuffer.put(intIt.next());
		indexBuffer.flip();

		if (!v2.isEmpty()) {
			Attribute at = new Attribute();
			at.buffer = FloatBuffer.allocate(v2.size());
			at.name = "position";
			at.size = 3;
			at.type = Type.FLOAT;

			Iterator<Float> fIt = v2.iterator();
			while (fIt.hasNext())
				((FloatBuffer) at.buffer).put(fIt.next());
			at.buffer.flip();

			attributes.add(at);
		}

		if (!vt2.isEmpty()) {
			Attribute at = new Attribute();
			at.buffer = FloatBuffer.allocate(vt2.size());
			at.name = "textureCoord";
			at.size = 2;
			at.type = Type.FLOAT;

			Iterator<Float> fIt = vt2.iterator();
			while (fIt.hasNext())
				((FloatBuffer) at.buffer).put(fIt.next());
			at.buffer.flip();

			attributes.add(at);
		}

		if (!vn2.isEmpty()) {
			Attribute at = new Attribute();
			at.buffer = FloatBuffer.allocate(vn2.size());
			at.name = "normal";
			at.size = 3;
			at.type = Type.FLOAT;

			Iterator<Float> fIt = vn2.iterator();
			while (fIt.hasNext())
				((FloatBuffer) at.buffer).put(fIt.next());
			at.buffer.flip();

			attributes.add(at);
		}

		Geometry geometry = new Geometry(indexBuffer, attributes);

		return geometry;
	}
	
	static public void setContext(Context context) {
		GeometryLoader.context = context;
	}

}
