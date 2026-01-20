package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

/**
 * Wrapper for humanoid limbs (arms and legs) that supports animation with bendable joints.
 * Creates sliced armor geometry that can bend at the elbow/knee.
 *
 * Updated for Minecraft 1.20.1 - creates its own geometry rather than modifying vanilla parts.
 *
 * @deprecated This class is part of the legacy armor rendering system.
 *             Use the three-tier rendering system (ArmorRenderingFacade) with
 *             Tier 1 SplitLimbRenderer for joint splitting instead.
 *             This class is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated
@OnlyIn(Dist.CLIENT)
public class HumanoidLimbWrapper implements IPartWrapper
{
    protected IPartWrapper.DataPartSelector upperPartDataSelector;
    protected IPartWrapper.DataPartSelector lowerPartDataSelector;
    protected IPartWrapper.ModelPartSetter modelPartSetter;

    /**
     * The upper part of the limb (upper arm or upper leg).
     */
    protected ArmorPart upperPart;

    /**
     * The lower part of the limb (forearm or foreleg).
     */
    protected ArmorPart lowerPart;

    /**
     * Whether this wrapper is mirrored.
     */
    protected boolean mirror;

    /**
     * Whether geometry has been created.
     */
    protected boolean geometryCreated = false;

    /**
     * Offset for the lower part anchor.
     */
    protected float lowerOffsetX, lowerOffsetY, lowerOffsetZ;

    /**
     * Inner offset for the upper part.
     */
    protected float innerOffsetX, innerOffsetY, innerOffsetZ;

    /**
     * Parent transform for the body.
     */
    protected IModelPart parentTransform;

    /**
     * The original vanilla part's position for reference.
     */
    protected float vanillaX, vanillaY, vanillaZ;

    public HumanoidLimbWrapper(
        HumanoidModel<?> vanillaModel,
        ModelPart vanillaPart,
        IPartWrapper.ModelPartSetter modelPartSetter,
        IPartWrapper.DataPartSelector upperPartDataSelector,
        IPartWrapper.DataPartSelector lowerPartDataSelector,
        float cutPlane,
        float inflation)
    {
        this.upperPartDataSelector = upperPartDataSelector;
        this.lowerPartDataSelector = lowerPartDataSelector;
        this.modelPartSetter = modelPartSetter;
        // Detect left limbs by positive X position (left arm at x=5, left leg at x=1.9)
        this.mirror = vanillaPart.x > 0;

        // Store vanilla part position for reference
        this.vanillaX = vanillaPart.x;
        this.vanillaY = vanillaPart.y;
        this.vanillaZ = vanillaPart.z;

        // Create the armor parts
        this.upperPart = new ArmorPart();
        this.lowerPart = new ArmorPart();

        // Set up hierarchy - lower part is child of upper part
        this.upperPart.addChild(this.lowerPart);

        // Create the geometry by extracting from vanilla model and slicing
        createSlicedGeometry(vanillaPart, cutPlane, inflation);
    }

    /**
     * Create sliced geometry from the vanilla ModelPart.
     */
    protected void createSlicedGeometry(ModelPart vanillaPart, float cutPlane, float inflation)
    {
        // In 1.20.1, ModelPart stores cubes in a list. We need to extract them.
        // Since ModelPart.cubes is private, we'll create standard humanoid limb geometry
        // that matches the vanilla layout.

        // Standard arm: 4x12x4, positioned at (-1, -2, -2) or (-3, -2, -2)
        // Standard leg: 4x12x4, positioned at (-2, 0, -2)

        // For now, create standard geometry - in a real implementation,
        // we'd use reflection or access transformers to get the actual cube data

        float textureWidth = 64.0F;
        float textureHeight = 32.0F;

        // Get the texture offsets from the vanilla part
        // ModelPart doesn't directly expose these, so we use standard humanoid offsets
        int texU = 40; // Standard arm texture offset
        int texV = 16;

        // Determine if this is an arm or leg based on cut plane
        boolean isArm = cutPlane < 5.0F;

        if (isArm)
        {
            // Arm geometry: 4 wide, 12 tall, 4 deep
            // Upper arm: height 6 (from -2 to 4)
            // Lower arm: height 6 (from 4 to 10)
            createArmGeometry(cutPlane, inflation, textureWidth, textureHeight, texU, texV);
        }
        else
        {
            // Leg geometry: 4 wide, 12 tall, 4 deep
            // Upper leg: height 6 (from 0 to 6)
            // Lower leg: height 6 (from 6 to 12)
            texU = 0;
            createLegGeometry(cutPlane, inflation, textureWidth, textureHeight, texU, texV);
        }

        geometryCreated = true;
    }

    /**
     * Create arm geometry with slice at the elbow.
     * Vanilla arm: box at (-3, -2, -2) with size 4x12x4 for right, (-1, -2, -2) for left
     * Cut plane at 4.0F splits into upper (y=-2 to 4) and lower (y=4 to 10)
     *
     * Vanilla arm positions in local space:
     * - Right arm (mirror=false): box from (-3, -2, -2) to (1, 10, 2) - pivot at shoulder edge
     * - Left arm (mirror=true): box from (-1, -2, -2) to (3, 10, 2) - pivot at shoulder edge
     */
    protected void createArmGeometry(float cutPlane, float inflation, float textureWidth, float textureHeight, int texU, int texV)
    {
        // Full arm dimensions for UV mapping: 4 wide, 12 tall, 4 deep
        int uvWidth = 4;
        int uvHeight = 12;
        int uvDepth = 4;

        // Arm dimensions: 4 wide, 12 tall, 4 deep
        // X offset depends on which arm (pivot is at shoulder edge, arm extends outward)
        // Right arm: x from -3 to 1 (extends in -X direction, toward center of body)
        // Left arm: x from -1 to 3 (extends in +X direction, toward center of body)
        float xOffset = mirror ? 1.0F : -1.0F;  // Left arm +1, Right arm -1

        // Upper portion: y from -2 to cutPlane (height = 6)
        float upperMinX = -2.0F + xOffset;
        float upperMinY = -2.0F;
        float upperMinZ = -2.0F;
        float upperMaxX = 2.0F + xOffset;
        float upperMaxY = cutPlane;  // 4.0F
        float upperMaxZ = 2.0F;

        int upperHeight = (int)(upperMaxY - upperMinY);  // 6

        // Use full arm dimensions for UV, with no V offset for upper portion
        ArmorCube upperCube = new ArmorCube(
                upperMinX, upperMinY, upperMinZ,
                upperMaxX, upperMaxY, upperMaxZ,
                inflation,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, 0  // vOffset = 0 for upper portion
        );
        upperCube.hideFace(ArmorCube.BOTTOM);
        upperPart.addCube(upperCube);

        // Lower portion: positioned relative to the joint at cutPlane
        // In local space of lowerPart, geometry starts at y=0
        // Same X offset as upper portion
        float lowerMinX = -2.0F + xOffset;
        float lowerMinY = 0.0F;  // Local to the lower part
        float lowerMinZ = -2.0F;
        float lowerMaxX = 2.0F + xOffset;
        float lowerMaxY = 6.0F;  // 6 units tall (from cutPlane to original maxY)
        float lowerMaxZ = 2.0F;

        // Use full arm dimensions for UV, with V offset for lower portion
        ArmorCube lowerCube = new ArmorCube(
                lowerMinX, lowerMinY, lowerMinZ,
                lowerMaxX, lowerMaxY, lowerMaxZ,
                inflation + 0.001F,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, upperHeight  // vOffset = upper height for lower portion
        );
        lowerCube.hideFace(ArmorCube.TOP);
        lowerPart.addCube(lowerCube);

        // Note: lowerPart's position comes from animation data via syncUp()
        // The offsetLower() call positions the geometry within the joint
    }

    /**
     * Create leg geometry with slice at the knee.
     * Vanilla leg: box at (-2, 0, -2) with size 4x12x4
     * Cut plane at 6.0F splits into upper (y=0 to 6) and lower (y=6 to 12)
     */
    protected void createLegGeometry(float cutPlane, float inflation, float textureWidth, float textureHeight, int texU, int texV)
    {
        // Full leg dimensions for UV mapping: 4 wide, 12 tall, 4 deep
        int uvWidth = 4;
        int uvHeight = 12;
        int uvDepth = 4;

        // Leg dimensions: 4 wide, 12 tall, 4 deep
        // Both legs box: (-2, 0, -2) to (2, 12, 2) in local space
        // Cut plane at y=6

        // Upper portion: y from 0 to cutPlane (height = 6)
        float upperMinX = -2.0F;
        float upperMinY = 0.0F;
        float upperMinZ = -2.0F;
        float upperMaxX = 2.0F;
        float upperMaxY = cutPlane;  // 6.0F
        float upperMaxZ = 2.0F;

        int upperHeight = (int)(upperMaxY - upperMinY);  // 6

        // Use full leg dimensions for UV, with no V offset for upper portion
        ArmorCube upperCube = new ArmorCube(
                upperMinX, upperMinY, upperMinZ,
                upperMaxX, upperMaxY, upperMaxZ,
                inflation,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, 0  // vOffset = 0 for upper portion
        );
        upperCube.hideFace(ArmorCube.BOTTOM);
        upperPart.addCube(upperCube);

        // Lower portion: positioned relative to the joint at cutPlane
        // In local space of lowerPart, geometry starts at y=0
        float lowerMinX = -2.0F;
        float lowerMinY = 0.0F;  // Local to the lower part
        float lowerMinZ = -2.0F;
        float lowerMaxX = 2.0F;
        float lowerMaxY = 6.0F;  // 6 units tall (from cutPlane to original maxY)
        float lowerMaxZ = 2.0F;

        // Use full leg dimensions for UV, with V offset for lower portion
        ArmorCube lowerCube = new ArmorCube(
                lowerMinX, lowerMinY, lowerMinZ,
                lowerMaxX, lowerMaxY, lowerMaxZ,
                inflation + 0.001F,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, upperHeight  // vOffset = upper height for lower portion
        );
        lowerCube.hideFace(ArmorCube.TOP);
        lowerPart.addCube(lowerCube);

        // Note: lowerPart's position comes from animation data via syncUp()
        // The offsetLower() call positions the geometry within the joint
    }

    @Override
    public void syncUp(BipedEntityData<?> data)
    {
        if (data == null) return;

        IModelPart upperData = upperPartDataSelector.selectPart(data);
        IModelPart lowerData = lowerPartDataSelector.selectPart(data);

        upperPart.syncUp(upperData);
        lowerPart.syncUp(lowerData);
    }

    @Override
    public void apply(ArmorWrapper armorWrapper)
    {
        // In 1.20.1, we don't swap parts - we just mark that we should render our custom geometry
        upperPart.visible = true;
    }

    @Override
    public void deapply(ArmorWrapper armorWrapper)
    {
        // Mark parts as not needing custom rendering
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
        upperPart.setInnerOffset(x, y, z);
        return this;
    }

    public HumanoidLimbWrapper offsetLower(float x, float y, float z)
    {
        this.lowerOffsetX = x;
        this.lowerOffsetY = y;
        this.lowerOffsetZ = z;
        lowerPart.setInnerOffset(x, y, z);
        return this;
    }

    /**
     * Render this limb wrapper's geometry.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        if (!upperPart.visible) return;

        poseStack.pushPose();

        // Apply parent transform (body)
        if (parentTransform != null)
        {
            parentTransform.applyCharacterTransform(poseStack, 1.0F / 16.0F);
        }

        // Render the upper part (which includes the lower part as a child)
        upperPart.renderLocal(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.popPose();
    }

    public ArmorPart getUpperPart()
    {
        return upperPart;
    }

    public ArmorPart getLowerPart()
    {
        return lowerPart;
    }

    /**
     * Update the rotation interpolation.
     */
    public void update(float ticksPerFrame)
    {
        upperPart.update(ticksPerFrame);
    }
}
