package goblinbob.mobends.mixin;

import goblinbob.mobends.standard.animation.bit.biped.RowingAnimationBit;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatModel.class)
public abstract class BoatModelMixin
{
    @Shadow
    @Final
    private ModelPart leftPaddle;

    @Shadow
    @Final
    private ModelPart rightPaddle;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/vehicle/Boat;FFFFF)V", at = @At("TAIL"))
    private void mobends$animateOars(Boat boat, float partialTicks, float limbSwingAmount, float ageInTicks,
                                     float netHeadYaw, float headPitch, CallbackInfo ci)
    {
        RowingAnimationBit.animateOars(boat, leftPaddle, rightPaddle, partialTicks);
    }
}
