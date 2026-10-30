package goblinbob.mobends.standard.main;

import goblinbob.mobends.api.platform.PlatformServices;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigOptions
{
    public enum Category
    {
        TRAILS,
        COMBAT,
        MOBS,
        INTERFACE;

        public String getTranslationKey()
        {
            return "mobends.gui.config.category." + name().toLowerCase(Locale.ROOT);
        }
    }

    public static class Option
    {
        private final String key;
        private final String translationKey;
        private final Category category;
        private final boolean defaultValue;
        private final BooleanSupplier getter;
        private final Consumer<Boolean> setter;
        @Nullable
        private Option parent;

        private Option(String key, String translationKey, Category category, boolean defaultValue,
                       BooleanSupplier getter, Consumer<Boolean> setter)
        {
            this.key = key;
            this.translationKey = translationKey;
            this.category = category;
            this.defaultValue = defaultValue;
            this.getter = getter;
            this.setter = setter;
        }

        private Option under(Option parent)
        {
            this.parent = parent;
            return this;
        }

        public String getTranslationKey()
        {
            return translationKey;
        }

        public String getDescriptionKey()
        {
            return translationKey + ".desc";
        }

        public Category getCategory()
        {
            return category;
        }

        @Nullable
        public Option getParent()
        {
            return parent;
        }

        public boolean getDefault()
        {
            return defaultValue;
        }

        public boolean get()
        {
            return getter.getAsBoolean();
        }

        public void set(boolean value)
        {
            setter.accept(value);
            PlatformServices.get().setConfigBoolean(key, value);
        }
    }

    private static final Option SHOW_SWORD_TRAIL = new Option("showSwordTrail",
            "mobends.gui.config.show_sword_trail", Category.TRAILS, true,
            () -> ModConfig.showSwordTrail,
            value -> ModConfig.showSwordTrail = value);

    private static final Option SHOW_ARROW_TRAIL = new Option("showArrowTrail",
            "mobends.gui.config.show_arrow_trail", Category.TRAILS, true,
            () -> ModConfig.showArrowTrails,
            value -> ModConfig.showArrowTrails = value);

    private static final List<Option> OPTIONS = List.of(
            SHOW_SWORD_TRAIL,

            new Option("swordTrailFullBright", "mobends.gui.config.sword_trail_full_bright", Category.TRAILS, false,
                    () -> ModConfig.swordTrailFullBright,
                    value -> ModConfig.swordTrailFullBright = value).under(SHOW_SWORD_TRAIL),

            SHOW_ARROW_TRAIL,

            new Option("arrowTrailFullBright", "mobends.gui.config.arrow_trail_full_bright", Category.TRAILS, false,
                    () -> ModConfig.arrowTrailFullBright,
                    value -> ModConfig.arrowTrailFullBright = value).under(SHOW_ARROW_TRAIL),

            new Option("arrowTrailPotionColor", "mobends.gui.config.arrow_trail_potion_color", Category.TRAILS, true,
                    () -> ModConfig.arrowTrailPotionColor,
                    value -> ModConfig.arrowTrailPotionColor = value).under(SHOW_ARROW_TRAIL),

            new Option("spectralArrowTrailEffect", "mobends.gui.config.spectral_arrow_trail_effect", Category.TRAILS, true,
                    () -> ModConfig.spectralArrowTrailEffect,
                    value -> ModConfig.spectralArrowTrailEffect = value).under(SHOW_ARROW_TRAIL),

            new Option("tridentTrail", "mobends.gui.config.trident_trail", Category.TRAILS, true,
                    () -> ModConfig.tridentTrail,
                    value -> ModConfig.tridentTrail = value),

            new Option("enableSpinSwing", "mobends.gui.config.spin_swing", Category.COMBAT, false,
                    () -> ModConfig.performSpinAttack,
                    value -> ModConfig.performSpinAttack = value),

            new Option("mobsCanSpin", "mobends.gui.config.mobs_can_spin", Category.MOBS, false,
                    () -> ModConfig.mobsCanSpin,
                    value -> ModConfig.mobsCanSpin = value),

            new Option("zombieFistAttacks", "mobends.gui.config.zombie_fist_attacks", Category.MOBS, false,
                    () -> ModConfig.zombieFistAttacks,
                    value -> ModConfig.zombieFistAttacks = value),

            new Option("bendRobesOnlyWhenSitting", "mobends.gui.config.bend_robes_only_when_sitting", Category.MOBS, true,
                    () -> ModConfig.bendRobesOnlyWhenSitting,
                    value -> ModConfig.bendRobesOnlyWhenSitting = value),

            new Option("disableMovementInGui", "mobends.gui.config.disable_movement_in_gui", Category.INTERFACE, false,
                    () -> ModConfig.disableMovementInGui,
                    value -> ModConfig.disableMovementInGui = value)
    );

    public static List<Option> all()
    {
        return OPTIONS;
    }
}
