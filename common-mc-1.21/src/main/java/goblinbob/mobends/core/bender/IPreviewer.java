package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.lib.math.vector.IVec3fRead;
import goblinbob.mobends.lib.math.vector.Vec3f;

import java.util.Map;

public interface IPreviewer<D extends EntityData<?>>
{

	void prePreview(D data, String animationToPreview);

	void postPreview(D data, String animationToPreview);

	default IVec3fRead getAnchorPoint() { return Vec3f.ZERO; }

	Map<String, BoneMetadata> getBoneMetadata();

}
