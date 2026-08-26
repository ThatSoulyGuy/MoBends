package goblinbob.mobends.mixin;

import goblinbob.mobends.core.client.MoBendsRenderContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin
{
    @Inject(method = "renderEntityInInventory", at = @At("HEAD"), require = 0)
    private static void mobends$beginGuiEntityRender(GuiGraphics guiGraphics, float x, float y, float scale,
                                                     Vector3f translate, Quaternionf pose,
                                                     Quaternionf cameraOrientation, LivingEntity entity,
                                                     CallbackInfo ci)
    {
        MoBendsRenderContext.beginGuiEntityRender();
    }

    @Inject(method = "renderEntityInInventory", at = @At("RETURN"), require = 0)
    private static void mobends$endGuiEntityRender(GuiGraphics guiGraphics, float x, float y, float scale,
                                                   Vector3f translate, Quaternionf pose,
                                                   Quaternionf cameraOrientation, LivingEntity entity,
                                                   CallbackInfo ci)
    {
        MoBendsRenderContext.endGuiEntityRender();
    }
}
