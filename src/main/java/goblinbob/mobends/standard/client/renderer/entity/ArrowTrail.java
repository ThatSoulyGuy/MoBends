package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.TrailRenderQueue;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.lib.math.vector.VectorUtils;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ArrowTrail
{
    public static final int MAX_LENGTH = 10;
    public static final float SPAWN_INTERVAL = 1;

    private final Minecraft mc;
    private AbstractArrow trackedArrow;
    private TrailNode[] nodes;
    private float spawnCooldown = 0;

    public ArrowTrail(AbstractArrow arrow)
    {
        this.mc = Minecraft.getInstance();
        this.trackedArrow = arrow;
        this.spawnCooldown = SPAWN_INTERVAL;
        this.nodes = new TrailNode[MAX_LENGTH];

        resetNodes();
    }

    public void onRenderTick()
    {
        spawnCooldown += DataUpdateHandler.ticksPerFrame;
    }

    public void render(PoseStack poseStack, float partialTicks)
    {
        advanceNodes();

        renderNodes(poseStack,
                Mth.lerp(partialTicks, trackedArrow.xOld, trackedArrow.getX()),
                Mth.lerp(partialTicks, trackedArrow.yOld, trackedArrow.getY()),
                Mth.lerp(partialTicks, trackedArrow.zOld, trackedArrow.getZ()));
    }

    public void renderFrom(PoseStack poseStack, double originX, double originY, double originZ)
    {
        advanceNodes();

        renderNodes(poseStack, originX, originY, originZ);
    }

    private void advanceNodes()
    {
        if (this.spawnCooldown > 40)
        {
            this.spawnCooldown = 0;
            resetNodes();
        }

        while (this.spawnCooldown >= SPAWN_INTERVAL)
        {
            for (int i = MAX_LENGTH - 1; i > 0; i--)
            {
                nodes[i].moveTo(nodes[i - 1]);
            }
            nodes[0].moveTo(trackedArrow);
            this.spawnCooldown -= SPAWN_INTERVAL;
        }
    }

    public void resetNodes()
    {
        for (int i = 0; i < MAX_LENGTH; i++)
            this.nodes[i] = new TrailNode(trackedArrow);
    }

    public void renderNodes(PoseStack poseStack, double originX, double originY, double originZ)
    {
        final Matrix4f matrix = poseStack.last().pose();
        final Level level = trackedArrow.level();

        for (int i = 1; i < MAX_LENGTH; i++)
        {
            TrailNode node0 = nodes[i - 1];
            TrailNode node1 = nodes[i];

            Vec3 pos0 = new Vec3(node0.x - originX, node0.y - originY, node0.z - originZ);
            Vec3 pos1 = new Vec3(node1.x - originX, node1.y - originY, node1.z - originZ);
            float scale0 = ((float) (MAX_LENGTH - i)) / MAX_LENGTH * .1F;
            float scale1 = ((float) MAX_LENGTH - i - 1.0f) / MAX_LENGTH * .1F;
            if (i == 1)
            {
                scale1 = 0;
            }
            final Vec3f up0 = node0.up;
            final Vec3f right0 = node0.right;
            final Vec3f up1 = node1.up;
            final Vec3f right1 = node1.right;

            final int color0 = trailColor(level, node0, i - 1);
            final int color1 = trailColor(level, node1, i);

            addVertex(matrix, pos0.x + (-right0.x) * scale0, pos0.y + (-right0.y) * scale0, pos0.z + (-right0.z) * scale0, color0);
            addVertex(matrix, pos0.x + (right0.x) * scale0, pos0.y + (right0.y) * scale0, pos0.z + (right0.z) * scale0, color0);
            addVertex(matrix, pos1.x + (right1.x) * scale1, pos1.y + (right1.y) * scale1, pos1.z + (right1.z) * scale1, color1);
            addVertex(matrix, pos1.x + (-right1.x) * scale1, pos1.y + (-right1.y) * scale1, pos1.z + (-right1.z) * scale1, color1);

            addVertex(matrix, pos0.x + (-up0.x) * scale0, pos0.y + (-up0.y) * scale0, pos0.z + (-up0.z) * scale0, color0);
            addVertex(matrix, pos0.x + (up0.x) * scale0, pos0.y + (up0.y) * scale0, pos0.z + (up0.z) * scale0, color0);
            addVertex(matrix, pos1.x + (up1.x) * scale1, pos1.y + (up1.y) * scale1, pos1.z + (up1.z) * scale1, color1);
            addVertex(matrix, pos1.x + (-up1.x) * scale1, pos1.y + (-up1.y) * scale1, pos1.z + (-up1.z) * scale1, color1);
        }
    }

    private static final int SPECTRAL_TINT = 0xFFC83C;
    private static final float SPARKLE_SPEED = 0.9F;
    private static final float SPARKLE_SPACING = 1.7F;
    private static final float SPARKLE_DEPTH = 0.35F;

    private int trailColor(Level level, TrailNode node, int nodeIndex)
    {
        float brightness = 1.0F;

        if (!ModConfig.arrowTrailFullBright)
        {
            final int packedLight = LevelRenderer.getLightColor(level, BlockPos.containing(node.x, node.y, node.z));
            final int blockLight = LightTexture.block(packedLight);
            final int skyLight = Math.max(0, LightTexture.sky(packedLight) - level.getSkyDarken());
            brightness = 0.15F + 0.85F * (Math.max(blockLight, skyLight) / 15.0F);
        }

        float red = brightness;
        float green = brightness;
        float blue = brightness;

        if (isSpectral())
        {
            final float phase = DataUpdateHandler.getTicks() * SPARKLE_SPEED + nodeIndex * SPARKLE_SPACING;
            final float sparkle = 1.0F - SPARKLE_DEPTH + SPARKLE_DEPTH * Mth.sin(phase);
            final float whiten = Math.max(0.0F, Mth.sin(phase)) * 0.4F;

            red = tintChannel(SPECTRAL_TINT, 16, brightness * sparkle, whiten);
            green = tintChannel(SPECTRAL_TINT, 8, brightness * sparkle, whiten);
            blue = tintChannel(SPECTRAL_TINT, 0, brightness * sparkle, whiten);
        }
        else
        {
            final int effectColor = potionColor();

            if (effectColor != -1)
            {
                red = tintChannel(effectColor, 16, brightness, 0.0F);
                green = tintChannel(effectColor, 8, brightness, 0.0F);
                blue = tintChannel(effectColor, 0, brightness, 0.0F);
            }
        }

        return 0x80000000
                | (channelOf(red) << 16)
                | (channelOf(green) << 8)
                | channelOf(blue);
    }

    private static float tintChannel(int color, int shift, float brightness, float whiten)
    {
        final float value = ((color >> shift) & 0xFF) / 255.0F;
        return (value + (1.0F - value) * whiten) * brightness;
    }

    private static int channelOf(float value)
    {
        return Math.max(0, Math.min(255, (int) (value * 255.0F)));
    }

    private boolean isSpectral()
    {
        return ModConfig.spectralArrowTrailEffect && ArrowEffectColor.isSpectral(trackedArrow);
    }

    private int potionColor()
    {
        if (!ModConfig.arrowTrailPotionColor)
        {
            return ArrowEffectColor.NO_COLOR;
        }

        return ArrowEffectColor.getEffectColor(trackedArrow);
    }

    private static void addVertex(Matrix4f matrix, double x, double y, double z, int color)
    {
        Vector4f vec = new Vector4f((float) x, (float) y, (float) z, 1.0F);
        vec.mul(matrix);
        TrailRenderQueue.vertex(vec.x(), vec.y(), vec.z(), color);
    }

    public boolean shouldBeRemoved()
    {
        return mc.level == null
                || trackedArrow.isRemoved()
                || mc.level.getEntity(trackedArrow.getId()) != trackedArrow;
    }

    static class TrailNode
    {
        public double x;
        public double y;
        public double z;

        public final Vec3f up;
        public final Vec3f right;

        TrailNode(AbstractArrow arrow)
        {
            this.up = new Vec3f();
            this.right = new Vec3f();

            this.moveTo(arrow);
        }

        public void moveTo(TrailNode trailNode)
        {
            this.x = trailNode.x;
            this.y = trailNode.y;
            this.z = trailNode.z;
            this.up.set(trailNode.up);
            this.right.set(trailNode.right);
        }

        public void moveTo(AbstractArrow arrow)
        {
            this.x = arrow.getX();
            this.y = arrow.getY();
            this.z = arrow.getZ();

            final Vec3 forward = arrow.getForward();

            float pitch = arrow.getXRot();
            float yaw = arrow.getYRot();
            float upPitch = pitch + 90F;

            float f = Mth.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
            float f1 = Mth.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
            float f2 = -Mth.cos(-upPitch * ((float)Math.PI / 180F));
            float f3 = Mth.sin(-upPitch * ((float)Math.PI / 180F));
            Vec3 up = new Vec3(f1 * f2, f3, f * f2);

            this.up.set((float) -up.x, (float) -up.y, (float) up.z);

            VectorUtils.cross(
                    (float) -forward.x, (float) -forward.y, (float) forward.z,
                    this.up.x, this.up.y, this.up.z, this.right);
        }
    }
}
