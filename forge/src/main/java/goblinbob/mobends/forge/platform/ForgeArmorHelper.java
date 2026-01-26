package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.rendering.IArmorHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Forge 1.20.1 implementation of IArmorHelper.
 *
 * MC 1.20.1: ArmorItem.getMaterial() returns ArmorMaterial enum directly
 */
public class ForgeArmorHelper implements IArmorHelper
{
    @Override
    public String getArmorMaterialName(Object armorItem)
    {
        if (!(armorItem instanceof ArmorItem item))
        {
            return "unknown";
        }

        ArmorMaterial material = item.getMaterial();
        // In 1.20.1, ArmorMaterial is an enum with getName()
        return material.getName();
    }
}
