package goblinbob.mobends.forge.client.event;

import goblinbob.mobends.standard.client.renderer.entity.RenderBendsSpectralArrow;
import goblinbob.mobends.standard.client.renderer.entity.RenderBendsTippedArrow;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
public class EntityRendererRegistrar
{
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityType.ARROW, RenderBendsTippedArrow::new);
        event.registerEntityRenderer(EntityType.SPECTRAL_ARROW, RenderBendsSpectralArrow::new);
    }
}
