package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.elements.GuiSmallToggleButton;
import goblinbob.mobends.core.client.gui.elements.GuiTooltip;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * An expandable settings panel for entity benders.
 * Shows a master toggle when collapsed, and per-animation toggles when expanded.
 */
public class GuiExpandableBenderSettings implements IGuiListElement
{
    private static final int COLLAPSED_HEIGHT = 28;
    private static final int EXPANDED_HEADER_HEIGHT = 28;
    private static final int ANIMATION_ROW_HEIGHT = 22;
    private static final int BG_COLLAPSED = 0x40FFFFFF;
    private static final int BG_EXPANDED = 0x60FFFFFF;
    private static final int BG_HOVER = 0x50FFFFFF;
    private static final int ACCENT_ENABLED = 0xFF44AA44;
    private static final int ACCENT_DISABLED = 0xFF666666;

    private final EntityBender<?> bender;
    private final Minecraft mc;
    private final GuiSmallToggleButton masterToggle;
    private final List<AnimationToggle> animationToggles;
    private final Consumer<EntityBender<?>> onSelected;

    @Nullable
    private Consumer<String> onAnimationSelected;

    private int x, y;
    private int width;
    private int listOrder;
    private boolean expanded;
    private boolean hovered;
    private boolean selected;
    private float expandTween;
    private String selectedAnimationType = null;

    // Tooltip tracking
    @Nullable
    private AnimationToggle hoveredToggle;
    private long hoverStartTime;
    private int tooltipX, tooltipY;

    public GuiExpandableBenderSettings(EntityBender<?> bender, Consumer<EntityBender<?>> onSelected)
    {
        this.bender = bender;
        this.mc = Minecraft.getInstance();
        this.onSelected = onSelected;

        this.masterToggle = new GuiSmallToggleButton();
        this.masterToggle.setToggleState(bender.isAnimated());

        // Use animations supported by this specific bender
        this.animationToggles = new ArrayList<>();
        String[] supportedAnims = bender.getSupportedAnimations();
        for (String animType : supportedAnims)
        {
            animationToggles.add(new AnimationToggle(animType, true));
        }

        this.expanded = false;
        this.hovered = false;
        this.selected = false;
        this.expandTween = 0;
        this.width = 200; // Default width, will be updated
    }

    @Override
    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.masterToggle.initGui(x + width - 38, y + 4);
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    /**
     * Sets a callback that fires when an animation toggle is clicked.
     * The callback receives the animation type name (e.g., "walk", "sprint").
     */
    public void setOnAnimationSelected(Consumer<String> callback)
    {
        this.onAnimationSelected = callback;
    }

    /**
     * Gets the currently selected animation type, or null if none.
     */
    @Nullable
    public String getSelectedAnimationType()
    {
        return selectedAnimationType;
    }

    public boolean isSelected()
    {
        return selected;
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        // Check if clicked on master toggle
        if (masterToggle.mouseClicked(mouseX, mouseY, mouseButton))
        {
            bender.setAnimate(masterToggle.getToggleState());
            return true;
        }

        // Check if clicked on header (expand/collapse)
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + COLLAPSED_HEIGHT)
        {
            if (mouseButton == 0)
            {
                // Left click - select and expand
                if (onSelected != null)
                {
                    onSelected.accept(bender);
                }
                expanded = !expanded;
                return true;
            }
        }

        // Check animation toggles if expanded
        if (expanded)
        {
            int toggleY = y + EXPANDED_HEADER_HEIGHT;
            for (AnimationToggle toggle : animationToggles)
            {
                // Check if clicked anywhere in the row (to select animation for preview)
                if (mouseX >= x + 5 && mouseX < x + width - 5 &&
                    mouseY >= toggleY && mouseY < toggleY + ANIMATION_ROW_HEIGHT)
                {
                    // Check if clicked on the toggle button specifically
                    int toggleX = x + width - 38;
                    if (mouseX >= toggleX && mouseX < toggleX + 30)
                    {
                        toggle.enabled = !toggle.enabled;
                        // Update master toggle state based on any enabled
                        updateMasterToggleFromAnimations();
                    }

                    // Select this animation for preview regardless
                    selectedAnimationType = toggle.name;
                    if (onAnimationSelected != null)
                    {
                        onAnimationSelected.accept(toggle.name);
                    }
                    return true;
                }
                toggleY += ANIMATION_ROW_HEIGHT;
            }
        }

