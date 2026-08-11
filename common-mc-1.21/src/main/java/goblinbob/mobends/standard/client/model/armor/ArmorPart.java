package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;

import java.util.ArrayList;
import java.util.List;

public class ArmorPart
{
    public Vec3f position = new Vec3f();

    public Vec3f innerOffset = new Vec3f();

    public SmoothOrientation rotation = new SmoothOrientation();

    public boolean visible = true;

    protected final List<ArmorCube> cubes = new ArrayList<>();

    protected final List<ArmorPart> children = new ArrayList<>();

    protected ArmorPart parent;

    public ArmorPart()
    {
    }

    public void syncUp(IModelPart source)
    {
        if (source == null) return;

        this.position.set(source.getPosition());
        this.rotation.set(source.getRotation());
    }

    public ArmorPart addCube(ArmorCube cube)
    {
        this.cubes.add(cube);
        return this;
    }

    public ArmorPart addChild(ArmorPart child)
    {
        child.parent = this;
        this.children.add(child);
        return this;
    }

    public ArmorPart setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    public ArmorPart setInnerOffset(float x, float y, float z)
    {
        this.innerOffset.set(x, y, z);
        return this;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        if (!visible) return;

        poseStack.pushPose();

        applyFullTransform(poseStack);

        for (ArmorCube cube : cubes)
        {
            cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        poseStack.popPose();

        for (ArmorPart child : children)
        {
            child.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    public void renderLocal(PoseStack poseStack, VertexConsumer vertexConsumer,
                            int packedLight, int packedOverlay,
                            float red, float green, float blue, float alpha)
    {
        if (!visible) return;

        float scale = 1.0F / 16.0F;

        poseStack.pushPose();

        applyTransformWithoutInnerOffset(poseStack);

        poseStack.pushPose();
        if (innerOffset.x != 0.0F || innerOffset.y != 0.0F || innerOffset.z != 0.0F)
        {
            poseStack.translate(innerOffset.x * scale, innerOffset.y * scale, innerOffset.z * scale);
        }

        for (ArmorCube cube : cubes)
        {
            cube.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        poseStack.popPose();

        for (ArmorPart child : children)
        {
            child.renderLocal(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        poseStack.popPose();
    }

    protected void applyFullTransform(PoseStack poseStack)
    {
        if (parent != null)
        {
            parent.applyFullTransform(poseStack);
        }
        applyLocalTransform(poseStack);
    }

    protected void applyLocalTransform(PoseStack poseStack)
    {
        float scale = 1.0F / 16.0F;

        if (position.x != 0.0F || position.y != 0.0F || position.z != 0.0F)
        {
            poseStack.translate(position.x * scale, position.y * scale, position.z * scale);
        }

        GlHelper.rotate(poseStack, rotation.getSmooth());

        if (innerOffset.x != 0.0F || innerOffset.y != 0.0F || innerOffset.z != 0.0F)
        {
            poseStack.translate(innerOffset.x * scale, innerOffset.y * scale, innerOffset.z * scale);
        }
    }

    protected void applyTransformWithoutInnerOffset(PoseStack poseStack)
    {
        float scale = 1.0F / 16.0F;

        if (position.x != 0.0F || position.y != 0.0F || position.z != 0.0F)
        {
            poseStack.translate(position.x * scale, position.y * scale, position.z * scale);
        }

        GlHelper.rotate(poseStack, rotation.getSmooth());
    }

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
