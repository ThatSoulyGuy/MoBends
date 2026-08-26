package goblinbob.mobends.core.bender;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.entity.IMobSpawnHelper;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import goblinbob.mobends.core.mutators.Mutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class EntityBender<T extends LivingEntity>
{
    protected final String key;
    protected final String unlocalizedName;

    private final MutatedRenderer<T> renderer;
    public final Class<T> entityClass;

    private final Map<LivingEntityRenderer<? extends T, ?>, Mutator<LivingEntityData<T>, T, ?>> mutatorMap = new HashMap<>();

    private boolean animate;
    protected Map<String, BoneMetadata> boneMetadataMap;

    public EntityBender(String modId, @Nullable String key, String unlocalizedName, Class<T> entityClass,
                        MutatedRenderer<T> renderer)
    {
        if (renderer == null)
            throw new NullPointerException("The mutated renderer cannot be null.");
        if (entityClass == null)
            throw new NullPointerException("The entity class cannot be null.");
        if (modId == null)
            throw new NullPointerException("The Mod ID cannot be null.");

        if (key == null)
        {
            EntityType<?> entityType = getEntityTypeForClass(entityClass);
            if (entityType == null)
                throw new RuntimeException("Unable to find an EntityType for " + entityClass.getName());

            ResourceLocation resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (resourceLocation == null)
                throw new RuntimeException("Unable to find a key for " + entityClass.getName());

            key = resourceLocation.toString();
            unlocalizedName = "entity." + resourceLocation.getNamespace() + "." + resourceLocation.getPath();
        }

        this.key = modId + "-" + key;
        this.unlocalizedName = unlocalizedName;
        this.entityClass = entityClass;
        this.renderer = renderer;
    }

    @SuppressWarnings("unchecked")
    private static <T extends LivingEntity> EntityType<T> getEntityTypeForClass(Class<T> entityClass)
    {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE)
        {
            try
            {
                if (entityClass.getSimpleName().equalsIgnoreCase(
                        BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath().replace("_", "")))
                {
                    return (EntityType<T>) entityType;
                }
            }
            catch (Exception ignored) {}
        }
        return null;
    }

    public abstract String[] getAlterableParts();

    public String[] getSupportedAnimations()
    {
        return new String[] { "walk", "jump", "fall" };
    }

    public abstract IEntityDataFactory<T> getDataFactory();

    public abstract IMutatorFactory<T> getMutatorFactory();

    public abstract IPreviewer<?> getPreviewer();

    public abstract LivingEntityData<?> getDataForPreview();

    protected MutatedRenderer<T> getMutatedRenderer()
    {
        return this.renderer;
    }

    public String getKey()
    {
        return this.key;
    }

    public String getUnlocalizedName()
    {
        return this.unlocalizedName;
    }

    public String getLocalizedName()
    {
        return I18n.get(this.unlocalizedName);
    }

    public boolean isAnimated()
    {
        return this.animate;
    }

    public void setAnimate(boolean animate)
    {
        this.animate = animate;
    }

    public void beforeRender(EntityData<T> data, T entity, float partialTicks, PoseStack poseStack)
    {
        this.renderer.beforeRender(data, entity, partialTicks, poseStack);
    }

    public void afterRender(T entity, float partialTicks, PoseStack poseStack)
    {
        this.renderer.afterRender(entity, partialTicks, poseStack);
    }

    @SuppressWarnings("unchecked")
    public <M extends EntityModel<T>> boolean applyMutation(LivingEntityRenderer<T, M> renderer, T entity, float partialTicks)
    {
        Mutator<LivingEntityData<T>, T, M> mutator = (Mutator<LivingEntityData<T>, T, M>) mutatorMap.get(renderer);
        if (mutator == null)
        {
            mutator = (Mutator<LivingEntityData<T>, T, M>) this.getMutatorFactory().createMutator(this.getDataFactory());
            if (!mutator.mutate(renderer))
            {
                return false;
            }

            mutatorMap.put(renderer, (Mutator<LivingEntityData<T>, T, ?>) mutator);
        }

        mutator.updateModel(entity, renderer, partialTicks);
        LivingEntityData<T> data = mutator.getOrMakeData(entity);
        mutator.performAnimations(data, this.key, renderer, partialTicks);
        mutator.syncUpWithData(data);

        return true;
    }

    @SuppressWarnings("unchecked")
    public <M extends EntityModel<T>> void deapplyMutation(LivingEntityRenderer<T, M> renderer, LivingEntity entity)
    {
        if (mutatorMap.containsKey(renderer))
        {
            Mutator<LivingEntityData<T>, T, M> mutator = (Mutator<LivingEntityData<T>, T, M>) mutatorMap.get(renderer);
            mutator.demutate(renderer);
            mutatorMap.remove(renderer);
        }
    }

    @SuppressWarnings("unchecked")
    public void demutateAll()
    {
        for (Entry<LivingEntityRenderer<? extends T, ?>, Mutator<LivingEntityData<T>, T, ?>> entry : mutatorMap.entrySet())
        {
            LivingEntityRenderer<T, EntityModel<T>> renderer = (LivingEntityRenderer<T, EntityModel<T>>) entry.getKey();
            Mutator<LivingEntityData<T>, T, EntityModel<T>> mutator = (Mutator<LivingEntityData<T>, T, EntityModel<T>>) entry.getValue();
            mutator.demutate(renderer);
        }

        mutatorMap.clear();
    }

    @SuppressWarnings("unchecked")
    public void refreshMutation()
    {
        for (Entry<LivingEntityRenderer<? extends T, ?>, Mutator<LivingEntityData<T>, T, ?>> entry : mutatorMap.entrySet())
        {
            LivingEntityRenderer<T, EntityModel<T>> renderer = (LivingEntityRenderer<T, EntityModel<T>>) entry.getKey();
            Mutator<LivingEntityData<T>, T, EntityModel<T>> mutator = (Mutator<LivingEntityData<T>, T, EntityModel<T>>) entry.getValue();
            mutator.demutate(renderer);
            mutator.mutate(renderer);
            mutator.postRefresh();
        }
    }

    /**
     * Builds a throwaway entity for the settings screen's preview, or null when there is no world.
     *
     * <p>Previews do not work at the main menu, which is where this screen opens from the mod list.
     * That is a known limitation rather than an oversight, and the widget says so instead of
     * reporting a failure.
     *
     * <p>A synthetic {@code ClientLevel} was tried and does not work: its constructor dereferences
     * its {@code ClientPacketListener} immediately, at {@code ClientLevel:171}
     * ({@code connection.registryAccess()}), so the listener cannot be null and cannot be worked
     * around by overriding {@code registryAccess()}. Building a real listener needs a live
     * {@code Connection} plus collaborators whose constructors differ between 1.20.1 and 1.21.1
     * ({@code Screen/ServerData/GameProfile/WorldSessionTelemetryManager} versus a
     * {@code CommonListenerCookie}), and both of those need a {@code RegistryAccess} — which is
     * the thing the fake level was trying to obtain in the first place.
     */
    @SuppressWarnings("unchecked")
    public T createPreviewEntity()
    {
        try
        {
            Level level = Minecraft.getInstance().level;
            if (level == null) return null;

            Mob entity = (Mob) this.entityClass.getConstructor(EntityType.class, Level.class)
                .newInstance(getEntityTypeForClass(entityClass), level);
            entity.moveTo(0, 0, 0, 0, 0);
            IMobSpawnHelper helper = IMobSpawnHelper.Holder.getHelper();
            if (helper != null)
            {
                Object serverLevel = Minecraft.getInstance().getSingleplayerServer() != null
                    ? Minecraft.getInstance().getSingleplayerServer().overworld()
                    : null;
                helper.finalizeSpawn(
                    entity,
                    serverLevel,
                    level.getCurrentDifficultyAt(entity.blockPosition()),
                    MobSpawnType.COMMAND
                );
            }
            PreviewHelper.registerPreviewEntity(entity);

            return (T) entity;
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public Mutator<?, ?, ?> getMutator(LivingEntityRenderer<? extends LivingEntity, ?> renderer)
    {
        return this.mutatorMap.get(renderer);
    }
}
