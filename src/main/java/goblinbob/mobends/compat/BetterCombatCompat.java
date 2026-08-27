package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class BetterCombatCompat
{
    private static final String MOD_ID = "bettercombat";

    public enum Animations
    {
        BETTER_COMBAT_ONLY,
        BETTER_COMBAT_AND_MOBENDS,
        MOBENDS_FIRST_PERSON,
        NO_INTEGRATION
    }

    private static final float FIRST_PERSON_WINDOW = 10.0F;
    private static final int TRIGGER_PRIORITY = 2001;

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<?> attackStackClass;
    private static Class<?> poseStackClass;

    private static Field attackBaseField;
    private static Field attackMirrorField;
    private static Field poseBaseField;
    private static Field poseLastPoseField;

    private static Method getAnimationMethod;
    private static Method setAnimationMethod;
    private static Method isMirroredMethod;
    private static Method firstPersonPassMethod;
    private static Method animationIsActiveMethod;

    private static Class<?> animationInterface;
    private static Method getAnimationStackMethod;
    private static Method addAnimLayerMethod;
    private static Object firstPersonModeOn;
    private static Object firstPersonModeOff;
    private static Object firstPersonConfiguration;

    private static final Map<Class<?>, PlayerLayers> LAYER_CACHE = new IdentityHashMap<>();
    private static final Map<LivingEntity, Object> LAST_ATTACK = new WeakHashMap<>();
    private static final Map<LivingEntity, Object> TRIGGER_LAYERS = new WeakHashMap<>();


    private static boolean firstPersonRequested = false;
    private static float firstPersonTicks = 100.0F;

    private BetterCombatCompat()
    {
    }

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
            initReflection();
            isLoaded = true;
        }
        catch (Exception e)
        {
            isLoaded = false;
        }
    }

    private static void initReflection() throws Exception
    {
        attackStackClass = Class.forName("net.bettercombat.client.animation.AttackAnimationSubStack");
        poseStackClass = Class.forName("net.bettercombat.client.animation.PoseSubStack");

        attackBaseField = attackStackClass.getField("base");
        attackMirrorField = attackStackClass.getField("mirror");
        poseBaseField = poseStackClass.getField("base");

        animationInterface = Class.forName("dev.kosmx.playerAnim.api.layered.IAnimation");

        final Class<?> modifierLayerClass = Class.forName("dev.kosmx.playerAnim.api.layered.ModifierLayer");
        final Class<?> mirrorClass = Class.forName("dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier");

        getAnimationMethod = modifierLayerClass.getMethod("getAnimation");
        setAnimationMethod = modifierLayerClass.getMethod("setAnimation", animationInterface);
        isMirroredMethod = mirrorClass.getMethod("isEnabled");
        animationIsActiveMethod = animationInterface.getMethod("isActive");

        try
        {
            poseLastPoseField = poseStackClass.getDeclaredField("lastPose");
            poseLastPoseField.setAccessible(true);
        }
        catch (Exception e)
        {
            poseLastPoseField = null;
        }

        try
        {
            initTriggerReflection();
        }
        catch (Exception e)
        {
            animationInterface = null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void initTriggerReflection() throws Exception
    {
        final Class<?> firstPersonModeClass = Class.forName("dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode");
        final Class<?> firstPersonConfigClass =
                Class.forName("dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration");
        final Class<?> playerInterface = Class.forName("dev.kosmx.playerAnim.api.IPlayer");
        final Class<?> animationStackClass = Class.forName("dev.kosmx.playerAnim.api.layered.AnimationStack");

        firstPersonPassMethod = firstPersonModeClass.getMethod("isFirstPersonPass");
        getAnimationStackMethod = playerInterface.getMethod("getAnimationStack");
        addAnimLayerMethod = animationStackClass.getMethod("addAnimLayer", int.class, animationInterface);

        firstPersonModeOn = Enum.valueOf((Class<Enum>) firstPersonModeClass, "THIRD_PERSON_MODEL");
        firstPersonModeOff = Enum.valueOf((Class<Enum>) firstPersonModeClass, "NONE");

        final Constructor<?> configConstructor = firstPersonConfigClass.getConstructor(
                boolean.class, boolean.class, boolean.class, boolean.class);
        firstPersonConfiguration = configConstructor.newInstance(false, false, true, true);
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static Animations getAnimations()
    {
        final String stored = CoreClientConfig.getInstance().getBetterCombatAnimations();

        for (final Animations mode : Animations.values())
        {
            if (mode.name().equals(stored))
            {
                return mode;
            }
        }

        return isModLoaded() ? Animations.BETTER_COMBAT_AND_MOBENDS : Animations.NO_INTEGRATION;
    }

    public static void setAnimations(Animations mode)
    {
        final Animations resolved = mode == null ? Animations.NO_INTEGRATION : mode;
        CoreClientConfig.getInstance().setBetterCombatAnimations(resolved.name());
    }

    private static boolean keepsAttackAnimation(Animations mode, AbstractClientPlayer player)
    {
        if (mode == Animations.BETTER_COMBAT_ONLY)
        {
            return true;
        }

        if (mode != Animations.BETTER_COMBAT_AND_MOBENDS)
        {
            return false;
        }

        final Minecraft mc = Minecraft.getInstance();
        return player == mc.player && mc.options.getCameraType().isFirstPerson();
    }

    public static boolean suppressesTrail(LivingEntity entity)
    {
        if (!isModLoaded())
        {
            return false;
        }

        final Animations mode = getAnimations();

        if (mode == Animations.BETTER_COMBAT_ONLY)
        {
            return true;
        }

        return mode == Animations.BETTER_COMBAT_AND_MOBENDS
                && entity == Minecraft.getInstance().player
                && isFirstPersonPass();
    }

    public static boolean blocksAdoption(LivingEntity entity)
    {
        if (!isModLoaded() || shouldYieldModel(entity))
        {
            return false;
        }

        return ownsActiveAnimation(entity);
    }

    public static boolean shouldYieldModel(LivingEntity entity)
    {
        if (!isModLoaded())
        {
            return false;
        }

        final Animations mode = getAnimations();

        if (mode == Animations.BETTER_COMBAT_ONLY)
        {
            return ownsActiveAnimation(entity);
        }

        if (mode == Animations.BETTER_COMBAT_AND_MOBENDS)
        {
            return entity == Minecraft.getInstance().player && isFirstPersonPass();
        }

        return false;
    }

    public static boolean shouldYieldModel()
    {
        return shouldYieldModel(goblinbob.mobends.core.client.MoBendsRenderContext.getCurrentEntity());
    }

    public static boolean ownsActiveAnimation(LivingEntity entity)
    {
        if (entity == null || LAST_ATTACK.isEmpty())
        {
            return false;
        }

        final Object animation = LAST_ATTACK.get(entity);
        if (animation == null || animationIsActiveMethod == null)
        {
            return false;
        }

        try
        {
            return (Boolean) animationIsActiveMethod.invoke(animation);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static boolean isFirstPersonPass()
    {
        if (firstPersonPassMethod == null)
        {
            return false;
        }

        try
        {
            return (Boolean) firstPersonPassMethod.invoke(null);
        }
        catch (Exception e)
        {
            firstPersonPassMethod = null;
            return false;
        }
    }

    public static InteractionHand overrideAnimations(LivingEntity entity)
    {
        if (!isModLoaded() || !(entity instanceof AbstractClientPlayer player))
        {
            return null;
        }

        final Animations mode = getAnimations();

        try
        {
            final PlayerLayers layers = layersFor(player.getClass());

            int posesCleared = 0;
            for (final Field pose : layers.poses)
            {
                if (clearLayer(poseBaseField, pose.get(player)))
                {
                    ++posesCleared;
                }
            }

            if (layers.attack == null)
            {
                return null;
            }

            final Object attackStack = layers.attack.get(player);
            if (attackStack == null)
            {
                return null;
            }

            final Object base = attackBaseField.get(attackStack);
            if (base == null)
            {
                return null;
            }

            final Object animation = getAnimationMethod.invoke(base);
            final boolean started = animation != null && animation != LAST_ATTACK.get(player);

            if (keepsAttackAnimation(mode, player))
            {
                if (animation == null)
                {
                    LAST_ATTACK.remove(player);
                }
                else
                {
                    LAST_ATTACK.put(player, animation);
                }
            }
            else
            {
                LAST_ATTACK.remove(player);

                if (animation != null)
                {
                    setAnimationMethod.invoke(base, new Object[]{null});
                }
            }


            if (!started)
            {
                return null;
            }

            final Object mirror = attackMirrorField.get(attackStack);
            final boolean mirrored = mirror != null && (Boolean) isMirroredMethod.invoke(mirror);
            final boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
            if (player == Minecraft.getInstance().player)
            {
                firstPersonTicks = 0.0F;
            }

            if (mode == Animations.BETTER_COMBAT_ONLY)
            {
                return null;
            }

            return mirrored != leftHanded ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        }
        catch (Exception e)
        {
            isLoaded = false;
            return null;
        }
    }

    public static void updateFirstPersonTrigger(LivingEntity entity, float ticksAfterAttack)
    {
        if (!isModLoaded()
                || animationInterface == null
                || entity != Minecraft.getInstance().player
                || getAnimations() != Animations.MOBENDS_FIRST_PERSON)
        {
            if (entity == Minecraft.getInstance().player)
            {
                firstPersonRequested = false;
            }
            return;
        }

        firstPersonTicks += 1.0F;

        final boolean wanted = firstPersonTicks < FIRST_PERSON_WINDOW;

        if (wanted && !firstPersonRequested)
        {
        }
        else if (!wanted && firstPersonRequested)
        {
        }

        firstPersonRequested = wanted;

        if (wanted)
        {
            installTriggerLayer(entity);
        }
    }

    private static void installTriggerLayer(LivingEntity entity)
    {
        if (TRIGGER_LAYERS.containsKey(entity))
        {
            return;
        }

        try
        {
            final Object stack = getAnimationStackMethod.invoke(entity);
            if (stack == null)
            {
                return;
            }

            final Object layer = Proxy.newProxyInstance(
                    BetterCombatCompat.class.getClassLoader(),
                    new Class<?>[]{animationInterface},
                    new TriggerHandler());

            addAnimLayerMethod.invoke(stack, TRIGGER_PRIORITY, layer);
            TRIGGER_LAYERS.put(entity, layer);
        }
        catch (Exception e)
        {
            animationInterface = null;
        }
    }

    private static final class TriggerHandler implements InvocationHandler
    {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            switch (method.getName())
            {
                case "isActive":
                    return firstPersonRequested;
                case "get3DTransform":
                    return args[3];
                case "getFirstPersonMode":
                    return firstPersonRequested ? firstPersonModeOn : firstPersonModeOff;
                case "getFirstPersonConfiguration":
                    return firstPersonConfiguration;
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "MoBendsFirstPersonTrigger";
                default:
                    return null;
            }
        }
    }

    public static void releaseAnimations(LivingEntity entity)
    {
        LAST_ATTACK.remove(entity);

        if (entity == Minecraft.getInstance().player)
        {
            firstPersonRequested = false;
        }

        if (!isModLoaded() || poseLastPoseField == null || !(entity instanceof AbstractClientPlayer player))
        {
            return;
        }

        try
        {
            final PlayerLayers layers = layersFor(player.getClass());

            for (final Field pose : layers.poses)
            {
                final Object subStack = pose.get(player);
                if (subStack != null)
                {
                    poseLastPoseField.set(subStack, null);
                }
            }
        }
        catch (Exception e)
        {
            isLoaded = false;
        }
    }

    private static boolean clearLayer(Field baseField, Object subStack) throws Exception
    {
        if (subStack == null)
        {
            return false;
        }

        final Object base = baseField.get(subStack);
        if (base == null || getAnimationMethod.invoke(base) == null)
        {
            return false;
        }

        setAnimationMethod.invoke(base, new Object[]{null});
        return true;
    }

    private static PlayerLayers layersFor(Class<?> playerClass)
    {
        PlayerLayers cached = LAYER_CACHE.get(playerClass);
        if (cached != null)
        {
            return cached;
        }

        Field attack = null;
        final List<Field> poses = new ArrayList<>();

        for (Class<?> current = playerClass; current != null && current != Object.class; current = current.getSuperclass())
        {
            for (final Field field : current.getDeclaredFields())
            {
                if (attack == null && attackStackClass.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    attack = field;
                }
                else if (poseStackClass.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    poses.add(field);
                }
            }
        }

        cached = new PlayerLayers(attack, poses.toArray(new Field[0]));
        LAYER_CACHE.put(playerClass, cached);
        return cached;
    }

    private static final class PlayerLayers
    {
        private final Field attack;
        private final Field[] poses;

        private PlayerLayers(Field attack, Field[] poses)
        {
            this.attack = attack;
            this.poses = poses;
        }
    }
}
