package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.rendering.IArmorColorProvider;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ArmorRenderContext<E extends LivingEntity>
{
    private final BipedEntityData<?> entityData;
    private final EquipmentSlot slot;
    private final ItemStack armorStack;
    private final PoseStack poseStack;
    private final MultiBufferSource bufferSource;
    private final int packedLight;
    private final int packedOverlay;

    private final boolean isSlimArms;

    @Nullable
    private final Integer colorOverride;

    public ArmorRenderContext(
            E entity,
            BipedEntityData<?> entityData,
            EquipmentSlot slot,
            ItemStack armorStack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            @Nullable Integer colorOverride)
    {
        this.entityData = entityData;
        this.slot = slot;
        this.armorStack = armorStack;
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
        this.packedLight = packedLight;
        this.packedOverlay = packedOverlay;

        this.isSlimArms = detectSlimArms(entity);
        this.colorOverride = colorOverride;
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

    public BipedEntityData<?> getEntityData()
    {
        return entityData;
    }

    public EquipmentSlot getSlot()
    {
        return slot;
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

    public boolean isSlimArms()
    {
        return isSlimArms;
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
}
