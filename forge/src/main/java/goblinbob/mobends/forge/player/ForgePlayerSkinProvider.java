package goblinbob.mobends.forge.player;

import goblinbob.mobends.api.player.IPlayerSkinProvider;
import net.minecraft.client.player.AbstractClientPlayer;

public class ForgePlayerSkinProvider implements IPlayerSkinProvider
{
    @Override
    public boolean isSlimModel(Object player)
    {
        if (player instanceof AbstractClientPlayer acp)
        {
            return "slim".equals(acp.getModelName());
        }
        return false;
    }

    @Override
    public Object getCapeTexture(Object player)
    {
        if (player instanceof AbstractClientPlayer acp)
        {
            return acp.getCloakTextureLocation();
        }
        return null;
    }

    @Override
    public Object getElytraTexture(Object player)
    {
        return null;
    }
}
