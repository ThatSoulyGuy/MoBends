package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.compat.BetterCombatCompat;
import goblinbob.mobends.core.client.TrailRenderQueue;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class WeaponTrailCapture
{
    private static final Vector3f scratchStart = new Vector3f();
    private static final Vector3f scratchEnd = new Vector3f();
    private static final double[] worldStart = new double[3];
    private static final double[] worldEnd = new double[3];

    private WeaponTrailCapture()
    {
    }

    public static void captureVanillaHand(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext,
                                          HumanoidArm arm, PoseStack poseStack)
    {
        if (entity == null || itemStack.isEmpty() || !ModConfig.showSwordTrail || !TrailRenderQueue.hasFrame())
        {
            return;
        }

        if (!(itemStack.getItem() instanceof SwordItem))
        {
            return;
        }

        final LivingEntityData<?> raw = EntityDatabase.instance.get(entity);
        if (!(raw instanceof BipedEntityData<?> data))
        {
            return;
        }

        final SwordTrail trail = arm == entity.getMainArm() ? data.swordTrail : data.offHandSwordTrail;

        if (!BetterCombatCompat.ownsActiveAnimation(entity))
        {
            trail.stopTracking();
            return;
        }

        final WeaponTrailMetrics.Segment segment = WeaponTrailMetrics.getTrailSegment(itemStack, entity, displayContext);
        final Matrix4f pose = poseStack.last().pose();

        scratchStart.set(segment.startX / 16.0F, segment.startY / 16.0F, segment.startZ / 16.0F);
        scratchEnd.set(segment.endX / 16.0F, segment.endY / 16.0F, segment.endZ / 16.0F);
        pose.transformPosition(scratchStart);
        pose.transformPosition(scratchEnd);

        TrailRenderQueue.viewToWorld(scratchStart.x, scratchStart.y, scratchStart.z, worldStart);
        TrailRenderQueue.viewToWorld(scratchEnd.x, scratchEnd.y, scratchEnd.z, worldEnd);

        trail.trackWorldBlade(worldStart[0], worldStart[1], worldStart[2], worldEnd[0], worldEnd[1], worldEnd[2]);
    }
}
