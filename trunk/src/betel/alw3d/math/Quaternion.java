
package betel.alw3d.math;

import java.nio.FloatBuffer;


public class Quaternion
{
	public float x, y, z, w;
	
	public static final Quaternion UNIT = new Quaternion(0f, 0f, 0f, 1f);

	public Quaternion()
	{
		x = y = z = 0;
		w = 1;
	}

	public Quaternion(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Quaternion(Quaternion quaternion)
	{
		this.x = quaternion.x;
		this.y = quaternion.y;
		this.z = quaternion.z;
		this.w = quaternion.w;
	}

	public void set(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void set(Quaternion quaternion)
	{
		this.x = quaternion.x;
		this.y = quaternion.y;
		this.z = quaternion.z;
		this.w = quaternion.w;
	}

	public Quaternion mult(Quaternion quaternion)
	{
		Quaternion q = new Quaternion(this);
		q.multThis(quaternion);
		return q;
	}

	public void multThis(Quaternion quaternion)
	{
		float tempX =
				x * quaternion.w + y * quaternion.z - z * quaternion.y + w
						* quaternion.x;
		float tempY =
				-x * quaternion.z + y * quaternion.w + z * quaternion.x + w
						* quaternion.y;
		float tempZ =
				x * quaternion.y - y * quaternion.x + z * quaternion.w + w
						* quaternion.z;
		this.w =
				-x * quaternion.x - y * quaternion.y - z * quaternion.z + w
						* quaternion.w;
		this.x = tempX;
		this.y = tempY;
		this.z = tempZ;
	}

	public Vector3f mult(Vector3f vector)
	{
		Vector3f result = new Vector3f();
		mult(vector, result);

		return result;
	}

	// TODO: Fix this shit! Java::new is the enemy. Keep it thread safe!
	public void mult(Vector3f vector, Vector3f result)
	{
	/*	result.x =
				(1 - 2*y*y - 2*z*z) * vector.x
				+ 2*(y*x - z*w) * vector.y
				+ 2*(z*x + y*w) * vector.z;
		result.y =
				2*(x*y - w*z) * vector.x
				+ (1 - 2*x*x - 2*z*z) * vector.y
				+ 2*(z*y - 2*x*w) * vector.z;
		result.z =
				2*(x*z - w*y) * vector.x
				+ 2*(y*z + w*x) * vector.y
				+ (1 - 2*x*x - y*y) * vector.z;*/
		
		Vector3f vn = new Vector3f(vector);
		float length = vector.getLength();
		vn.normalizeThis();
		Quaternion vecQuat = new Quaternion(vn.x, vn.y, vn.z, 0f);
		
		Quaternion resQuat = vecQuat.mult(this.conjugate());
		resQuat = this.mult(resQuat);
		
		result.x = resQuat.x;
		result.y = resQuat.y;
		result.z = resQuat.z;
		result.normalizeThis();
		result.multThis(length);
	}

	public Quaternion inverse()
	{
		Quaternion q = new Quaternion(this);
		q.inverseThis();
		return q;
	}

	public void inverseThis()
	{
		float inverseNorm = 1 / getNorm();
		this.x *= -inverseNorm;
		this.y *= -inverseNorm;
		this.z *= -inverseNorm;
		this.w *= inverseNorm;
	}
	
	public Quaternion conjugate()
	{
		return new Quaternion(-this.x, -this.y, -this.z, this.w);
	}

	public float getAngleAxis(Vector3f axis)
	{
		float angle = 2 * (float) Math.acos(w);

		if(angle == 0)
		{
			// arbitrary axis
			axis.set(Vector3f.UNIT_X);
			return 0;
		}
		else if(angle == (float) Math.PI)
		{
			axis.x = x;
			axis.y = y;
			axis.z = z;
		}
		else
		{
			float factor = x * x + y * y + z * z;
			if(factor == 0)
			{
				axis.set(Vector3f.UNIT_X);
				return 0;
			}
			factor = 1 / (float) Math.sqrt(factor);
			axis.x = factor * x;
			axis.y = factor * y;
			axis.z = factor * z;
		}

		return angle;
	}

	public Quaternion fromAngleAxis(float angle, Vector3f axis)
	{
		return fromAngleNormalAxis(angle, axis.normalize());
	}

	public Quaternion fromAngleNormalAxis(float angle, Vector3f axis)
	{
		float sin = (float) Math.sin(0.5 * angle);
		x = sin * axis.x;
		y = sin * axis.y;
		z = sin * axis.z;
		w = (float) Math.cos(0.5 * angle);

		return this;
	}

	// TODO: not thread safe.
	private static Quaternion q3 = new Quaternion();
	public static Quaternion slerp(Quaternion q1, Quaternion q2, float value, Quaternion result)
	{
		//TODO: This is just a tmep workaround for "exploding" objects.
		if( value > 1 ) value = 1;

		// Cos( 1/2 (angle between the quaternions) )
		float cosHalfAngle =
				q1.x * q2.x + q1.y * q2.y + q1.z * q1.z + q1.w * q2.w;

		if(Math.abs(cosHalfAngle) == 1f)
		{
			// q1 == q2
			result.set(q1);
			return result;
		}

		q3.set(q2);
		if(cosHalfAngle < 0f)
		{
			q3.x = -q3.x;
			q3.y = -q3.y;
			q3.z = -q3.z;
			q3.w = -q3.w;
			cosHalfAngle = -cosHalfAngle;
		}

		float scale1 = 1 - value;
		float scale2 = value;

		if(1 - cosHalfAngle > 0.05f)
		{
			float halfAngle = (float) Math.acos(cosHalfAngle);
			float sinHalfAngle =
					(float) Math.sqrt(1.0 - cosHalfAngle * cosHalfAngle);

			scale1 = (float) Math.sin(scale1 * halfAngle) / sinHalfAngle;
			scale2 = (float) Math.sin(scale2 * halfAngle) / sinHalfAngle;
		}

		result.set(scale1 * q1.x + scale2 * q3.x,
				scale1 * q1.y + scale2 * q3.y, scale1 * q1.z + scale2 * q3.z,
				scale1 * q1.w + scale2 * q3.w);

		return result;
	}

	public void lookAt(Vector3f direction, Vector3f up)
	{
		set(0, 0, 0, 1);
		Quaternion otherRot = new Quaternion();

		Vector3f normalizedDirection = direction.normalize();
		Vector3f axis =
				Vector3f.UNIT_Z.cross(normalizedDirection).mult(-1f);
		float angle =
				(float) Math.acos(-Vector3f.UNIT_Z.dot(normalizedDirection));
		otherRot.fromAngleAxis(angle, axis);

		otherRot.normalizeThis();

		Vector3f newUp =
				normalizedDirection.cross(up.cross(normalizedDirection))
						.normalize();
		angle =
				(float) Math.acos(-otherRot.mult(Vector3f.UNIT_Y).dot(
						newUp));
		fromAngleAxis(angle, newUp.cross(otherRot.mult(Vector3f.UNIT_Y)));

		multThis(otherRot);

		normalizeThis();

	}

	public float getNorm()
	{
		return x * x + y * y + z * z + w * w;
	}

	public Quaternion normalize()
	{
		Quaternion result = new Quaternion(this);
		result.normalizeThis();
		return result;
	}

	public void normalizeThis()
	{
		float inverseNorm = 1f / (float) Math.sqrt(getNorm());
		x *= inverseNorm;
		y *= inverseNorm;
		z *= inverseNorm;
		w *= inverseNorm;
	}

	public void toMatrix3(FloatBuffer buf) {
		buf.clear();
		
		buf.put(1 - 2 * (y*y + z*z));
		buf.put(2 * (x*y + z*w));
		buf.put(2 * (x*z - y*w));
		
		buf.put(2 * (x*y - z*w));
		buf.put(1 - 2* (x*x + z*z));
		buf.put(2 * (y*z + x*w));
		
		buf.put(2 * (x*z + y*w));
		buf.put(2 * (y*z - x*w));
		buf.put(1 - 2 * (x*x + y*y));
		
		buf.flip();
	}
	
	public float[] toMatrix3(float[] m) {
		//float[] m = new float[9];
		
		m[0] = 1 - 2 * (y*y + z*z);
		m[1] = 2 * (x*y + z*w);
		m[2] = 2 * (x*z - y*w);
		
		m[3] = 2 * (x*y - z*w);
		m[4] = 1 - 2* (x*x + z*z);
		m[5] = 2 * (y*z + x*w);
		
		m[6] = 2 * (x*z + y*w);
		m[7] = 2 * (y*z - x*w);
		m[8] = 1 - 2 * (x*x + y*y);
		
		return m;
	}

	public String toString()
	{
		return "" + x + ", " + y + ", " + z + ", " + w;
	}

	public boolean equals(Object object)
	{
		if(this == object)
		{
			return true;
		}

		if(!(object instanceof Quaternion) || object == null)
		{
			return false;
		}

		return this.x == ((Quaternion) object).x
				&& this.y == ((Quaternion) object).y
				&& this.z == ((Quaternion) object).z
				&& this.w == ((Quaternion) object).w;
	}

	public int hashCode()
	{
		return Float.floatToIntBits(x) ^ Float.floatToIntBits(y)
				^ Float.floatToIntBits(z) ^ Float.floatToIntBits(w);
	}
}
