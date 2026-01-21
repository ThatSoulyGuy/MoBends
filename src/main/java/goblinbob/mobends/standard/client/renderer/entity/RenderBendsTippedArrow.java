package goblinbob.mobends.standard.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBendsTippedArrow extends RenderBendsArrow<Arrow>
{
    public static final ResourceLocation RES_ARROW = ResourceLocation.parse("textures/entity/projectiles/arrow.png");
    public static final ResourceLocation RES_TIPPED_ARROW = ResourceLocation.parse(
            "textures/entity/projectiles/tipped_arrow.png");

    public RenderBendsTippedArrow(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Arrow entity)
    {
        return entity.getColor() > 0 ? RES_TIPPED_ARROW : RES_ARROW;
    }
}
