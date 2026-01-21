package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Wrapper for a single body part in humanoid armor models (head, body, headwear).
 * Creates armor geometry that follows Mo' Bends animation.
 *
 * Updated for Minecraft 1.20.1 - creates its own renderable geometry.
 *
 * @deprecated This class is part of the legacy armor rendering system.
 *             Use the three-tier rendering system (ArmorRenderingFacade) instead.
 *             This class is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated
@OnlyIn(Dist.CLIENT)
public class HumanoidPartWrapper implements IPartWrapper
{
    protected IPartWrapper.DataPartSelector dataPartSelector;
    protected IPartWrapper.ModelPartSetter modelPartSetter;

    /**
     * The armor part containing our geometry.
     */
    protected ArmorPart armorPart;

    /**
     * Parent transform.
     */
    protected IModelPart parentTransform;

    /**
     * Inner offset.
     */
    protected float innerOffsetX, innerOffsetY, innerOffsetZ;

    /**
     * The type of part (for determining geometry).
     */
    public enum PartType
    {
        HEAD,
        BODY,
        HEADWEAR
    }

    protected PartType partType;
    protected float inflation;

    public HumanoidPartWrapper(
        HumanoidModel<?> vanillaModel,
        ModelPart vanillaPart,
        IPartWrapper.ModelPartSetter modelPartSetter,
        IPartWrapper.DataPartSelector dataPartSelector,
        PartType partType,
        float inflation)
    {
        this.dataPartSelector = dataPartSelector;
        this.modelPartSetter = modelPartSetter;
        this.partType = partType;
        this.inflation = inflation;

        // Create the armor part with appropriate geometry
        this.armorPart = new ArmorPart();
        createGeometry(vanillaPart, partType, inflation);
    }

    /**
     * Create geometry appropriate for this part type.
     * @param vanillaPart The vanilla model part for reference
     * @param partType The type of part (HEAD, BODY, HEADWEAR)
     * @param inflation The inflation amount from the armor layer
     */
    protected void createGeometry(ModelPart vanillaPart, PartType partType, float inflation)
    {
        float textureWidth = 64.0F;
        float textureHeight = 32.0F;

        switch (partType)
        {
            case HEAD:
                // Head: 8x8x8, positioned at (-4, -8, -4), texture at (0, 0)
                ArmorCube headCube = new ArmorCube(
                        -4.0F, -8.0F, -4.0F,
                        4.0F, 0.0F, 4.0F,
                        inflation,
                        0, 0,
                        textureWidth, textureHeight,
                        false
                );
                armorPart.addCube(headCube);
                break;

            case HEADWEAR:
                // Headwear (hat layer): 8x8x8, positioned at (-4, -8, -4), texture at (32, 0)
                // Headwear is slightly larger than base head
                ArmorCube hatCube = new ArmorCube(
                        -4.0F, -8.0F, -4.0F,
                        4.0F, 0.0F, 4.0F,
                        inflation + 0.5F,  // Extra 0.5 on top of base inflation for outer layer
                        32, 0,
                        textureWidth, textureHeight,
                        false
                );
                armorPart.addCube(hatCube);
                break;

            case BODY:
            default:
                // Body: 8x12x4, positioned at (-4, 0, -2), texture at (16, 16)
                ArmorCube bodyCube = new ArmorCube(
                        -4.0F, 0.0F, -2.0F,
                        4.0F, 12.0F, 2.0F,
                        inflation,
                        16, 16,
                        textureWidth, textureHeight,
                        false
                );
                armorPart.addCube(bodyCube);
                break;
        }
    }

    @Override
    public void syncUp(BipedEntityData<?> data)
    {
        if (data == null) return;
        armorPart.syncUp(dataPartSelector.selectPart(data));
    }

    @Override
    public void apply(ArmorWrapper armorWrapper)
    {
        armorPart.visible = true;
    }

    @Override
    public void deapply(ArmorWrapper armorWrapper)
    {
        // No action needed
    }

    @Override
    public IPartWrapper setParent(IModelPart parent)
    {
        this.parentTransform = parent;
        return this;
    }

    @Override
    public IPartWrapper offsetInner(float x, float y, float z)
    {
        this.innerOffsetX = x;
        this.innerOffsetY = y;
        this.innerOffsetZ = z;
        armorPart.setInnerOffset(x, y, z);
        return this;
    }

    /**
     * Render this part's geometry.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        if (!armorPart.visible) return;

        poseStack.pushPose();

        // Apply parent transform (typically body for head)
        if (parentTransform != null)
        {
            parentTransform.applyCharacterTransform(poseStack, 1.0F / 16.0F);
        }

        // Render the part
        armorPart.renderLocal(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.popPose();
    }

    public ArmorPart getArmorPart()
    {
        return armorPart;
    }

    /**
     * Update rotation interpolation.
     */
    public void update(float ticksPerFrame)
    {
        armorPart.update(ticksPerFrame);
    }
}
