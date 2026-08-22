package goblinbob.mobends.standard.previewer;

import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class PiglinPreviewer<D extends BipedEntityData<? extends AbstractPiglin>> extends BipedPreviewer<D>
{
    @Override
    public void prePreview(D data, String animationToPreview)
    {
        final AbstractPiglin piglin = data.getEntity();
        if (piglin != null)
        {
            piglin.setImmuneToZombification(true);
        }

        super.prePreview(data, animationToPreview);
    }
}
