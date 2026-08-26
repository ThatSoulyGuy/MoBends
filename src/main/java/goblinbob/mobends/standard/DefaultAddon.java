package goblinbob.mobends.standard;

import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.api.addon.IAddon;
import goblinbob.mobends.standard.kumo.EquipmentNameCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.standard.client.renderer.entity.ArrowTrailManager;
import goblinbob.mobends.standard.client.renderer.entity.mutated.*;
import goblinbob.mobends.standard.data.*;
import goblinbob.mobends.standard.kumo.WolfStateCondition;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.mutators.*;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import goblinbob.mobends.standard.previewer.PiglinPreviewer;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import goblinbob.mobends.standard.previewer.SpiderPreviewer;
import goblinbob.mobends.standard.previewer.SquidPreviewer;
import goblinbob.mobends.standard.previewer.WolfPreviewer;
import goblinbob.mobends.standard.previewer.ZombiePreviewer;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;

public class DefaultAddon implements IAddon
{
	protected static final String[] BIPED_ANIMATIONS = {"walk", "jump", "fall", "attack", "ride"};
	protected static final String[] SPRINTING_BIPED_ANIMATIONS = {"walk", "sprint", "jump", "fall", "attack", "ride"};
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

		// Registered explicitly rather than left to BenderDiscovery's derived-bender scan, which
		// only runs once the player opens the mob-selection screen. ZombieVillagerData/Mutator/
		// Controller have always existed; they were simply never wired up here.
		registry.registerNewEntity(ZombieVillager.class, ZombieVillagerData::new, ZombieVillagerMutator::new,
				new ZombieRenderer<>(), new BipedPreviewer<>(), BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(Skeleton.class, SkeletonData::new, SkeletonMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(WitherSkeleton.class, SkeletonData::new, SkeletonMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(Stray.class, SkeletonData::new,
				dataFactory -> new SkeletonMutator<>(dataFactory, STRAY_CLOTHING_DEFORMATION), new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(ZombifiedPiglin.class, PigZombieData::new, PigZombieMutator::new, new ZombieRenderer<>(),
				new BipedPreviewer<>(), BIPED_ANIMATIONS,
				"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
				"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg");

		registry.registerNewEntity(Piglin.class, PiglinData::new, PiglinMutator::new, new BipedRenderer<>(),
				new PiglinPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(PiglinBrute.class, PiglinData::new, PiglinMutator::new, new BipedRenderer<>(),
				new PiglinPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(Pillager.class, IllagerData::new, IllagerMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(Vindicator.class, IllagerData::new, IllagerMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(Evoker.class, IllagerData::new, IllagerMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(Illusioner.class, IllagerData::new, IllagerMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(net.minecraft.world.entity.npc.Villager.class,
				VillagerData::new, VillagerMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(net.minecraft.world.entity.npc.WanderingTrader.class,
				VillagerData::new, VillagerMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		registry.registerNewEntity(net.minecraft.world.entity.monster.Witch.class,
				WitchData::new, WitchMutator::new, new BipedRenderer<>(),
				new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

		final Class<net.minecraft.world.entity.LivingEntity> guardClass =
				goblinbob.mobends.compat.GuardVillagersCompat.getEntityClass();
		if (guardClass != null)
		{
			registry.registerNewEntity(guardClass, HumanoidMobData::new, HumanoidMobMutator::new,
					new BipedRenderer<>(), new BipedPreviewer<>(), SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);
		}

		goblinbob.mobends.compat.VampirismCompat.register(registry, SPRINTING_BIPED_ANIMATIONS, BIPED_PARTS);

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

		// Registered directly rather than through registry.registerTriggerCondition, which would
		// namespace it as "mobends:equipment_name" and break every pack that already uses
		// "core:equipment_name". It lives here rather than in the core module because it reads
		// item stacks out of equipment slots.
		TriggerConditionRegistry.instance.register("core:equipment_name",
				EquipmentNameCondition::new, EquipmentNameCondition.Template.class);

		registerVersionSpecificContent(registry);
	}

	protected void registerVersionSpecificContent(AddonAnimationRegistry registry)
	{
	}

	@Override
	public void onRenderTick(float partialTicks)
	{
		if (ModConfig.showArrowTrails || ModConfig.tridentTrail)
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
		// Nothing to do. This used to call ArmorModelFactory.refresh(), which demutated cached
		// armor wrappers -- but nothing ever populated those caches, so it looped over empty maps.
	}

	@Override
	public String getDisplayName()
	{
		return "Default";
	}
}
