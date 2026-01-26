package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.rendering.IArmorColorProvider;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Context object passed through the armor render pipeline.
 * Contains all information needed to render armor with Mo'Bends transforms.
 * Immutable - created once per render call.
 */
public class ArmorRenderContext<E extends LivingEntity>
{
    private final E entity;
    private final BipedEntityData<?> entityData;
    private final EquipmentSlot slot;
    private final ItemStack armorStack;
    private final PoseStack poseStack;
    private final MultiBufferSource bufferSource;
    private final int packedLight;
    private final int packedOverlay;
    private final float partialTicks;
    @Nullable
    private final HumanoidModel<?> armorModel;
    @Nullable
    private RenderTier determinedTier;

    // Cached entity properties
    private final boolean isBaby;
    private final boolean isSlimArms;

    private ArmorRenderContext(Builder<E> builder)
    {
        this.entity = builder.entity;
        this.entityData = builder.entityData;
        this.slot = builder.slot;
        this.armorStack = builder.armorStack;
        this.poseStack = builder.poseStack;
        this.bufferSource = builder.bufferSource;
        this.packedLight = builder.packedLight;
        this.packedOverlay = builder.packedOverlay;
        this.partialTicks = builder.partialTicks;
        this.armorModel = builder.armorModel;
        this.determinedTier = builder.determinedTier;

        // Cache entity properties for efficient access
        this.isBaby = builder.entity != null && builder.entity.isBaby();
        this.isSlimArms = detectSlimArms(builder.entity);
    }

    /**
     * Detect if the entity uses slim arm model.
     * For players, this is determined by the skin model name.
     */
    private static <E extends LivingEntity> boolean detectSlimArms(E entity)
    {
        if (entity instanceof net.minecraft.client.player.AbstractClientPlayer player)
        {
            IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
            return skinProvider != null && skinProvider.isSlimModel(player);
        }
        return false;
    }

    public E getEntity()
    {
        return entity;
    }

    public BipedEntityData<?> getEntityData()
    {
        return entityData;
    }

    public EquipmentSlot getSlot()
    {
        return slot;
    }

    public ItemStack getArmorStack()
    {
        return armorStack;
    }

    public PoseStack getPoseStack()
    {
        return poseStack;
    }

    public MultiBufferSource getBufferSource()
    {
        return bufferSource;
    }

    public int getPackedLight()
    {
        return packedLight;
    }

    public int getPackedOverlay()
    {
        return packedOverlay;
    }

    public float getPartialTicks()
    {
        return partialTicks;
    }

    @Nullable
    public HumanoidModel<?> getArmorModel()
    {
        return armorModel;
    }

    @Nullable
    public RenderTier getDeterminedTier()
    {
        return determinedTier;
    }

    /**
     * Returns true if the entity is a baby (uses 0.5 scale).
     */
    public boolean isBaby()
    {
        return isBaby;
    }

    /**
     * Returns true if the entity uses slim arm model.
     */
    public boolean isSlimArms()
    {
        return isSlimArms;
    }

    /**
     * Returns the scale factor for baby entities.
     * @return 0.5 for babies, 1.0 for adults
     */
    public float getEntityScale()
    {
        return isBaby ? 0.5f : 1.0f;
    }

    /**
     * Returns true if this is rendering a limb slot (arms or legs).
     * Limb slots require special handling for joint splitting.
     */
    public boolean isLimbSlot()
    {
        return slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    /**
     * Returns true if this is rendering an arm slot.
     * Arms need elbow joint handling.
     */
    public boolean isArmSlot()
    {
        return slot == EquipmentSlot.CHEST; // Chest slot includes arms
    }

    /**
     * Default leather armor color (brownish/tan) - used when no dye is applied.
     * RGB: (160, 101, 64) = 0xA06540 = 10511680
     */
    private static final int DEFAULT_LEATHER_COLOR = 0xFFA06540;

    /**
     * Get the armor color as an ARGB int for rendering.
     * Returns the dyed color for leather armor, the default leather color for undyed leather,
     * or 0xFFFFFFFF (white) for other armor types.
     * The returned value has alpha in the high byte (ARGB format).
     */
    public int getArmorColor()
    {
        if (armorStack == null || armorStack.isEmpty())
        {
            return 0xFFFFFFFF;
        }

        IArmorColorProvider colorProvider = IArmorColorProvider.Holder.getProvider();
        if (colorProvider != null)
        {
            int dyedColor = colorProvider.getDyedColor(armorStack);
            if (dyedColor != -1)
            {
                // getDyedColor returns RGB, we need to add full alpha
                return 0xFF000000 | dyedColor;
            }

            // Check if this is a dyeable item (leather armor) without a dye applied
            // Return the default leather color instead of white
            if (colorProvider.isDyeable(armorStack))
            {
                return DEFAULT_LEATHER_COLOR;
            }
        }

        return 0xFFFFFFFF;
    }

    /**
     * Returns true if this armor has a custom dye color (leather armor).
     */
    public boolean hasDyedColor()
    {
        if (armorStack == null)
        {
            return false;
        }
        IArmorColorProvider colorProvider = IArmorColorProvider.Holder.getProvider();
        return colorProvider != null && colorProvider.hasDyedColor(armorStack);
    }

    public static <E extends LivingEntity> Builder<E> builder()
    {
        return new Builder<>();
    }

    public static class Builder<E extends LivingEntity>
    {
        private E entity;
        private BipedEntityData<?> entityData;
        private EquipmentSlot slot;
        private ItemStack armorStack;
        private PoseStack poseStack;
        private MultiBufferSource bufferSource;
        private int packedLight;
        private int packedOverlay;
        private float partialTicks;
        private HumanoidModel<?> armorModel;
        private RenderTier determinedTier;

        public Builder<E> entity(E entity)
        {
            this.entity = entity;
            return this;
        }

        public Builder<E> entityData(BipedEntityData<?> entityData)
        {
            this.entityData = entityData;
            return this;
        }

        public Builder<E> slot(EquipmentSlot slot)
        {
            this.slot = slot;
            return this;
        }

        public Builder<E> armorStack(ItemStack armorStack)
        {
            this.armorStack = armorStack;
            return this;
        }

        public Builder<E> poseStack(PoseStack poseStack)
        {
            this.poseStack = poseStack;
            return this;
        }

        public Builder<E> bufferSource(MultiBufferSource bufferSource)
        {
            this.bufferSource = bufferSource;
            return this;
        }

        public Builder<E> packedLight(int packedLight)
        {
            this.packedLight = packedLight;
            return this;
        }

        public Builder<E> packedOverlay(int packedOverlay)
        {
            this.packedOverlay = packedOverlay;
            return this;
        }

        public Builder<E> partialTicks(float partialTicks)
        {
            this.partialTicks = partialTicks;
            return this;
        }

        public Builder<E> armorModel(HumanoidModel<?> armorModel)
        {
            this.armorModel = armorModel;
            return this;
        }

        public Builder<E> determinedTier(RenderTier determinedTier)
        {
            this.determinedTier = determinedTier;
            return this;
        }

        public ArmorRenderContext<E> build()
        {
            return new ArmorRenderContext<>(this);
        }
    }
}
