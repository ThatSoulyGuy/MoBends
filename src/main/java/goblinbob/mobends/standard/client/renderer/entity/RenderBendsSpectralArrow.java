package goblinbob.mobends.standard.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBendsSpectralArrow extends RenderBendsArrow<SpectralArrow>
{
    public static final ResourceLocation RES_SPECTRAL_ARROW = new ResourceLocation(
            "textures/entity/projectiles/spectral_arrow.png");

    public RenderBendsSpectralArrow(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralArrow entity)
    {
        return RES_SPECTRAL_ARROW;
    }
}
