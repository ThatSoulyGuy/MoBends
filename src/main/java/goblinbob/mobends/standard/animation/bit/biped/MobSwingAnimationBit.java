package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

public class MobSwingAnimationBit extends AnimationBit<BipedEntityData<?>>
{

    public static boolean canPerform(LivingEntity entity)
    {
        return entity != null
                && BipedActionController.getItemAttackAction(entity.getMainHandItem().getItem())
                        != AttackActionType.FISTS;
    }

    private static final List<AnimationBit<BipedEntityData<?>>> SLASHES = Arrays.asList(
            new AttackSlashUpAnimationBit(),
            new AttackSlashDownAnimationBit(),
            new AttackSlashInwardAnimationBit(),
            new AttackSlashOutwardAnimationBit()
    );

    private static final AnimationBit<BipedEntityData<?>> SPIN = new AttackWhirlSlashAnimationBit();

    private final HardAnimationLayer<BipedEntityData<?>> layer = new HardAnimationLayer<>();


    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        final int variantCount = ModConfig.mobsCanSpin ? SLASHES.size() + 1 : SLASHES.size();
        final int variant = Math.floorMod(data.getEntity().tickCount / 6, variantCount);

        this.layer.playBit(variant < SLASHES.size() ? SLASHES.get(variant) : SPIN, data);
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        this.layer.perform(data);
    }
}
