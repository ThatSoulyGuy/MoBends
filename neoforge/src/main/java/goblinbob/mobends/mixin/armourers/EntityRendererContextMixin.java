package goblinbob.mobends.mixin.armourers;

import goblinbob.mobends.api.skeleton.MoBendsAPI;
import goblinbob.mobends.compat.armourers.MoBendsArmatureTransformerManager;
import moe.plushie.armourers_workshop.api.client.IEntityModel;
import moe.plushie.armourers_workshop.core.armature.ArmatureTransformerManager;
import moe.plushie.armourers_workshop.core.client.bake.BakedArmatureTransformer;
import moe.plushie.armourers_workshop.core.client.other.EntityRendererContext;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(value = EntityRendererContext.class, remap = false)
public abstract class EntityRendererContextMixin
{
    @Shadow
    public abstract EntityType<?> entityType();

    @Shadow
    public abstract BakedArmatureTransformer createTransformer(IEntityModel<?> entityModel,
                                                               ArmatureTransformerManager transformerManager);

    @Unique
    private final Map<IEntityModel<?>, BakedArmatureTransformer> mobends$transformers = new IdentityHashMap<>();

    @Inject(method = "getTransformer", at = @At("HEAD"), cancellable = true, require = 0)
    private void mobends$useMoBendsArmature(IEntityModel<?> entityModel,
                                            CallbackInfoReturnable<BakedArmatureTransformer> cir)
    {
        if (entityModel == null || !MoBendsAPI.isAvailable())
        {
            return;
        }

        if (entityType() != EntityType.PLAYER)
        {
            return;
        }

        final BakedArmatureTransformer transformer = mobends$transformers.computeIfAbsent(entityModel,
                model -> createTransformer(model, MoBendsArmatureTransformerManager.INSTANCE));

        if (transformer != null)
        {
            cir.setReturnValue(transformer);
        }
    }
}
