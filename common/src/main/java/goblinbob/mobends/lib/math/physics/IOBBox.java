package goblinbob.mobends.lib.math.physics;

import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.vector.IVec3fRead;

public interface IOBBox
{
	
	IVec3fRead getMin();
	IVec3fRead getMax();
	IMat4x4d getTransform();
	
}
