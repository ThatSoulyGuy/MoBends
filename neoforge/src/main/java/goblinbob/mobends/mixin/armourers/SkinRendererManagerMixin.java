package goblinbob.mobends.mixin.armourers;

import goblinbob.mobends.compat.armourers.MoBendsArmatureTransformerManager;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IODataObject;
import moe.plushie.armourers_workshop.core.utils.OpenResourceKey;
import moe.plushie.armourers_workshop.core.utils.OpenResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "moe.plushie.armourers_workshop.core.client.skinrender.SkinRendererManager$TransformerLoaderImpl", remap = false)
public abstract class SkinRendererManagerMixin
{
    @Inject(method = "begin", at = @At("HEAD"), require = 0)
    private void mobends$beginLoading(OpenResourceManager resourceManager, CallbackInfo ci)
    {
        MoBendsArmatureTransformerManager.INSTANCE.clear();
    }

    @Inject(method = "load", at = @At("HEAD"), cancellable = true, require = 0)
    private void mobends$loadArmature(OpenResourceKey name, IODataObject object, CallbackInfo ci)
    {
        final IODataObject type = object.get("type");
        if (type == null)
        {
            return;
        }

        if (!MoBendsArmatureTransformerManager.ARMATURE_TYPE.equals(type.stringValue()))
        {
            return;
        }

        MoBendsArmatureTransformerManager.INSTANCE.append(name, object);
        ci.cancel();
    }

    @Inject(method = "end", at = @At("HEAD"), require = 0)
    private void mobends$endLoading(OpenResourceManager resourceManager, CallbackInfo ci)
    {
        MoBendsArmatureTransformerManager.INSTANCE.freeze();
    }
}
