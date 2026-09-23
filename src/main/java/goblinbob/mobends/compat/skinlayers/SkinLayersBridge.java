package goblinbob.mobends.compat.skinlayers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.accessor.ModelPartInjector;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.api.Mesh;
import dev.tr7zw.skinlayers.api.OffsetProvider;
import dev.tr7zw.skinlayers.versionless.config.Config;
import goblinbob.mobends.compat.SkinLayersCompat;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.standard.client.model.armor.ArmorPoseHelper;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SkinLayersBridge
{
    private static final float FACE_NUDGE = 0.0005F;
    private static final float CLIP_EPSILON = 1.0E-6F;

    private static final CapturedVertex[] UPPER_POLY = new CapturedVertex[16];
    private static final CapturedVertex[] LOWER_POLY = new CapturedVertex[16];
    private static final CapturedVertex[] MOVED = new CapturedVertex[16];
    private static final float[] UPPER_DIST = new float[16];
    private static final float[] LOWER_DIST = new float[16];
    private static final float[] SCRATCH_DIST = new float[16];

    private static final CapturingVertexConsumer CAPTURE = new CapturingVertexConsumer();

    private SkinLayersBridge()
    {
    }

    public static int activeParts(AbstractClientPlayer player, boolean slim)
    {
        final Config config = SkinLayersModBase.config;
        if (config == null || player.isInvisible() || !(player instanceof PlayerSettings settings))
        {
            return 0;
        }

        final double lod = config.renderDistanceLOD;
        if (player.distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()) > lod * lod)
        {
            return 0;
        }

        if (!SkinUtil.setup3dLayers(player, settings, slim))
        {
            return 0;
        }

        int parts = 0;
        if (config.enableHat && player.isModelPartShown(PlayerModelPart.HAT) && settings.getHeadMesh() != null
                && !SkinLayersModBase.hideHeadLayers.contains(player.getItemBySlot(EquipmentSlot.HEAD).getItem()))
        {
            parts |= SkinLayersCompat.HEAD;
        }
        if (config.enableJacket && player.isModelPartShown(PlayerModelPart.JACKET) && settings.getTorsoMesh() != null)
        {
            parts |= SkinLayersCompat.BODY;
        }
        if (config.enableLeftSleeve && player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE) && settings.getLeftArmMesh() != null)
        {
            parts |= SkinLayersCompat.LEFT_ARM;
        }
        if (config.enableRightSleeve && player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE) && settings.getRightArmMesh() != null)
        {
            parts |= SkinLayersCompat.RIGHT_ARM;
        }
        if (config.enableLeftPants && player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG) && settings.getLeftLegMesh() != null)
        {
            parts |= SkinLayersCompat.LEFT_LEG;
        }
        if (config.enableRightPants && player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG) && settings.getRightLegMesh() != null)
        {
            parts |= SkinLayersCompat.RIGHT_LEG;
        }
        return parts;
    }

    public static void clearInjectedMeshes(PlayerModel<?> model, int parts)
    {
        clearInjected(model.hat, parts, SkinLayersCompat.HEAD);
        clearInjected(model.jacket, parts, SkinLayersCompat.BODY);
        clearInjected(model.leftSleeve, parts, SkinLayersCompat.LEFT_ARM);
        clearInjected(model.rightSleeve, parts, SkinLayersCompat.RIGHT_ARM);
        clearInjected(model.leftPants, parts, SkinLayersCompat.LEFT_LEG);
        clearInjected(model.rightPants, parts, SkinLayersCompat.RIGHT_LEG);
    }

    private static void clearInjected(ModelPart part, int parts, int flag)
    {
        if ((parts & flag) != 0 && (Object) part instanceof ModelPartInjector injector)
        {
            injector.setInjectedMesh(null, null);
        }
    }

    public static void render(PlayerMutator mutator, AbstractClientPlayer player, int parts, boolean slim,
                              PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        if (!(player instanceof PlayerSettings settings))
        {
            return;
        }

        final PlayerModel<?> model = MoBendsRenderContext.getCurrentVanillaModel() instanceof PlayerModel<?> playerModel
                ? playerModel : null;

        if ((parts & SkinLayersCompat.HEAD) != 0)
        {
            renderRigid(mutator.getHead(), null, settings.getHeadMesh(), OffsetProvider.HEAD,
                    model != null ? model.hat : null, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if ((parts & SkinLayersCompat.BODY) != 0)
        {
            renderRigid(mutator.getBody(), mutator.getHead(), settings.getTorsoMesh(), OffsetProvider.BODY,
                    model != null ? model.jacket : null, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if ((parts & SkinLayersCompat.LEFT_ARM) != 0)
        {
            renderLimb(mutator.getLeftArm(), mutator.getLeftForeArm(), settings.getLeftArmMesh(),
                    slim ? OffsetProvider.LEFT_ARM_SLIM : OffsetProvider.LEFT_ARM,
                    model != null ? model.leftSleeve : null, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if ((parts & SkinLayersCompat.RIGHT_ARM) != 0)
        {
            renderLimb(mutator.getRightArm(), mutator.getRightForeArm(), settings.getRightArmMesh(),
                    slim ? OffsetProvider.RIGHT_ARM_SLIM : OffsetProvider.RIGHT_ARM,
                    model != null ? model.rightSleeve : null, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if ((parts & SkinLayersCompat.LEFT_LEG) != 0)
        {
            renderLimb(mutator.getLeftLeg(), mutator.getLeftForeLeg(), settings.getLeftLegMesh(), OffsetProvider.LEFT_LEG,
                    model != null ? model.leftPants : null, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if ((parts & SkinLayersCompat.RIGHT_LEG) != 0)
        {
            renderLimb(mutator.getRightLeg(), mutator.getRightForeLeg(), settings.getRightLegMesh(), OffsetProvider.RIGHT_LEG,
                    model != null ? model.rightPants : null, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }

    private static void renderRigid(BendsModelPart bone, BendsModelPart pivotChild, Mesh mesh, OffsetProvider offset,
                                    ModelPart vanillaPart, PoseStack poseStack, VertexConsumer vertexConsumer,
                                    int packedLight, int packedOverlay, int color)
    {
        if (bone == null || mesh == null || !bone.isShowing())
        {
            return;
        }

        poseStack.pushPose();
        bone.applyCharacterTransformPoseStack(poseStack);
        if (pivotChild != null)
        {
            translateToPivot(poseStack, pivotChild);
        }
        offset.applyOffset(poseStack, mesh);
        mesh.render(vanillaPart, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    private static void renderLimb(BendsModelPart bone, BendsModelPart foreBone, Mesh mesh, OffsetProvider offset,
                                   ModelPart vanillaPart, PoseStack poseStack, VertexConsumer vertexConsumer,
                                   int packedLight, int packedOverlay, int color)
    {
        if (foreBone == null || !foreBone.isShowing())
        {
            renderRigid(bone, null, mesh, offset, vanillaPart, poseStack, vertexConsumer, packedLight, packedOverlay, color);
            return;
        }
        if (bone == null || mesh == null || !bone.isShowing())
        {
            return;
        }

        final IEntityVertexHelper helper = IEntityVertexHelper.Holder.getHelper();
        if (helper == null)
        {
            return;
        }

        poseStack.pushPose();
        foreBone.applyCharacterTransformPoseStack(poseStack);
        final Matrix4f forePose = new Matrix4f(poseStack.last().pose());
        poseStack.popPose();

        poseStack.pushPose();
        bone.applyCharacterTransformPoseStack(poseStack);
        final Matrix4f limbPose = new Matrix4f(poseStack.last().pose());
        offset.applyOffset(poseStack, mesh);
        CAPTURE.clear();
        mesh.render(vanillaPart, poseStack, CAPTURE, packedLight, packedOverlay, color);
        poseStack.popPose();

        final Matrix4f toLimb = new Matrix4f(limbPose).invert();
        final Matrix4f joint = new Matrix4f(toLimb).mul(forePose);
        final Vector3f pivot = joint.getTranslation(new Vector3f());
        final Quaternionf bend = joint.getNormalizedRotation(new Quaternionf());

        final Vector3f upperNormal = bend.transform(new Vector3f(0.0F, 1.0F, 0.0F)).add(0.0F, 1.0F, 0.0F);
        if (upperNormal.lengthSquared() < 1.0E-6F)
        {
            upperNormal.set(0.0F, 1.0F, 0.0F);
        }
        upperNormal.normalize();
        final Vector3f lowerNormal = bend.transformInverse(new Vector3f(upperNormal));

        final Matrix4f upperToLower = new Matrix4f(limbPose)
                .translate(pivot)
                .rotate(bend)
                .translate(-pivot.x, -pivot.y, -pivot.z)
                .mul(toLimb);
        final Matrix3f upperToLowerNormal = new Matrix3f(upperToLower);
        final Matrix3f normalToLimb = new Matrix3f(limbPose).transpose();

        final Vector3f point = new Vector3f();
        final Vector3f normal = new Vector3f();

        for (CapturedVertex[] quad : ArmorPoseHelper.groupIntoQuads(CAPTURE.getVertices()))
        {
            normal.set(0.0F, 0.0F, 0.0F);
            for (CapturedVertex vertex : quad)
            {
                normal.add(vertex.normalX, vertex.normalY, vertex.normalZ);
            }
            normalToLimb.transform(normal);
            if (normal.lengthSquared() > 0.0F)
            {
                normal.normalize();
            }
            final float upperNudge = FACE_NUDGE * normal.dot(upperNormal);
            final float lowerNudge = FACE_NUDGE * normal.dot(lowerNormal);

            boolean allUpper = true;
            boolean allLower = true;
            for (int i = 0; i < quad.length; ++i)
            {
                point.set(quad[i].x, quad[i].y, quad[i].z);
                toLimb.transformPosition(point);
                point.sub(pivot);
                UPPER_DIST[i] = point.dot(upperNormal) - upperNudge;
                LOWER_DIST[i] = point.dot(lowerNormal) - lowerNudge;
                allUpper &= UPPER_DIST[i] <= 0.0F;
                allLower &= LOWER_DIST[i] >= 0.0F;
            }

            if (allUpper)
            {
                emitPolygon(helper, vertexConsumer, quad, quad.length);
            }
            else
            {
                final int count = clipPolygon(quad, UPPER_DIST, quad.length, true, UPPER_POLY, SCRATCH_DIST);
                emitPolygon(helper, vertexConsumer, UPPER_POLY, count);
            }

            if (allLower)
            {
                emitMoved(helper, vertexConsumer, quad, quad.length, upperToLower, upperToLowerNormal);
            }
            else
            {
                final int count = clipPolygon(quad, LOWER_DIST, quad.length, false, LOWER_POLY, SCRATCH_DIST);
                emitMoved(helper, vertexConsumer, LOWER_POLY, count, upperToLower, upperToLowerNormal);
            }
        }

        CAPTURE.clear();
    }

    private static int clipPolygon(CapturedVertex[] polygon, float[] distance, int count, boolean keepNegative,
                                   CapturedVertex[] out, float[] outDistance)
    {
        int outCount = 0;
        for (int i = 0; i < count; ++i)
        {
            final int j = (i + 1) % count;
            final boolean insideI = keepNegative ? distance[i] <= 0.0F : distance[i] >= 0.0F;
            final boolean insideJ = keepNegative ? distance[j] <= 0.0F : distance[j] >= 0.0F;

            if (insideI)
            {
                out[outCount] = polygon[i];
                outDistance[outCount++] = distance[i];
            }
            if (insideI != insideJ)
            {
                final float t = distance[i] / (distance[i] - distance[j]);
                if (t > CLIP_EPSILON && t < 1.0F - CLIP_EPSILON)
                {
                    out[outCount] = lerp(polygon[i], polygon[j], t);
                    outDistance[outCount++] = 0.0F;
                }
            }
        }
        return outCount;
    }

    private static void emitMoved(IEntityVertexHelper helper, VertexConsumer vertexConsumer,
                                  CapturedVertex[] polygon, int count,
                                  Matrix4f upperToLower, Matrix3f upperToLowerNormal)
    {
        if (count < 3)
        {
            return;
        }

        for (int i = 0; i < count; ++i)
        {
            final CapturedVertex vertex = polygon[i];
            final Vector3f position = upperToLower.transformPosition(new Vector3f(vertex.x, vertex.y, vertex.z));
            final Vector3f normal = upperToLowerNormal.transform(new Vector3f(vertex.normalX, vertex.normalY, vertex.normalZ));
            if (normal.lengthSquared() > 0.0F)
            {
                normal.normalize();
            }
            MOVED[i] = new CapturedVertex(position.x, position.y, position.z,
                    vertex.red, vertex.green, vertex.blue, vertex.alpha,
                    vertex.u, vertex.v, vertex.overlayUV, vertex.lightmapUV,
                    normal.x, normal.y, normal.z);
        }
        emitPolygon(helper, vertexConsumer, MOVED, count);
    }

    private static CapturedVertex lerp(CapturedVertex a, CapturedVertex b, float t)
    {
        return new CapturedVertex(
                a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t,
                a.red, a.green, a.blue, a.alpha,
                a.u + (b.u - a.u) * t, a.v + (b.v - a.v) * t,
                a.overlayUV, a.lightmapUV,
                a.normalX, a.normalY, a.normalZ);
    }

    private static void emitPolygon(IEntityVertexHelper helper, VertexConsumer vertexConsumer,
                                    CapturedVertex[] vertices, int count)
    {
        if (count < 3)
        {
            return;
        }

        for (int start = 1; start + 1 < count; start += 2)
        {
            final int third = Math.min(start + 2, count - 1);
            emit(helper, vertexConsumer, vertices[0]);
            emit(helper, vertexConsumer, vertices[start]);
            emit(helper, vertexConsumer, vertices[start + 1]);
            emit(helper, vertexConsumer, vertices[third]);
        }
    }

    private static void emit(IEntityVertexHelper helper, VertexConsumer vertexConsumer, CapturedVertex vertex)
    {
        helper.emitVertex(vertexConsumer, vertex.x, vertex.y, vertex.z,
                packColor(vertex), vertex.u, vertex.v, vertex.overlayUV, vertex.lightmapUV,
                vertex.normalX, vertex.normalY, vertex.normalZ);
    }

    private static int packColor(CapturedVertex vertex)
    {
        return ((int) (vertex.alpha * 255.0F) & 0xFF) << 24
                | ((int) (vertex.red * 255.0F) & 0xFF) << 16
                | ((int) (vertex.green * 255.0F) & 0xFF) << 8
                | ((int) (vertex.blue * 255.0F) & 0xFF);
    }

    private static void translateToPivot(PoseStack poseStack, BendsModelPart part)
    {
        final float scale = part.offsetScale / 16.0F;
        poseStack.translate((part.position.x + part.offset.x) * scale,
                (part.position.y + part.offset.y) * scale,
                (part.position.z + part.offset.z) * scale);
    }
}
