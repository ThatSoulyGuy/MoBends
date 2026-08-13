package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

@Deprecated
public class HumanoidPartWrapper implements IPartWrapper
{
    protected IPartWrapper.DataPartSelector dataPartSelector;
    protected IPartWrapper.ModelPartSetter modelPartSetter;

    protected ArmorPart armorPart;

    protected IModelPart parentTransform;

    protected float innerOffsetX, innerOffsetY, innerOffsetZ;

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

        this.armorPart = new ArmorPart();
        createGeometry(vanillaPart, partType, inflation);
    }

    protected void createGeometry(ModelPart vanillaPart, PartType partType, float inflation)
    {
        float textureWidth = 64.0F;
        float textureHeight = 32.0F;

        switch (partType)
        {
            case HEAD:
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
                ArmorCube hatCube = new ArmorCube(
                        -4.0F, -8.0F, -4.0F,
                        4.0F, 0.0F, 4.0F,
                        inflation + 0.5F,
                        32, 0,
                        textureWidth, textureHeight,
                        false
                );
                armorPart.addCube(hatCube);
                break;

            case BODY:
            default:
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

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        if (!armorPart.visible) return;

        poseStack.pushPose();

        if (parentTransform != null)
        {
            parentTransform.applyCharacterTransform(poseStack, 1.0F / 16.0F);
        }

        armorPart.renderLocal(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.popPose();
    }

    public ArmorPart getArmorPart()
    {
        return armorPart;
    }

    public void update(float ticksPerFrame)
    {
        armorPart.update(ticksPerFrame);
    }
}
