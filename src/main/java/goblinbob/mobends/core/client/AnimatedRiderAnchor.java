package goblinbob.mobends.core.client;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public final class AnimatedRiderAnchor
{
    private static final float MODEL_TO_BLOCKS = 1.0F / 16.0F;

    private static final float BODY_REST_Y = 12.0F;
    private static final float HEAD_REST_LOCAL_Y = -12.0F;

    private static final float HEAD_ANCHOR_RATIO = 0.6F;
    private static final double NEGLIGIBLE = 1.0E-4;

    private AnimatedRiderAnchor()
    {
    }

    @Nullable
    public static Vec3 getRenderOffset(Entity rider, float partialTicks)
    {
        if (!(rider.getVehicle() instanceof LivingEntity host)) return null;
        if (!BenderHelper.isEntityAnimated(host)) return null;

        LivingEntityData<?> data = EntityDatabase.instance.get(host);
        if (!(data instanceof BipedEntityData<?> biped)) return null;

        ModelPartTransform body = biped.body;
        ModelPartTransform head = biped.head;
        if (body == null) return null;

        Vector3f delta = new Vector3f(
                body.position.x + body.offset.x,
                body.position.y + body.offset.y - BODY_REST_Y,
                body.position.z + body.offset.z);

        if (head != null && anchorsToHead(rider, host))
        {
            Vector3f headLocal = new Vector3f(
                    head.position.x + head.offset.x,
                    head.position.y + head.offset.y,
                    head.position.z + head.offset.z);

            Quaternion bodyRotation = body.rotation.getSmooth();
            new Quaternionf(bodyRotation.x, bodyRotation.y, bodyRotation.z, bodyRotation.w)
                    .transform(headLocal);

            delta.add(headLocal.x, headLocal.y - HEAD_REST_LOCAL_Y, headLocal.z);
        }

        return toWorldOffset(delta, host, partialTicks);
    }

    private static boolean anchorsToHead(Entity rider, LivingEntity host)
    {
        float hostHeight = host.getBbHeight();
        if (hostHeight <= 0.0F) return true;

        return (rider.getY() - host.getY()) > hostHeight * HEAD_ANCHOR_RATIO;
    }

    @Nullable
    private static Vec3 toWorldOffset(Vector3f modelDelta, LivingEntity host, float partialTicks)
    {
        double localX = -modelDelta.x * MODEL_TO_BLOCKS;
        double localY = -modelDelta.y * MODEL_TO_BLOCKS;
        double localZ = modelDelta.z * MODEL_TO_BLOCKS;

        if (Math.abs(localX) < NEGLIGIBLE && Math.abs(localY) < NEGLIGIBLE && Math.abs(localZ) < NEGLIGIBLE)
        {
            return null;
        }

        float bodyYaw = Mth.rotLerp(partialTicks, host.yBodyRotO, host.yBodyRot);
        float angle = (180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);

        return new Vec3(
                localX * cos + localZ * sin,
                localY,
                -localX * sin + localZ * cos);
    }
}
