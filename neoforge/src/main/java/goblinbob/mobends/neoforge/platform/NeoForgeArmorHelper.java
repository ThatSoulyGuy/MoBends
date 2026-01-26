package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.rendering.IArmorHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * NeoForge 1.21.1 implementation of IArmorHelper.
 *
 * MC 1.21.1: ArmorItem.getMaterial() returns Holder<ArmorMaterial> with unwrapKey()
 */
public class NeoForgeArmorHelper implements IArmorHelper
{
    @Override
    public String getArmorMaterialName(Object armorItem)
    {
        if (!(armorItem instanceof ArmorItem item))
        {
            return "unknown";
        }

        net.minecraft.core.Holder<ArmorMaterial> materialHolder = item.getMaterial();
        return materialHolder.unwrapKey()
                .map(ResourceKey::location)
                .map(loc -> loc.getPath())
                .orElse("unknown");
    }
}
