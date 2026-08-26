package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.lib.animation.keyframe.ArmatureMask;
import goblinbob.mobends.lib.animation.keyframe.Bone;
import goblinbob.mobends.lib.animation.keyframe.Keyframe;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.lib.client.model.IAnimatedPart;
import goblinbob.mobends.lib.data.IEntityAnimationData;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.core.kumo.state.*;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.lib.util.KeyframeUtils;
import goblinbob.mobends.lib.util.Tween;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyframeLayerState implements ILayerState
{

    private List<INodeState> nodeStates = new ArrayList<>();
    private ArmatureMask mask;
    private final boolean additive;
    private INodeState previousNode;
    private INodeState currentNode;
    private float transitionProgress = 0.0F;
    private float transitionDuration = 0.0F;
    private ConnectionTemplate.Easing transitionEasing = ConnectionTemplate.Easing.EASE_IN_OUT;

    public KeyframeLayerState(IKumoInstancingContext context, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        this.mask = layerTemplate.mask;
        this.additive = layerTemplate.additive;

        for (KeyframeNodeTemplate nodeTemplate : layerTemplate.nodes)
        {
            nodeStates.add(KeyframeNodeRegistry.INSTANCE.createFromTemplate(context, nodeTemplate));
        }

        for (int i = 0; i < nodeStates.size(); ++i)
        {
            nodeStates.get(i).parseConnections(nodeStates, layerTemplate.nodes.get(i));
        }

        try
        {
            currentNode = nodeStates.get(layerTemplate.entryNode);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new MalformedKumoTemplateException("Entry node index is out of bounds.");
        }
    }

    @Override
    public void start(IKumoContext context)
    {
        currentNode.start(context);
    }

    List<INodeState> getNodeStates()
    {
        return nodeStates;
    }

    @Override
    public void update(IKumoContext context, float deltaTime) throws MalformedKumoTemplateException
    {
        final IEntityAnimationData data = context.getEntityData();

        if (currentNode != null)
        {
            KeyframeAnimation animation = currentNode.getAnimation();

            if (animation != null)
            {
                if (!additive)
                {
                    applyRestPose(data, animation);
                }

                if (previousNode != null)
                {
                    float t = transitionProgress / transitionDuration;
                    switch (transitionEasing)
                    {
                        case EASE_IN:
                            t = (float) Tween.easeIn(t, 2.0);
                            break;
                        case EASE_OUT:
                            t = (float) Tween.easeOut(t, 2.0);
                            break;
                        case EASE_IN_OUT:
                            t = (float) Tween.easeInOut(t, 2.0);
                            break;
                        case LINEAR:
                            break;
                    }

                    KeyframeAnimation previousAnimation = previousNode.getAnimation();

                    if (previousAnimation != null)
                    {
                        if (!additive)
                        {
                            applyRestPose(data, previousAnimation);
                        }

                        applyKeyframeAnimation(data, previousAnimation, previousNode.getProgress(), 1 - t);
                    }

                    applyKeyframeAnimation(data, animation, currentNode.getProgress(), t);

                    transitionProgress += deltaTime;
                    if (transitionProgress >= transitionDuration)
                    {
                        previousNode = null;
                    }
                }
                else
                {
                    applyKeyframeAnimation(data, animation, currentNode.getProgress(), 1.0F);
                }
            }
        }

        context.setCurrentNode(currentNode);

        if (currentNode != null)
        {
            currentNode.update(context, deltaTime);
        }

        if (previousNode != null && previousNode != currentNode)
        {
            previousNode.update(context, deltaTime);
        }

        for (ConnectionState connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(context))
            {
                transitionDuration = connection.transitionDuration;
                transitionEasing = connection.transitionEasing;
                if (transitionDuration == 0.0F)
                {
                    previousNode = null;
                }
                else
                {
                    previousNode = currentNode;
                    transitionProgress = 0;
                }

                currentNode = connection.targetNode;
                currentNode.start(context);

                break;
            }
        }
    }

    public void applyRestPose(IEntityAnimationData entityData, KeyframeAnimation animation)
    {
        if (shouldPartBeAffected("root") && animation.bones.containsKey("root"))
        {
            entityData.getGlobalOffset().set(0, 0, 0);
        }

        if ((shouldPartBeAffected("root") && animation.bones.containsKey("root")) ||
            (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation")))
        {
            entityData.getCenterRotation().set(0F, 0F, 0F, 0F);
        }

        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            final String key = entry.getKey();

            if (shouldPartBeAffected(key))
            {
                Object part = entityData.getPartForName(key);

                if (part instanceof IAnimatedPart)
                {
                    ((IAnimatedPart) part).getRotation().set(0F, 0F, 0F, 0F);
                    ((IAnimatedPart) part).getOffset().set(0F, 0F, 0F);
                }
            }
        }
    }

    private static Keyframe frameAt(java.util.List<Keyframe> keyframes, int index)
    {
        if (keyframes == null || keyframes.isEmpty() || index < 0)
        {
            return null;
        }
        if (index >= keyframes.size())
        {
            return keyframes.get(keyframes.size() - 1);
        }
        return keyframes.get(index);
    }

    private void composeRotation(SmoothOrientation target, float[] rotationA, float[] rotationB, float tween, float amount)
    {
        if (additive)
        {
            KeyframeUtils.tweenOrientationMultiplicative(target, rotationA, rotationB, tween, amount);
        }
        else
        {
            KeyframeUtils.tweenOrientationAdditive(target, rotationA, rotationB, tween, amount);
        }
    }

    public void applyKeyframeAnimation(IEntityAnimationData entityData, KeyframeAnimation animation, float keyframeIndex, float amount)
    {
        final int frameA = (int) keyframeIndex;
        final int frameB = (int) keyframeIndex + 1;
        final float tween = keyframeIndex - frameA;

        if (shouldPartBeAffected("root") && animation.bones.containsKey("root"))
        {
            final Bone rootBone = animation.bones.get("root");
            final Keyframe keyframe = frameAt(rootBone.keyframes, frameA);
            final Keyframe nextFrame = frameAt(rootBone.keyframes, frameB);

            if (keyframe != null && nextFrame != null)
            {
                KeyframeUtils.tweenVectorAdditive(entityData.getGlobalOffset(), keyframe.position, nextFrame.position, tween, amount);
            }
        }

        if (shouldPartBeAffected("centerRotation") && animation.bones.containsKey("centerRotation"))
        {
            final Bone rootBone = animation.bones.get("centerRotation");
            final Keyframe keyframe = frameAt(rootBone.keyframes, frameA);
            final Keyframe nextFrame = frameAt(rootBone.keyframes, frameB);

            if (keyframe != null && nextFrame != null)
            {
                composeRotation(entityData.getCenterRotation(), keyframe.rotation, nextFrame.rotation, tween, amount);
                KeyframeUtils.tweenVectorAdditive(entityData.getGlobalOffset(), keyframe.position, nextFrame.position, tween, amount);
            }
        }

        for (Map.Entry<String, Bone> entry : animation.bones.entrySet())
        {
            final String key = entry.getKey();

            if (shouldPartBeAffected(key))
            {
                Bone bone = entry.getValue();
                Object part = entityData.getPartForName(key);

                if (part != null)
                {
                    Keyframe keyframe = frameAt(bone.keyframes, frameA);
                    Keyframe nextFrame = frameAt(bone.keyframes, frameB);

                    if (keyframe != null && nextFrame != null)
                    {
                        if (part instanceof IAnimatedPart)
                        {
                            composeRotation(((IAnimatedPart) part).getRotation(), keyframe.rotation, nextFrame.rotation, tween, amount);
                            KeyframeUtils.tweenVectorAdditive(((IAnimatedPart) part).getOffset(), keyframe.position, nextFrame.position, tween, -amount);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldPartBeAffected(String partName)
    {
        return mask == null || mask.doesAllow(partName);
    }

    public static KeyframeLayerState createFromTemplate(IKumoInstancingContext data, KeyframeLayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        return new KeyframeLayerState(data, layerTemplate);
    }

}
