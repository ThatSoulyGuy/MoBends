package goblinbob.mobends.core.pack;

import net.minecraft.resources.ResourceLocation;

public interface IBendsPack
{

    String getKey();

    String getDisplayName();

    String getAuthor();

    String getDescription();

    ResourceLocation getThumbnail();

    boolean canPackBeEdited();

}
