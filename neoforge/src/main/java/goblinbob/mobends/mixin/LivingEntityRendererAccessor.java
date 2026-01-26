package goblinbob.mobends.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Accessor mixin for LivingEntityRenderer to expose internal fields.
 */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor<T extends LivingEntity, M extends EntityModel<T>> {

    /**
     * Gets the list of render layers.
     * @return The render layers list
     */
    @Accessor("layers")
    List<RenderLayer<T, M>> mobends$getLayers();
}
