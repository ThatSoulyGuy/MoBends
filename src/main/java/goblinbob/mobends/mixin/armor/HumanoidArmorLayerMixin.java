package goblinbob.mobends.mixin.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin placeholder for armor layer.
 *
 * <p>Mo'Bends animated poses are synced to the parent entity model in EntityRenderHandler.
 * Vanilla's copyPropertiesTo() then copies these poses from the parent model to the armor model.
 * This means armor automatically follows Mo'Bends animations without needing any mixin logic.</p>
 *
 * <p>Benefits of this approach:</p>
 * <ul>
 *   <li>Perfect synchronization - armor uses exact same poses as player via copyPropertiesTo</li>
 *   <li>Vanilla handles trims, enchantment glint, and all other effects</li>
 *   <li>Full compatibility with other mods that modify armor rendering</li>
 *   <li>No double-sync issues or texture problems</li>
 * </ul>
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
{
    // No injection needed - poses flow through copyPropertiesTo() from the parent model
    // which already has Mo'Bends poses synced by EntityRenderHandler
}
