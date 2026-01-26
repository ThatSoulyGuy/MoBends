package goblinbob.mobends.standard.data;

import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.animation.controller.PlayerController;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public class PlayerData extends BipedEntityData<AbstractClientPlayer>
{
    protected boolean sprintJumpLeg = false;
    protected boolean sprintJumpLegSwitched = false;
    protected boolean fistPunchArm = false;
    protected int currentAttack = 0;
    protected float capeWavePhase = 0;
    protected float capeWaveSpeed = 0;

    private Boolean flyingStateOverride = null;

    public ModelPartTransform cape;

    private final PlayerController controller = new PlayerController();

    public PlayerData(AbstractClientPlayer entity)
    {
        super(entity);
    }

    @Override
    public PlayerController getController()
    {
        return controller;
    }

    public void setCapeWaveSpeed(float value)
    {
        capeWaveSpeed = value;
    }

    public float getCapeWavePhase()
    {
        return capeWavePhase;
    }

    public void overrideFlyingState(boolean flying)
    {
        this.flyingStateOverride = flying;
    }

    public void unsetFlyingStateOverride()
    {
        this.flyingStateOverride = null;
    }

    @Override
    public void onTicksRestart()
    {
        // No behaviour
    }

    @Override
    public void initModelPose()
    {
        super.initModelPose();

        EntityRenderer<? super AbstractClientPlayer> render = Minecraft.getInstance()
                .getEntityRenderDispatcher().getRenderer(this.entity);

        cape = new ModelPartTransform(body);
        nameToPartMap.put("cape", cape);
        cape.position.set(0F, 0F, 0F);

        // Player-specific leg positions (different from generic biped)
        rightLeg.position.set(-1.9F, 12.0F, 0.0F);
        leftLeg.position.set(1.9F, 12.0F, 0.0F);

        // Check if player has slim arms by checking the skin model name
        IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
        if (skinProvider != null && skinProvider.isSlimModel(this.entity))
        {
            rightArm.position.set(-5F, -9.5F, 0F);
            leftArm.position.set(5F, -9.5F, 0F);
        }
    }

    @Override
    public void updateParts(float ticksPerFrame)
    {
        super.updateParts(ticksPerFrame);

        cape.update(ticksPerFrame);
    }

    @Override
    public void update(float partialTicks)
    {
        super.update(partialTicks);

        if (getTicksAfterAttack() > 20)
        {
            currentAttack = 0;
        }

        if (motionY < 0)
        {
            sprintJumpLegSwitched = false;
        }

        if (!sprintJumpLegSwitched && motionY > 0)
        {
            sprintJumpLeg = !sprintJumpLeg;
            sprintJumpLegSwitched = true;
        }

        this.capeWavePhase += this.capeWaveSpeed * DataUpdateHandler.ticksPerFrame;
        if (this.capeWavePhase > 380.0F)
            this.capeWavePhase -= 380.0F;
    }

    @Override
    public void onLiftoff()
    {
        super.onLiftoff();
        if (!sprintJumpLegSwitched)
        {
            sprintJumpLeg = !sprintJumpLeg;
            sprintJumpLegSwitched = true;
        }
    }

    @Override
    public void onAttack()
    {
        if (this.entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.AIR)
        {
            this.fistPunchArm = !this.fistPunchArm;
            this.ticksAfterAttack = 0;
            return;
        }

        if (this.ticksAfterAttack <= 6.0F)
        {
            // Sword swing cooldown
            return;
        }

        switch (this.currentAttack)
        {
            case 1:
                this.currentAttack = 2;
                break;
            case 2:
                this.currentAttack = 3;
                break;
            case 3:
                this.currentAttack = 4;
                break;
            case 4:
                this.currentAttack = (!ModConfig.performSpinAttack || this.getEntity().isPassenger()) ? 1 : 5;
                break;
            default:
                this.currentAttack = 1;
                break;
        }

        this.ticksAfterAttack = 0;
    }

    public int getCurrentAttack()
    {
        return currentAttack;
    }

    public boolean getFistPunchArm()
    {
        return fistPunchArm;
    }

    public boolean getSprintJumpLeg()
    {
        return sprintJumpLeg;
    }

    public boolean isFlying()
    {
        return this.flyingStateOverride != null ?
                this.flyingStateOverride :
                this.entity.getAbilities().flying;
    }

}
