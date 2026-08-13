package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

@Deprecated
public interface IPartWrapper
{
    void apply(ArmorWrapper armorWrapper);
    void deapply(ArmorWrapper armorWrapper);
    void syncUp(BipedEntityData<?> data);

    IPartWrapper offsetInner(float x, float y, float z);
    IPartWrapper setParent(IModelPart parent);

    @FunctionalInterface
    interface DataPartSelector
    {
        ModelPartTransform selectPart(BipedEntityData<?> data);
    }

    @FunctionalInterface
    interface ModelPartSetter
    {
        void replacePart(HumanoidModel<?> model, ModelPart part);
    }
}
