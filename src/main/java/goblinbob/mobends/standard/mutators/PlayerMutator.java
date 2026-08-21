package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.core.client.model.BendsMesh;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.client.model.adaptive.HumanoidLayout;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomCape;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomElytra;
import goblinbob.mobends.standard.data.PlayerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class PlayerMutator extends BipedMutator<PlayerData, AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    private static final Logger LOG = LoggerFactory.getLogger(PlayerMutator.class);

    protected BendsModelPart bodywear;
    protected BendsModelPart leftArmwear;
    protected BendsModelPart rightArmwear;
    protected BendsModelPart leftForeArmwear;
    protected BendsModelPart rightForeArmwear;
    protected BendsModelPart leftLegwear;
    protected BendsModelPart rightLegwear;
    protected BendsModelPart leftForeLegwear;
    protected BendsModelPart rightForeLegwear;

    protected boolean smallArms;

    protected LayerCustomCape layerCape;
    protected CapeLayer layerCapeVanilla;

    protected LayerCustomElytra layerElytra;
    protected RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layerElytraVanilla;

    public PlayerMutator(IEntityDataFactory<AbstractClientPlayer> dataFactory)
    {
        super(dataFactory);
    }

    public boolean hasSmallArms()
    {
        return this.smallArms;
    }

    @Override
    public boolean mutate(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        return super.mutate(renderer);
    }

    @Override
    public void demutate(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super.demutate(renderer);
    }

    @Override
    public void fetchFields(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super.fetchFields(renderer);

        if (renderer instanceof PlayerRenderer playerRenderer)
        {
            this.smallArms = detectSlimArms(playerRenderer);
        }
    }

    private boolean detectSlimArms(PlayerRenderer playerRenderer)
    {
        String[] fieldNames = {"slim", "f_117788_"};
        for (String fieldName : fieldNames)
        {
            try
            {
                java.lang.reflect.Field slimField = PlayerRenderer.class.getDeclaredField(fieldName);
                slimField.setAccessible(true);
                boolean result = slimField.getBoolean(playerRenderer);
                return result;
            }
            catch (NoSuchFieldException | IllegalAccessException ignored)
            {
            }
        }

        try
        {
            PlayerModel<?> model = playerRenderer.getModel();
            if (model != null && model.leftArm != null)
            {
                String[] cubeFieldNames = {"cubes", "f_104222_"};
                java.util.List<?> cubes = null;
                for (String fieldName : cubeFieldNames)
                {
                    try
                    {
                        java.lang.reflect.Field cubesField = net.minecraft.client.model.geom.ModelPart.class.getDeclaredField(fieldName);
                        cubesField.setAccessible(true);
                        cubes = (java.util.List<?>) cubesField.get(model.leftArm);
                        break;
                    }
                    catch (NoSuchFieldException ignored)
                    {
                    }
                }
                if (cubes != null)
                {
                    for (Object obj : cubes)
                    {
                        net.minecraft.client.model.geom.ModelPart.Cube cube = (net.minecraft.client.model.geom.ModelPart.Cube) obj;
                        float width = cube.maxX - cube.minX;
                        if (Math.abs(width - 3.0f) < 0.1f)
                        {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            LOG.warn("Failed to detect slim arms via model cube width: {}", e.getMessage());
        }

        LOG.warn("Could not detect slim arms, defaulting to standard arms");
        return false;
    }

    public void updateSmallArms(AbstractClientPlayer player)
    {
        if (player != null)
        {
            IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
            this.smallArms = skinProvider != null && skinProvider.isSlimModel(player);
        }
    }

    @Override
    public void updateModel(AbstractClientPlayer entity,
                            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                            float partialTicks)
    {
        if (entity != null && this.body != null && !PlayerPreviewer.isPreviewInProgress())
        {
            IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
            boolean playerIsSlim = skinProvider != null && skinProvider.isSlimModel(entity);
            if (playerIsSlim != this.smallArms)
            {
                this.smallArms = playerIsSlim;
                createParts(renderer.getModel(), 0F);
            }
        }

        super.updateModel(entity, renderer, partialTicks);
    }

    @Override
    public void storeVanillaModel(PlayerModel<AbstractClientPlayer> model)
    {
        super.storeVanillaModel(model);
    }

    @Override
    public void applyVanillaModel(PlayerModel<AbstractClientPlayer> model)
    {
        super.applyVanillaModel(model);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void swapLayer(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, int index, boolean isModelVanilla)
    {
        super.swapLayer(renderer, index, isModelVanilla);

        final RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layer = layerRenderers.get(index);
        if (layer instanceof CapeLayer)
        {
            this.layerCape = new LayerCustomCape((PlayerRenderer) renderer);
            if (isModelVanilla)
                this.layerCapeVanilla = (CapeLayer) layer;
            layerRenderers.set(index, this.layerCape);
        }

        if (layer instanceof ElytraLayer)
        {
            this.layerElytra = new LayerCustomElytra(renderer, Minecraft.getInstance().getEntityModels());
            if (isModelVanilla)
                this.layerElytraVanilla = layer;
            layerRenderers.set(index, this.layerElytra);
        }

    }

    @Override
    public void deswapLayer(LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, int index)
    {
        super.deswapLayer(renderer, index);

        final RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layer = layerRenderers.get(index);
        if (layer instanceof LayerCustomCape)
        {
            layerRenderers.set(index, this.layerCapeVanilla);
        }

        if (layer instanceof LayerCustomElytra && this.layerElytraVanilla != null)
        {
            layerRenderers.set(index, this.layerElytraVanilla);
        }

    }

    @Override
    protected AdaptiveHumanoidGeometry.WearParts adaptiveWearParts(PlayerModel<AbstractClientPlayer> original)
    {
        if (original == null)
        {
            return null;
        }

        return new AdaptiveHumanoidGeometry.WearParts(original.jacket,
                original.leftSleeve, original.rightSleeve,
                original.leftPants, original.rightPants);
    }

    @Override
    protected AdaptiveHumanoidGeometry.CaptureMode adaptiveLimbCaptureMode()
    {
        return AdaptiveHumanoidGeometry.CaptureMode.SUBTREE;
    }

    @Override
    protected void createAdaptiveWearParts(AdaptiveHumanoidGeometry geometry)
    {
        bodywear = attachWear(body, geometry.bodyWearMesh);
        leftArmwear = attachWear(leftArm, geometry.leftArmWearMesh);
        rightArmwear = attachWear(rightArm, geometry.rightArmWearMesh);
        leftForeArmwear = attachWear(leftForeArm, geometry.leftForeArmWearMesh);
        rightForeArmwear = attachWear(rightForeArm, geometry.rightForeArmWearMesh);
        leftLegwear = attachWear(leftLeg, geometry.leftLegWearMesh);
        rightLegwear = attachWear(rightLeg, geometry.rightLegWearMesh);
        leftForeLegwear = attachWear(leftForeLeg, geometry.leftForeLegWearMesh);
        rightForeLegwear = attachWear(rightForeLeg, geometry.rightForeLegWearMesh);
    }

    private static BendsModelPart attachWear(BendsModelPart parent, BendsMesh mesh)
    {
        if (parent == null || mesh == null)
        {
            return null;
        }

        final BendsModelPart wear = new BendsModelPart().addMesh(mesh);
        parent.addChild(wear);
        return wear;
    }

    @Override
    protected void reconcileWithVanillaModel(HumanoidModel<?> original)
    {
        super.reconcileWithVanillaModel(original);

        if (!(original instanceof PlayerModel<?> playerModel) || body == null)
        {
            return;
        }

        if (limbSubtreesBaked())
        {
            return;
        }

        final float[] bodyAnchor = {body.position.x, body.position.y, body.position.z};

        attach(playerModel.jacket, playerModel.jacket, body, bodyAnchor);
        attach(playerModel.leftSleeve, playerModel.leftSleeve, leftArm, childAnchor(bodyAnchor, leftArm));
        attach(playerModel.rightSleeve, playerModel.rightSleeve, rightArm, childAnchor(bodyAnchor, rightArm));
        attach(playerModel.leftPants, playerModel.leftPants, leftLeg, rootAnchor(leftLeg));
        attach(playerModel.rightPants, playerModel.rightPants, rightLeg, rootAnchor(rightLeg));
    }

    @Override
    public boolean createParts(PlayerModel<AbstractClientPlayer> original, float scaleFactor)
    {
        if (tryCreateAdaptiveParts(original, HumanoidLayout.PLAYER, HumanoidLayout.PLAYER_SLIM))
        {
            return true;
        }

        int armWidth = this.smallArms ? 3 : 4;
        float armY = this.smallArms ? -9.5F : -10F;

        body = new BendsModelPart(16, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F);
        body.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        body.addChild(head);

        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 64);
        headwear.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        head.addChild(headwear);

        leftArm = new BendsModelPart(32, 48)
                .setTextureSize(64, 64)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        leftArm.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(leftArm);

        rightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(-5.0F, armY, 0.0F);
        rightArm.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(rightArm);

        leftForeArm = new BendsModelPart(32, 48 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        leftForeArm.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftArm.addChild(leftForeArm);

        rightForeArm = new BendsModelPart(40, 16 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F);
        rightForeArm.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightArm.addChild(rightForeArm);

        leftLeg = new BendsModelPart(16, 48)
                .setTextureSize(64, 64)
                .setPosition(1.9F, 12.0F, 0.0F)
                .setMirror(true);
        leftLeg.addCube(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        rightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(-1.9F, 12.0F, 0.0F);
        rightLeg.addCube(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        leftForeLeg = new BendsModelPart(16, 48 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 6.0F, -2.0F)
                .setMirror(true);
        leftForeLeg.developBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftLeg.addChild(leftForeLeg);

        rightForeLeg = new BendsModelPart(0, 16 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 6.0F, -2.0F);
        rightForeLeg.developBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightLeg.addChild(rightForeLeg);

        float wearOffset = 0.25F;
        float limbWearHeight = (6F + 2 * scaleFactor + 0.5F) - 0.25F;

        bodywear = new BendsModelPart(16, 32)
                .setTextureSize(64, 64);
        bodywear.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor + wearOffset);
        body.addChild(bodywear);

        leftArmwear = new BendsModelPart(48, 48)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftArmwear.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        leftArm.addChild(leftArmwear);

        rightArmwear = new BendsModelPart(40, 32)
                .setTextureSize(64, 64);
        rightArmwear.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        rightArm.addChild(rightArmwear);

        leftForeArmwear = new BendsModelPart(48, 48 + 6)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftForeArmwear.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftForeArm.addChild(leftForeArmwear);

        rightForeArmwear = new BendsModelPart(40, 32 + 6)
                .setTextureSize(64, 64);
        rightForeArmwear.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightForeArm.addChild(rightForeArmwear);

        leftLegwear = new BendsModelPart(0, 48)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftLegwear.developBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();
        leftLeg.addChild(leftLegwear);

        rightLegwear = new BendsModelPart(0, 32)
                .setTextureSize(64, 64);
        rightLegwear.developBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();
        rightLeg.addChild(rightLegwear);

        leftForeLegwear = new BendsModelPart(0, 48 + 6)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftForeLegwear.developBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftForeLeg.addChild(leftForeLegwear);

        rightForeLegwear = new BendsModelPart(0, 32 + 6)
                .setTextureSize(64, 64);
        rightForeLegwear.developBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + wearOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightForeLeg.addChild(rightForeLegwear);

        return true;
    }

    @Override
    public void syncUpWithData(PlayerData data)
    {
        super.syncUpWithData(data);
    }

    @Override
    public void performAnimations(PlayerData data, String animatedEntityKey,
                                   LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                                   float partialTicks)
    {
        if (leftForeArmwear != null && leftArmwear != null)
            leftForeArmwear.setVisible(leftArmwear.isShowing());
        if (rightForeArmwear != null && rightArmwear != null)
            rightForeArmwear.setVisible(rightArmwear.isShowing());
        if (leftForeLegwear != null && leftLegwear != null)
            leftForeLegwear.setVisible(leftLegwear.isShowing());
        if (rightForeLegwear != null && rightLegwear != null)
            rightForeLegwear.setVisible(rightLegwear.isShowing());

        super.performAnimations(data, animatedEntityKey, renderer, partialTicks);
    }

    @Override
    public void postRefresh()
    {
        if (this.layerArmor != null)
            this.layerArmor.initArmor();
    }

    public void poseForFirstPersonView()
    {
        if (this.body != null) this.body.getRotation().identity();
        if (this.rightArm != null) this.rightArm.getRotation().identity();
        if (this.rightForeArm != null) this.rightForeArm.getRotation().identity();
        if (this.leftArm != null) this.leftArm.getRotation().identity();
        if (this.leftForeArm != null) this.leftForeArm.getRotation().identity();
    }

    @Override
    public boolean isModelVanilla(PlayerModel<AbstractClientPlayer> model)
    {
        return this.body == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof PlayerModel);
    }

    @Override
    public PlayerData getData(AbstractClientPlayer entity)
    {
        if (entity != null && !PlayerPreviewer.isPreviewInProgress())
        {
            IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
            boolean playerIsSlim = skinProvider != null && skinProvider.isSlimModel(entity);
            if (playerIsSlim != this.smallArms)
            {
                this.smallArms = playerIsSlim;
            }
        }
        return super.getData(entity);
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay,
                              int packedColor)
    {
        resolveAdaptivePivots();
        applyBabyHeadScale();
        syncConcealmentFromVanillaModel();

        captureRenderAnchorPose(poseStack);

        if (body != null)
        {
            body.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        }

        renderAttachedParts(poseStack, vertexConsumer, packedLight, packedOverlay);

        if (leftLeg != null)
        {
            leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        }
        if (rightLeg != null)
        {
            rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        }
    }

    @Override
    protected void clearConcealment()
    {
        super.clearConcealment();

        clearConcealed(bodywear);
        clearConcealed(leftArmwear);
        clearConcealed(leftForeArmwear);
        clearConcealed(rightArmwear);
        clearConcealed(rightForeArmwear);
        clearConcealed(leftLegwear);
        clearConcealed(leftForeLegwear);
        clearConcealed(rightLegwear);
        clearConcealed(rightForeLegwear);
    }

    @Override
    protected void syncOuterConcealment(net.minecraft.client.model.HumanoidModel<?> model)
    {
        super.syncOuterConcealment(model);

        if (!(model instanceof net.minecraft.client.model.PlayerModel<?> playerModel))
        {
            return;
        }

        concealWith(bodywear, body, playerModel.jacket);
        concealWith(leftArmwear, leftArm, playerModel.leftSleeve);
        concealWith(leftForeArmwear, leftForeArm, playerModel.leftSleeve);
        concealWith(rightArmwear, rightArm, playerModel.rightSleeve);
        concealWith(rightForeArmwear, rightForeArm, playerModel.rightSleeve);
        concealWith(leftLegwear, leftLeg, playerModel.leftPants);
        concealWith(leftForeLegwear, leftForeLeg, playerModel.leftPants);
        concealWith(rightLegwear, rightLeg, playerModel.rightPants);
        concealWith(rightForeLegwear, rightForeLeg, playerModel.rightPants);
    }

    public BendsModelPart getBodywear() { return bodywear; }
    public BendsModelPart getLeftArmwear() { return leftArmwear; }
    public BendsModelPart getRightArmwear() { return rightArmwear; }
    public BendsModelPart getLeftForeArmwear() { return leftForeArmwear; }
    public BendsModelPart getRightForeArmwear() { return rightForeArmwear; }
    public BendsModelPart getLeftLegwear() { return leftLegwear; }
    public BendsModelPart getRightLegwear() { return rightLegwear; }
    public BendsModelPart getLeftForeLegwear() { return leftForeLegwear; }
    public BendsModelPart getRightForeLegwear() { return rightForeLegwear; }
}
