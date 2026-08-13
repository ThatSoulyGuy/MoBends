package goblinbob.mobends.core.kumo.state.procedural;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.lib.animation.keyframe.ArmatureMask;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.expression.ExpressionContext;
import goblinbob.mobends.core.expression.ExpressionException;
import goblinbob.mobends.core.kumo.KumoExpressionContext;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.ILayerState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.procedural.ProceduralBoneTemplate;
import goblinbob.mobends.core.kumo.state.template.procedural.ProceduralLayerTemplate;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ProceduralLayerState implements ILayerState
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, CompiledBoneExpression> compiledBones;
    private final ArmatureMask mask;
    private final float blendWeight;
    private final boolean additive;
    private final Set<String> loggedWarnings = new HashSet<>();

    public ProceduralLayerState(IKumoInstancingContext context, ProceduralLayerTemplate template)
            throws MalformedKumoTemplateException
    {
        this.mask = template.mask;
        this.blendWeight = template.blendWeight;
        this.additive = template.additive;
        this.compiledBones = new HashMap<>();

        for (Map.Entry<String, ProceduralBoneTemplate> entry : template.bones.entrySet())
        {
            String boneName = entry.getKey();
            ProceduralBoneTemplate boneTemplate = entry.getValue();

            try
            {
                compiledBones.put(boneName, new CompiledBoneExpression(boneTemplate));
            }
            catch (ExpressionException e)
            {
                throw new MalformedKumoTemplateException(
                        String.format("Failed to compile expression for bone '%s': %s", boneName, e.getMessage()), e);
            }
        }
    }

    @Override
    public void start(IKumoContext context)
    {
    }

    @Override
    public void update(IKumoContext context, float deltaTime) throws MalformedKumoTemplateException
    {
        EntityData<?> entityData = context.getEntityData();
        ExpressionContext exprContext = new KumoExpressionContext(entityData);

        for (Map.Entry<String, CompiledBoneExpression> entry : compiledBones.entrySet())
        {
            String boneName = entry.getKey();
            CompiledBoneExpression bone = entry.getValue();

            if (!shouldPartBeAffected(boneName))
            {
                continue;
            }

            Object partObj = entityData.getPartForName(boneName);

            if (boneName.equals("root"))
            {
                applyRootBone(entityData, bone, exprContext);
                continue;
            }
            if (boneName.equals("centerRotation"))
            {
                applyCenterRotation(entityData, bone, exprContext);
                continue;
            }

            if (partObj instanceof IModelPart part)
            {
                applyBoneTransform(part, bone, exprContext);
            }
            else if (partObj == null && !loggedWarnings.contains(boneName))
            {
                LOGGER.warn("Procedural layer references unknown bone: {}", boneName);
                loggedWarnings.add(boneName);
            }
        }
    }

    private void applyBoneTransform(IModelPart part, CompiledBoneExpression bone, ExpressionContext context)
    {
        SmoothOrientation rotation = part.getRotation();
        IVec3f offset = part.getOffset();

        if (bone.hasRotation())
        {
            float rotX = bone.evaluateRotationX(context);
            float rotY = bone.evaluateRotationY(context);
            float rotZ = bone.evaluateRotationZ(context);

            if (additive)
            {
                if (bone.hasRotationX()) rotation.rotateInstantX(rotX * blendWeight);
                if (bone.hasRotationY()) rotation.rotateInstantY(rotY * blendWeight);
                if (bone.hasRotationZ()) rotation.rotateInstantZ(rotZ * blendWeight);
            }
            else
            {
                if (blendWeight >= 1.0f)
                {
                    rotation.identity();
                    if (bone.hasRotationX()) rotation.orientInstantX(rotX);
                    if (bone.hasRotationY()) rotation.rotateInstantY(rotY);
                    if (bone.hasRotationZ()) rotation.rotateInstantZ(rotZ);
                }
                else
                {
                    if (bone.hasRotationX()) rotation.rotateInstantX(rotX * blendWeight);
                    if (bone.hasRotationY()) rotation.rotateInstantY(rotY * blendWeight);
                    if (bone.hasRotationZ()) rotation.rotateInstantZ(rotZ * blendWeight);
                }
            }
        }

        if (bone.hasOffset())
        {
            float offX = bone.evaluateOffsetX(context) * blendWeight;
            float offY = bone.evaluateOffsetY(context) * blendWeight;
            float offZ = bone.evaluateOffsetZ(context) * blendWeight;

            if (additive)
            {
                offset.set(
                        offset.getX() + offX,
                        offset.getY() + offY,
                        offset.getZ() + offZ
                );
            }
            else
            {
                offset.set(offX, offY, offZ);
            }
        }
    }

    private void applyRootBone(EntityData<?> entityData, CompiledBoneExpression bone, ExpressionContext context)
    {
        if (bone.hasOffset())
        {
            float offX = bone.evaluateOffsetX(context) * blendWeight;
            float offY = bone.evaluateOffsetY(context) * blendWeight;
            float offZ = bone.evaluateOffsetZ(context) * blendWeight;

            if (additive)
            {
                entityData.globalOffset.set(
                        entityData.globalOffset.getX() + offX,
                        entityData.globalOffset.getY() + offY,
                        entityData.globalOffset.getZ() + offZ
                );
            }
            else
            {
                entityData.globalOffset.set(offX, offY, offZ);
            }
        }
    }

    private void applyCenterRotation(EntityData<?> entityData, CompiledBoneExpression bone, ExpressionContext context)
    {
        SmoothOrientation rotation = entityData.centerRotation;

        if (bone.hasRotation())
        {
            float rotX = bone.evaluateRotationX(context);
            float rotY = bone.evaluateRotationY(context);
            float rotZ = bone.evaluateRotationZ(context);

            if (additive)
            {
                if (bone.hasRotationX()) rotation.rotateInstantX(rotX * blendWeight);
                if (bone.hasRotationY()) rotation.rotateInstantY(rotY * blendWeight);
                if (bone.hasRotationZ()) rotation.rotateInstantZ(rotZ * blendWeight);
            }
            else
            {
                if (blendWeight >= 1.0f)
                {
                    rotation.identity();
                    if (bone.hasRotationX()) rotation.orientInstantX(rotX);
                    if (bone.hasRotationY()) rotation.rotateInstantY(rotY);
                    if (bone.hasRotationZ()) rotation.rotateInstantZ(rotZ);
                }
                else
                {
                    if (bone.hasRotationX()) rotation.rotateInstantX(rotX * blendWeight);
                    if (bone.hasRotationY()) rotation.rotateInstantY(rotY * blendWeight);
                    if (bone.hasRotationZ()) rotation.rotateInstantZ(rotZ * blendWeight);
                }
            }
        }

        if (bone.hasOffset())
        {
            float offX = bone.evaluateOffsetX(context) * blendWeight;
            float offY = bone.evaluateOffsetY(context) * blendWeight;
            float offZ = bone.evaluateOffsetZ(context) * blendWeight;

            if (additive)
            {
                entityData.globalOffset.set(
                        entityData.globalOffset.getX() + offX,
                        entityData.globalOffset.getY() + offY,
                        entityData.globalOffset.getZ() + offZ
                );
            }
            else
            {
                entityData.globalOffset.set(offX, offY, offZ);
            }
        }
    }

    private boolean shouldPartBeAffected(String partName)
    {
        return mask == null || mask.doesAllow(partName);
    }

    public static ProceduralLayerState createFromTemplate(IKumoInstancingContext context, ProceduralLayerTemplate template)
            throws MalformedKumoTemplateException
    {
        return new ProceduralLayerState(context, template);
    }
}
