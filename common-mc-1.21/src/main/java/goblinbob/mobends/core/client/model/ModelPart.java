package goblinbob.mobends.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.TransformUtils;
import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.physics.AABBoxGroup;
import goblinbob.mobends.lib.math.physics.IAABBox;
import goblinbob.mobends.lib.math.physics.ICollider;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.List;

public class ModelPart implements IModelPart
{
    public Vec3f position = new Vec3f();
    public Vec3f scale = new Vec3f(1, 1, 1);
    public Vec3f offset = new Vec3f();
    public SmoothOrientation rotation = new SmoothOrientation();
    public float offsetScale = 1.0F;
    public Vec3f globalOffset = new Vec3f();
    protected List<MutatedBox> mutatedBoxes;

    protected IModelPart parent;
    protected ICollider collider;

    protected int textureOffsetX;
    protected int textureOffsetY;
    protected boolean showModel = true;
    protected boolean isHidden = false;
    protected boolean mirror = false;
    protected List<net.minecraft.client.model.geom.ModelPart> childModels;
    protected List<ModelPart> bendsChildren;
    protected List<Cube> cubeList;

    public ModelPart(int texOffsetX, int texOffsetY)
    {
        this.textureOffsetX = texOffsetX;
        this.textureOffsetY = texOffsetY;
        this.mutatedBoxes = new ArrayList<>();
        this.childModels = new ArrayList<>();
        this.bendsChildren = new ArrayList<>();
        this.cubeList = new ArrayList<>();
    }

    public ModelPart()
    {
        this(0, 0);
    }

    @Override
    public void renderPart(PoseStack poseStack, float scale)
    {
        if (!(this.isShowing())) return;

        poseStack.pushPose();

        this.applyCharacterTransform(poseStack, scale);

        if (this.childModels != null)
        {
            for (net.minecraft.client.model.geom.ModelPart childModel : this.childModels)
            {
            }
        }

        poseStack.popPose();
    }

    @Override
    public void renderJustPart(PoseStack poseStack, float scale)
    {
        if (!(this.isShowing())) return;

        poseStack.pushPose();

        this.applyLocalTransform(poseStack, scale);

        if (this.childModels != null)
        {
            for (net.minecraft.client.model.geom.ModelPart childModel : this.childModels)
            {
            }
        }

        poseStack.popPose();
    }

    @Override
    public void applyPreTransform(PoseStack poseStack, float scale)
    {
        if (this.globalOffset.x != 0.0F || this.globalOffset.y != 0.0F || this.globalOffset.z != 0.0F)
            poseStack.translate(this.globalOffset.x * scale, this.globalOffset.y * scale, this.globalOffset.z * scale);
    }

    @Override
    public void applyPreTransform(float scale, IMat4x4d dest)
    {
        if (this.globalOffset.x != 0.0F || this.globalOffset.y != 0.0F || this.globalOffset.z != 0.0F)
            TransformUtils.translate(dest, this.globalOffset.x * scale, this.globalOffset.y * scale, this.globalOffset.z * scale);
    }

    @Override
    public void applyLocalTransform(PoseStack poseStack, float scale)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            poseStack.translate(this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);

        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            poseStack.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        GlHelper.rotate(poseStack, rotation.getSmooth());

        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            poseStack.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    @Override
    public void applyLocalTransform(float scale, IMat4x4d matrix)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            TransformUtils.translate(matrix, this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);

        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            TransformUtils.translate(matrix, this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        TransformUtils.rotate(matrix, rotation.getSmooth());

        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            TransformUtils.scale(matrix, this.scale.x, this.scale.y, this.scale.z, matrix);
    }

    @Override
    public void applyPostTransform(PoseStack poseStack, float scale)
    {
    }

    @Override
    public void update(float ticksPerFrame)
    {
        this.rotation.update(ticksPerFrame);
    }

    public ModelPart setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    public ModelPart setOffset(float x, float y, float z)
    {
        this.offset.set(x, y, z);
        return this;
    }

    public ModelPart setScale(float x, float y, float z)
    {
        this.scale.x = x;
        this.scale.y = y;
        this.scale.z = z;
        return this;
    }

    public ModelPart resetScale()
    {
        this.scale.set(0, 0, 0);
        return this;
    }

    public BoxFactory developBox(float x, float y, float z, int dx, int dy, int dz, float scaleFactor)
    {
        return new BoxFactory(x, y, z, dx, dy, dz, scaleFactor).setTarget(this);
    }

    public ModelPart addBox(MutatedBox box)
    {
        this.mutatedBoxes.add(box);
        return this;
    }

    public MutatedBox getBox()
    {
        return getBox(0);
    }

    public MutatedBox getBox(int idx)
    {
        return this.mutatedBoxes.get(idx);
    }

    @Override
    public Vec3f getPosition()
    {
        return this.position;
    }

    @Override
    public Vec3f getScale()
    {
        return this.scale;
    }

    @Override
    public Vec3f getOffset()
    {
        return this.offset;
    }

    @Override
    public SmoothOrientation getRotation()
    {
        return this.rotation;
    }

    @Override
    public float getOffsetScale()
    {
        return this.offsetScale;
    }

    @Override
    public IVec3f getGlobalOffset()
    {
        return globalOffset;
    }

    @Override
    public IModelPart getParent()
    {
        return this.parent;
    }

    @Override
    public boolean isShowing()
    {
        return this.showModel && !this.isHidden;
    }

    protected void updateBounds()
    {
        if (this.mutatedBoxes.size() == 1)
        {
            this.collider = this.mutatedBoxes.get(0).createAABB();
        }
        else if (this.mutatedBoxes.size() > 1)
        {
            IAABBox[] bounds = new IAABBox[this.mutatedBoxes.size()];
            for (int i = 0; i < bounds.length; ++i)
            {
                bounds[i] = this.mutatedBoxes.get(i).createAABB();
            }

            this.collider = new AABBoxGroup(bounds);
        }
    }

    public ModelPart setMirror(boolean mirror)
    {
        this.mirror = mirror;
        return this;
    }

    public void finish()
    {
        this.rotation.finish();
    }

    @Override
    public void syncUp(IModelPart part)
    {
        if (part == null)
            return;

        this.position.set(part.getPosition());
        this.offset.set(part.getOffset());
        this.rotation.set(part.getRotation());
        this.scale.set(part.getScale());
        this.offsetScale = part.getOffsetScale();
        this.globalOffset.set(part.getGlobalOffset());
    }

    @Override
    public void setVisible(boolean showModel)
    {
        this.showModel = showModel;
    }

    public ModelPart setParent(IModelPart parent)
    {
        this.parent = parent;
        return this;
    }

    public int getTextureOffsetX()
    {
        return this.textureOffsetX;
    }

    public int getTextureOffsetY()
    {
        return this.textureOffsetY;
    }

    public void setTextureOffset(int x, int y)
    {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
    }

    public ModelPart addChild(ModelPart child)
    {
        this.bendsChildren.add(child);
        child.setParent(this);
        return this;
    }

    public List<ModelPart> getChildren()
    {
        return this.bendsChildren;
    }
}
