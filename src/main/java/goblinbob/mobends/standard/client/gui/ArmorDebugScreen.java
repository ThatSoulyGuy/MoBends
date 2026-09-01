package goblinbob.mobends.standard.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.ScreenHelper;
import goblinbob.mobends.standard.client.model.armor.ArmorDebugRenderer;
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

public class ArmorDebugScreen extends Screen
{
    private static boolean showOriginalVertices = true;
    private static boolean showTransformedVertices = true;
    private static boolean showBoneRegions = true;
    private static boolean showBoneAxes = true;
    private static boolean showTestGeometry = false;
    private static boolean freezeAnimation = false;
    private static float debugScale = 50.0f;
    private static float rotationY = 0.0f;
    private static float rotationX = 20.0f;

    private static final Map<BoneRegion, Boolean> boneEnabled = new EnumMap<>(BoneRegion.class);
    static {
        for (BoneRegion region : BoneRegion.values()) {
            boneEnabled.put(region, true);
        }
    }

    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    public ArmorDebugScreen()
    {
        super(Component.literal("Armor Debug"));
    }

    @Override
    protected void init()
    {
        super.init();

        int buttonWidth = 120;
        int buttonHeight = 20;
        int x = 10;
        int y = 30;
        int spacing = 22;

        addRenderableWidget(Button.builder(
                Component.literal("Original: " + (showOriginalVertices ? "ON" : "OFF")),
                btn -> {
                    showOriginalVertices = !showOriginalVertices;
                    btn.setMessage(Component.literal("Original: " + (showOriginalVertices ? "ON" : "OFF")));
                })
                .pos(x, y)
                .size(buttonWidth, buttonHeight)
                .build());
        y += spacing;

        addRenderableWidget(Button.builder(
                Component.literal("Transformed: " + (showTransformedVertices ? "ON" : "OFF")),
                btn -> {
                    showTransformedVertices = !showTransformedVertices;
                    btn.setMessage(Component.literal("Transformed: " + (showTransformedVertices ? "ON" : "OFF")));
                })
                .pos(x, y)
                .size(buttonWidth, buttonHeight)
                .build());
        y += spacing;

        addRenderableWidget(Button.builder(
                Component.literal("Bone Regions: " + (showBoneRegions ? "ON" : "OFF")),
                btn -> {
                    showBoneRegions = !showBoneRegions;
                    btn.setMessage(Component.literal("Bone Regions: " + (showBoneRegions ? "ON" : "OFF")));
                })
                .pos(x, y)
                .size(buttonWidth, buttonHeight)
                .build());
        y += spacing;

        addRenderableWidget(Button.builder(
                Component.literal("Bone Axes: " + (showBoneAxes ? "ON" : "OFF")),
                btn -> {
                    showBoneAxes = !showBoneAxes;
                    btn.setMessage(Component.literal("Bone Axes: " + (showBoneAxes ? "ON" : "OFF")));
                })
                .pos(x, y)
                .size(buttonWidth, buttonHeight)
                .build());
        y += spacing;

        addRenderableWidget(Button.builder(
                Component.literal("Freeze: " + (freezeAnimation ? "ON" : "OFF")),
                btn -> {
                    freezeAnimation = !freezeAnimation;
                    btn.setMessage(Component.literal("Freeze: " + (freezeAnimation ? "ON" : "OFF")));
                })
                .pos(x, y)
                .size(buttonWidth, buttonHeight)
                .build());
        y += spacing;

        addRenderableWidget(Button.builder(
                Component.literal("Test Geo: " + (showTestGeometry ? "ON" : "OFF")),
                btn -> {
                    showTestGeometry = !showTestGeometry;
                    btn.setMessage(Component.literal("Test Geo: " + (showTestGeometry ? "ON" : "OFF")));
                })
                .pos(x, y)
                .size(buttonWidth, buttonHeight)
                .build());
        y += spacing;

        addRenderableWidget(Button.builder(
                Component.literal("Scale -"),
                btn -> debugScale = Math.max(10, debugScale - 10))
                .pos(x, y)
                .size(55, buttonHeight)
                .build());

        addRenderableWidget(Button.builder(
                Component.literal("Scale +"),
                btn -> debugScale = Math.min(200, debugScale + 10))
                .pos(x + 60, y)
                .size(55, buttonHeight)
                .build());
        y += spacing;

        addRenderableWidget(Button.builder(
                Component.literal("Reset View"),
                btn -> {
                    rotationX = 20.0f;
                    rotationY = 0.0f;
                    debugScale = 50.0f;
                })
                .pos(x, y)
                .size(buttonWidth, buttonHeight)
                .build());
        y += spacing + 10;

        BoneRegion[] regions = BoneRegion.values();
        for (int i = 0; i < regions.length; i++) {
            BoneRegion region = regions[i];
            int finalY = y;
            addRenderableWidget(Button.builder(
                    Component.literal(getShortBoneName(region) + ": " + (boneEnabled.get(region) ? "ON" : "OFF")),
                    btn -> {
                        boneEnabled.put(region, !boneEnabled.get(region));
                        btn.setMessage(Component.literal(getShortBoneName(region) + ": " + (boneEnabled.get(region) ? "ON" : "OFF")));
                    })
                    .pos(x, finalY)
                    .size(buttonWidth, buttonHeight)
                    .build());
            y += spacing;
        }
    }

