package goblinbob.mobends.core.animation.layer;

import goblinbob.mobends.core.data.EntityData;

import java.util.Arrays;
import java.util.Collection;

public abstract class AnimationLayer<T extends EntityData<?>>
{
	public abstract String[] getActions(T entityData);

	public abstract void perform(T entityData);

	public void addActionsToList(T entityData, Collection<String> list) {
		String[] actions = getActions(entityData);
		if (actions != null)
			list.addAll(Arrays.asList(actions));
	}

	public void perform(T entityData, Collection<String> list) {
		this.perform(entityData);
		this.addActionsToList(entityData, list);
	}
}
