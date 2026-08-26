package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.TrailRenderQueue;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.lib.util.GUtil;
import goblinbob.mobends.core.util.IColorRead;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Supplier;

public class SwordTrail
{
    protected final Supplier<IColorRead> baseColor;
    protected LinkedList<TrailPart> trailPartList = new LinkedList<>();

    public SwordTrail(Supplier<IColorRead> baseColor)
    {
        this.baseColor = baseColor;
    }

    public void reset()
    {
        trailPartList.clear();
    }

    protected static class TrailPart
    {
        protected HumanoidArm primaryHand;
        protected IColorRead baseColor;

        protected ModelPartTransform body;
        protected ModelPartTransform arm;
        protected ModelPartTransform foreArm;

        protected Quaternion renderRotation = new Quaternion();
        protected Vec3f renderOffset = new Vec3f();
        protected Quaternion itemRotation = new Quaternion();
        protected Vec3f position = new Vec3f();

        protected float velocityX, velocityY, velocityZ;
        protected float ticksExisted = 0F;

        protected float trailInner = 0F;
        protected float trailOuter = WeaponTrailMetrics.VANILLA_SPAN;

        protected double originX, originY, originZ;
        protected float originYaw;
        protected boolean anchored = false;

        public TrailPart(HumanoidArm primaryHand, IColorRead baseColor, float velocityX, float velocityY, float velocityZ)
        {
            this.body = new ModelPartTransform();
            this.arm = new ModelPartTransform();
            this.foreArm = new ModelPartTransform();
            this.primaryHand = primaryHand;
            this.baseColor = baseColor;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
        }

        public void update(float ticksPerFrame)
        {
            this.ticksExisted += ticksPerFrame;
            this.position.x += this.velocityX * ticksPerFrame;
            this.position.y += this.velocityY * ticksPerFrame;
            this.position.z += this.velocityZ * ticksPerFrame;
        }

        public Vec3f[] getPoints(double currentX, double currentY, double currentZ, float currentYaw)
        {
            final Vec3f[] points = getPoints();

            if (!anchored)
            {
                return points;
            }

            final float sinThen = (float) Math.sin(Math.toRadians(-originYaw));
            final float cosThen = (float) Math.cos(Math.toRadians(-originYaw));
            final float sinNow = (float) Math.sin(Math.toRadians(currentYaw));
            final float cosNow = (float) Math.cos(Math.toRadians(currentYaw));

            final float driftX = (float) ((originX - currentX) * 16.0);
            final float driftY = (float) ((originY - currentY) * 16.0);
            final float driftZ = (float) ((originZ - currentZ) * 16.0);

            for (final Vec3f point : points)
            {
                final float worldX = point.x * cosThen + point.z * sinThen + driftX;
                final float worldZ = -point.x * sinThen + point.z * cosThen + driftZ;
                final float worldY = point.y + driftY;

                point.x = worldX * cosNow + worldZ * sinNow;
                point.z = -worldX * sinNow + worldZ * cosNow;
                point.y = worldY;
            }

            return points;
        }

        public Vec3f[] getPoints()
        {
            float alpha = ticksExisted / 5F;
            alpha = Math.min(alpha, 1F);
            alpha = 1F - alpha;

            final float middle = (trailOuter + trailInner) * 0.5F;
            final float reach = (trailOuter - trailInner) * 0.5F;

            final Vec3f[] points = new Vec3f[] {
                    new Vec3f(0, 0, middle + reach * alpha),
                    new Vec3f(0, 0, middle - reach * alpha)
            };

            GUtil.rotate(points, itemRotation);
            GUtil.translate(points, primaryHand == HumanoidArm.LEFT ? 1 : -1, -6, 0);
            GUtil.rotate(points, foreArm.rotation.getSmooth());
            GUtil.translate(points, 0, -6 + 2, 0);
            GUtil.rotate(points, arm.rotation.getSmooth());
            GUtil.translate(points, arm.position.x, 10, 0);
            GUtil.rotate(points, body.rotation.getSmooth());
            GUtil.translate(points, 0, 12, 0);
            GUtil.rotate(points, renderRotation);
            GUtil.translate(points, renderOffset.x, renderOffset.y, renderOffset.z);

            for (final Vec3f point : points)
            {
                point.add(position);
            }

            return points;
        }

        public float getAlpha()
        {
            float alpha = ticksExisted / 5F;
            alpha = Math.min(alpha, 1F);
            return 1F - alpha;
        }
    }

    public void render(PoseStack poseStack, LivingEntity entity)
    {
        if (trailPartList.isEmpty())
        {
            return;
        }

        if (trailPartList.size() < 2)
        {
            return;
        }

        Matrix4f matrix = poseStack.last().pose();
        final float brightness = trailBrightness(entity);

        final float partialTicks = DataUpdateHandler.partialTicks;
        final double currentX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        final double currentY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        final double currentZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        final float currentYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);

        Iterator<TrailPart> it = trailPartList.iterator();
        TrailPart prevPart = null;
        Vec3f[] prevTransformedPoints = null;
        float prevAlpha = 0;

