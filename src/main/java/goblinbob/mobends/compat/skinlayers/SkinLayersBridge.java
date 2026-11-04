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
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.LimbBending;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Matrix4f;

public final class SkinLayersBridge
{
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

        LimbBending.emitBent(CAPTURE.getVertices(), limbPose, forePose, vertexConsumer);
        CAPTURE.clear();
    }

    private static void translateToPivot(PoseStack poseStack, BendsModelPart part)
    {
        final float scale = part.offsetScale / 16.0F;
        poseStack.translate((part.position.x + part.offset.x) * scale,
                (part.position.y + part.offset.y) * scale,
                (part.position.z + part.offset.z) * scale);
    }
}
