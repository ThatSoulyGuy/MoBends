package goblinbob.mobends.standard.main;

import goblinbob.mobends.api.platform.PlatformServices;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigOptions
{
    public static class Option
    {
        private final String key;
        private final String translationKey;
        private final BooleanSupplier getter;
        private final Consumer<Boolean> setter;

        private Option(String key, String translationKey, BooleanSupplier getter, Consumer<Boolean> setter)
        {
            this.key = key;
            this.translationKey = translationKey;
            this.getter = getter;
            this.setter = setter;
        }

        public String getTranslationKey()
        {
            return translationKey;
        }

        public String getDescriptionKey()
        {
            return translationKey + ".desc";
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

    private static final List<Option> OPTIONS = Collections.unmodifiableList(Arrays.asList(
            new Option("showSwordTrail", "mobends.gui.config.show_sword_trail",
                    () -> ModConfig.showSwordTrail,
                    value -> ModConfig.showSwordTrail = value),

            new Option("swordTrailFullBright", "mobends.gui.config.sword_trail_full_bright",
                    () -> ModConfig.swordTrailFullBright,
                    value -> ModConfig.swordTrailFullBright = value),

            new Option("showArrowTrail", "mobends.gui.config.show_arrow_trail",
                    () -> ModConfig.showArrowTrails,
                    value -> ModConfig.showArrowTrails = value),

            new Option("arrowTrailFullBright", "mobends.gui.config.arrow_trail_full_bright",
                    () -> ModConfig.arrowTrailFullBright,
                    value -> ModConfig.arrowTrailFullBright = value),

            new Option("newEnchantGlint", "mobends.gui.config.new_enchant_glint",
                    () -> ModConfig.newEnchantGlint,
                    value -> ModConfig.newEnchantGlint = value),

            new Option("disableSpinSwing", "mobends.gui.config.disable_spin_swing",
                    () -> !ModConfig.performSpinAttack,
                    value -> ModConfig.performSpinAttack = !value),

            new Option("mobsCanSpin", "mobends.gui.config.mobs_can_spin",
                    () -> ModConfig.mobsCanSpin,
                    value -> ModConfig.mobsCanSpin = value),

            new Option("disableMovementInGui", "mobends.gui.config.disable_movement_in_gui",
                    () -> ModConfig.disableMovementInGui,
                    value -> ModConfig.disableMovementInGui = value)
    ));

    public static List<Option> all()
    {
        return OPTIONS;
    }
}
