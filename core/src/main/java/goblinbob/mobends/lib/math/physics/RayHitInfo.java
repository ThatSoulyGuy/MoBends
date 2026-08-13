package goblinbob.mobends.lib.math.physics;

import goblinbob.mobends.lib.math.vector.IVec3fRead;
import goblinbob.mobends.lib.math.vector.Vec3fReadonly;

public class RayHitInfo
{

	public final Vec3fReadonly hitPoint;

	public RayHitInfo(IVec3fRead hitPoint)
	{
		this.hitPoint = new Vec3fReadonly(hitPoint);
	}

	public RayHitInfo(float x, float y, float z)
	{
		this.hitPoint = new Vec3fReadonly(x, y, z);
	}

}
