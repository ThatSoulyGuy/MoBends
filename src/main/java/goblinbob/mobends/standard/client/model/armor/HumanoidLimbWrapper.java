package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;


@Deprecated
public class HumanoidLimbWrapper implements IPartWrapper
{
    protected IPartWrapper.DataPartSelector upperPartDataSelector;
    protected IPartWrapper.DataPartSelector lowerPartDataSelector;
    protected IPartWrapper.ModelPartSetter modelPartSetter;

    protected ArmorPart upperPart;

    protected ArmorPart lowerPart;

    protected boolean mirror;

    protected boolean geometryCreated = false;

    protected float lowerOffsetX, lowerOffsetY, lowerOffsetZ;

    protected float innerOffsetX, innerOffsetY, innerOffsetZ;

    protected IModelPart parentTransform;

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
        this.mirror = vanillaPart.x > 0;

        this.vanillaX = vanillaPart.x;
        this.vanillaY = vanillaPart.y;
        this.vanillaZ = vanillaPart.z;

        this.upperPart = new ArmorPart();
        this.lowerPart = new ArmorPart();

        this.upperPart.addChild(this.lowerPart);

        createSlicedGeometry(vanillaPart, cutPlane, inflation);
    }

    protected void createSlicedGeometry(ModelPart vanillaPart, float cutPlane, float inflation)
    {

        float textureWidth = 64.0F;
        float textureHeight = 32.0F;

        int texU = 40;
        int texV = 16;

        boolean isArm = cutPlane < 5.0F;

        if (isArm)
        {
            createArmGeometry(cutPlane, inflation, textureWidth, textureHeight, texU, texV);
        }
        else
        {
            texU = 0;
            createLegGeometry(cutPlane, inflation, textureWidth, textureHeight, texU, texV);
        }

        geometryCreated = true;
    }

    protected void createArmGeometry(float cutPlane, float inflation, float textureWidth, float textureHeight, int texU, int texV)
    {
        int uvWidth = 4;
        int uvHeight = 12;
        int uvDepth = 4;

        float xOffset = mirror ? 1.0F : -1.0F;

        float upperMinX = -2.0F + xOffset;
        float upperMinY = -2.0F;
        float upperMinZ = -2.0F;
        float upperMaxX = 2.0F + xOffset;
        float upperMaxY = cutPlane;
        float upperMaxZ = 2.0F;

        int upperHeight = (int)(upperMaxY - upperMinY);

        ArmorCube upperCube = new ArmorCube(
                upperMinX, upperMinY, upperMinZ,
                upperMaxX, upperMaxY, upperMaxZ,
                inflation,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, 0
        );
        upperCube.hideFace(ArmorCube.BOTTOM);
        upperPart.addCube(upperCube);

        float lowerMinX = -2.0F + xOffset;
        float lowerMinY = 0.0F;
        float lowerMinZ = -2.0F;
        float lowerMaxX = 2.0F + xOffset;
        float lowerMaxY = 6.0F;
        float lowerMaxZ = 2.0F;

        ArmorCube lowerCube = new ArmorCube(
                lowerMinX, lowerMinY, lowerMinZ,
                lowerMaxX, lowerMaxY, lowerMaxZ,
                inflation + 0.001F,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, upperHeight
        );
        lowerCube.hideFace(ArmorCube.TOP);
        lowerPart.addCube(lowerCube);

    }

    protected void createLegGeometry(float cutPlane, float inflation, float textureWidth, float textureHeight, int texU, int texV)
    {
        int uvWidth = 4;
        int uvHeight = 12;
        int uvDepth = 4;

        float upperMinX = -2.0F;
        float upperMinY = 0.0F;
        float upperMinZ = -2.0F;
        float upperMaxX = 2.0F;
        float upperMaxY = cutPlane;
        float upperMaxZ = 2.0F;

        int upperHeight = (int)(upperMaxY - upperMinY);

        ArmorCube upperCube = new ArmorCube(
                upperMinX, upperMinY, upperMinZ,
                upperMaxX, upperMaxY, upperMaxZ,
                inflation,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, 0
        );
        upperCube.hideFace(ArmorCube.BOTTOM);
        upperPart.addCube(upperCube);

        float lowerMinX = -2.0F;
        float lowerMinY = 0.0F;
        float lowerMinZ = -2.0F;
        float lowerMaxX = 2.0F;
        float lowerMaxY = 6.0F;
        float lowerMaxZ = 2.0F;

        ArmorCube lowerCube = new ArmorCube(
                lowerMinX, lowerMinY, lowerMinZ,
                lowerMaxX, lowerMaxY, lowerMaxZ,
                inflation + 0.001F,
                texU, texV,
                textureWidth, textureHeight,
                mirror,
                uvWidth, uvHeight, uvDepth, upperHeight
        );
        lowerCube.hideFace(ArmorCube.TOP);
        lowerPart.addCube(lowerCube);

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
        upperPart.visible = true;
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

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer,
                       int packedLight, int packedOverlay,
                       float red, float green, float blue, float alpha)
    {
        if (!upperPart.visible) return;

        poseStack.pushPose();

        if (parentTransform != null)
        {
            parentTransform.applyCharacterTransform(poseStack, 1.0F / 16.0F);
        }

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

    public void update(float ticksPerFrame)
    {
        upperPart.update(ticksPerFrame);
    }
}
