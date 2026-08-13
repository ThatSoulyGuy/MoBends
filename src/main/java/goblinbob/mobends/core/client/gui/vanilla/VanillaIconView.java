package goblinbob.mobends.core.client.gui.vanilla;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

public class VanillaIconView extends VanillaView
{
    private static final float IDLE_DEGREES_PER_SECOND = 12.0F;
    private static final float HOVER_DEGREES_PER_SECOND = 60.0F;

    private final ResourceLocation texture;
    private final int textureSize;
    private int iconSize = 32;

    private boolean spinning;
    private float angle;
    private long lastSpinTime;

    @Nullable
    private BooleanSupplier hoverSupplier;

    public VanillaIconView(ResourceLocation texture, int textureSize)
    {
        this.texture = texture;
        this.textureSize = textureSize;
    }

    public void setIconSize(int iconSize)
    {
        this.iconSize = iconSize;
    }

    public void setSpinning(boolean spinning)
    {
        this.spinning = spinning;
    }

    public void setHoverSupplier(@Nullable BooleanSupplier hoverSupplier)
    {
        this.hoverSupplier = hoverSupplier;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (backgroundColor != 0)
        {
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, backgroundColor);
        }

        int size = Math.min(iconSize, Math.min(measuredWidth, measuredHeight));
        int drawX = x + (measuredWidth - size) / 2;
        int drawY = y + (measuredHeight - size) / 2;

        smoothTexture();
        advanceSpin();

        guiGraphics.pose().pushPose();

        if (spinning)
        {
            float centerX = drawX + size / 2.0F;
            float centerY = drawY + size / 2.0F;

            guiGraphics.pose().translate(centerX, centerY, 0.0F);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(angle));
            guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
        }

        guiGraphics.blit(texture, drawX, drawY, size, size,
                0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);

        guiGraphics.pose().popPose();
    }

    private void advanceSpin()
    {
        if (!spinning) return;

        long now = System.currentTimeMillis();

        if (lastSpinTime != 0L)
        {
            boolean hovered = hoverSupplier != null && hoverSupplier.getAsBoolean();
            float speed = hovered ? HOVER_DEGREES_PER_SECOND : IDLE_DEGREES_PER_SECOND;

            angle = (angle + (now - lastSpinTime) / 1000.0F * speed) % 360.0F;
        }

        lastSpinTime = now;
    }

    private void smoothTexture()
    {
        var abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
        if (abstractTexture != null)
        {
            abstractTexture.setFilter(true, false);
        }
    }

    @Override
    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(iconSize, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(iconSize, minHeight));
    }
}
