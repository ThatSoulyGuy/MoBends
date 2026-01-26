package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.TransformUtils;
import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A composition-based model part for 1.20.1.
 * This replaces the old ModelPart that extended ModelRenderer.
 * Uses PoseStack and VertexConsumer for modern rendering.
 */
public class BendsModelPart implements IModelPart
{
    public Vec3f position = new Vec3f();
    public Vec3f scale = new Vec3f(1, 1, 1);
    public Vec3f offset = new Vec3f();
    public SmoothOrientation rotation = new SmoothOrientation();

    /**
     * The scale at which animation position offset is applied, used for child models.
     */
    public float offsetScale = 1.0F;

    /**
     * Offset applied before the parent transformation.
     */
    public Vec3f globalOffset = new Vec3f();

    /**
     * Texture offset for UV mapping.
     */
    protected int textureOffsetX;
    protected int textureOffsetY;
    protected float textureWidth = 64.0F;
    protected float textureHeight = 32.0F;

    /**
     * An optional parent.
     */
    protected IModelPart parent;

    /**
     * The list of cubes (boxes) that make up this part.
     */
    protected final List<BendsCube> cubes = new ArrayList<>();

    /**
     * Child model parts.
     */
    protected final List<BendsModelPart> children = new ArrayList<>();

    /**
     * Whether this part is visible.
     */
    public boolean visible = true;

    /**
     * Whether this part is hidden (different from visible for animation purposes).
     */
    public boolean hidden = false;

    /**
     * Whether to mirror the UV coordinates.
     */
    public boolean mirror = false;

    public BendsModelPart()
    {
        this(0, 0);
    }

    public BendsModelPart(int texOffsetX, int texOffsetY)
    {
        this.textureOffsetX = texOffsetX;
        this.textureOffsetY = texOffsetY;
    }

