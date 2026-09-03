package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.neoforge.mixin.MixinBridge;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {

    @Inject(method = "translateAndRotate", at = @At("HEAD"))
    private void mobends$compensateScaledPivot(PoseStack poseStack, CallbackInfo ci) {
        MixinBridge.compensateScaledPivot(this, poseStack);
    }
}
