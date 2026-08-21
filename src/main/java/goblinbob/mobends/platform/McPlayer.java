package goblinbob.mobends.platform;

import goblinbob.mobends.api.entity.IPlayer;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class McPlayer extends McLivingEntity implements IPlayer
{
    private final Player player;

    public McPlayer(Player player)
    {
        super(player);
        this.player = player;
    }

    @Override
    public UUID getUUID()
    {
        return player.getUUID();
    }

    @Override
    public String getName()
    {
        return player.getName().getString();
    }

    @Override
    public boolean hasSlimArms()
    {
        if (player instanceof AbstractClientPlayer clientPlayer)
        {
            //? if >=1.21 {
            /*String model = clientPlayer.getSkin().model().id();
            *///?} else {
            String model = clientPlayer.getModelName();
            //?}
            return "slim".equals(model);
        }
        return false;
    }

    @Override
    public boolean isFlying()
    {
        if (player.getAbilities().flying)
            return true;

        if (player instanceof AbstractClientPlayer clientPlayer)
        {
            LivingEntityData<?> data = EntityDatabase.instance.get(clientPlayer);
            if (data instanceof PlayerData)
                return ((PlayerData) data).isFlying();
        }

        return false;
    }

    @Override
    public boolean isCreative()
    {
        return player.isCreative();
    }

    @Override
    public boolean isSpectator()
    {
        return player.isSpectator();
    }

    @Override
    public boolean isSprinting()
    {
        return player.isSprinting();
    }

    @Override
    public boolean isBlocking()
    {
        return player.isBlocking();
    }

    @Override
    public boolean isClimbing()
    {
        return player.onClimbable();
    }

    @Override
    public int getFoodLevel()
    {
        return player.getFoodData().getFoodLevel();
    }

    @Override
    public int getExperienceLevel()
    {
        return player.experienceLevel;
    }

    @Override
    public boolean isLocalPlayer()
    {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getUUID().equals(player.getUUID());
    }

    @Override
    public int getCapeAnimationTick()
    {
        return player.tickCount;
    }

    @Override
    public double getPrevCapeX()
    {
        return player.xOld;
    }

    @Override
    public double getPrevCapeY()
    {
        return player.yOld;
    }

    @Override
    public double getPrevCapeZ()
    {
        return player.zOld;
    }

    @Override
    public double getCapeX()
    {
        return player.getX();
    }

    @Override
    public double getCapeY()
    {
        return player.getY();
    }

    @Override
    public double getCapeZ()
    {
        return player.getZ();
    }

    public Player getPlayer()
    {
        return player;
    }
}
