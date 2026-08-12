package goblinbob.mobends.neoforge;

import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.DefaultAddon;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.SkeletonData;
import goblinbob.mobends.standard.mutators.SkeletonMutator;
import net.minecraft.world.entity.monster.Bogged;

public class NeoForgeAddon extends DefaultAddon
{
    @Override
    protected void registerVersionSpecificContent(AddonAnimationRegistry registry)
    {
        registry.registerNewEntity(Bogged.class, SkeletonData::new,
                dataFactory -> new SkeletonMutator<>(dataFactory, BOGGED_CLOTHING_DEFORMATION), new BipedRenderer<>(),
                null, BIPED_ANIMATIONS, BIPED_PARTS);
    }
}
