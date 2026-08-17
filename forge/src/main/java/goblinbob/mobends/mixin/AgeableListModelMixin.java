package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.forge.mixin.MixinBridge;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin<T extends LivingEntity> {

    @Shadow protected abstract Iterable<ModelPart> headParts();

    @Shadow protected abstract Iterable<ModelPart> bodyParts();

    @Shadow @Final private boolean scaleHead;
    @Shadow @Final private float babyHeadScale;
    @Shadow @Final private float babyBodyScale;

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        if ((Object) this instanceof HumanoidModel<?> humanoidModel) {
            if (MixinBridge.shouldRenderBipedCustom()) {
                MixinBridge.setBabyHeadScale(mobends$babyHeadScale(humanoidModel.young));
                MixinBridge.renderBipedMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                ci.cancel();
            }
            else if (MixinBridge.mayRenderBipedOverlay()) {
                Set<ModelPart> renderedParts = mobends$renderedParts();
                if (MixinBridge.shouldRenderBipedOverlay(humanoidModel, renderedParts)) {
                    MixinBridge.renderBipedOverlay(humanoidModel, renderedParts, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    ci.cancel();
                }
            }
        }
        else if ((Object) this instanceof WolfModel<?> wolfModel) {
            if (MixinBridge.shouldRenderWolfCustom()) {
                MixinBridge.setWolfBabyHeadScale(mobends$babyHeadScale(wolfModel.young));
                MixinBridge.renderWolfMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                ci.cancel();
            }
        }
    }

    @Unique
    private Set<ModelPart> mobends$renderedParts() {
        Set<ModelPart> parts = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ModelPart part : headParts()) {
            parts.add(part);
        }
        for (ModelPart part : bodyParts()) {
            parts.add(part);
        }
        return parts;
    }

    @Unique
    private float mobends$babyHeadScale(boolean young) {
        if (!young) {
            return 1.0F;
        }
        float headScale = this.scaleHead ? 1.5F / this.babyHeadScale : 1.0F;
        return headScale * this.babyBodyScale;
    }

}
