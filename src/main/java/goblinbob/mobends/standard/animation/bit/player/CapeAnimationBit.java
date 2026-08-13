package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

public class CapeAnimationBit extends AnimationBit<PlayerData>
{

    @Override
    public String[] getActions(PlayerData entityData)
    {
        return null;
    }

    @Override
    public void perform(PlayerData data)
    {
        final AbstractClientPlayer player = data.getEntity();

        data.cape.rotation.orientX(0.0F);

        final double partialTicks = DataUpdateHandler.partialTicks;
        double d0 = player.xCloakO + (player.xCloak - player.xCloakO) * partialTicks - (player.xo + (player.getX() - player.xo) * partialTicks);
        double d1 = player.yCloakO + (player.yCloak - player.yCloakO) * partialTicks - (player.yo + (player.getY() - player.yo) * partialTicks);
        double d2 = player.zCloakO + (player.zCloak - player.zCloakO) * partialTicks - (player.zo + (player.getZ() - player.zo) * partialTicks);
        double f = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO) * partialTicks;
        double d3 = Math.sin(f * 0.017453292);
        double d4 = -Math.cos(f * 0.017453292);
        double f1 = d1 * 10.0;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);
        float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
        float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;

        if (f2 < 0.0F)
        {
            f2 = 0.0F;
        }

        double f4 = player.oBob + (player.bob - player.oBob) * partialTicks;
        f1 = f1 + Math.sin((player.walkDistO + (player.walkDist - player.walkDistO) * (float)partialTicks) * 6.0F) * 32.0F * f4;

        if (player.isCrouching())
        {
            f1 += 25.0F;
        }

        if (data.isFlying() && player.isSprinting())
        {
            data.cape.rotation.setSmoothness(0.5F).orientX(0.0F);

            data.setCapeWaveSpeed(4.0F);
        }
        else
        {
            data.cape.rotation.setSmoothness(0.5F).orientX((float) (6.0F + f2 / 2.0F + f1));
            data.cape.rotation.rotateZ(f3 / 2.0F);
            data.cape.rotation.rotateY(-f3 / 2.0F);

            data.setCapeWaveSpeed(1.0F);
        }
    }

}
