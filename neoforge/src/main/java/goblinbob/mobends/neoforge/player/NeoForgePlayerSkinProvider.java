package goblinbob.mobends.neoforge.player;

import goblinbob.mobends.api.player.IPlayerSkinProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;

public class NeoForgePlayerSkinProvider implements IPlayerSkinProvider
{
    @Override
    public boolean isSlimModel(Object player)
    {
        if (player instanceof AbstractClientPlayer acp)
        {
            return acp.getSkin().model() == PlayerSkin.Model.SLIM;
        }
        return false;
    }

    @Override
    public Object getCapeTexture(Object player)
    {
        if (player instanceof AbstractClientPlayer acp)
        {
            return acp.getSkin().capeTexture();
        }
        return null;
    }

    @Override
    public Object getElytraTexture(Object player)
    {
        if (player instanceof AbstractClientPlayer acp)
        {
            return acp.getSkin().elytraTexture();
        }
        return null;
    }
}