    /**
     * Render this part and all children using the modern PoseStack/VertexConsumer system.
     * This applies the full character transform chain (including all parent transforms).
     * Use this method for root parts that need to be positioned in world space.
     *
     * @deprecated Use the overload with int color parameter for 1.21.1+
     */
    @Deprecated
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        // Convert RGBA floats to packed ARGB int (range 0.0-1.0 to 0-255)
        int color = ((int)(alpha * 255.0F) << 24) |
                    ((int)(red * 255.0F) << 16) |
                    ((int)(green * 255.0F) << 8) |
                    (int)(blue * 255.0F);
        render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * Render this part and all children using the modern PoseStack/VertexConsumer system (1.21.1+).
     * This applies the full character transform chain (including all parent transforms).
     * Use this method for root parts that need to be positioned in world space.
     */
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay, int color)
    {
        if (!isShowing()) return;

        poseStack.pushPose();

        applyCharacterTransformPoseStack(poseStack);

        // Render all cubes
        for (BendsCube cube : cubes)
        {
            cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, color);
        }

        // Render children using renderJust since our transform is already on the stack
        for (BendsModelPart child : children)
        {
            child.renderJust(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        poseStack.popPose();
    }

    /**
     * Render just this part without the full character transform.
     *
     * @deprecated Use the overload with int color parameter for 1.21.1+
     */
    @Deprecated
    public void renderJust(PoseStack poseStack, VertexConsumer vertexConsumer,
                           int packedLight, int packedOverlay,
                           float red, float green, float blue, float alpha)
    {
        // Convert RGBA floats to packed ARGB int (range 0.0-1.0 to 0-255)
        int color = ((int)(alpha * 255.0F) << 24) |
                    ((int)(red * 255.0F) << 16) |
                    ((int)(green * 255.0F) << 8) |
                    (int)(blue * 255.0F);
        renderJust(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * Render just this part without the full character transform (1.21.1+).
     */
    public void renderJust(PoseStack poseStack, VertexConsumer vertexConsumer,
                           int packedLight, int packedOverlay, int color)
    {
        if (!isShowing()) return;

        poseStack.pushPose();

        applyLocalTransformPoseStack(poseStack);

        // Render all cubes
        for (BendsCube cube : cubes)
        {
            cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, color);
        }

        // Render children
        for (BendsModelPart child : children)
        {
            child.renderJust(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        poseStack.popPose();
    }

    /**
     * Apply the character transform to a PoseStack (modern 1.20.1 style).
     * Parent transforms are applied first, then this part's transforms.
     */
    public void applyCharacterTransformPoseStack(PoseStack poseStack)
    {
        // First apply parent's entire transform chain (recursive up to root)
        if (parent != null && parent instanceof BendsModelPart)
        {
            ((BendsModelPart) parent).applyCharacterTransformPoseStack(poseStack);
        }
        // Then apply this part's transforms
        applyPreTransformPoseStack(poseStack);
        applyLocalTransformPoseStack(poseStack);
    }

    /**
     * Apply the pre-transform (global offset) to a PoseStack.
     */
    public void applyPreTransformPoseStack(PoseStack poseStack)
    {
        if (globalOffset.x != 0.0F || globalOffset.y != 0.0F || globalOffset.z != 0.0F)
        {
            float scale = 1.0F / 16.0F;
            poseStack.translate(globalOffset.x * scale, globalOffset.y * scale, globalOffset.z * scale);
        }
    }

    /**
     * Apply the local transform (position, rotation, scale) to a PoseStack.
     */
    public void applyLocalTransformPoseStack(PoseStack poseStack)
    {
        float scale = 1.0F / 16.0F;

        if (position.x != 0.0F || position.y != 0.0F || position.z != 0.0F)
        {
            poseStack.translate(position.x * scale * offsetScale,
                               position.y * scale * offsetScale,
                               position.z * scale * offsetScale);
        }

        if (offset.x != 0.0F || offset.y != 0.0F || offset.z != 0.0F)
        {
            poseStack.translate(offset.x * scale * offsetScale,
                               offset.y * scale * offsetScale,
                               offset.z * scale * offsetScale);
        }

        // Apply quaternion rotation
        GlHelper.rotate(poseStack, rotation.getSmooth());

        if (this.scale.x != 1.0F || this.scale.y != 1.0F || this.scale.z != 1.0F)
        {
            poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
        }
    }

    // ============================================
    // IModelPart implementation
    // ============================================

    @Override
    public void applyPreTransform(PoseStack poseStack, float scale)
    {
        if (globalOffset.x != 0.0F || globalOffset.y != 0.0F || globalOffset.z != 0.0F)
        {
            poseStack.translate(globalOffset.x * scale, globalOffset.y * scale, globalOffset.z * scale);
        }
    }

    @Override
    public void applyPreTransform(float scale, IMat4x4d dest)
    {
        if (globalOffset.x != 0.0F || globalOffset.y != 0.0F || globalOffset.z != 0.0F)
        {
            TransformUtils.translate(dest, globalOffset.x * scale, globalOffset.y * scale, globalOffset.z * scale);
        }
    }

    @Override
    public void applyLocalTransform(PoseStack poseStack, float scale)
    {
        if (position.x != 0.0F || position.y != 0.0F || position.z != 0.0F)
        {
            poseStack.translate(position.x * scale * offsetScale,
                               position.y * scale * offsetScale,
                               position.z * scale * offsetScale);
        }

        if (offset.x != 0.0F || offset.y != 0.0F || offset.z != 0.0F)
        {
            poseStack.translate(offset.x * scale * offsetScale,
                               offset.y * scale * offsetScale,
                               offset.z * scale * offsetScale);
        }

        // Apply quaternion rotation
        GlHelper.rotate(poseStack, rotation.getSmooth());

        if (this.scale.x != 1.0F || this.scale.y != 1.0F || this.scale.z != 1.0F)
        {
            poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
        }
    }

    @Override
    public void applyLocalTransform(float scale, IMat4x4d matrix)
    {
        if (position.x != 0.0F || position.y != 0.0F || position.z != 0.0F)
        {
            TransformUtils.translate(matrix, position.x * scale * offsetScale,
                                    position.y * scale * offsetScale,
                                    position.z * scale * offsetScale);
        }

        if (offset.x != 0.0F || offset.y != 0.0F || offset.z != 0.0F)
        {
            TransformUtils.translate(matrix, offset.x * scale * offsetScale,
                                    offset.y * scale * offsetScale,
                                    offset.z * scale * offsetScale);
        }

        TransformUtils.rotate(matrix, rotation.getSmooth());

        if (this.scale.x != 1.0F || this.scale.y != 1.0F || this.scale.z != 1.0F)
        {
            TransformUtils.scale(matrix, this.scale.x, this.scale.y, this.scale.z, matrix);
        }
    }

    @Override
    public void applyPostTransform(PoseStack poseStack, float scale)
    {
        // No post-transform needed in the new system
    }

    @Override
    public void renderPart(PoseStack poseStack, float scale)
    {
        // Legacy method - in 1.20.1 we use render(PoseStack, VertexConsumer, ...) with full params
        // This is a stub for interface compliance
    }

    @Override
    public void renderJustPart(PoseStack poseStack, float scale)
    {
        // Legacy method - in 1.20.1 we use renderJust(PoseStack, VertexConsumer, ...) with full params
        // This is a stub for interface compliance
    }

    @Override
    public void update(float ticksPerFrame)
    {
        rotation.update(ticksPerFrame);

        for (BendsModelPart child : children)
        {
            child.update(ticksPerFrame);
        }
    }

    @Override
    public void syncUp(IModelPart part)
    {
        if (part == null) return;

        position.set(part.getPosition());
        offset.set(part.getOffset());
        rotation.set(part.getRotation());
        scale.set(part.getScale());
        offsetScale = part.getOffsetScale();
        globalOffset.set(part.getGlobalOffset());
    }

    @Override
    public void setVisible(boolean showModel)
    {
        this.visible = showModel;
    }

    @Override
    public IVec3f getPosition()
    {
        return position;
    }

    @Override
    public IVec3f getScale()
    {
        return scale;
    }

    @Override
    public IVec3f getOffset()
    {
        return offset;
    }

    @Override
    public SmoothOrientation getRotation()
    {
        return rotation;
    }

    @Override
    public float getOffsetScale()
    {
        return offsetScale;
    }

    @Override
    public IVec3f getGlobalOffset()
    {
        return globalOffset;
    }

    @Override
    public IModelPart getParent()
    {
        return parent;
    }

    @Override
    public boolean isShowing()
    {
        return visible && !hidden;
    }

    // ============================================
    // Builder methods
    // ============================================

    public BendsModelPart setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    public BendsModelPart setOffset(float x, float y, float z)
    {
        this.offset.set(x, y, z);
        return this;
    }

    public BendsModelPart setScale(float x, float y, float z)
    {
        this.scale.set(x, y, z);
        return this;
    }

    public BendsModelPart setParent(IModelPart parent)
    {
        this.parent = parent;
        return this;
    }

    public BendsModelPart setTextureOffset(int x, int y)
    {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public BendsModelPart setTextureSize(float width, float height)
    {
        this.textureWidth = width;
        this.textureHeight = height;
        return this;
    }

    public BendsModelPart setMirror(boolean mirror)
    {
        this.mirror = mirror;
        return this;
    }

    public BendsModelPart addCube(BendsCube cube)
    {
        this.cubes.add(cube);
        return this;
    }

    public BendsModelPart addCube(float x, float y, float z, int width, int height, int depth, float inflation)
    {
        BendsCube cube = new BendsCube(textureOffsetX, textureOffsetY,
                                       x, y, z, width, height, depth,
                                       inflation, textureWidth, textureHeight, mirror);
        this.cubes.add(cube);
        return this;
    }

    public BendsModelPart addChild(BendsModelPart child)
    {
        child.setParent(this);
        this.children.add(child);
        return this;
    }

    public List<BendsCube> getCubes()
    {
        return cubes;
    }

    public List<BendsModelPart> getChildren()
    {
        return children;
    }

    public int getTextureOffsetX()
    {
        return textureOffsetX;
    }

    public int getTextureOffsetY()
    {
        return textureOffsetY;
    }

    public void finish()
    {
        rotation.finish();
    }
}
