package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.api.item.IItemCapabilityProvider;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;
import goblinbob.mobends.standard.animation.bit.biped.EatingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.ShieldAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

import java.util.HashMap;
import java.util.Map;

public class BipedActionController
{
    protected HardAnimationLayer<BipedEntityData<?>> layerAction = new HardAnimationLayer<>();
    protected UseActionType currentUseActionType = null;
    protected AttackActionType currentAttackActionType = null;
    protected AnimationBit<BipedEntityData<?>> actionBit = null;

    private static final Map<UseActionType, ItemActionFactory<AnimationBit<BipedEntityData<?>>>> ITEM_USE_ACTION_MAP = new HashMap<>();
    private static final Map<AttackActionType, ItemActionFactory<AnimationBit<BipedEntityData<?>>>> ITEM_ATTACK_ACTION_MAP = new HashMap<>();
    static
    {
        ITEM_USE_ACTION_MAP.put(UseActionType.FOOD, EatingAnimationBit::new);
        ITEM_USE_ACTION_MAP.put(UseActionType.BOW, BowAction::new);
        ITEM_USE_ACTION_MAP.put(UseActionType.SHIELD, ShieldAnimationBit::new);

        ITEM_ATTACK_ACTION_MAP.put(AttackActionType.TOOL, ToolAction::new);
        ITEM_ATTACK_ACTION_MAP.put(AttackActionType.FISTS, PunchingAction::new);
        ITEM_ATTACK_ACTION_MAP.put(AttackActionType.SWORD, SwordAction::new);

        // Completeness checks
        for (UseActionType type : UseActionType.values())
        {
            if (!ITEM_USE_ACTION_MAP.containsKey(type))
                throw new IllegalStateException("The ITEM_USE_ACTION_MAP map needs to be complete.");
        }

        // Completeness checks
        for (AttackActionType type : AttackActionType.values())
        {
            if (!ITEM_ATTACK_ACTION_MAP.containsKey(type))
                throw new IllegalStateException("The ITEM_ATTACK_ACTION_MAP map needs to be complete.");
        }
    }

    private static HumanoidModel.ArmPose getAction(LivingEntity entity, ItemStack heldItem)
    {
        if (!heldItem.isEmpty())
        {
            if (entity.getUseItemRemainingTicks() > 0)
            {
                UseAnim useAnim = heldItem.getUseAnimation();

                if (useAnim == UseAnim.BLOCK)
                    return HumanoidModel.ArmPose.BLOCK;
                else if (useAnim == UseAnim.BOW)
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                else if (useAnim == UseAnim.CROSSBOW)
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                else if (useAnim == UseAnim.SPYGLASS)
                    return HumanoidModel.ArmPose.SPYGLASS;
            }

            return HumanoidModel.ArmPose.ITEM;
        }

        return HumanoidModel.ArmPose.EMPTY;
    }

    public static UseActionType getBuiltInItemUseAction(Item item, HumanoidModel.ArmPose armPoseMain, HumanoidModel.ArmPose armPoseOff)
    {
        if (item == Items.AIR)
            return null;

        // Use platform abstraction to check if item is food (handles DataComponents in 1.21.1, isEdible in 1.20.1)
        IItemCapabilityProvider capabilityProvider = IItemCapabilityProvider.Holder.getProvider();
        if (capabilityProvider != null && capabilityProvider.isFood(item))
            return UseActionType.FOOD;

        if (item instanceof BowItem || item instanceof CrossbowItem ||
                armPoseMain == HumanoidModel.ArmPose.BOW_AND_ARROW || armPoseOff == HumanoidModel.ArmPose.BOW_AND_ARROW ||
                armPoseMain == HumanoidModel.ArmPose.CROSSBOW_HOLD || armPoseOff == HumanoidModel.ArmPose.CROSSBOW_HOLD)
            return UseActionType.BOW;

        if (armPoseMain == HumanoidModel.ArmPose.BLOCK || armPoseOff == HumanoidModel.ArmPose.BLOCK)
            return UseActionType.SHIELD;

        UseAnim useAnim = new ItemStack(item).getUseAnimation();
        if (useAnim == UseAnim.EAT || useAnim == UseAnim.DRINK)
            return UseActionType.FOOD;

        return null;
    }

    public static UseActionType getItemUseAction(Item item, HumanoidModel.ArmPose armPoseMain, HumanoidModel.ArmPose armPoseOff)
    {
        UseActionType useActionType = ModConfig.getItemUseAction(item);

        return useActionType != null ? useActionType : getBuiltInItemUseAction(item, armPoseMain, armPoseOff);
    }

    public static AttackActionType getBuiltInItemAttackAction(Item item)
    {
        if (item instanceof SwordItem)
            return AttackActionType.SWORD;

        if (item == Items.AIR)
            return AttackActionType.FISTS;

        return AttackActionType.TOOL;
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        AttackActionType attackActionType = ModConfig.getItemAttackAction(item);

        return attackActionType != null ? attackActionType : getBuiltInItemAttackAction(item);
    }

    public void perform(
            BipedEntityData<?> data,
            HumanoidArm primaryHand,
            ItemStack heldItemMainhand,
            ItemStack heldItemOffhand,
            Item activeItem
    ) {
        final LivingEntity entity = data.getEntity();
        final HumanoidArm offHand = primaryHand == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        final HumanoidModel.ArmPose armPoseMain = getAction(entity, heldItemMainhand);
        final HumanoidModel.ArmPose armPoseOff = getAction(entity, heldItemOffhand);
        final HumanoidArm activeHandSide = entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? primaryHand : offHand;

        UseActionType useActionType = getItemUseAction(activeItem, armPoseMain, armPoseOff);
        if (useActionType != currentUseActionType)
        {
            currentUseActionType = useActionType;

            if (useActionType != null)
            {
                ItemActionFactory<AnimationBit<BipedEntityData<?>>> factory = ITEM_USE_ACTION_MAP.get(useActionType);
                this.actionBit = factory.create(activeHandSide);
                this.layerAction.playOrContinueBit(this.actionBit, data);
            }
            else
            {
                this.layerAction.clearAnimation();
                this.currentAttackActionType = null;
            }
        }

        AttackActionType attackActionType = getItemAttackAction(heldItemMainhand.getItem());

        if (this.currentAttackActionType != attackActionType)
        {
            this.currentAttackActionType = attackActionType;

            ItemActionFactory<AnimationBit<BipedEntityData<?>>> factory = ITEM_ATTACK_ACTION_MAP.get(attackActionType);
            if (factory == null)
            {
                this.actionBit = null;
                this.layerAction.clearAnimation();
            }
            else
            {
                this.actionBit = factory.create(primaryHand);
                this.layerAction.playOrContinueBit(this.actionBit, data);
            }
        }

        this.layerAction.perform(data);
    }

    public void clearAction()
    {
        layerAction.clearAnimation();
    }
}
