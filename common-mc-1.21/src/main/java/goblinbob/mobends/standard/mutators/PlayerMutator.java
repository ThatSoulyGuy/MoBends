package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomCape;
import goblinbob.mobends.standard.data.PlayerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Instantiated once per PlayerRenderer
 */
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

        // Does the renderer have Small Arms?
        // In 1.20.1, slim is a field in PlayerRenderer that determines arm width.
        // The access transformer makes f_117788_ (slim) public.
        if (renderer instanceof PlayerRenderer playerRenderer)
        {
            this.smallArms = detectSlimArms(playerRenderer);
        }
    }

    /**
     * Detect slim arms from the PlayerRenderer.
     * Uses reflection on the 'slim' field (made accessible via access widener),
     * with model cube width analysis as fallback.
     */
    private boolean detectSlimArms(PlayerRenderer playerRenderer)
    {
        // Approach 1: Reflection on 'slim' field (Mojang name + SRG fallback)
        // Access widener makes setAccessible succeed reliably at runtime
        String[] fieldNames = {"slim", "f_117788_"};
        for (String fieldName : fieldNames)
        {
            try
            {
                java.lang.reflect.Field slimField = PlayerRenderer.class.getDeclaredField(fieldName);
                slimField.setAccessible(true);
                boolean result = slimField.getBoolean(playerRenderer);
                LOG.debug("Detected slim arms via field '{}': {}", fieldName, result);
                return result;
            }
            catch (NoSuchFieldException | IllegalAccessException ignored)
            {
            }
        }

        // Approach 2: Check model arm cube width (slim = 3px, default = 4px)
        try
        {
            PlayerModel<?> model = playerRenderer.getModel();
            if (model != null && model.leftArm != null)
            {
                // 'cubes' field also made accessible via access widener
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
                            LOG.debug("Detected slim arms via cube width: true");
                            return true;
                        }
                    }
                    LOG.debug("Detected slim arms via cube width: false");
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

    /**
     * Update the smallArms field based on the player's model name.
     * This should be called when we have access to the player entity.
     */
    public void updateSmallArms(AbstractClientPlayer player)
    {
        if (player != null)
        {
            IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
            this.smallArms = skinProvider != null && skinProvider.isSlimModel(player);
        }
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

        // Note: ElytraLayer is NOT swapped - vanilla elytra layer uses the synced
        // rotations from syncPosesToVanillaModel() and renders correctly
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

    }

    @Override
    public boolean createParts(PlayerModel<AbstractClientPlayer> original, float scaleFactor)
    {
        // Arms
        int armWidth = this.smallArms ? 3 : 4;
        float armY = this.smallArms ? -9.5F : -10F;

        // Create custom bendable parts using BendsModelPart
        // Body - root of upper body hierarchy
        body = new BendsModelPart(16, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F);
        body.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

        // Head - child of body
        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        body.addChild(head);

        // Headwear - child of head
        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 64);
        headwear.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        head.addChild(headwear);

        // Left Arm (texture at 32, 48 for player) - child of body
        leftArm = new BendsModelPart(32, 48)
                .setTextureSize(64, 64)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        leftArm.addCube(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
        body.addChild(leftArm);

        // Right Arm (texture at 40, 16 for player) - child of body
        rightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(-5.0F, armY, 0.0F);
        rightArm.addCube(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor);
        body.addChild(rightArm);

        // Left Forearm - child of leftArm
        leftForeArm = new BendsModelPart(32, 48 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        leftForeArm.addCube(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor, 32, 48);
        leftArm.addChild(leftForeArm);

        // Right Forearm - child of rightArm
        rightForeArm = new BendsModelPart(40, 16 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F);
        rightForeArm.addCube(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor, 40, 16);
        rightArm.addChild(rightForeArm);

        // Legs (texture at 16, 48 for left leg, 0, 16 for right leg in player model)
        // Legs are independent roots (not children of body)
        leftLeg = new BendsModelPart(16, 48)
                .setTextureSize(64, 64)
                .setPosition(1.9F, 12.0F, 0.0F)
                .setMirror(true);
        leftLeg.addCube(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        rightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(-1.9F, 12.0F, 0.0F);
        rightLeg.addCube(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        // Left Foreleg - child of leftLeg
        leftForeLeg = new BendsModelPart(16, 48 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 6.0F, -2.0F)
                .setMirror(true);
        leftForeLeg.addCube(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor, 16, 48);
        leftLeg.addChild(leftForeLeg);

        // Right Foreleg - child of rightLeg
        rightForeLeg = new BendsModelPart(0, 16 + 6)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 6.0F, -2.0F);
        rightForeLeg.addCube(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor, 0, 16);
        rightLeg.addChild(rightForeLeg);

        // Wear layers (second skin layer)
        float wearOffset = 0.25F;

        // Bodywear - child of body
        bodywear = new BendsModelPart(16, 32)
                .setTextureSize(64, 64);
        bodywear.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor + wearOffset);
        body.addChild(bodywear);

        // Left arm wear - child of leftArm
        leftArmwear = new BendsModelPart(48, 48)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftArmwear.addCube(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + wearOffset);
        leftArm.addChild(leftArmwear);

        // Right arm wear - child of rightArm
        rightArmwear = new BendsModelPart(40, 32)
                .setTextureSize(64, 64);
        rightArmwear.addCube(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + wearOffset);
        rightArm.addChild(rightArmwear);

        // Left forearm wear - child of leftForeArm
        leftForeArmwear = new BendsModelPart(48, 48 + 6)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftForeArmwear.addCube(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + wearOffset);
        leftForeArm.addChild(leftForeArmwear);

        // Right forearm wear - child of rightForeArm
        rightForeArmwear = new BendsModelPart(40, 32 + 6)
                .setTextureSize(64, 64);
        rightForeArmwear.addCube(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + wearOffset);
        rightForeArm.addChild(rightForeArmwear);

        // Left leg wear - child of leftLeg
        leftLegwear = new BendsModelPart(0, 48)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftLegwear.addCube(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + wearOffset);
        leftLeg.addChild(leftLegwear);

        // Right leg wear - child of rightLeg
        rightLegwear = new BendsModelPart(0, 32)
                .setTextureSize(64, 64);
        rightLegwear.addCube(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + wearOffset);
        rightLeg.addChild(rightLegwear);

        // Left foreleg wear - child of leftForeLeg
        leftForeLegwear = new BendsModelPart(0, 48 + 6)
                .setTextureSize(64, 64)
                .setMirror(true);
        leftForeLegwear.addCube(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + wearOffset);
        leftForeLeg.addChild(leftForeLegwear);

        // Right foreleg wear - child of rightForeLeg
        rightForeLegwear = new BendsModelPart(0, 32 + 6)
                .setTextureSize(64, 64);
        rightForeLegwear.addCube(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + wearOffset);
        rightForeLeg.addChild(rightForeLegwear);

        return true;
    }

    @Override
    public void syncUpWithData(PlayerData data)
    {
        super.syncUpWithData(data);
        // Sync wear parts with their base parts - they share the same transforms
    }

    @Override
    public void performAnimations(PlayerData data, String animatedEntityKey,
                                   LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                                   float partialTicks)
    {
        // Sync wear visibility with base parts
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

    /**
     * Called before the first person hand is rendered, so the mutator can pose it
     * in any way.
     */
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
        // Check if we've already created custom parts
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
        // Update slim arms based on the actual player's skin model
        // This ensures correct detection even if initial reflection failed
        if (entity != null && !PlayerPreviewer.isPreviewInProgress())
        {
            IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
            boolean playerIsSlim = skinProvider != null && skinProvider.isSlimModel(entity);
            if (playerIsSlim != this.smallArms)
            {
                LOG.debug(
                    "Slim arm mismatch detected for {}: mutator={}, player={}. Updating.",
                    entity.getName().getString(), this.smallArms, playerIsSlim);
                this.smallArms = playerIsSlim;
                // Note: Parts are already created with the old dimension.
                // They will be corrected on next mutation cycle.
            }
        }
        return PlayerPreviewer.isPreviewInProgress() ? PlayerPreviewer.getPreviewData() : super.getData(entity);
    }

    @Override
    public PlayerData getOrMakeData(AbstractClientPlayer entity)
    {
        return PlayerPreviewer.isPreviewInProgress() ? PlayerPreviewer.getPreviewData() : super.getOrMakeData(entity);
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay,
                              int packedColor)
    {
        // Render body and all attached parts (head, arms)
        if (body != null)
        {
            body.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        }

        // Render legs (not attached to body)
        if (leftLeg != null)
        {
            leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        }
        if (rightLeg != null)
        {
            rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        }
    }

    // Getters for wear parts
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
