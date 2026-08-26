package goblinbob.mobends.lib.client.model;

import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;

public interface IAnimatedPart
{

    IVec3f getOffset();

    SmoothOrientation getRotation();

}
