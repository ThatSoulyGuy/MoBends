package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.rendering.IArmorColorProvider;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

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
    private final Model armorModel;
    @Nullable
    private RenderTier determinedTier;

    private final boolean isBaby;
    private final boolean isSlimArms;

    @Nullable
    private final Integer colorOverride;

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

        this.isBaby = builder.entity != null && builder.entity.isBaby();
        this.isSlimArms = detectSlimArms(builder.entity);
        this.colorOverride = builder.colorOverride;
    }

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
    public Model getArmorModel()
    {
        return armorModel;
    }

    @Nullable
    public RenderTier getDeterminedTier()
    {
        return determinedTier;
    }

    public boolean isBaby()
    {
        return isBaby;
    }

    public boolean isSlimArms()
    {
        return isSlimArms;
    }

    public boolean isLimbSlot()
    {
        return slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    public boolean isArmSlot()
    {
        return slot == EquipmentSlot.CHEST;
    }

    private static final int DEFAULT_LEATHER_COLOR = 0xFFA06540;

    public int getArmorColor()
    {
        if (colorOverride != null)
        {
            return colorOverride;
        }

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
                return 0xFF000000 | dyedColor;
            }

            if (colorProvider.isDyeable(armorStack))
            {
                return DEFAULT_LEATHER_COLOR;
            }
        }

        return 0xFFFFFFFF;
    }

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
        private Model armorModel;
        private RenderTier determinedTier;
        private Integer colorOverride;

        public Builder<E> colorOverride(Integer colorOverride)
        {
            this.colorOverride = colorOverride;
            return this;
        }

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

        public Builder<E> armorModel(Model armorModel)
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
