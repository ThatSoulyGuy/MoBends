package goblinbob.mobends.mixin.armor;

import goblinbob.mobends.neoforge.mixin.MixinBridge;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorTrimLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
{
    @Inject(
            method = "renderTrim",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void mobends$onRenderTrim(CallbackInfo ci)
    {
    }
}
