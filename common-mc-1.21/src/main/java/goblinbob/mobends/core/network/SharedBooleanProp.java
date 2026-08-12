package goblinbob.mobends.core.network;

import net.minecraft.nbt.CompoundTag;

public class SharedBooleanProp extends SharedProperty<Boolean>
{

    public SharedBooleanProp(String key, Boolean value, String description)
    {
        super(key, value, description);
    }

    @Override
    public void writeToNBT(CompoundTag tag)
    {
        tag.putBoolean(key, value);
    }

    @Override
    public void readFromNBT(CompoundTag tag)
    {
        if (tag.contains(key))
        {
            value = tag.getBoolean(key);
        }
        else
        {
            value = defaultValue;
        }
    }

}
