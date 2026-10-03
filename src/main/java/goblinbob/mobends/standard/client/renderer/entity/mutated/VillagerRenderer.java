package goblinbob.mobends.standard.client.renderer.entity.mutated;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.data.EntityData;
import net.minecraft.world.entity.npc.Villager;

public class VillagerRenderer extends BipedRenderer<Villager>
{

    @Override
    protected void transformLocally(Villager entity, EntityData<?> data, float partialTicks, PoseStack poseStack)
    {
        super.transformLocally(entity, data, partialTicks, poseStack);

        if (entity.isBaby())
        {
            final float undo = 1.0F / getChildScale();
            poseStack.scale(undo, undo, undo);
        }
    }

}
