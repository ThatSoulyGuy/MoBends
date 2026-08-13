package goblinbob.mobends.standard;

import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.standard.client.model.armor.ArmorModelFactory;
import goblinbob.mobends.standard.client.renderer.entity.ArrowTrailManager;
import goblinbob.mobends.standard.client.renderer.entity.mutated.*;
import goblinbob.mobends.standard.data.*;
import goblinbob.mobends.standard.kumo.WolfStateCondition;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.mutators.*;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import goblinbob.mobends.standard.previewer.SpiderPreviewer;
import goblinbob.mobends.standard.previewer.SquidPreviewer;
import goblinbob.mobends.standard.previewer.WolfPreviewer;
import goblinbob.mobends.standard.previewer.ZombiePreviewer;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;

public class DefaultAddon implements IAddon
{
	protected static final String[] BIPED_ANIMATIONS = {"walk", "jump", "fall"};
	private static final String[] SPIDER_ANIMATIONS = {"walk", "jump"};
	private static final String[] SQUID_ANIMATIONS = {"swim"};
	private static final String[] WOLF_ANIMATIONS = {"walk", "sit"};

	protected static final String[] BIPED_PARTS = {
			"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
			"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"};

	protected static final float STRAY_CLOTHING_DEFORMATION = 0.25F;
	protected static final float BOGGED_CLOTHING_DEFORMATION = 0.2F;

	@Override
	public void registerContent(AddonAnimationRegistry registry)
	{
		registry.registerEntity(new PlayerBender());

		registry.registerNewEntity(Zombie.class, ZombieData::new, ZombieMutator::new, new ZombieRenderer<>(),
				new ZombiePreviewer(), BIPED_ANIMATIONS,
				"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
				"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg");

		registry.registerNewEntity(Skeleton.class, SkeletonData::new, SkeletonMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(Stray.class, SkeletonData::new,
				dataFactory -> new SkeletonMutator<>(dataFactory, STRAY_CLOTHING_DEFORMATION), new BipedRenderer<>(),
				new BipedPreviewer<>(), BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(ZombifiedPiglin.class, PigZombieData::new, PigZombieMutator::new, new ZombieRenderer<>(),
				new BipedPreviewer<>(), BIPED_ANIMATIONS,
				"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
				"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg");

		registry.registerNewEntity(Spider.class, SpiderData::new, SpiderMutator::new, new SpiderRenderer<>(),
				new SpiderPreviewer(), SPIDER_ANIMATIONS,
				"head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7", "leg8",
				"foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6", "foreLeg7", "foreLeg8");

		registry.registerNewEntity(Squid.class, SquidData::new, SquidMutator::new, new SquidRenderer<>(),
				new SquidPreviewer(), SQUID_ANIMATIONS,
				"body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6", "tentacle7", "tentacle8");

		registry.registerNewEntity(Wolf.class, WolfData::new, WolfMutator::new, new WolfRenderer<>(),
				new WolfPreviewer(), WOLF_ANIMATIONS,
				"head", "body", "mane", "tail", "leg1", "leg2", "leg3", "leg4",
				"foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4",
				"nose", "mouth", "tongue", "leftEar", "rightEar");

		registry.registerTriggerCondition("wolf_state", WolfStateCondition::new, WolfStateCondition.Template.class);

		registerVersionSpecificContent(registry);
	}

	protected void registerVersionSpecificContent(AddonAnimationRegistry registry)
	{
	}

	@Override
	public void onRenderTick(float partialTicks)
	{
		if (ModConfig.showArrowTrails)
			ArrowTrailManager.onRenderTick();
		PlayerPreviewer.updatePreviewData(partialTicks);
	}

	@Override
	public void onClientTick()
	{
		PlayerPreviewer.updatePreviewDataClient();
	}

	@Override
	public void onRefresh()
	{
		ArmorModelFactory.refresh();
	}

	@Override
	public String getDisplayName()
	{
		return "Default";
	}
}
