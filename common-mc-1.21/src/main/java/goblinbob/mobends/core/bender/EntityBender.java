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
            // Get entity type from registry
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
            // We need to check if this entity type creates instances of our class
            // This is a simplified check - in practice, this should work for most entities
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

    /**
     * Returns the animation types supported by this entity bender.
     * Override this to provide entity-specific animations.
     * Default returns common biped animations.
     */
    public String[] getSupportedAnimations()
    {
        // Default: common animations most bipeds support
        return new String[] { "walk", "jump", "fall" };
    }

    public abstract IEntityDataFactory<T> getDataFactory();

    public abstract IMutatorFactory<T> getMutatorFactory();

    public abstract IPreviewer<?> getPreviewer();

    public abstract LivingEntityData<?> getDataForPreview();

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

    /**
     * Returns true if entities assigned to this EntityBender
     * should be animated.
     */
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

    /**
     * Used to apply the effect of the mutation, or just to update the model if it was already mutated.
     * Called from EntityBender.
     */
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

    /**
     * Used to reverse the effect of the mutation.
     * Called from EntityBender.
     */
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

    /**
     * Used to refresh the mutators in case of real-time changes during development.
     */
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

    @SuppressWarnings("unchecked")
    protected T createPreviewEntity()
    {
        try
        {
            Level level = Minecraft.getInstance().level;
            if (level == null) return null;

            Mob entity = (Mob) this.entityClass.getConstructor(EntityType.class, Level.class)
                .newInstance(getEntityTypeForClass(entityClass), level);
            entity.moveTo(0, 0, 0, 0, 0);
            // Use abstraction for cross-version compatibility (4 params in 1.21.1, 5 params in 1.20.1)
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
