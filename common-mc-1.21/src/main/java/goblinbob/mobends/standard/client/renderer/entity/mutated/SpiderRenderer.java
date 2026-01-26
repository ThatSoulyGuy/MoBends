package goblinbob.mobends.standard.client.renderer.entity.mutated;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import net.minecraft.world.entity.monster.Spider;

public class SpiderRenderer<T extends Spider> extends MutatedRenderer<T>
{
    /**
     * Override beforeRender to skip the sandwich transform pattern.
     * For spider, we apply transforms directly in SpiderMutator.renderMutated instead,
     * because the sandwich pattern doesn't work correctly with the 1.20.1 PoseStack nesting.
     */
    @Override
    public void beforeRender(EntityData<T> data, T entity, float partialTicks, PoseStack poseStack)
    {
        // Skip the sandwich transforms - they will be applied in SpiderMutator.renderMutated
        // Only call renderLocalAccessories for any accessories that might need rendering
        this.renderLocalAccessories(entity, data, partialTicks, poseStack);
    }
}
