package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayDeque;

public class WearableBackpacksCompat
{
    private static final String MOD_ID = "wearablebackpacks";

    private static final Follow NONE = new Follow(null, 0.0F);

    private static final ThreadLocal<ArrayDeque<Follow>> follows =
            ThreadLocal.withInitial(ArrayDeque::new);

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Method getBackpackStackMethod;
    private static Class<?> backpackItemClass;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            try
            {
                Class<?> holderClass = Class.forName("com.nyfaria.wearablebackpacks.backpack.BackpackHolder");
                getBackpackStackMethod = holderClass.getMethod("getBackpackStack", LivingEntity.class);
                backpackItemClass = Class.forName("com.nyfaria.wearablebackpacks.item.BackpackItem");
            }
            catch (Exception e)
            {
                getBackpackStackMethod = null;
                backpackItemClass = null;
                isLoaded = false;
            }
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

    public static boolean isBackpackItem(ItemStack itemStack)
    {
        if (itemStack == null || itemStack.isEmpty() || !isModLoaded() || backpackItemClass == null)
        {
            return false;
        }
        return backpackItemClass.isInstance(itemStack.getItem());
    }

    private static ItemStack getBackpackStack(LivingEntity entity)
    {
        try
        {
            return (ItemStack) getBackpackStackMethod.invoke(null, entity);
        }
        catch (Exception e)
        {
            getBackpackStackMethod = null;
            isLoaded = false;
            return null;
        }
    }

    public static void beginFollow(PoseStack poseStack, LivingEntity entity)
    {
        final ArrayDeque<Follow> stack = follows.get();

        final HumanoidModel<?> model = resolveAnimatedModel(poseStack, entity);
        if (model == null)
        {
            stack.push(NONE);
            return;
        }

        poseStack.pushPose();
        model.body.translateAndRotate(poseStack);

        final float savedXRot = model.body.xRot;
        model.body.xRot = 0.0F;

        stack.push(new Follow(model, savedXRot));
    }

    public static boolean isFollowing()
    {
        final Follow follow = follows.get().peek();
        return follow != null && follow != NONE;
    }

    public static void endFollow(PoseStack poseStack)
    {
        final ArrayDeque<Follow> stack = follows.get();
        if (stack.isEmpty())
        {
            return;
        }

        final Follow follow = stack.pop();
        if (follow == NONE || follow.model == null)
        {
            return;
        }

        follow.model.body.xRot = follow.savedXRot;
        poseStack.popPose();
    }

    private static HumanoidModel<?> resolveAnimatedModel(PoseStack poseStack, LivingEntity entity)
    {
        if (poseStack == null || entity == null || !isModLoaded())
        {
            return null;
        }

        if (MoBendsRenderContext.getCurrentEntity() != entity)
        {
            return null;
        }

        final BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null || !mutator.shouldRenderCustom())
        {
            return null;
        }

        final ItemStack stack = getBackpackStack(entity);
        if (stack == null || stack.isEmpty())
        {
            return null;
        }

        return MoBendsRenderContext.getCurrentVanillaModel();
    }

    private record Follow(HumanoidModel<?> model, float savedXRot) {}
}
