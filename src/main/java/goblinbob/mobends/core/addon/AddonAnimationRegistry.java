package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.client.gui.AnimationEditorRegistry;
import goblinbob.mobends.core.client.gui.IAnimationEditor;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionFactory;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import net.minecraft.world.entity.LivingEntity;

public class AddonAnimationRegistry
{

    private final String modId;

    public AddonAnimationRegistry(String modId)
    {
        this.modId = modId;
    }

    public <T extends LivingEntity> String registerNewEntity(Class<T> entityClass,
                                                             IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                             MutatedRenderer<T> renderer, String... alterableParts)
    {
        return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, alterableParts);
    }

    public <T extends LivingEntity> String registerNewEntity(Class<T> entityClass,
                                                             IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                             MutatedRenderer<T> renderer, IPreviewer<?> previewer, String... alterableParts)
    {
        return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterableParts);
    }

    public <T extends LivingEntity> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
                                                             IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                             MutatedRenderer<T> renderer, String... alterableParts)
    {
        EntityBender<T> entityBender = new DefaultEntityBender<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, null, alterableParts);
        return registerEntity(entityBender);
    }

    public <T extends LivingEntity> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
                                                             IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                             MutatedRenderer<T> renderer, IPreviewer<?> previewer, String... alterableParts)
    {
        EntityBender<T> entityBender = new DefaultEntityBender<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterableParts);
        return registerEntity(entityBender);
    }

    public <T extends LivingEntity> String registerNewEntity(Class<T> entityClass,
                                                             IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                             MutatedRenderer<T> renderer, IPreviewer<?> previewer,
                                                             String[] supportedAnimations, String... alterableParts)
    {
        EntityBender<T> entityBender = new DefaultEntityBender<T>(modId, null, null, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, supportedAnimations, alterableParts);
        return registerEntity(entityBender);
    }

    public <T extends LivingEntity> String registerEntity(EntityBender<T> entityBender)
    {
        String key = entityBender.getKey();
        if (!key.startsWith(this.modId))
        {
            throw new IllegalArgumentException("The EntityBender's ModID does not match that of the AddonAnimationRegistry.");
        }
        EntityBenderRegistry.instance.registerBender(entityBender);
        return entityBender.getKey();
    }

    public <T extends TriggerConditionTemplate> void registerTriggerCondition(String key, ITriggerConditionFactory<?, T> factory, Class<T> templateType)
    {
        TriggerConditionRegistry.instance.register(String.format("%s:%s", modId, key), factory, templateType);
    }

    public void registerTriggerCondition(String key, ITriggerCondition condition)
    {
        TriggerConditionRegistry.instance.register(String.format("%s:%s", modId, key), condition);
    }

    public void registerAnimationEditor(IAnimationEditor editor)
    {
        AnimationEditorRegistry.INSTANCE.registerEditor(editor);
    }

}
