package goblinbob.mobends.core.network;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SharedBooleanProp extends SharedProperty<Boolean>
{

    public SharedBooleanProp(String key, Boolean value, String description)
    {
        super(key, value, description);
    }

    @Override
    public Boolean getValue()
    {
        return super.getValue();
    }

    @Override
    public void writeToNBT(CompoundTag tag)
    {
        tag.putBoolean(key, value);
    }

    @Override
    public void readFromNBT(CompoundTag tag)
    {
        value = tag.getBoolean(key);
    }

    @Override
    public void updateWithConfigValue(ModConfigSpec.ConfigValue<Boolean> configValue)
    {
        value = configValue.get();
    }

}
