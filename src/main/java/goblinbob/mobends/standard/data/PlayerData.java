package goblinbob.mobends.standard.data;

import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.animation.controller.PlayerController;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

public class PlayerData extends BipedEntityData<AbstractClientPlayer>
{
    private static final double FALLING_SPEED_THRESHOLD = -0.35D;
    private static final int ALTITUDE_HISTORY_TICKS = 20;
    private static final int HOVER_WINDOW_TICKS = 10;
    private static final double HOVER_MAX_SPREAD = 0.35D;
    private static final double CLIMB_MIN_RISE = 3.5D;
    private static final int TICKS_AIRBORNE_BEFORE_DETECTION = 14;
    private static final int TICKS_FALLING_TO_RELEASE_FLIGHT = 3;

    protected boolean sprintJumpLeg = false;
    protected boolean sprintJumpLegSwitched = false;
    protected boolean fistPunchArm = false;
    protected int currentAttack = 0;
    protected float capeWavePhase = 0;
    protected float capeWaveSpeed = 0;

    private final double[] altitudeHistory = new double[ALTITUDE_HISTORY_TICKS];

    private Boolean flyingStateOverride = null;
    private boolean flightDetected = false;
    private int airborneTicks = 0;
    private int ticksSinceAltitudeHeld = 0;
    private int fallingTicks = 0;

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

        rightLeg.position.set(-1.9F, 12.0F, 0.0F);
        leftLeg.position.set(1.9F, 12.0F, 0.0F);

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
    public void updateClient()
    {
        super.updateClient();

        this.updateFlightDetection();
    }

    private void updateFlightDetection()
    {
        if (this.entity == Minecraft.getInstance().player || !this.couldBeFlying())
        {
            this.flightDetected = false;
            this.airborneTicks = 0;
            this.ticksSinceAltitudeHeld = 0;
            this.fallingTicks = 0;
            return;
        }

        this.altitudeHistory[this.airborneTicks % ALTITUDE_HISTORY_TICKS] = this.entity.getY();
        this.airborneTicks++;
        this.ticksSinceAltitudeHeld = this.motionY >= 0.0D ? 0 : this.ticksSinceAltitudeHeld + 1;
        this.fallingTicks = this.motionY < FALLING_SPEED_THRESHOLD ? this.fallingTicks + 1 : 0;

        if (this.fallingTicks >= TICKS_FALLING_TO_RELEASE_FLIGHT)
        {
            this.flightDetected = false;
            return;
        }

        if (this.airborneTicks >= TICKS_AIRBORNE_BEFORE_DETECTION
                && this.ticksSinceAltitudeHeld <= HOVER_WINDOW_TICKS
                && this.altitudeSpread(HOVER_WINDOW_TICKS) < HOVER_MAX_SPREAD)
        {
            this.flightDetected = true;
        }
        else if (this.airborneTicks >= ALTITUDE_HISTORY_TICKS
                && this.altitudeAt(0) - this.altitudeAt(ALTITUDE_HISTORY_TICKS - 1) > CLIMB_MIN_RISE)
        {
            this.flightDetected = true;
        }
    }

    private double altitudeAt(int ticksAgo)
    {
        return this.altitudeHistory[Math.floorMod(this.airborneTicks - 1 - ticksAgo, ALTITUDE_HISTORY_TICKS)];
    }

    private double altitudeSpread(int window)
    {
        double lowest = this.altitudeAt(0);
        double highest = lowest;

        for (int i = 1; i < window; i++)
        {
            final double altitude = this.altitudeAt(i);
            lowest = Math.min(lowest, altitude);
            highest = Math.max(highest, altitude);
        }

        return highest - lowest;
    }

    private boolean couldBeFlying()
    {
        return !this.isOnGround()
                && !this.entity.onGround()
                && !this.isRiding()
                && !this.isClimbing()
                && !this.entity.onClimbable()
                && !this.isInWater()
                && !this.entity.isInLava()
                && !this.entity.isFallFlying()
                && !this.entity.hasEffect(MobEffects.LEVITATION)
                && !this.entity.hasEffect(MobEffects.SLOW_FALLING);
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
        boolean isLocalPlayer = this.entity == Minecraft.getInstance().player;

        if (this.entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.AIR)
        {
            if (isLocalPlayer)
            {
                this.fistPunchArm = !this.fistPunchArm;
            }
            else
            {
                this.fistPunchArm = (this.entity.tickCount / 6) % 2 == 0;
            }
            this.ticksAfterAttack = 0;
            return;
        }

        if (this.ticksAfterAttack <= 6.0F)
        {
            return;
        }

        if (isLocalPlayer)
        {
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
        }
        else
        {
            int numAttacks = (ModConfig.performSpinAttack && !this.getEntity().isPassenger()) ? 5 : 4;
            this.currentAttack = ((this.entity.tickCount / 6) % numAttacks) + 1;
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
        if (this.flyingStateOverride != null)
            return this.flyingStateOverride;

        return this.entity.getAbilities().flying || this.flightDetected;
    }

}