    private String getShortBoneName(BoneRegion region) {
        switch (region) {
            case HEAD: return "Head";
            case BODY: return "Body";
            case LEFT_ARM_UPPER: return "L.Arm Up";
            case LEFT_ARM_LOWER: return "L.Arm Low";
            case RIGHT_ARM_UPPER: return "R.Arm Up";
            case RIGHT_ARM_LOWER: return "R.Arm Low";
            case LEFT_LEG_UPPER: return "L.Leg Up";
            case LEFT_LEG_LOWER: return "L.Leg Low";
            case RIGHT_LEG_UPPER: return "R.Leg Up";
            case RIGHT_LEG_LOWER: return "R.Leg Low";
            case ROOT: return "Root";
            default: return region.name();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        ScreenHelper.renderBackground(this, guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(font, "Armor Debug - Drag to rotate", width / 2, 10, 0xFFFFFF);

        int previewX = width / 2 + 50;
        int previewY = height / 2 + 50;

        renderArmorPreview(guiGraphics, previewX, previewY, debugScale, partialTicks);

        int infoX = width - 200;
        int infoY = 30;
        guiGraphics.drawString(font, "Scale: " + (int)debugScale, infoX, infoY, 0xAAAAAA);
        infoY += 12;
        guiGraphics.drawString(font, "Rotation Y: " + (int)rotationY, infoX, infoY, 0xAAAAAA);
        infoY += 12;
        guiGraphics.drawString(font, "Rotation X: " + (int)rotationX, infoX, infoY, 0xAAAAAA);
        infoY += 12;

        int vertexCount = ArmorDebugRenderer.getCapturedVertexCount();
        int vertexColor = vertexCount > 0 ? 0x00FF00 : 0xFF0000;
        guiGraphics.drawString(font, "Vertices: " + vertexCount, infoX, infoY, vertexColor);
        infoY += 12;

        String armorInfo = ArmorDebugRenderer.getArmorSlotInfo();
        guiGraphics.drawString(font, "Armor: " + armorInfo, infoX, infoY, 0xAAAAAA);
        infoY += 12;

        String captureStatus = ArmorDebugRenderer.getCaptureStatus();
        guiGraphics.drawString(font, captureStatus, infoX, infoY, 0xAAAAAA);
        infoY += 20;

        guiGraphics.drawString(font, "Legend:", infoX, infoY, 0xFFFFFF);
        infoY += 12;
        guiGraphics.drawString(font, "Green = Original", infoX, infoY, 0x00FF00);
        infoY += 12;
        guiGraphics.drawString(font, "Red = Transformed", infoX, infoY, 0xFF0000);
        infoY += 12;
        guiGraphics.drawString(font, "Blue boxes = Bone regions", infoX, infoY, 0x4444FF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderArmorPreview(GuiGraphics guiGraphics, int x, int y, float scale, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        EntityData<?> entityData = EntityDatabase.instance.get(player);
        if (!(entityData instanceof BipedEntityData<?>)) return;

        BipedEntityData<?> data = (BipedEntityData<?>) entityData;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(x, y, 100);
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(rotationX)));
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(rotationY)));

        poseStack.translate(0, -0.5f, 0);

        RenderSystem.enableDepthTest();

        ArmorDebugRenderer.renderDebug(
                poseStack,
                data,
                showOriginalVertices,
                showTransformedVertices,
                showBoneRegions,
                showBoneAxes,
                showTestGeometry,
                boneEnabled
        );

        RenderSystem.disableDepthTest();

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button == 0 && mouseX > 140) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (dragging) {
            rotationY += (float) (mouseX - lastMouseX) * 0.5f;
            rotationX += (float) (mouseY - lastMouseY) * 0.5f;
            rotationX = Math.max(-90, Math.min(90, rotationX));
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        return handleMouseScrolled(mouseX, mouseY, scrollY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        return handleMouseScrolled(mouseX, mouseY, scrollY);
    }

    private boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        debugScale += (float) scrollY * 5;
        debugScale = Math.max(10, Math.min(200, debugScale));
        return true;
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    protected void renderBlurredBackground(float partialTick)
    {
    }

    public static boolean isFreezeAnimation() {
        return freezeAnimation;
    }

    public static boolean isBoneEnabled(BoneRegion region) {
        return boneEnabled.getOrDefault(region, true);
    }

    public static boolean isShowTestGeometry() {
        return showTestGeometry;
    }
}