        while (it.hasNext())
        {
            final TrailPart part = it.next();
            final Vec3f[] points = part.getPoints(currentX, currentY, currentZ, currentYaw);
            final float alpha = part.getAlpha();
            final IColorRead color = part.baseColor;

            Vec3f[] transformedPoints = transformPoints(points, matrix);

            if (prevPart != null && prevTransformedPoints != null)
            {
                int prevColor = ((int)(prevAlpha * 255.0F) << 24) |
                               ((int)(color.getR() * brightness * 255.0F) << 16) |
                               ((int)(color.getG() * brightness * 255.0F) << 8) |
                               (int)(color.getB() * brightness * 255.0F);
                int currColor = ((int)(alpha * 255.0F) << 24) |
                               ((int)(color.getR() * brightness * 255.0F) << 16) |
                               ((int)(color.getG() * brightness * 255.0F) << 8) |
                               (int)(color.getB() * brightness * 255.0F);

                TrailRenderQueue.vertex(prevTransformedPoints[0].x, prevTransformedPoints[0].y, prevTransformedPoints[0].z, prevColor);
                TrailRenderQueue.vertex(prevTransformedPoints[1].x, prevTransformedPoints[1].y, prevTransformedPoints[1].z, prevColor);
                TrailRenderQueue.vertex(transformedPoints[1].x, transformedPoints[1].y, transformedPoints[1].z, currColor);
                TrailRenderQueue.vertex(transformedPoints[0].x, transformedPoints[0].y, transformedPoints[0].z, currColor);
            }

            prevPart = part;
            prevTransformedPoints = transformedPoints;
            prevAlpha = alpha;
        }

    }

    private static float trailBrightness(LivingEntity entity)
    {
        if (ModConfig.swordTrailFullBright)
        {
            return 1.0F;
        }

        final Level level = entity.level();
        final BlockPos pos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        final int packedLight = LevelRenderer.getLightColor(level, pos);
        final int blockLight = LightTexture.block(packedLight);
        final int skyLight = Math.max(0, LightTexture.sky(packedLight) - level.getSkyDarken());

        return 0.15F + 0.85F * (Math.max(blockLight, skyLight) / 15.0F);
    }

    private Vec3f[] transformPoints(Vec3f[] points, Matrix4f matrix)
    {
        Vec3f[] result = new Vec3f[points.length];
        for (int i = 0; i < points.length; i++)
        {
            Vector4f vec = new Vector4f(points[i].x, points[i].y, points[i].z, 1.0f);
            vec.mul(matrix);
            result[i] = new Vec3f(vec.x(), vec.y(), vec.z());
        }
        return result;
    }

    public void add(BipedEntityData<?> entityData, float velocityX, float velocityY, float velocityZ)
    {
        add(entityData, entityData.getEntity().getMainArm(), velocityX, velocityY, velocityZ);
    }

    public void add(BipedEntityData<?> entityData, HumanoidArm arm)
    {
        add(entityData, arm, 0, 0, 0);
    }

    public void add(BipedEntityData<?> entityData, HumanoidArm arm, float velocityX, float velocityY, float velocityZ)
    {
        final HumanoidArm primaryHand = arm;
        final TrailPart newPart = new TrailPart(primaryHand, this.baseColor.get(), velocityX, velocityY, velocityZ);

        final LivingEntity entity = entityData.getEntity();
        if (entity != null)
        {
            final ItemStack weapon = arm == entity.getMainArm()
                    ? entity.getMainHandItem()
                    : entity.getOffhandItem();

            final ItemDisplayContext displayContext = arm == HumanoidArm.RIGHT
                    ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                    : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

            final float[] extent = WeaponTrailMetrics.getTrailExtent(weapon, entity, displayContext);
            newPart.trailInner = extent[0];
            newPart.trailOuter = extent[1];

            final float partialTicks = DataUpdateHandler.partialTicks;
            newPart.originX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            newPart.originY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            newPart.originZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            newPart.originYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
            newPart.anchored = true;
        }

        newPart.body.syncUp(entityData.body);

        if (primaryHand == HumanoidArm.RIGHT)
        {
            newPart.arm.syncUp(entityData.rightArm);
            newPart.foreArm.syncUp(entityData.rightForeArm);
            newPart.itemRotation.set(entityData.renderRightItemRotation.getSmooth());
        }
        else
        {
            newPart.arm.syncUp(entityData.leftArm);
            newPart.foreArm.syncUp(entityData.leftForeArm);
            newPart.itemRotation.set(entityData.renderLeftItemRotation.getSmooth());
        }

        newPart.renderOffset.set(entityData.globalOffset.getX(),
                entityData.globalOffset.getY(),
                entityData.globalOffset.getZ());
        newPart.renderRotation.set(entityData.renderRotation.getSmooth());
        // Inverse of the entity's render rotation, used to undo it when replaying the arm chain.
        newPart.renderRotation.conjugate();

        trailPartList.add(newPart);
    }

    public void add(BipedEntityData<?> entityData)
    {
        add(entityData, 0, 0, 0);
    }

    public void update(float ticksPerFrame)
    {
        final Iterator<TrailPart> it = trailPartList.iterator();
        while (it.hasNext())
        {
            final TrailPart trailPart = it.next();
            trailPart.update(ticksPerFrame);

            if (trailPart.ticksExisted > 20)
            {
                it.remove();
            }
        }
    }
}
