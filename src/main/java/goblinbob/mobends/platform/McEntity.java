package goblinbob.mobends.platform;

import goblinbob.mobends.api.entity.IEntity;
import net.minecraft.world.entity.Entity;

public class McEntity implements IEntity
{
    protected final Entity entity;

    public McEntity(Entity entity)
    {
        this.entity = entity;
    }

    @Override
    public int getId()
    {
        return entity.getId();
    }

    @Override
    public double getX()
    {
        return entity.getX();
    }

    @Override
    public double getY()
    {
        return entity.getY();
    }

    @Override
    public double getZ()
    {
        return entity.getZ();
    }

    @Override
    public double getPrevX()
    {
        return entity.xOld;
    }

    @Override
    public double getPrevY()
    {
        return entity.yOld;
    }

    @Override
    public double getPrevZ()
    {
        return entity.zOld;
    }

    @Override
    public float getYRot()
    {
        return entity.getYRot();
    }

    @Override
    public float getXRot()
    {
        return entity.getXRot();
    }

    @Override
    public float getPrevYRot()
    {
        return entity.yRotO;
    }

    @Override
    public float getPrevXRot()
    {
        return entity.xRotO;
    }

    @Override
    public boolean isOnGround()
    {
        return entity.onGround();
    }

    @Override
    public boolean isInWater()
    {
        return entity.isInWater();
    }

    @Override
    public boolean isPassenger()
    {
        return entity.isPassenger();
    }

    @Override
    public float getBbWidth()
    {
        return entity.getBbWidth();
    }

    @Override
    public float getBbHeight()
    {
        return entity.getBbHeight();
    }

    @Override
    public Object getNative()
    {
        return entity;
    }

    public Entity getEntity()
    {
        return entity;
    }
}
