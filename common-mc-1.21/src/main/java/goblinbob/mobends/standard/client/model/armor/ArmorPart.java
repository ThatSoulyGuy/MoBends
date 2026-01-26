package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * An armor part that can contain multiple cubes and apply animation transforms.
 * This is used for rendering armor with proper Mo' Bends animation.
 */
public class ArmorPart
{
    /**
     * Position offset in model units.
     */
    public Vec3f position = new Vec3f();

    /**
     * Additional offset for positioning.
     */
    public Vec3f innerOffset = new Vec3f();

    /**
     * Rotation quaternion.
     */
    public SmoothOrientation rotation = new SmoothOrientation();

    /**
     * Whether this part is visible.
     */
    public boolean visible = true;

    /**
     * The cubes that make up this part.
     */
    protected final List<ArmorCube> cubes = new ArrayList<>();

    /**
     * Child parts that inherit this part's transform.
     */
    protected final List<ArmorPart> children = new ArrayList<>();

    /**
     * Optional parent for transform hierarchy.
     */
    protected ArmorPart parent;

    public ArmorPart()
    {
    }

    /**
     * Sync this part's transform with an IModelPart.
     */
    public void syncUp(IModelPart source)
    {
        if (source == null) return;

        this.position.set(source.getPosition());
        this.rotation.set(source.getRotation());
    }

    /**
     * Add a cube to this part.
     */
    public ArmorPart addCube(ArmorCube cube)
    {
        this.cubes.add(cube);
        return this;
    }

    /**
     * Add a child part.
     */
    public ArmorPart addChild(ArmorPart child)
    {
        child.parent = this;
        this.children.add(child);
        return this;
    }

    /**
     * Set the position offset.
     */
    public ArmorPart setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    /**
     * Set the inner offset (applied after position and rotation).
     */
    public ArmorPart setInnerOffset(float x, float y, float z)
    {
        this.innerOffset.set(x, y, z);
        return this;
    }

    /**
     * Render this part and all children with the full transform chain.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        if (!visible) return;

        poseStack.pushPose();

        // Apply parent transforms first (recursive up the chain)
        applyFullTransform(poseStack);

        // Render all cubes
        for (ArmorCube cube : cubes)
        {
            cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        poseStack.popPose();

        // Render children (they handle their own transforms)
        for (ArmorPart child : children)
        {
            child.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    /**
     * Render just this part's cubes and children, using local transform only.
     * Inner offset is applied to geometry but NOT to children.
     */
    public void renderLocal(PoseStack poseStack, VertexConsumer vertexConsumer,
                            int packedLight, int packedOverlay,
                            float red, float green, float blue, float alpha)
    {
        if (!visible) return;

        float scale = 1.0F / 16.0F;

        poseStack.pushPose();

        // Apply position and rotation (but NOT inner offset yet)
        applyTransformWithoutInnerOffset(poseStack);

        // Now apply inner offset for geometry only
        poseStack.pushPose();
        if (innerOffset.x != 0.0F || innerOffset.y != 0.0F || innerOffset.z != 0.0F)
        {
            poseStack.translate(innerOffset.x * scale, innerOffset.y * scale, innerOffset.z * scale);
        }

        // Render geometry with inner offset applied
        for (ArmorCube cube : cubes)
        {
            cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        poseStack.popPose();  // Remove inner offset

        // Render children without inner offset (they have their own transforms)
        for (ArmorPart child : children)
        {
            child.renderLocal(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        poseStack.popPose();  // Remove position/rotation
    }

    /**
     * Apply the full transform chain (including all parents).
     */
    protected void applyFullTransform(PoseStack poseStack)
    {
        if (parent != null)
        {
            parent.applyFullTransform(poseStack);
        }
        applyLocalTransform(poseStack);
    }

    /**
     * Apply just this part's local transform (including inner offset).
     */
    protected void applyLocalTransform(PoseStack poseStack)
    {
        float scale = 1.0F / 16.0F;

        // Apply position
        if (position.x != 0.0F || position.y != 0.0F || position.z != 0.0F)
        {
            poseStack.translate(position.x * scale, position.y * scale, position.z * scale);
        }

        // Apply rotation
        GlHelper.rotate(poseStack, rotation.getSmooth());

        // Apply inner offset (after rotation, so it's in local space)
        if (innerOffset.x != 0.0F || innerOffset.y != 0.0F || innerOffset.z != 0.0F)
        {
            poseStack.translate(innerOffset.x * scale, innerOffset.y * scale, innerOffset.z * scale);
        }
    }

    /**
     * Apply just position and rotation, without inner offset.
     * Used when rendering so inner offset only affects geometry, not children.
     */
    protected void applyTransformWithoutInnerOffset(PoseStack poseStack)
    {
        float scale = 1.0F / 16.0F;

        // Apply position
        if (position.x != 0.0F || position.y != 0.0F || position.z != 0.0F)
        {
            poseStack.translate(position.x * scale, position.y * scale, position.z * scale);
        }

        // Apply rotation
        GlHelper.rotate(poseStack, rotation.getSmooth());
    }

    /**
     * Update rotation interpolation.
     */
    public void update(float ticksPerFrame)
    {
        rotation.update(ticksPerFrame);
        for (ArmorPart child : children)
        {
            child.update(ticksPerFrame);
        }
    }

    public List<ArmorCube> getCubes()
    {
        return cubes;
    }

    public List<ArmorPart> getChildren()
    {
        return children;
    }
}
