package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.rendering.IArmorHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

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
        return material.getName();
    }
}
