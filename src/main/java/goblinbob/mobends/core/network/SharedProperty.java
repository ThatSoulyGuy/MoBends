package goblinbob.mobends.core.network;

import net.minecraft.nbt.CompoundTag;

public abstract class SharedProperty<T>
{

    protected final String key;
    protected final T defaultValue;
    protected T value;

    public SharedProperty(String key, T defaultValue)
    {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getKey()
    {
        return key;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    public void resetToDefault()
    {
        this.value = this.defaultValue;
    }

    public abstract void writeToNBT(CompoundTag tag);

    public abstract void readFromNBT(CompoundTag tag);

}
