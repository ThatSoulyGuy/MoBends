package goblinbob.mobends.lib.client.model;

import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;

/**
 * The render-free view of a model part that the animation runtime writes into.
 *
 * <p>Implemented in the mod by {@code goblinbob.mobends.core.client.model.IModelPart}, which adds
 * the PoseStack-bound rendering half. Of that interface's eighteen methods the animation runtime
 * calls exactly these two, and neither has Minecraft in its signature — which is what lets the
 * runtime live in this module at all.
 *
 * <p><b>Do not make {@code SmoothOrientation} implement this.</b> {@code EntityData} puts bare
 * {@code SmoothOrientation} objects into its part map alongside real parts, and the runtime relies
 * on {@code instanceof IAnimatedPart} to filter them out. Widening the check would change which
 * bones the keyframe and procedural layers touch.
 */
public interface IAnimatedPart
{

    IVec3f getOffset();

    SmoothOrientation getRotation();

}
