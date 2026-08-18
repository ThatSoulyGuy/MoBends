package goblinbob.mobends.standard.mutators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsMesh;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.client.model.adaptive.HumanoidLayout;
import goblinbob.mobends.standard.client.model.adaptive.PartCapture;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomBipedArmor;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomHeldItem;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BipedMutator<D extends BipedEntityData<E>,
                                   E extends LivingEntity,
                                   M extends HumanoidModel<E>>
                                  extends Mutator<D, E, M>
{
    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger("MoBends-BipedMutator");

    protected BendsModelPart body;
    protected BendsModelPart head;
    protected BendsModelPart headwear;
    protected BendsModelPart leftArm;
    protected BendsModelPart rightArm;
    protected BendsModelPart leftForeArm;
    protected BendsModelPart rightForeArm;
    protected BendsModelPart leftLeg;
    protected BendsModelPart rightLeg;
    protected BendsModelPart leftForeLeg;
    protected BendsModelPart rightForeLeg;

    protected BendsModelPart outerBody;
    protected BendsModelPart outerHead;
    protected BendsModelPart outerLeftArm;
    protected BendsModelPart outerRightArm;
    protected BendsModelPart outerLeftForeArm;
    protected BendsModelPart outerRightForeArm;
    protected BendsModelPart outerLeftLeg;
    protected BendsModelPart outerRightLeg;
    protected BendsModelPart outerLeftForeLeg;
    protected BendsModelPart outerRightForeLeg;

    protected final List<AttachedPart> attachedParts = new ArrayList<>();

    protected AdaptiveHumanoidGeometry adaptiveGeometry;

    private boolean adaptivePivotsResolved = false;

    private final Set<HumanoidModel<?>> overlayModels =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private final Map<HumanoidModel<?>, AdaptiveHumanoidGeometry> overlayGeometry =
            new IdentityHashMap<>();

    private boolean overlayModelsResolved = false;

    private static final int MAX_CACHED_OVERLAY_GEOMETRY = 128;

    private Set<ModelPart> overlayRenderedParts = null;

    private final float[] scratchVec = new float[3];
    private final float[] scratchPivot = new float[3];
    private final float[] scratchEuler = new float[3];
    private final Quaternion scratchRotation = new Quaternion();

    private HumanoidModel<?> overlayRenderedModel = null;

    private final org.joml.Matrix4f mainRenderPose = new org.joml.Matrix4f();
    private final org.joml.Matrix3f mainRenderNormal = new org.joml.Matrix3f();
    private boolean mainRenderPoseValid = false;

    protected float babyHeadScale = 1.0F;

    protected ModelPart vanillaBody;
    protected ModelPart vanillaHead;
    protected ModelPart vanillaHat;
    protected ModelPart vanillaLeftArm;
    protected ModelPart vanillaRightArm;
    protected ModelPart vanillaLeftLeg;
    protected ModelPart vanillaRightLeg;

    private VanillaPartState vanillaBodyState;
    private VanillaPartState vanillaHeadState;
    private VanillaPartState vanillaHatState;
    private VanillaPartState vanillaLeftArmState;
    private VanillaPartState vanillaRightArmState;
    private VanillaPartState vanillaLeftLegState;
    private VanillaPartState vanillaRightLegState;

    protected LayerCustomBipedArmor<E, M> layerArmor;
    protected HumanoidArmorLayer<E, M, ?> layerArmorVanilla;
    protected LayerCustomHeldItem<E, M> layerHeldItem;
    protected ItemInHandLayer<E, M> layerHeldItemVanilla;
    protected CustomHeadLayer<E, M> layerCustomHead;
    protected CustomHeadLayer<E, M> layerCustomHeadVanilla;

    public BipedMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(M model)
    {
        this.vanillaBody = model.body;
        this.vanillaHead = model.head;
        this.vanillaHat = model.hat;
        this.vanillaLeftArm = model.leftArm;
        this.vanillaRightArm = model.rightArm;
        this.vanillaLeftLeg = model.leftLeg;
        this.vanillaRightLeg = model.rightLeg;

        this.vanillaBodyState = VanillaPartState.capture(model.body);
        this.vanillaHeadState = VanillaPartState.capture(model.head);
        this.vanillaHatState = VanillaPartState.capture(model.hat);
        this.vanillaLeftArmState = VanillaPartState.capture(model.leftArm);
        this.vanillaRightArmState = VanillaPartState.capture(model.rightArm);
        this.vanillaLeftLegState = VanillaPartState.capture(model.leftLeg);
        this.vanillaRightLegState = VanillaPartState.capture(model.rightLeg);
    }

    @Override
    public void applyVanillaModel(M model)
    {
        if (model == null)
            return;

        VanillaPartState.restore(this.vanillaBodyState, model.body);
        VanillaPartState.restore(this.vanillaHeadState, model.head);
        VanillaPartState.restore(this.vanillaHatState, model.hat);
        VanillaPartState.restore(this.vanillaLeftArmState, model.leftArm);
        VanillaPartState.restore(this.vanillaRightArmState, model.rightArm);
        VanillaPartState.restore(this.vanillaLeftLegState, model.leftLeg);
        VanillaPartState.restore(this.vanillaRightLegState, model.rightLeg);

        this.vanillaPositionsStored = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void swapLayer(LivingEntityRenderer<E, M> renderer, int index, boolean isModelVanilla)
    {
        RenderLayer<E, M> layer = layerRenderers.get(index);
        if (layer instanceof HumanoidArmorLayer)
        {
            HumanoidArmorLayer<E, M, ?> vanillaArmor = (HumanoidArmorLayer<E, M, ?>) layer;
            if (isModelVanilla)
                this.layerArmorVanilla = vanillaArmor;

            this.layerArmor = new LayerCustomBipedArmor<>(renderer, this);
            this.layerArmor.setVanillaArmorLayer(vanillaArmor);

            try
            {
                net.minecraft.client.model.geom.ModelLayerLocation innerLocation =
                    net.minecraft.client.model.geom.ModelLayers.PLAYER_INNER_ARMOR;
                net.minecraft.client.model.geom.ModelLayerLocation outerLocation =
                    net.minecraft.client.model.geom.ModelLayers.PLAYER_OUTER_ARMOR;

                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                HumanoidModel<?> innerModel = new HumanoidModel<>(mc.getEntityModels().bakeLayer(innerLocation));
                HumanoidModel<?> outerModel = new HumanoidModel<>(mc.getEntityModels().bakeLayer(outerLocation));

                this.layerArmor.setArmorModels(innerModel, outerModel);
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to bake the armor models for {}; armor will not render",
                        renderer.getClass().getName(), e);
            }

            layerRenderers.set(index, this.layerArmor);
        }
        else if (layer instanceof ItemInHandLayer)
        {
            this.layerHeldItem = new LayerCustomHeldItem<>(renderer, this);
            if (isModelVanilla)
                this.layerHeldItemVanilla = (ItemInHandLayer<E, M>) layer;
            layerRenderers.set(index, this.layerHeldItem);
        }
        else if (layer instanceof CustomHeadLayer)
        {
            if (isModelVanilla)
                this.layerCustomHeadVanilla = (CustomHeadLayer<E, M>) layer;
        }
    }

    @Override
    public void deswapLayer(LivingEntityRenderer<E, M> renderer, int index)
    {
        RenderLayer<E, M> layer = layerRenderers.get(index);
        if (layer instanceof LayerCustomBipedArmor && this.layerArmorVanilla != null)
        {
            layerRenderers.set(index, this.layerArmorVanilla);
        }
        else if (layer instanceof LayerCustomHeldItem && this.layerHeldItemVanilla != null)
        {
            layerRenderers.set(index, this.layerHeldItemVanilla);
        }
        else if (layer == this.layerCustomHead && this.layerCustomHeadVanilla != null)
        {
            layerRenderers.set(index, this.layerCustomHeadVanilla);
        }
    }

    protected void createHeadParts(float scaleFactor)
    {
        head = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        head.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        body.addChild(head);

        headwear = new BendsModelPart(32, 0)
                .setTextureSize(64, 64);
        headwear.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        head.addChild(headwear);
    }

    protected void createOuterHeadParts(float scaleFactor, float outerOffset)
    {
        outerHead = new BendsModelPart(0, 0)
                .setTextureSize(64, 64)
                .setPosition(0.0F, -12.0F, 0.0F);
        outerHead.addCube(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + outerOffset);
        outerBody.addChild(outerHead);
    }

    protected boolean usesAdaptiveGeometry()
    {
        return true;
    }

    protected AdaptiveHumanoidGeometry.WearParts adaptiveWearParts(M original)
    {
        return null;
    }

    protected AdaptiveHumanoidGeometry.CaptureMode adaptiveHeadCaptureMode()
    {
        return AdaptiveHumanoidGeometry.CaptureMode.OWN_CUBES;
    }

    protected AdaptiveHumanoidGeometry.CaptureMode adaptiveLimbCaptureMode()
    {
        return AdaptiveHumanoidGeometry.CaptureMode.OWN_CUBES;
    }

    protected void createAdaptiveWearParts(AdaptiveHumanoidGeometry geometry)
    {
    }

    protected boolean tryCreateAdaptiveParts(M original, HumanoidLayout... baselines)
    {
        this.adaptiveGeometry = null;
        this.adaptivePivotsResolved = false;

        if (original == null || !usesAdaptiveGeometry())
        {
            return false;
        }

        for (HumanoidLayout baseline : baselines)
        {
            if (baseline.describes(original))
            {
                return false;
            }
        }

        final AdaptiveHumanoidGeometry geometry = AdaptiveHumanoidGeometry.build(original,
                adaptiveHeadCaptureMode(), adaptiveLimbCaptureMode(), null,
                adaptiveWearParts(original));
        if (geometry == null)
        {
            return false;
        }

        body = boneAt(geometry.bodyPivot).addMesh(geometry.bodyMesh);

        head = boneAt(geometry.headPivot).addMesh(geometry.headMesh);
        body.addChild(head);

        headwear = new BendsModelPart().addMesh(geometry.hatMesh);
        head.addChild(headwear);

        leftArm = boneAt(geometry.leftArmPivot).addMesh(geometry.leftArmMesh);
        body.addChild(leftArm);
        leftForeArm = boneAt(geometry.leftForeArmPivot).addMesh(geometry.leftForeArmMesh);
        leftArm.addChild(leftForeArm);

        rightArm = boneAt(geometry.rightArmPivot).addMesh(geometry.rightArmMesh);
        body.addChild(rightArm);
        rightForeArm = boneAt(geometry.rightForeArmPivot).addMesh(geometry.rightForeArmMesh);
        rightArm.addChild(rightForeArm);

        leftLeg = boneAt(geometry.leftLegPivot).addMesh(geometry.leftLegMesh);
        leftForeLeg = boneAt(geometry.leftForeLegPivot).addMesh(geometry.leftForeLegMesh);
        leftLeg.addChild(leftForeLeg);

        rightLeg = boneAt(geometry.rightLegPivot).addMesh(geometry.rightLegMesh);
        rightForeLeg = boneAt(geometry.rightForeLegPivot).addMesh(geometry.rightForeLegMesh);
        rightLeg.addChild(rightForeLeg);

        outerBody = null;
        outerHead = null;
        outerLeftArm = null;
        outerRightArm = null;
        outerLeftForeArm = null;
        outerRightForeArm = null;
        outerLeftLeg = null;
        outerRightLeg = null;
        outerLeftForeLeg = null;
        outerRightForeLeg = null;

        this.adaptiveGeometry = geometry;

        createAdaptiveWearParts(geometry);

        reconcileWithVanillaModel(original);

        return true;
    }

    private static BendsModelPart boneAt(float[] pivot)
    {
        return new BendsModelPart().setPosition(pivot[0], pivot[1], pivot[2]);
    }

    protected void reconcileWithVanillaModel(HumanoidModel<?> original)
    {
        attachedParts.clear();
        overlayModels.clear();
        overlayGeometry.clear();
        overlayModelsResolved = false;

        if (original == null || body == null)
        {
            return;
        }

        final float[] bodyAnchor = {body.position.x, body.position.y, body.position.z};
        final float[] headAnchor = childAnchor(bodyAnchor, head);

        if (limbSubtreesBaked())
        {
            attachChildrenUnderBone(original.head, head);
        }
        else
        {
            attach(original.head, original.head, head, headAnchor);
        }

        if (limbSubtreesBaked())
        {
            attachChildrenUnderBone(original.hat, headwear != null ? headwear : head);
        }
        else
        {
            attach(original.hat, original.head, head, headAnchor);
            attach(original.body, original.body, body, bodyAnchor);
            attach(original.leftArm, original.leftArm, leftArm, childAnchor(bodyAnchor, leftArm));
            attach(original.rightArm, original.rightArm, rightArm, childAnchor(bodyAnchor, rightArm));
            attach(original.leftLeg, original.leftLeg, leftLeg, rootAnchor(leftLeg));
            attach(original.rightLeg, original.rightLeg, rightLeg, rootAnchor(rightLeg));
        }

        if (headwear != null && adaptiveGeometry == null
                && PartCapture.ofOwnCubes(original.hat).isEmpty())
        {
            headwear.hidden = true;
        }

    }

    protected void attachChildrenUnderBone(ModelPart source, BendsModelPart bone)
    {
        if (source == null || bone == null)
        {
            return;
        }

        attachedParts.add(new AttachedPart(source, bone, 0.0F, 0.0F, 0.0F,
                !bone.hasGeometry(), true));
    }

    protected boolean limbSubtreesBaked()
    {
        return adaptiveGeometry != null && adaptiveGeometry.limbSubtreesBaked;
    }

    protected static float[] childAnchor(float[] parentAnchor, BendsModelPart bone)
    {
        if (bone == null)
        {
            return null;
        }
        return new float[] {
                parentAnchor[0] + bone.position.x,
                parentAnchor[1] + bone.position.y,
                parentAnchor[2] + bone.position.z
        };
    }

    protected static float[] rootAnchor(BendsModelPart bone)
    {
        if (bone == null)
        {
            return null;
        }
        return new float[] {bone.position.x, bone.position.y, bone.position.z};
    }

    protected void attach(ModelPart source, ModelPart anchorSource, BendsModelPart bone, float[] boneAnchor)
    {
        if (source == null || anchorSource == null || bone == null || boneAnchor == null)
        {
            return;
        }

        final boolean drawOwnCubes = !bone.hasGeometry();

        if (source.getAllParts().count() <= 1L && !drawOwnCubes)
        {
            return;
        }

        attachedParts.add(new AttachedPart(source, bone,
                anchorSource.x - boneAnchor[0],
                anchorSource.y - boneAnchor[1],
                anchorSource.z - boneAnchor[2],
                drawOwnCubes));
    }

    @Override
    public boolean createParts(M original, float scaleFactor)
    {
        if (tryCreateAdaptiveParts(original, HumanoidLayout.ZOMBIE))
        {
            return true;
        }

        body = new BendsModelPart(16, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F);
        body.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

        createHeadParts(scaleFactor);

        int armWidth = 4;
        float armY = -10F;

        leftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        leftArm.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(leftArm);

        rightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(-5.0F, armY, 0.0F);
        rightArm.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        body.addChild(rightArm);

        leftForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        leftForeArm.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftArm.addChild(leftForeArm);

        rightForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F);
        rightForeArm.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightArm.addChild(rightForeArm);

        rightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12F, 0F);
        rightLeg.addCube(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        leftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F)
                .setMirror(true);
        leftLeg.addCube(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);

        leftForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F)
                .setMirror(true);
        leftForeLeg.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        leftLeg.addChild(leftForeLeg);

        rightForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F);
        rightForeLeg.developBox(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
                .inflate(0.01F, 0F, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        rightLeg.addChild(rightForeLeg);

        createOuterParts(scaleFactor);

        reconcileWithVanillaModel(original);

        return true;
    }

    protected void createOuterParts(float scaleFactor)
    {
        final float outerOffset = 0.25F;
        final float limbWearHeight = (6F + 2 * scaleFactor + 0.5F) - 0.25F;
        int armWidth = 4;
        float armY = -10F;

        outerBody = new BendsModelPart(16, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F);
        outerBody.addCube(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor + outerOffset);

        createOuterHeadParts(scaleFactor, outerOffset);

        outerLeftArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(5.0F, armY, 0.0F)
                .setMirror(true);
        outerLeftArm.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        outerBody.addChild(outerLeftArm);

        outerRightArm = new BendsModelPart(40, 16)
                .setTextureSize(64, 64)
                .setPosition(-5.0F, armY, 0.0F);
        outerRightArm.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.0025F, 0F, 0.0025F)
                .hideFace(BoxSide.BOTTOM)
                .create();
        outerBody.addChild(outerRightArm);

        outerLeftForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F)
                .setMirror(true);
        outerLeftForeArm.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerLeftArm.addChild(outerLeftForeArm);

        outerRightForeArm = new BendsModelPart(40, 22)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 4.0F, 2.0F);
        outerRightForeArm.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerRightArm.addChild(outerRightForeArm);

        outerRightLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12F, 0F);
        outerRightLeg.developBox(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();

        outerLeftLeg = new BendsModelPart(0, 16)
                .setTextureSize(64, 64)
                .setPosition(0.0F, 12.0F, 0.0F)
                .setMirror(true);
        outerLeftLeg.developBox(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .hideFace(BoxSide.BOTTOM)
                .create();

        outerLeftForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F)
                .setMirror(true);
        outerLeftForeLeg.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerLeftLeg.addChild(outerLeftForeLeg);

        outerRightForeLeg = new BendsModelPart(0, 22)
                .setTextureSize(64, 64)
                .setPosition(0, 6.0F, -2.0F);
        outerRightForeLeg.developBox(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor + outerOffset)
                .setHeight(limbWearHeight)
                .inflate(0.005F, 0F, 0.005F)
                .offset(0F, 0.25F, 0F)
                .hideFace(BoxSide.TOP)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        outerRightLeg.addChild(outerRightForeLeg);
    }

    @Override
    public void syncUpWithData(D data)
    {
        head.syncUp(data.head);
        body.syncUp(data.body);
        leftArm.syncUp(data.leftArm);
        rightArm.syncUp(data.rightArm);
        leftLeg.syncUp(data.leftLeg);
        rightLeg.syncUp(data.rightLeg);
        leftForeArm.syncUp(data.leftForeArm);
        rightForeArm.syncUp(data.rightForeArm);
        leftForeLeg.syncUp(data.leftForeLeg);
        rightForeLeg.syncUp(data.rightForeLeg);

        applyAdaptivePivots();
    }

    protected void resolveAdaptivePivots()
    {
        if (adaptiveGeometry == null || adaptivePivotsResolved)
        {
            return;
        }

        final HumanoidModel<?> vanilla = MoBendsRenderContext.getCurrentVanillaModel();
        if (vanilla != null)
        {
            adaptiveGeometry.adoptRuntimePivots(vanilla);
            applyAdaptivePivots();
            reconcileWithVanillaModel(vanilla);
        }

        adaptivePivotsResolved = true;
    }

    public boolean isOverlayModel(Object model)
    {
        return isOverlayModel(model, null);
    }

    public boolean isOverlayModel(Object model, Object renderedParts)
    {
        if (!(model instanceof HumanoidModel<?>))
        {
            return false;
        }
        if (model == MoBendsRenderContext.getCurrentVanillaModel())
        {
            return true;
        }
        if (!overlayModelsResolved)
        {
            overlayModelsResolved = true;
            collectOverlayModels();
        }
        if (overlayModels.contains(model))
        {
            return true;
        }

        final HumanoidModel<?> humanoidModel = (HumanoidModel<?>) model;

        if (!rendersSplitLimb(humanoidModel, renderedParts))
        {
            return false;
        }

        return isBendableAccessoryModel(humanoidModel);
    }

    private static boolean rendersSplitLimb(HumanoidModel<?> model, Object renderedParts)
    {
        if (!(renderedParts instanceof Set<?> parts))
        {
            return false;
        }
        return parts.contains(model.leftArm) || parts.contains(model.rightArm)
                || parts.contains(model.leftLeg) || parts.contains(model.rightLeg);
    }

    private float[] baseJointOverride()
    {
        if (leftForeArm == null || leftForeLeg == null)
        {
            return null;
        }
        return new float[]{
                leftForeArm.position.y, leftForeArm.position.z,
                leftForeLeg.position.y, leftForeLeg.position.z
        };
    }

    private boolean isBendableAccessoryModel(HumanoidModel<?> model)
    {
        if (MoBendsRenderContext.isInArmorRender())
        {
            return false;
        }

        if (overlayGeometry.containsKey(model))
        {
            return overlayGeometry.get(model) != null;
        }

        AdaptiveHumanoidGeometry geometry = AdaptiveHumanoidGeometry.build(model, true, baseJointOverride());
        if (geometry != null)
        {
            geometry.adoptRuntimePivots(model);
        }

        if (overlayGeometry.size() >= MAX_CACHED_OVERLAY_GEOMETRY)
        {
            overlayGeometry.clear();
        }
        overlayGeometry.put(model, geometry);
        return geometry != null;
    }

    private void collectOverlayModels()
    {
        if (layerRenderers == null)
        {
            return;
        }

        for (RenderLayer<E, M> layer : layerRenderers)
        {
            if (layer == null
                    || layer instanceof HumanoidArmorLayer
                    || layer instanceof LayerCustomBipedArmor)
            {
                continue;
            }

            for (Class<?> type = layer.getClass(); type != null && type != Object.class; type = type.getSuperclass())
            {
                for (Field field : type.getDeclaredFields())
                {
                    if (!HumanoidModel.class.isAssignableFrom(field.getType()))
                    {
                        continue;
                    }
                    try
                    {
                        field.setAccessible(true);
                        if (field.get(layer) instanceof HumanoidModel<?> found)
                        {
                            overlayModels.add(found);
                        }
                    }
                    catch (Exception ignored)
                    {
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void renderOverlayModel(HumanoidModel<?> model, Object renderedParts,
                                   PoseStack poseStack, VertexConsumer vertexConsumer,
                                   int packedLight, int packedOverlay, int color)
    {
        if (model == null || body == null)
        {
            return;
        }

        this.overlayRenderedParts = renderedParts instanceof Set<?> parts
                ? (Set<ModelPart>) parts
                : null;
        this.overlayRenderedModel = model;

        poseStack.pushPose();
        applyMainRenderPose(poseStack);

        try
        {
            renderOverlayGeometry(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        finally
        {
            poseStack.popPose();
        }
    }

    private void applyMainRenderPose(PoseStack poseStack)
    {
        if (!mainRenderPoseValid)
        {
            return;
        }
        poseStack.last().pose().set(mainRenderPose);
        poseStack.last().normal().set(mainRenderNormal);
    }

    private void renderOverlayGeometry(HumanoidModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
                                       int packedLight, int packedOverlay, int color)
    {
        if (model == MoBendsRenderContext.getCurrentVanillaModel())
        {
            renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            return;
        }

        AdaptiveHumanoidGeometry geometry = overlayGeometry.get(model);
        if (geometry == null)
        {
            geometry = AdaptiveHumanoidGeometry.build(model, true, baseJointOverride());
            if (geometry == null)
            {
                return;
            }
            geometry.adoptRuntimePivots(model);
            overlayGeometry.put(model, geometry);
        }

        final float[] baseBody = absoluteOf(null, body);
        final float[] overlayBody = geometry.bodyPivot;

        drawOverlay(poseStack, vertexConsumer, packedLight, packedOverlay, color,
                model.body, body, geometry.bodyMesh, baseBody, overlayBody);

        final float[] baseHead = absoluteOf(baseBody, head);
        final float[] overlayHead = sum(overlayBody, geometry.headPivot);
        drawOverlay(poseStack, vertexConsumer, packedLight, packedOverlay, color,
                model.head, head, geometry.headMesh, baseHead, overlayHead);
        drawOverlay(poseStack, vertexConsumer, packedLight, packedOverlay, color,
                model.hat, head, geometry.hatMesh, baseHead, overlayHead);

        drawLimb(poseStack, vertexConsumer, packedLight, packedOverlay, color, model.leftArm,
                leftArm, leftForeArm, geometry.leftArmMesh, geometry.leftForeArmMesh,
                baseBody, overlayBody, geometry.leftArmPivot, geometry.leftForeArmPivot);
        drawLimb(poseStack, vertexConsumer, packedLight, packedOverlay, color, model.rightArm,
                rightArm, rightForeArm, geometry.rightArmMesh, geometry.rightForeArmMesh,
                baseBody, overlayBody, geometry.rightArmPivot, geometry.rightForeArmPivot);

        drawLimb(poseStack, vertexConsumer, packedLight, packedOverlay, color, model.leftLeg,
                leftLeg, leftForeLeg, geometry.leftLegMesh, geometry.leftForeLegMesh,
                null, null, geometry.leftLegPivot, geometry.leftForeLegPivot);
        drawLimb(poseStack, vertexConsumer, packedLight, packedOverlay, color, model.rightLeg,
                rightLeg, rightForeLeg, geometry.rightLegMesh, geometry.rightForeLegMesh,
                null, null, geometry.rightLegPivot, geometry.rightForeLegPivot);
    }

    private void drawLimb(PoseStack poseStack, VertexConsumer vertexConsumer,
                          int packedLight, int packedOverlay, int color,
                          ModelPart source, BendsModelPart upperBone, BendsModelPart foreBone,
                          BendsMesh upperMesh, BendsMesh foreMesh,
                          float[] baseParent, float[] overlayParent,
                          float[] overlayUpperPivot, float[] overlayForePivot)
    {
        final float[] baseUpper = absoluteOf(baseParent, upperBone);
        final float[] overlayUpper = overlayParent == null
                ? overlayUpperPivot
                : sum(overlayParent, overlayUpperPivot);

        drawOverlay(poseStack, vertexConsumer, packedLight, packedOverlay, color,
                source, upperBone, upperMesh, baseUpper, overlayUpper);

        drawOverlay(poseStack, vertexConsumer, packedLight, packedOverlay, color,
                source, foreBone, foreMesh,
                absoluteOf(baseUpper, foreBone), sum(overlayUpper, overlayForePivot));
    }

    private boolean isOverlayPartRendered(ModelPart source)
    {
        if (overlayRenderedParts == null || source == null)
        {
            return true;
        }
        if (overlayRenderedParts.contains(source))
        {
            return true;
        }
        return overlayRenderedModel != null
                && source == overlayRenderedModel.hat
                && overlayRenderedParts.contains(overlayRenderedModel.head);
    }

    private void drawOverlay(PoseStack poseStack, VertexConsumer vertexConsumer,
                             int packedLight, int packedOverlay, int color,
                             ModelPart source, BendsModelPart bone, BendsMesh mesh,
                             float[] baseAbsolute, float[] overlayAbsolute)
    {
        if (mesh == null || bone == null || !bone.isShowing())
        {
            return;
        }
        if (source != null && !source.visible)
        {
            return;
        }
        if (!isOverlayPartRendered(source))
        {
            return;
        }

        poseStack.pushPose();
        bone.applyCharacterTransformPoseStack(poseStack);
        poseStack.translate((overlayAbsolute[0] - baseAbsolute[0]) / 16.0F,
                            (overlayAbsolute[1] - baseAbsolute[1]) / 16.0F,
                            (overlayAbsolute[2] - baseAbsolute[2]) / 16.0F);
        mesh.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    private static float[] absoluteOf(float[] parentAbsolute, BendsModelPart bone)
    {
        if (bone == null)
        {
            return parentAbsolute != null ? parentAbsolute : new float[3];
        }
        if (parentAbsolute == null)
        {
            return new float[] {bone.position.x, bone.position.y, bone.position.z};
        }
        return new float[] {
                parentAbsolute[0] + bone.position.x,
                parentAbsolute[1] + bone.position.y,
                parentAbsolute[2] + bone.position.z
        };
    }

    private static float[] sum(float[] a, float[] b)
    {
        return new float[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    protected void applyAdaptivePivots()
    {
        if (adaptiveGeometry == null)
        {
            return;
        }

        setBonePivot(body, adaptiveGeometry.bodyPivot);
        setBonePivot(head, adaptiveGeometry.headPivot);
        setBonePivot(leftArm, adaptiveGeometry.leftArmPivot);
        setBonePivot(rightArm, adaptiveGeometry.rightArmPivot);
        setBonePivot(leftForeArm, adaptiveGeometry.leftForeArmPivot);
        setBonePivot(rightForeArm, adaptiveGeometry.rightForeArmPivot);
        setBonePivot(leftLeg, adaptiveGeometry.leftLegPivot);
        setBonePivot(rightLeg, adaptiveGeometry.rightLegPivot);
        setBonePivot(leftForeLeg, adaptiveGeometry.leftForeLegPivot);
        setBonePivot(rightForeLeg, adaptiveGeometry.rightForeLegPivot);
    }

    private static void setBonePivot(BendsModelPart bone, float[] pivot)
    {
        if (bone != null)
        {
            bone.position.set(pivot[0], pivot[1], pivot[2]);
        }
    }

    @Override
    public boolean isModelVanilla(M model)
    {
        return this.body == null;
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof HumanoidModel);
    }

    @Override
    public boolean shouldRenderCustom()
    {
        return this.body != null;
    }

    @Override
    public void renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        resolveAdaptivePivots();
        applyBabyHeadScale();
        syncConcealmentFromVanillaModel();

        if (MoBendsRenderContext.isInMainModelRender())
        {
            mainRenderPose.set(poseStack.last().pose());
            mainRenderNormal.set(poseStack.last().normal());
            mainRenderPoseValid = true;
        }

        if (body != null)
        {
            body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        renderAttachedParts(poseStack, vertexConsumer, packedLight, packedOverlay);

        if (leftLeg != null)
        {
            leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
        if (rightLeg != null)
        {
            rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }

    protected void renderAttachedParts(PoseStack poseStack, VertexConsumer vertexConsumer,
                                       int packedLight, int packedOverlay)
    {

        if (attachedParts.isEmpty())
        {
            return;
        }

        for (AttachedPart attached : attachedParts)
        {
            final ModelPart part = attached.part;

            if (!attached.bone.isShowing())
            {
                continue;
            }

            final java.util.Collection<ModelPart> foreignChildren = attached.useOwnTransform
                    ? goblinbob.mobends.compat.EmfSupport.childrenOf(part)
                    : java.util.Collections.emptyList();

            if (!foreignChildren.isEmpty())
            {
                goblinbob.mobends.compat.EmfSupport.advanceAnimation(part);

                poseStack.pushPose();
                attached.bone.applyCharacterTransformPoseStack(poseStack);

                for (ModelPart child : foreignChildren)
                {
                    child.render(poseStack, vertexConsumer, packedLight, packedOverlay);
                }

                poseStack.popPose();
                continue;
            }

            poseStack.pushPose();
            attached.bone.applyCharacterTransformPoseStack(poseStack);
            poseStack.translate(attached.offsetX / 16.0F,
                                attached.offsetY / 16.0F,
                                attached.offsetZ / 16.0F);

            final float x = part.x, y = part.y, z = part.z;
            final float xRot = part.xRot, yRot = part.yRot, zRot = part.zRot;
            final boolean skipDraw = part.skipDraw;
            final boolean visible = part.visible;

            part.x = 0.0F;
            part.y = 0.0F;
            part.z = 0.0F;
            part.xRot = 0.0F;
            part.yRot = 0.0F;
            part.zRot = 0.0F;
            part.skipDraw = !attached.drawOwnCubes;
            part.visible = true;

            part.render(poseStack, vertexConsumer, packedLight, packedOverlay);

            part.x = x;
            part.y = y;
            part.z = z;
            part.xRot = xRot;
            part.yRot = yRot;
            part.zRot = zRot;
            part.skipDraw = skipDraw;
            part.visible = visible;

            poseStack.popPose();
        }
    }

    protected void syncConcealmentFromVanillaModel()
    {
        final HumanoidModel<?> model = MoBendsRenderContext.getCurrentVanillaModel();
        if (model == null)
        {
            return;
        }

        clearConcealment();

        applyConcealment(head, model.head);
        applyConcealment(body, model.body);
        applyConcealment(leftArm, model.leftArm);
        applyConcealment(leftForeArm, model.leftArm);
        applyConcealment(rightArm, model.rightArm);
        applyConcealment(rightForeArm, model.rightArm);
        applyConcealment(leftLeg, model.leftLeg);
        applyConcealment(leftForeLeg, model.leftLeg);
        applyConcealment(rightLeg, model.rightLeg);
        applyConcealment(rightForeLeg, model.rightLeg);

        applySkinConcealment(model);

        syncOuterConcealment(model);

        if (goblinbob.mobends.compat.FirstPersonModelCompat.isRenderingFirstPersonBody())
        {
            concealHeadParts();
        }
    }

    protected void concealHeadParts()
    {
        if (head != null) head.concealed = true;
        if (headwear != null) headwear.concealed = true;
        if (outerHead != null) outerHead.concealed = true;
    }

    private void applySkinConcealment(HumanoidModel<?> model)
    {
        if (!goblinbob.mobends.compat.ArmourersWorkshopCompat.isModLoaded())
        {
            return;
        }

        concealIfSkinned(head, model.head);
        concealIfSkinned(body, model.body);
        concealIfSkinned(leftArm, model.leftArm);
        concealIfSkinned(leftForeArm, model.leftArm);
        concealIfSkinned(rightArm, model.rightArm);
        concealIfSkinned(rightForeArm, model.rightArm);
        concealIfSkinned(leftLeg, model.leftLeg);
        concealIfSkinned(leftForeLeg, model.leftLeg);
        concealIfSkinned(rightLeg, model.rightLeg);
        concealIfSkinned(rightForeLeg, model.rightLeg);
    }

    protected void clearConcealment()
    {
        clearConcealed(head);
        clearConcealed(body);
        clearConcealed(leftArm);
        clearConcealed(leftForeArm);
        clearConcealed(rightArm);
        clearConcealed(rightForeArm);
        clearConcealed(leftLeg);
        clearConcealed(leftForeLeg);
        clearConcealed(rightLeg);
        clearConcealed(rightForeLeg);
        clearConcealed(headwear);
        clearConcealed(outerHead);
        clearConcealed(outerBody);
        clearConcealed(outerLeftArm);
        clearConcealed(outerLeftForeArm);
        clearConcealed(outerRightArm);
        clearConcealed(outerRightForeArm);
        clearConcealed(outerLeftLeg);
        clearConcealed(outerLeftForeLeg);
        clearConcealed(outerRightLeg);
        clearConcealed(outerRightForeLeg);
    }

    protected static void clearConcealed(BendsModelPart part)
    {
        if (part != null)
        {
            part.concealed = false;
        }
    }

    protected void syncOuterConcealment(HumanoidModel<?> model)
    {
        concealWith(headwear, head, model.hat);
        concealWith(outerHead, head, model.hat);
        concealWith(outerBody, body, model.body);
        concealWith(outerLeftArm, leftArm, model.leftArm);
        concealWith(outerLeftForeArm, leftForeArm, model.leftArm);
        concealWith(outerRightArm, rightArm, model.rightArm);
        concealWith(outerRightForeArm, rightForeArm, model.rightArm);
        concealWith(outerLeftLeg, leftLeg, model.leftLeg);
        concealWith(outerLeftForeLeg, leftForeLeg, model.leftLeg);
        concealWith(outerRightLeg, rightLeg, model.rightLeg);
        concealWith(outerRightForeLeg, rightForeLeg, model.rightLeg);
    }

    protected static void concealIfSkinned(BendsModelPart part, ModelPart modelPart)
    {
        if (part == null || modelPart == null)
        {
            return;
        }
        if (goblinbob.mobends.compat.armourers.AWHiddenParts.isHidden(modelPart))
        {
            part.concealed = true;
        }
    }

    protected static void concealWith(BendsModelPart part, BendsModelPart basePart, ModelPart overlayPart)
    {
        if (part == null)
        {
            return;
        }
        if (basePart != null && basePart.concealed)
        {
            part.concealed = true;
            return;
        }
        if (overlayPart != null && !overlayPart.visible)
        {
            part.concealed = true;
            return;
        }
        concealIfSkinned(part, overlayPart);
    }

    private static void applyConcealment(BendsModelPart part, ModelPart modelPart)
    {
        if (part == null || modelPart == null)
        {
            return;
        }
        part.concealed = !modelPart.visible;
    }

    public void setBabyHeadScale(float scale)
    {
        this.babyHeadScale = scale;
    }

    protected void applyBabyHeadScale()
    {
        if (head != null)
        {
            head.scale.set(babyHeadScale, babyHeadScale, babyHeadScale);
        }
    }

    public boolean hasOuterParts()
    {
        return outerBody != null;
    }

    public void renderOuter(PoseStack poseStack, VertexConsumer vertexConsumer,
                            int packedLight, int packedOverlay, int color)
    {
        if (!hasOuterParts())
        {
            return;
        }
        syncOuterFromBase();

        outerBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        outerLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        outerRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void syncOuterFromBase()
    {
        copyAnimatedState(body, outerBody);
        copyAnimatedState(head, outerHead);
        copyAnimatedState(leftArm, outerLeftArm);
        copyAnimatedState(rightArm, outerRightArm);
        copyAnimatedState(leftForeArm, outerLeftForeArm);
        copyAnimatedState(rightForeArm, outerRightForeArm);
        copyAnimatedState(leftLeg, outerLeftLeg);
        copyAnimatedState(rightLeg, outerRightLeg);
        copyAnimatedState(leftForeLeg, outerLeftForeLeg);
        copyAnimatedState(rightForeLeg, outerRightForeLeg);
    }

    private static void copyAnimatedState(BendsModelPart src, BendsModelPart dst)
    {
        if (src == null || dst == null) return;
        dst.position.set(src.position);
        dst.offset.set(src.offset);
        dst.scale.set(src.scale);
        dst.offsetScale = src.offsetScale;
        dst.globalOffset.set(src.globalOffset);
        dst.rotation.set(src.rotation);
        dst.visible = src.visible;
        dst.hidden = src.hidden;
    }

    public BendsModelPart getBody() { return body; }
    public BendsModelPart getHead() { return head; }
    public BendsModelPart getLeftArm() { return leftArm; }
    public BendsModelPart getRightArm() { return rightArm; }
    public BendsModelPart getLeftForeArm() { return leftForeArm; }
    public BendsModelPart getRightForeArm() { return rightForeArm; }
    public BendsModelPart getLeftLeg() { return leftLeg; }
    public BendsModelPart getRightLeg() { return rightLeg; }
    public BendsModelPart getLeftForeLeg() { return leftForeLeg; }
    public BendsModelPart getRightForeLeg() { return rightForeLeg; }

    private float[] vanillaBodyPos, vanillaHeadPos, vanillaLeftArmPos, vanillaRightArmPos,
                    vanillaLeftLegPos, vanillaRightLegPos;
    private boolean vanillaPositionsStored = false;

    public void syncPosesToVanillaModel(HumanoidModel<?> model)
    {
        if (model == null) return;

        if (adaptiveGeometry != null && !adaptivePivotsResolved) return;

        if (!vanillaPositionsStored)
        {
            vanillaBodyPos = new float[]{model.body.x, model.body.y, model.body.z};
            vanillaHeadPos = new float[]{model.head.x, model.head.y, model.head.z};
            vanillaLeftArmPos = new float[]{model.leftArm.x, model.leftArm.y, model.leftArm.z};
            vanillaRightArmPos = new float[]{model.rightArm.x, model.rightArm.y, model.rightArm.z};
            vanillaLeftLegPos = new float[]{model.leftLeg.x, model.leftLeg.y, model.leftLeg.z};
            vanillaRightLegPos = new float[]{model.rightLeg.x, model.rightLeg.y, model.rightLeg.z};
            vanillaPositionsStored = true;
        }

        Quaternion bodyRotation = body.rotation.getSmooth();
        float bodyPivotX = body.globalOffset.x + (body.position.x + body.offset.x) * body.offsetScale;
        float bodyPivotY = body.globalOffset.y + (body.position.y + body.offset.y) * body.offsetScale;
        float bodyPivotZ = body.globalOffset.z + (body.position.z + body.offset.z) * body.offsetScale;

        float[] bodyNeck = rotateVectorByQuaternion(bodyRotation, 0.0F, -12.0F, 0.0F, scratchVec);
        model.body.x = bodyPivotX + bodyNeck[0];
        model.body.y = bodyPivotY + bodyNeck[1];
        model.body.z = bodyPivotZ + bodyNeck[2];
        float[] bodyEuler = quaternionToEulerXYZ(bodyRotation, scratchEuler);
        model.body.xRot = bodyEuler[0];
        model.body.yRot = bodyEuler[1];
        model.body.zRot = bodyEuler[2];
        model.body.visible = model.body.visible && body.isShowingIgnoringConcealment();

        syncBodyChildToModelPart(head, model.head, bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation);

        if (limbSubtreesBaked() && model.hat != null)
        {
            syncBodyChildToModelPart(head, model.hat, bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation);
        }
        syncBodyChildToModelPart(leftArm, model.leftArm, bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation);
        syncBodyChildToModelPart(rightArm, model.rightArm, bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation);

        syncPartToModelPart(leftLeg, model.leftLeg, vanillaLeftLegPos);
        syncPartToModelPart(rightLeg, model.rightLeg, vanillaRightLegPos);

        model.head.xScale = babyHeadScale;
        model.head.yScale = babyHeadScale;
        model.head.zScale = babyHeadScale;

        if (model.hat != null && head != null)
        {
            model.hat.xScale = babyHeadScale;
            model.hat.yScale = babyHeadScale;
            model.hat.zScale = babyHeadScale;
            model.hat.visible = model.hat.visible && head.isShowingIgnoringConcealment();
            model.hat.x = model.head.x;
            model.hat.y = model.head.y;
            model.hat.z = model.head.z;
            model.hat.xRot = model.head.xRot;
            model.hat.yRot = model.head.yRot;
            model.hat.zRot = model.head.zRot;
        }
    }

    public void restoreVanillaPivots(HumanoidModel<?> model)
    {
        if (model == null || !vanillaPositionsStored) return;

        applyStoredPivot(model.body, vanillaBodyPos);
        applyStoredPivot(model.head, vanillaHeadPos);
        applyStoredPivot(model.hat, vanillaHeadPos);
        applyStoredPivot(model.leftArm, vanillaLeftArmPos);
        applyStoredPivot(model.rightArm, vanillaRightArmPos);
        applyStoredPivot(model.leftLeg, vanillaLeftLegPos);
        applyStoredPivot(model.rightLeg, vanillaRightLegPos);
    }

    private static void applyStoredPivot(ModelPart modelPart, float[] pivot)
    {
        if (modelPart == null || pivot == null) return;
        modelPart.x = pivot[0];
        modelPart.y = pivot[1];
        modelPart.z = pivot[2];
    }

    private void syncPartToModelPart(BendsModelPart bendsPart, ModelPart modelPart, float[] vanillaPos)
    {
        if (bendsPart == null || modelPart == null) return;

        if (vanillaPos != null)
        {
            modelPart.x = vanillaPos[0] + bendsPart.offset.x;
            modelPart.y = vanillaPos[1] + bendsPart.offset.y;
            modelPart.z = vanillaPos[2] + bendsPart.offset.z;
        }

        Quaternion q = bendsPart.rotation.getSmooth();
        float[] euler = quaternionToEulerXYZ(q, scratchEuler);
        modelPart.xRot = euler[0];
        modelPart.yRot = euler[1];
        modelPart.zRot = euler[2];

        modelPart.visible = modelPart.visible && bendsPart.isShowingIgnoringConcealment();
    }

    private void syncBodyChildToModelPart(BendsModelPart child, ModelPart modelPart,
                                          float bodyPivotX, float bodyPivotY, float bodyPivotZ,
                                          Quaternion bodyRotation)
    {
        if (child == null || modelPart == null) return;
        Quaternion rotation = composeChildWorld(bodyPivotX, bodyPivotY, bodyPivotZ, bodyRotation, child, scratchPivot);
        setEndModelPart(modelPart, scratchPivot, rotation, modelPart.visible && child.isShowingIgnoringConcealment());
    }

    private Quaternion composeChildWorld(float parentPivotX, float parentPivotY, float parentPivotZ,
                                        Quaternion parentRotation, BendsModelPart child, float[] outPivot)
    {
        float lx = (child.position.x + child.offset.x) * child.offsetScale;
        float ly = (child.position.y + child.offset.y) * child.offsetScale;
        float lz = (child.position.z + child.offset.z) * child.offsetScale;
        float[] rotated = rotateVectorByQuaternion(parentRotation, lx, ly, lz, scratchVec);
        outPivot[0] = parentPivotX + rotated[0];
        outPivot[1] = parentPivotY + rotated[1];
        outPivot[2] = parentPivotZ + rotated[2];
        return Quaternion.mul(parentRotation, child.rotation.getSmooth(), scratchRotation);
    }

    private void setEndModelPart(ModelPart modelPart, float[] pivot, Quaternion rotation, boolean visible)
    {
        modelPart.x = pivot[0];
        modelPart.y = pivot[1];
        modelPart.z = pivot[2];
        float[] euler = quaternionToEulerXYZ(rotation, scratchEuler);
        modelPart.xRot = euler[0];
        modelPart.yRot = euler[1];
        modelPart.zRot = euler[2];
        modelPart.visible = visible;
    }

    private static float[] rotateVectorByQuaternion(Quaternion q, float x, float y, float z)
    {
        return rotateVectorByQuaternion(q, x, y, z, new float[3]);
    }

    private static float[] rotateVectorByQuaternion(Quaternion q, float x, float y, float z, float[] dest)
    {
        float tx = 2.0F * (q.y * z - q.z * y);
        float ty = 2.0F * (q.z * x - q.x * z);
        float tz = 2.0F * (q.x * y - q.y * x);
        dest[0] = x + q.w * tx + (q.y * tz - q.z * ty);
        dest[1] = y + q.w * ty + (q.z * tx - q.x * tz);
        dest[2] = z + q.w * tz + (q.x * ty - q.y * tx);
        return dest;
    }

    private static final float[] ZERO_EULER = {0, 0, 0};

    public float[] getPartEulerAngles(BendsModelPart part)
    {
        if (part == null) return ZERO_EULER;
        return quaternionToEulerXYZ(part.rotation.getSmooth());
    }

    private static float[] quaternionToEulerXYZ(Quaternion q)
    {
        return quaternionToEulerXYZ(q, new float[3]);
    }

    private static float[] quaternionToEulerXYZ(Quaternion q, float[] euler)
    {

        float sinX = 2.0f * (q.w * q.x + q.y * q.z);
        float cosX = 1.0f - 2.0f * (q.x * q.x + q.y * q.y);
        euler[0] = (float) Math.atan2(sinX, cosX);

        float sinY = 2.0f * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinY) >= 1.0f)
        {
            euler[1] = (float) Math.copySign(Math.PI / 2, sinY);
        }
        else
        {
            euler[1] = (float) Math.asin(sinY);
        }

        float sinZ = 2.0f * (q.w * q.z + q.x * q.y);
        float cosZ = 1.0f - 2.0f * (q.y * q.y + q.z * q.z);
        euler[2] = (float) Math.atan2(sinZ, cosZ);

        return euler;
    }

    protected static final class AttachedPart
    {
        private final ModelPart part;
        private final BendsModelPart bone;
        private final float offsetX, offsetY, offsetZ;
        private final boolean drawOwnCubes;
        private final boolean useOwnTransform;

        private AttachedPart(ModelPart part, BendsModelPart bone,
                             float offsetX, float offsetY, float offsetZ,
                             boolean drawOwnCubes)
        {
            this(part, bone, offsetX, offsetY, offsetZ, drawOwnCubes, false);
        }

        private AttachedPart(ModelPart part, BendsModelPart bone,
                             float offsetX, float offsetY, float offsetZ,
                             boolean drawOwnCubes, boolean useOwnTransform)
        {
            this.useOwnTransform = useOwnTransform;
            this.part = part;
            this.bone = bone;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.drawOwnCubes = drawOwnCubes;
        }
    }

    private static final class VanillaPartState
    {
        private final float x, y, z;
        private final float xRot, yRot, zRot;
        private final float xScale, yScale, zScale;
        private final boolean visible, skipDraw;

        private VanillaPartState(ModelPart part)
        {
            this.x = part.x;
            this.y = part.y;
            this.z = part.z;
            this.xRot = part.xRot;
            this.yRot = part.yRot;
            this.zRot = part.zRot;
            this.xScale = part.xScale;
            this.yScale = part.yScale;
            this.zScale = part.zScale;
            this.visible = part.visible;
            this.skipDraw = part.skipDraw;
        }

        private static VanillaPartState capture(ModelPart part)
        {
            return part != null ? new VanillaPartState(part) : null;
        }

        private static void restore(VanillaPartState state, ModelPart part)
        {
            if (state == null || part == null)
                return;

            part.x = state.x;
            part.y = state.y;
            part.z = state.z;
            part.xRot = state.xRot;
            part.yRot = state.yRot;
            part.zRot = state.zRot;
            part.xScale = state.xScale;
            part.yScale = state.yScale;
            part.zScale = state.zScale;
            part.visible = state.visible;
            part.skipDraw = state.skipDraw;
        }
    }

}
