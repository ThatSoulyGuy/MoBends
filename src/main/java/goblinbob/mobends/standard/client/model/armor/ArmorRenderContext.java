package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Context object passed through the armor render pipeline.
 * Contains all information needed to render armor with Mo'Bends transforms.
 * Immutable - created once per render call.
 */
@OnlyIn(Dist.CLIENT)
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

    // Armor color in packed ARGB format (0xAARRGGBB)
    // Default is 0xFFFFFFFF (white, fully opaque = no tint)
    private final int armorColor;

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
        this.armorColor = builder.armorColor;

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
            return "slim".equals(player.getModelName());
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
     * Get the armor color in packed ARGB format (0xAARRGGBB).
     * Default is 0xFFFFFFFF (white, fully opaque = no tint).
     * Used for dyeable armor like leather.
     */
    public int getArmorColor()
    {
        return armorColor;
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
        private int armorColor = 0xFFFFFFFF;  // Default: white, fully opaque (no tint)

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

        /**
         * Set the armor color in packed ARGB format (0xAARRGGBB).
         * Default is 0xFFFFFFFF (white, fully opaque = no tint).
         * Used for dyeable armor like leather.
         */
        public Builder<E> armorColor(int armorColor)
        {
            this.armorColor = armorColor;
            return this;
        }

        /**
         * Set the armor color from RGB float components (0.0-1.0).
         * Alpha is set to 1.0 (fully opaque).
         * Convenience method for converting from Minecraft's color format.
         */
        public Builder<E> armorColor(float red, float green, float blue)
        {
            int r = (int)(red * 255.0F) & 0xFF;
            int g = (int)(green * 255.0F) & 0xFF;
            int b = (int)(blue * 255.0F) & 0xFF;
            this.armorColor = 0xFF000000 | (r << 16) | (g << 8) | b;
            return this;
        }

        public ArmorRenderContext<E> build()
        {
            return new ArmorRenderContext<>(this);
        }
    }
}
