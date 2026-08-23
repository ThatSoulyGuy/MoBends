package goblinbob.mobends.neoforge.client.event;

import goblinbob.mobends.standard.client.renderer.entity.RenderBendsSpectralArrow;
import goblinbob.mobends.standard.client.renderer.entity.RenderBendsTippedArrow;
import goblinbob.mobends.standard.client.renderer.entity.RenderBendsTrident;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
public class EntityRendererRegistrar
{
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityType.ARROW, RenderBendsTippedArrow::new);
        event.registerEntityRenderer(EntityType.SPECTRAL_ARROW, RenderBendsSpectralArrow::new);
        event.registerEntityRenderer(EntityType.TRIDENT, RenderBendsTrident::new);
    }
}
