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

public class BendsModelPart implements IModelPart
{
    public Vec3f position = new Vec3f();
    public Vec3f scale = new Vec3f(1, 1, 1);
    public Vec3f offset = new Vec3f();
    public SmoothOrientation rotation = new SmoothOrientation();

    public float offsetScale = 1.0F;

    public Vec3f globalOffset = new Vec3f();

    protected int textureOffsetX;
    protected int textureOffsetY;
    protected float textureWidth = 64.0F;
    protected float textureHeight = 32.0F;

    protected IModelPart parent;

    protected final List<BendsCube> cubes = new ArrayList<>();

    protected final List<BendsModelPart> children = new ArrayList<>();

    public boolean visible = true;

    public boolean hidden = false;

    public boolean concealed = false;

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

    @Deprecated
    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        int color = ((int)(alpha * 255.0F) << 24) |
                    ((int)(red * 255.0F) << 16) |
                    ((int)(green * 255.0F) << 8) |
                    (int)(blue * 255.0F);
        render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay, int color)
    {
        if (!isShowingIgnoringConcealment()) return;

        poseStack.pushPose();

        applyCharacterTransformPoseStack(poseStack);

        if (!concealed)
        {
            for (BendsCube cube : cubes)
            {
                cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, color);
            }
        }

        for (BendsModelPart child : children)
        {
            child.renderJust(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        poseStack.popPose();
    }

    @Deprecated
    public void renderJust(PoseStack poseStack, VertexConsumer vertexConsumer,
                           int packedLight, int packedOverlay,
                           float red, float green, float blue, float alpha)
    {
        int color = ((int)(alpha * 255.0F) << 24) |
                    ((int)(red * 255.0F) << 16) |
                    ((int)(green * 255.0F) << 8) |
                    (int)(blue * 255.0F);
        renderJust(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void renderJust(PoseStack poseStack, VertexConsumer vertexConsumer,
                           int packedLight, int packedOverlay, int color)
    {
        if (!isShowingIgnoringConcealment()) return;

        poseStack.pushPose();

        applyPreTransformPoseStack(poseStack);
        applyLocalTransformPoseStack(poseStack);

        if (!concealed)
        {
            for (BendsCube cube : cubes)
            {
                cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, color);
            }
        }

        for (BendsModelPart child : children)
        {
            child.renderJust(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        poseStack.popPose();
    }

    public void applyCharacterTransformPoseStack(PoseStack poseStack)
    {
        if (parent != null && parent instanceof BendsModelPart)
        {
            ((BendsModelPart) parent).applyCharacterTransformPoseStack(poseStack);
        }
        applyPreTransformPoseStack(poseStack);
        applyLocalTransformPoseStack(poseStack);
    }

    public void applyPreTransformPoseStack(PoseStack poseStack)
    {
        if (globalOffset.x != 0.0F || globalOffset.y != 0.0F || globalOffset.z != 0.0F)
        {
            float scale = 1.0F / 16.0F;
            poseStack.translate(globalOffset.x * scale, globalOffset.y * scale, globalOffset.z * scale);
        }
    }

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

        GlHelper.rotate(poseStack, rotation.getSmooth());

        if (this.scale.x != 1.0F || this.scale.y != 1.0F || this.scale.z != 1.0F)
        {
            poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
        }
    }

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
    }

    @Override
    public void renderPart(PoseStack poseStack, float scale)
    {
    }

    @Override
    public void renderJustPart(PoseStack poseStack, float scale)
    {
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
        return visible && !hidden && !concealed;
    }

    public boolean isShowingIgnoringConcealment()
    {
        return visible && !hidden;
    }

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

    public static byte hiding(BoxSide... hiddenFaces)
    {
        byte flag = (byte) 0b111111;
        for (BoxSide side : hiddenFaces)
        {
            flag &= (byte) ~(1 << side.faceIndex);
        }
        return flag;
    }

    public BendsModelPart addCube(float x, float y, float z, int width, int height, int depth, float inflation,
                                  byte faceVisibilityFlag)
    {
        BendsCube cube = new BendsCube(textureOffsetX, textureOffsetY,
                                       x, y, z, width, height, depth,
                                       inflation, textureWidth, textureHeight, mirror,
                                       faceVisibilityFlag);
        this.cubes.add(cube);
        return this;
    }

    public BendsModelPart addCube(float x, float y, float z, int width, int height, int depth, float inflation,
                                  int bottomTexOffsetX, int bottomTexOffsetY, byte faceVisibilityFlag)
    {
        BendsCube cube = new BendsCube(textureOffsetX, textureOffsetY,
                                       x, y, z, width, height, depth,
                                       inflation, textureWidth, textureHeight, mirror,
                                       faceVisibilityFlag,
                                       bottomTexOffsetX, bottomTexOffsetY);
        this.cubes.add(cube);
        return this;
    }

    public BendsModelPart addCube(float x, float y, float z, int width, int height, int depth, float inflation,
                                  int bottomTexOffsetX, int bottomTexOffsetY)
    {
        BendsCube cube = new BendsCube(textureOffsetX, textureOffsetY,
                                       x, y, z, width, height, depth,
                                       inflation, textureWidth, textureHeight, mirror,
                                       bottomTexOffsetX, bottomTexOffsetY);
        this.cubes.add(cube);
        return this;
    }

    public BendsBoxFactory developBox(float x, float y, float z, int dx, int dy, int dz, float delta)
    {
        return new BendsBoxFactory(x, y, z, dx, dy, dz, delta).setTarget(this);
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

    public float getTextureWidth()
    {
        return textureWidth;
    }

    public float getTextureHeight()
    {
        return textureHeight;
    }

    public void finish()
    {
        rotation.finish();
    }
}
