package goblinbob.mobends.standard.previewer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;

public class PreviewPlayer extends RemotePlayer
{
    public PreviewPlayer(ClientLevel level, GameProfile profile)
    {
        super(level, profile);
    }

    public void copySkinCustomisation(Player source)
    {
        if (source == null)
        {
            return;
        }

        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION,
                source.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
    }
}
