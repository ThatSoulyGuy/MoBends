package goblinbob.mobends.core.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.CustomFont;
import goblinbob.mobends.core.client.gui.CustomFontRenderer;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.core.util.IColorRead;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GuiSectionButton
{

    public static final ResourceLocation BUTTONS_TEXTURE = ResourceLocationFactory.create(ModStatics.MODID,
            "textures/gui/buttons.png");

    // Expressed in ticks
    public static final float HOVER_ICON_ANIMATION_DURATION = 2.0F;
    public static final float HOVER_BG_ANIMATION_DURATION = 2.0F;

    protected final Minecraft mc;
    private String label;
    private int x,
                y;
    private int width,
                height;
    private int bgTextureU,
                bgTextureV;
    private Color neutralColor;
    private Color bgColor;
    private SectionIcon leftIcon = null;
    private SectionIcon rightIcon = null;
    private CustomFontRenderer fontRenderer;

    private boolean hover;
    private boolean pressed;
    private float ticksAfterHovered = 0.0F;

    public GuiSectionButton(int x, int y, String label, IColorRead bgColor)
    {
        this.mc = Minecraft.getInstance();
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = 318;
        this.height = 43;
        this.bgTextureU = 0;
        this.bgTextureV = 0;
        this.neutralColor = new Color(0xFF777777);
        this.bgColor = new Color(bgColor);
        this.hover = false;
        this.pressed = false;

        this.fontRenderer = new CustomFontRenderer();
        this.fontRenderer.setFont(CustomFont.BOLD);
    }

    public GuiSectionButton(String label, IColorRead bgColor)
    {
        this(0, 0, label, bgColor);
    }

    public GuiSectionButton(String label, int bgColor)
    {
        this(0, 0, label, Color.fromHex(bgColor));
    }

    public GuiSectionButton setLeftIcon(int u, int v, int width, int height)
    {
        this.leftIcon = new SectionIcon(u, v, width, height);
        return this;
    }

    public GuiSectionButton setRightIcon(int u, int v, int width, int height)
    {
        this.rightIcon = new SectionIcon(u, v, width, height);
        return this;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void update(int mouseX, int mouseY)
    {
        boolean nowHover = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y
                && mouseY < this.y + this.height;

        if (nowHover && !this.hover)
        {
            this.onHover();
        }

        this.hover = nowHover;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int event)
    {
        if (this.hover)
        {
            GuiHelper.playButtonSound(mc.getSoundManager());
            this.pressed = true;
        }

        return this.pressed;
    }

    public void mouseReleased(int mouseX, int mouseY, int event)
    {
        this.pressed = false;
    }

    public void onHover()
    {
        this.ticksAfterHovered = 0F;
    }

    public void display(GuiGraphics guiGraphics)
    {
        this.ticksAfterHovered += DataUpdateHandler.ticksPerFrame;

        if (this.hover)
            RenderSystem.setShaderColor(this.bgColor.r, this.bgColor.g, this.bgColor.b, this.bgColor.a);
        else
            RenderSystem.setShaderColor(this.neutralColor.r, this.neutralColor.g, this.neutralColor.b, this.neutralColor.a);
        RenderSystem.setShaderTexture(0, BUTTONS_TEXTURE);

        int tX = this.bgTextureU;
        int tY = this.bgTextureV;

        float uScale = 0.001953125F;
        float vScale = 0.0078125F;

        if (this.hover)
            RenderSystem.setShaderColor(this.bgColor.r, this.bgColor.g, this.bgColor.b, this.bgColor.a);
        else
            RenderSystem.setShaderColor(this.neutralColor.r, this.neutralColor.g, this.neutralColor.b, this.neutralColor.a);

        Draw.rectangle(x, y, width, height);

        float bgt = 1;
        if (this.hover)
        {
            if (this.ticksAfterHovered < HOVER_BG_ANIMATION_DURATION)
            {
                bgt = (1 - this.ticksAfterHovered / HOVER_BG_ANIMATION_DURATION);
                bgt = bgt * bgt * bgt;
            }
            else
            {
                bgt = 0;
            }
        }

        int mountainOffsetY = (int) (bgt * 10);

        RenderSystem.setShaderTexture(0, BUTTONS_TEXTURE);
        Draw.texturedRectangle(x, y + mountainOffsetY, width, height - 2 - mountainOffsetY, tX * uScale, tY * vScale, (tX + width) * uScale, (tY + height - 2 - mountainOffsetY) * vScale);
        // Bottom bar
        Draw.texturedRectangle(x, y + height - 2, width, 2, tX * uScale, (tY + height - 2) * vScale, (tX + width) * uScale, (tY + 2) * vScale);

        if (this.hover)
        {
            float scale = 1.0F;
            if (this.ticksAfterHovered < HOVER_ICON_ANIMATION_DURATION)
            {
                float PI = (float) Math.PI;
                float t = this.ticksAfterHovered / HOVER_ICON_ANIMATION_DURATION;
                scale = (1F - Mth.cos(t * PI * 1.5F));
                scale = Mth.sqrt(scale);
            }

            int iconSpacing = 30;

            if (leftIcon != null)
            {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(x + iconSpacing, y + height / 2F, 0);
                guiGraphics.pose().scale(scale, scale, 1);
                leftIcon.draw(guiGraphics, uScale, vScale);
                guiGraphics.pose().popPose();
            }

            if (rightIcon != null)
            {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(x + width - iconSpacing, y + height / 2F, 0);
                guiGraphics.pose().scale(scale, scale, 1);
                rightIcon.draw(guiGraphics, uScale, vScale);
                guiGraphics.pose().popPose();
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.fontRenderer.drawCenteredText(this.label, x + width / 2, y + height / 2 + 6);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void setPosition(int i, int j)
    {
        this.x = i;
        this.y = j;
    }

    private static class SectionIcon
    {

        private int texU;
        private int texV;
        private int texWidth;
        private int texHeight;

        public SectionIcon(int u, int v, int width, int height)
        {
            this.texU = u;
            this.texV = v;
            this.texWidth = width;
            this.texHeight = height;
        }

        public void draw(GuiGraphics guiGraphics, float uScale, float vScale)
        {
            RenderSystem.setShaderTexture(0, BUTTONS_TEXTURE);
            Draw.texturedRectangle(-texWidth / 2, -texHeight / 2,
                    texWidth, texHeight,
                    texU * uScale, texV * vScale,
                    texWidth * uScale, texHeight * vScale);
        }

    }

}
