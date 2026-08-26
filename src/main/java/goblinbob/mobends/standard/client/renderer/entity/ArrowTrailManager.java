package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.HashMap;

public class ArrowTrailManager
{
    private static HashMap<AbstractArrow, ArrowTrail> trailMap = new HashMap<>();
    public static long time, lastTime;

    static
    {
        time = System.nanoTime() / 1000;
        lastTime = System.nanoTime() / 1000;
    }

    public static ArrowTrail getOrMake(AbstractArrow arrow)
    {
        ArrowTrail trail;
        if (!trailMap.containsKey(arrow))
        {
            trail = new ArrowTrail(arrow);
            trailMap.put(arrow, trail);
        }
        else
        {
            trail = trailMap.get(arrow);
        }

        return trail;
    }

    public static void renderTrail(AbstractArrow entity, PoseStack poseStack, float partialTicks)
    {
        getOrMake(entity).render(poseStack, partialTicks);
    }

    public static void renderExternalTrails(PoseStack poseStack)
    {
        if (!goblinbob.mobends.standard.main.ModConfig.showArrowTrails)
        {
            return;
        }

        final net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null)
        {
            return;
        }

        final net.minecraft.world.phys.Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

        for (final net.minecraft.world.entity.Entity entity : mc.level.entitiesForRendering())
        {
            if (!(entity instanceof AbstractArrow arrow) || isRenderedByMoBends(arrow))
            {
                continue;
            }

            getOrMake(arrow).renderFrom(poseStack, camera.x, camera.y, camera.z);
        }
    }

    private static boolean isRenderedByMoBends(AbstractArrow arrow)
    {
        final net.minecraft.world.entity.EntityType<?> type = arrow.getType();

        return type == net.minecraft.world.entity.EntityType.ARROW
                || type == net.minecraft.world.entity.EntityType.SPECTRAL_ARROW
                || type == net.minecraft.world.entity.EntityType.TRIDENT;
    }

    public static void cleanup()
    {
        trailMap.entrySet().removeIf(e -> e.getValue().shouldBeRemoved());
    }

    public static void onRenderTick()
    {
        for (final ArrowTrail trail : trailMap.values())
        {
            trail.onRenderTick();
        }

        cleanup();
    }
}