        return false;
    }

    public boolean handleMouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        // Nothing special to do on release
        return false;
    }

    private void updateMasterToggleFromAnimations()
    {
        boolean anyEnabled = animationToggles.stream().anyMatch(t -> t.enabled);
        masterToggle.setToggleState(anyEnabled);
        bender.setAnimate(anyEnabled);
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        masterToggle.update(mouseX, mouseY);

        // Update hover state
        hovered = mouseX >= x && mouseX < x + width &&
                  mouseY >= y && mouseY < y + getHeight();

        // Track which animation toggle is being hovered
        AnimationToggle previouslyHovered = hoveredToggle;
        hoveredToggle = null;

        if (expanded && expandTween > 0.5f)
        {
            int rowY = y + EXPANDED_HEADER_HEIGHT;
            for (AnimationToggle toggle : animationToggles)
            {
                // Check if mouse is over the animation row (excluding the toggle button area)
                if (mouseX >= x + 5 && mouseX < x + width - 40 &&
                    mouseY >= rowY && mouseY < rowY + ANIMATION_ROW_HEIGHT - 2)
                {
                    hoveredToggle = toggle;
                    tooltipX = mouseX + 10;
                    tooltipY = mouseY + 10;
                    break;
                }
                rowY += ANIMATION_ROW_HEIGHT;
            }
        }

        // Track hover start time for tooltip delay
        if (hoveredToggle != previouslyHovered)
        {
            hoverStartTime = hoveredToggle != null ? System.currentTimeMillis() : 0;
        }

        // Animate expansion
        if (expanded)
        {
            expandTween = Math.min(expandTween + 0.15f, 1.0f);
        }
        else
        {
            expandTween = Math.max(expandTween - 0.15f, 0.0f);
        }
    }

    @Override
    public void draw(GuiGraphics guiGraphics, float partialTicks)
    {
        // Background - use guiGraphics.fill() which respects PoseStack
        int bgColor = selected ? BG_EXPANDED : (hovered ? BG_HOVER : BG_COLLAPSED);
        guiGraphics.fill(x, y, x + width, y + getHeight(), bgColor);

        // Left accent bar
        int accentColor = bender.isAnimated() ? ACCENT_ENABLED : ACCENT_DISABLED;
        guiGraphics.fill(x, y, x + 3, y + getHeight(), accentColor);

        // Entity name
        String name = bender.getLocalizedName();
        guiGraphics.drawString(mc.font, name, x + 10, y + 10, 0xFFFFFF, true);

        // Expand indicator
        String expandIcon = expanded ? "\u25BC" : "\u25B6"; // Down/Right arrow
        guiGraphics.drawString(mc.font, expandIcon, x + width - 50, y + 10, 0x888888, false);

        // Master toggle
        masterToggle.draw(guiGraphics);

        // Draw animation toggles if expanded
        if (expandTween > 0)
        {
            drawAnimationToggles(guiGraphics);
        }

        // Queue tooltip if hovering long enough
        if (hoveredToggle != null && GuiTooltip.shouldShowTooltip(hoverStartTime, 400))
        {
            List<String> tooltipLines = getTooltipForAnimation(hoveredToggle.name);
            GuiTooltip.queueTooltip(tooltipLines, tooltipX, tooltipY);
        }
    }

    /**
     * Gets tooltip lines for an animation type.
     */
    private List<String> getTooltipForAnimation(String animationType)
    {
        String titleKey = "mobends.animation." + animationType;
        String descKey = "mobends.animation." + animationType + ".desc";

        String title = I18n.exists(titleKey) ? I18n.get(titleKey) : capitalizeFirst(animationType);
        String desc = I18n.exists(descKey) ? I18n.get(descKey) : getDefaultAnimationDescription(animationType);

        return Arrays.asList(title, desc);
    }

    /**
     * Gets a default description for animation types without localization.
     */
    private String getDefaultAnimationDescription(String animationType)
    {
        return switch (animationType) {
            case "walk" -> "Controls walking animation";
            case "sprint" -> "Controls sprinting animation";
            case "jump" -> "Controls jumping animation";
            case "fall" -> "Controls falling animation";
            case "sneak" -> "Controls sneaking animation";
            case "swim" -> "Controls swimming animation";
            case "attack" -> "Controls attack animation";
            case "use_item" -> "Controls item use animation";
            case "ride" -> "Controls riding animation";
            case "climb" -> "Controls climbing animation";
            default -> "Toggle this animation type";
        };
    }

    private void drawAnimationToggles(GuiGraphics guiGraphics)
    {
        int rowY = y + EXPANDED_HEADER_HEIGHT;
        float alpha = expandTween;

        for (AnimationToggle toggle : animationToggles)
        {
            // Row background - highlight selected animation
            boolean isSelected = toggle.name.equals(selectedAnimationType);
            int rowBg;
            if (isSelected)
            {
                rowBg = 0x40448888; // Blue highlight for selected
            }
            else if (toggle.enabled)
            {
                rowBg = 0x20448844; // Green tint for enabled
            }
            else
            {
                rowBg = 0x10FFFFFF; // Default subtle background
            }
            guiGraphics.fill(x + 5, rowY, x + width - 5, rowY + ANIMATION_ROW_HEIGHT - 2, rowBg);

            // Draw selection indicator on left side
            if (isSelected)
            {
                guiGraphics.fill(x + 5, rowY, x + 8, rowY + ANIMATION_ROW_HEIGHT - 2, 0xFF4488DD);
            }

            // Animation name
            String labelKey = "mobends.animation." + toggle.name;
            String label = I18n.exists(labelKey) ? I18n.get(labelKey) : capitalizeFirst(toggle.name);
            int textAlpha = (int) (255 * alpha);
            int textColor = (textAlpha << 24) | 0xCCCCCC;
            guiGraphics.drawString(mc.font, label, x + 15, rowY + 6, textColor, false);

            // Toggle indicator - use guiGraphics.fill() which respects PoseStack
            int indicatorX = x + width - 35;
            int indicatorY = rowY + 5;
            int indicatorColor = toggle.enabled ? ACCENT_ENABLED : 0xFF555555;
            guiGraphics.fill(indicatorX, indicatorY, indicatorX + 24, indicatorY + 12, indicatorColor);

            String toggleText = toggle.enabled ? "ON" : "OFF";
            int toggleTextColor = toggle.enabled ? 0xFFFFFF : 0x888888;
            guiGraphics.drawString(mc.font, toggleText, indicatorX + 5, indicatorY + 2, toggleTextColor, false);

            rowY += ANIMATION_ROW_HEIGHT;
        }
    }

    private String capitalizeFirst(String str)
    {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).replace("_", " ");
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public int getHeight()
    {
        if (expandTween <= 0)
        {
            return COLLAPSED_HEIGHT;
        }
        int expandedHeight = EXPANDED_HEADER_HEIGHT + (ANIMATION_ROW_HEIGHT * animationToggles.size());
        return (int) (COLLAPSED_HEIGHT + (expandedHeight - COLLAPSED_HEIGHT) * expandTween);
    }

    @Override
    public int getOrder()
    {
        return listOrder;
    }

    @Override
    public void setOrder(int order)
    {
        listOrder = order;
    }

    public EntityBender<?> getBender()
    {
        return bender;
    }

    /**
     * Internal class for animation toggles
     */
    private static class AnimationToggle
    {
        final String name;
        boolean enabled;

        AnimationToggle(String name, boolean enabled)
        {
            this.name = name;
            this.enabled = enabled;
        }
    }
}
