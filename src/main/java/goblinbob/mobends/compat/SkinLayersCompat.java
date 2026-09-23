package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.platform.Platform;
import goblinbob.mobends.compat.skinlayers.SkinLayersBridge;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public final class SkinLayersCompat
{
    private static final String MOD_ID = "skinlayers3d";
    private static final String LAYER_CLASS = "dev.tr7zw.skinlayers.renderlayers.CustomLayerFeatureRenderer";

    public static final int HEAD = 1;
    public static final int BODY = 2;
    public static final int LEFT_ARM = 4;
    public static final int RIGHT_ARM = 8;
    public static final int LEFT_LEG = 16;
    public static final int RIGHT_LEG = 32;

    private static Boolean loaded;

    private SkinLayersCompat()
    {
    }

    public static boolean isModLoaded()
    {
        if (loaded == null)
        {
            loaded = Platform.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    public static boolean isLayer(RenderLayer<?, ?> layer)
    {
        return layer != null && isModLoaded() && LAYER_CLASS.equals(layer.getClass().getName());
    }

    public static int activeParts(AbstractClientPlayer player, boolean slim)
    {
        if (player == null || !isModLoaded())
        {
            return 0;
        }

        try
        {
            return SkinLayersBridge.activeParts(player, slim);
        }
        catch (Throwable ignored)
        {
            return 0;
        }
    }

    public static void clearInjectedMeshes(PlayerModel<?> model, int parts)
    {
        if (model == null || parts == 0)
        {
            return;
        }

        try
        {
            SkinLayersBridge.clearInjectedMeshes(model, parts);
        }
        catch (Throwable ignored)
        {
        }
    }

    public static void render(PlayerMutator mutator, AbstractClientPlayer player, int parts, boolean slim,
                              PoseStack poseStack, VertexConsumer vertexConsumer,
                              int packedLight, int packedOverlay, int color)
    {
        if (parts == 0)
        {
            return;
        }

        try
        {
            SkinLayersBridge.render(mutator, player, parts, slim, poseStack, vertexConsumer,
                    packedLight, packedOverlay, color);
        }
        catch (Throwable ignored)
        {
        }
    }
}
