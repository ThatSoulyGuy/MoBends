package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EquipmentNameCondition implements ITriggerCondition
{

    private String namePattern;
    private EquipmentSlot slot;

    public EquipmentNameCondition(Template template)
    {
        this.namePattern = template.namePattern;
        this.slot = template.slot;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        if (!(context.getEntityData() instanceof EntityData<?> entityData))
        {
            return false;
        }

        Entity entity = entityData.getEntity();

        if (entity instanceof Player)
        {
            Player player = (Player) entity;
            ItemStack itemStack = player.getItemBySlot(this.slot);
            return itemStack.getHoverName().getString().matches(namePattern);
        }

        return false;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String namePattern;
        public EquipmentSlot slot;

    }

}
