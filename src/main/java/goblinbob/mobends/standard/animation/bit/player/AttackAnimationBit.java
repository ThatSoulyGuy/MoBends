package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.*;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;

public class AttackAnimationBit extends AnimationBit<PlayerData> {

    protected HardAnimationLayer<BipedEntityData<?>> layerBase;

    protected AttackSlashUpAnimationBit bitAttackSlashUp;
    protected AttackSlashDownAnimationBit bitAttackSlashDown;
    protected AttackSlashInwardAnimationBit bitAttackSlashInward;
    protected AttackSlashOutwardAnimationBit bitAttackSlashOutward;
    protected AttackWhirlSlashAnimationBit bitAttackWhirlSlash;
    protected FistGuardAnimationBit bitFistGuard;

    public AttackAnimationBit() {
        this.layerBase = new HardAnimationLayer<>();
        this.bitAttackSlashUp = new AttackSlashUpAnimationBit();
        this.bitAttackSlashDown = new AttackSlashDownAnimationBit();
        this.bitAttackSlashInward = new AttackSlashInwardAnimationBit();
        this.bitAttackSlashOutward = new AttackSlashOutwardAnimationBit();
        this.bitAttackWhirlSlash = new AttackWhirlSlashAnimationBit();
        this.bitFistGuard = new FistGuardAnimationBit();
    }

    @Override
    public String[] getActions(PlayerData entityData) {
        if (this.layerBase.isPlaying()) {
            return this.layerBase.getPerformedBit().getActions(entityData);
        }

        return null;
    }

    public boolean shouldPerformAttack(AbstractClientPlayer player) {
        final ItemStack heldItemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return !heldItemStack.is(Items.AIR);
    }

    @Override
    public void perform(PlayerData playerData) {

        this.layerBase.perform(playerData);
    }
}
