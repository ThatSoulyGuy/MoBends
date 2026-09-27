package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public class SwordBlockingCompat
{
    private static final String MOD_ID = "swordblockingmechanics";

    private static boolean initialized = false;
    private static Method isBlockingMethod = null;
    private static Method renderBlockingMethod = null;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            isBlockingMethod = Class.forName("fuzs.swordblockingmechanics.handler.SwordBlockingHandler")
                    .getMethod("isActiveItemStackBlocking", Player.class);
        }
        catch (Throwable e)
        {
            isBlockingMethod = null;
        }

        try
        {
            renderBlockingMethod = Class.forName("fuzs.swordblockingmechanics.client.helper.AdvancedBlockingRenderer")
                    .getMethod("renderBlockingWithSword", ItemInHandRenderer.class, ArmedModel.class,
                            LivingEntity.class, ItemStack.class, ItemDisplayContext.class, HumanoidArm.class,
                            PoseStack.class, MultiBufferSource.class, int.class);
        }
        catch (Throwable e)
        {
            renderBlockingMethod = null;
        }
    }

    public static boolean isBlocking(LivingEntity entity)
    {
        if (!initialized)
        {
            init();
        }

        if (isBlockingMethod == null || !(entity instanceof Player player))
        {
            return false;
        }

        try
        {
            return (boolean) isBlockingMethod.invoke(null, player);
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public static boolean renderBlockingSword(ArmedModel hand, LivingEntity entity, ItemStack itemStack,
                                              ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack,
                                              MultiBufferSource bufferSource, int packedLight)
    {
        if (itemStack != entity.getUseItem() || !isBlocking(entity) || renderBlockingMethod == null)
        {
            return false;
        }

        try
        {
            renderBlockingMethod.invoke(null, Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer(),
                    hand, entity, itemStack, displayContext, arm, poseStack, bufferSource, packedLight);
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }
}
