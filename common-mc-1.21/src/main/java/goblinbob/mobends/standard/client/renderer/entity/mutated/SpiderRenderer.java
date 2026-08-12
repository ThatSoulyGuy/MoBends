package goblinbob.mobends.standard.client.renderer.entity.mutated;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import net.minecraft.world.entity.monster.Spider;

public class SpiderRenderer<T extends Spider> extends MutatedRenderer<T>
{
    @Override
    public void beforeRender(EntityData<T> data, T entity, float partialTicks, PoseStack poseStack)
    {
        this.renderLocalAccessories(entity, data, partialTicks, poseStack);
    }
}
