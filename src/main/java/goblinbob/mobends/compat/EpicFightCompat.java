package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomBipedArmor;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomCape;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomElytra;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomHeldItem;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomPlayerHeldItem;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EpicFightCompat
{
    private static final String MOD_ID = "epicfight";

    private static final String CLIENT_ENGINE = "yesman.epicfight.client.ClientEngine";

    private static final Map<Class<?>, Class<?>> LAYER_ALIASES = new LinkedHashMap<>();

    static
    {
        alias(LayerCustomPlayerHeldItem.class, PlayerItemInHandLayer.class);
        alias(LayerCustomHeldItem.class, ItemInHandLayer.class);
        alias(LayerCustomBipedArmor.class, HumanoidArmorLayer.class);
        alias(LayerCustomCape.class, CapeLayer.class);
        alias(LayerCustomElytra.class, ElytraLayer.class);
    }

    private static void alias(Class<?> replacement, Class<?> vanillaLayer)
    {
        if (vanillaLayer.isAssignableFrom(replacement))
        {
            LAYER_ALIASES.put(replacement, vanillaLayer);
        }
    }

    private static final Set<Object> aliasedRenderers =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Method clientEngineInstanceMethod;
    private static Field renderEngineField;
    private static Method getEntityRendererMethod;
    private static Method hasRendererForMethod;

    private static BipedMutator<?, ?, ?> suspendedMutator;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (!isLoaded)
        {
            return;
        }

        try
        {
            Class<?> clientEngineClass = Class.forName(CLIENT_ENGINE);

            clientEngineInstanceMethod = clientEngineClass.getMethod("getInstance");
            renderEngineField = clientEngineClass.getField("renderEngine");
            getEntityRendererMethod = renderEngineField.getType().getMethod("getEntityRenderer", Entity.class);
            hasRendererForMethod = renderEngineField.getType().getMethod("hasRendererFor", Entity.class);
        }
        catch (Throwable t)
        {
            clientEngineInstanceMethod = null;
            renderEngineField = null;
            getEntityRendererMethod = null;
            hasRendererForMethod = null;
            isLoaded = false;
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static void registerLayerAliases(LivingEntity entity)
    {
        if (!isModLoaded() || entity == null)
        {
            return;
        }

        try
        {
            Object clientEngine = clientEngineInstanceMethod.invoke(null);
            if (clientEngine == null)
            {
                return;
            }

            Object renderEngine = renderEngineField.get(clientEngine);
            if (renderEngine == null)
            {
                return;
            }

            Object patchedRenderer = getEntityRendererMethod.invoke(renderEngine, entity);
            if (patchedRenderer == null || !aliasedRenderers.add(patchedRenderer))
            {
                return;
            }

            Map<Class<?>, Object> patchedLayers = resolvePatchedLayers(patchedRenderer);
            if (patchedLayers == null)
            {
                return;
            }

            for (Map.Entry<Class<?>, Class<?>> alias : LAYER_ALIASES.entrySet())
            {
                Object patchedLayer = patchedLayers.get(alias.getValue());

                if (patchedLayer != null)
                {
                    patchedLayers.putIfAbsent(alias.getKey(), patchedLayer);
                }
            }
        }
        catch (Throwable ignored)
        {
        }
    }

    public static void suspendLayerSwap(LivingEntity entity, LivingEntityRenderer<?, ?> renderer)
    {
        if (!isModLoaded() || suspendedMutator != null || entity == null || renderer == null)
        {
            return;
        }

        if (!rendersWithArmature(entity))
        {
            return;
        }

        final EntityBender<?> bender = EntityBenderRegistry.instance.getForEntity(entity);
        if (bender == null)
        {
            return;
        }

        final Mutator<?, ?, ?> mutator = bender.getMutator(renderer);
        if (!(mutator instanceof BipedMutator<?, ?, ?> bipedMutator))
        {
            return;
        }

        bipedMutator.suspendLayerSwap();
        suspendedMutator = bipedMutator;
    }

    public static void resumeLayerSwap()
    {
        if (suspendedMutator == null)
        {
            return;
        }

        suspendedMutator.resumeLayerSwap();
        suspendedMutator = null;
    }

    private static boolean rendersWithArmature(LivingEntity entity)
    {
        try
        {
            Object clientEngine = clientEngineInstanceMethod.invoke(null);
            if (clientEngine == null)
            {
                return false;
            }

            Object renderEngine = renderEngineField.get(clientEngine);
            if (renderEngine == null)
            {
                return false;
            }

            return Boolean.TRUE.equals(hasRendererForMethod.invoke(renderEngine, entity));
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<?>, Object> resolvePatchedLayers(Object patchedRenderer)
    {
        for (Class<?> type = patchedRenderer.getClass(); type != null; type = type.getSuperclass())
        {
            try
            {
                Field field = type.getDeclaredField("patchedLayers");
                field.setAccessible(true);

                Object value = field.get(patchedRenderer);

                return value instanceof Map ? (Map<Class<?>, Object>) value : null;
            }
            catch (NoSuchFieldException ignored)
            {
            }
            catch (Throwable t)
            {
                return null;
            }
        }

        return null;
    }
}
