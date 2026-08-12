package goblinbob.mobends.core.network;

import net.minecraft.nbt.CompoundTag;

import java.util.LinkedList;

public class SharedConfig
{

    private final LinkedList<SharedProperty<?>> properties = new LinkedList<>();

    public void addProperty(SharedProperty<?> property)
    {
        properties.add(property);
    }

    public Iterable<SharedProperty<?>> getProperties()
    {
        return properties;
    }

    public void writeToNBT(CompoundTag tag)
    {
        for (SharedProperty<?> property : properties)
        {
            property.writeToNBT(tag);
        }
    }

    public void readFromNBT(CompoundTag tag)
    {
        for (SharedProperty<?> property : properties)
        {
            property.readFromNBT(tag);
        }
    }

    public void resetToDefaults()
    {
        for (SharedProperty<?> property : properties)
        {
            property.resetToDefault();
        }
    }

}
