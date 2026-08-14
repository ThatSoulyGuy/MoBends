package goblinbob.mobends.mixin.armourers;

import goblinbob.mobends.compat.armourers.AWHiddenParts;
import moe.plushie.armourers_workshop.api.client.model.IModelPart;
import moe.plushie.armourers_workshop.core.client.render.state.ModelVisibilityState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = ModelVisibilityState.class, remap = false)
public abstract class ModelVisibilityStateMixin
{
    @Shadow
    @Final
    private ArrayList<IModelPart> applying;

    @Inject(method = "setVisible", at = @At("HEAD"), require = 0)
    private void mobends$recordVisibility(boolean visible, CallbackInfo ci)
    {
        if (this.applying.isEmpty())
        {
            return;
        }

        if (visible)
        {
            AWHiddenParts.show(this.applying);
        }
        else
        {
            AWHiddenParts.hide(this.applying);
        }
    }
}
