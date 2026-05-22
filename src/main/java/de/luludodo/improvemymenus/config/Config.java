package de.luludodo.improvemymenus.config;

import de.luludodo.improvemymenus.util.CommonComponentsUtil;
import de.luludodo.improvemymenus.util.Globals;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.CommonComponents;

import static de.luludodo.improvemymenus.config.MinimalConfig.Mod;

@Mod(name = Globals.MOD_NAME, id = Globals.MOD_ID)
public class Config extends MinimalConfig {
    public static Config INSTANCE = new Config();

    @SuppressWarnings("unused")
    public static class Preset {
        public static void defaults() {
            INSTANCE.reset();
        }

        public static void fixesOnly() {
            CycleButton.SCROLL = true;
            CycleButton.SHOW_ALTERNATIVES = Modifier.ALT;
            CycleButton.NEXT = ButtonBinding.LEFT;
            CycleButton.PREVIOUS = ButtonBinding.SHIFT_LEFT;
            CycleButton.INDICATORS = false;
            CycleButton.INDICATOR = ButtonBinding.UNBOUND;
            CycleButton.SWITCHES = false;
            CycleButton.DROPDOWN = ButtonBinding.UNBOUND;
            CycleButton.ON_OFF_COLORS = false;
            CycleButton.RANDOM_COLORS = false;

            Slider.MAX_INDICATORS = 0;
            Slider.HIGHLIGHT = false;
            Slider.SPACING = Slider.Spacing.AREA;
            Slider.ENUM_SPACING = Slider.Spacing.MIXED;
            Slider.SNAP = Slider.SnapTiming.ON_RELEASE;
            Slider.FORCE_CURSOR = true;

            EditBox.SOLID_CURSOR = EditBox.SolidCursor.WHILE_TYPING;
            EditBox.BLINK_INTERVAL = 300;
            EditBox.CONSISTENT_HIGHLIGHTING = true;

            List.SCROLL_BEHAVIOUR = List.ScrollBehaviour.PREFER_PARENT;
            List.SCROLLBAR_ON_CLICK = List.ScrollbarOnClick.DRAG;
            List.FORCE_CURSOR = true;
            List.ACCURATE_HOVERING = true;
            List.SUPPORT_PAGE_KEYS = false;

            Other.ZOOM = Other.ZoomOptions.VIDEO_SETTINGS;
            Other.UNBLUR_VIDEO_SETTINGS = false;
            Other.IMPROVE_DEBUG_OPTIONS = false;
            Other.IMPROVE_SOCIAL_INTERACTIONS = false;
            Other.FIX_ONBOARDING_PANORAMA_SPINNING = true;
            Other.INDEPENDENT_ANIMATIONS = true;

            INSTANCE.refresh();
        }

        public static void vanilla() {
            CycleButton.SCROLL = true;
            CycleButton.SHOW_ALTERNATIVES = Modifier.ALT;
            CycleButton.NEXT = ButtonBinding.LEFT;
            CycleButton.PREVIOUS = ButtonBinding.SHIFT_LEFT;
            CycleButton.INDICATORS = false;
            CycleButton.INDICATOR = ButtonBinding.UNBOUND;
            CycleButton.SWITCHES = false;
            CycleButton.DROPDOWN = ButtonBinding.UNBOUND;
            CycleButton.ON_OFF_COLORS = false;
            CycleButton.RANDOM_COLORS = false;

            Slider.MAX_INDICATORS = 0;
            Slider.HIGHLIGHT = false;
            Slider.SPACING = Slider.Spacing.AREA;
            Slider.ENUM_SPACING = Slider.Spacing.MIXED;
            Slider.SNAP = Slider.SnapTiming.VANILLA;
            Slider.FORCE_CURSOR = false;

            EditBox.SOLID_CURSOR = EditBox.SolidCursor.NEVER;
            EditBox.BLINK_INTERVAL = 300;
            EditBox.CONSISTENT_HIGHLIGHTING = false;

            List.SCROLL_BEHAVIOUR = List.ScrollBehaviour.ONLY_PARENT;
            List.SCROLLBAR_ON_CLICK = List.ScrollbarOnClick.VANILLA;
            List.FORCE_CURSOR = false;
            List.ACCURATE_HOVERING = false;
            List.SUPPORT_PAGE_KEYS = false;

            Other.ZOOM = Other.ZoomOptions.VIDEO_SETTINGS;
            Other.UNBLUR_VIDEO_SETTINGS = false;
            Other.IMPROVE_DEBUG_OPTIONS = false;
            Other.IMPROVE_SOCIAL_INTERACTIONS = false;
            Other.FIX_ONBOARDING_PANORAMA_SPINNING = false;
            Other.INDEPENDENT_ANIMATIONS = false;

            INSTANCE.refresh();
        }
    }

    public static class CycleButton {
        public static boolean SCROLL = true;

        public static Modifier SHOW_ALTERNATIVES = Modifier.ALT;

        public static ButtonBinding NEXT = ButtonBinding.LEFT;
        public static ButtonBinding PREVIOUS = ButtonBinding.SHIFT_LEFT;

        public static boolean INDICATORS = true;
        public static ButtonBinding INDICATOR = ButtonBinding.LEFT;

        public static boolean SWITCHES = true;

        public static ButtonBinding DROPDOWN = ButtonBinding.UNBOUND;

        @OnChange("reloadCommonComponents")
        public static boolean ON_OFF_COLORS = true;

        @OnChange("reloadMessages")
        public static boolean RANDOM_COLORS = false;
    }

    public static class Slider {

        @IntSlider(min = 0, max = 50)
        public static int MAX_INDICATORS = 5;

        public static boolean HIGHLIGHT = false;

        public static Spacing SPACING = Spacing.VISUAL;
        public static Spacing ENUM_SPACING = Spacing.MIXED;
        public enum Spacing {
            VISUAL,
            AREA,
            MIXED
        }

        public static SnapTiming SNAP = SnapTiming.ON_RELEASE;
        public enum SnapTiming {
            VANILLA,
            ON_RELEASE,
            WHILE_DRAGGING
        }

        public static boolean FORCE_CURSOR = true;
    }

    public static class EditBox {
        public static SolidCursor SOLID_CURSOR = SolidCursor.WHILE_TYPING;
        public enum SolidCursor {
            NEVER,
            WHILE_TYPING,
            ALWAYS
        }

        @IntSlider(min = 1, max = 1000)
        public static int BLINK_INTERVAL = 300;

        public static boolean CONSISTENT_HIGHLIGHTING = true;
    }

    public static class List {
        public static ScrollBehaviour SCROLL_BEHAVIOUR = ScrollBehaviour.PREFER_PARENT;
        public enum ScrollBehaviour {
            ONLY_PARENT,
            PREFER_PARENT,
            PREFER_CHILDREN
        }

        public static ScrollbarOnClick SCROLLBAR_ON_CLICK = ScrollbarOnClick.JUMP;
        public enum ScrollbarOnClick {
            VANILLA,
            DRAG,
            PAGE,
            JUMP
        }

        public static boolean FORCE_CURSOR = true;

        public static boolean ACCURATE_HOVERING = true;

        public static boolean SUPPORT_PAGE_KEYS = true;

        @IntSlider(min = 0, max = 25)
        public static int PAGE_OVERLAP = 10;
    }

    public static class Other {
        public static ZoomOptions ZOOM = ZoomOptions.EVERYWHERE;
        public enum ZoomOptions {
            NOWHERE,
            VIDEO_SETTINGS,
            EVERYWHERE
        }

        public static boolean UNBLUR_VIDEO_SETTINGS = true;

        public static boolean IMPROVE_DEBUG_OPTIONS = true;

        public static boolean IMPROVE_SOCIAL_INTERACTIONS = true;

        // Fixes MC-308112
        public static boolean FIX_ONBOARDING_PANORAMA_SPINNING = true;

        // Fixes MC-307860
        public static boolean INDEPENDENT_ANIMATIONS = true;
    }

    public enum Modifier {
        NEVER,
        SHIFT,
        ALT,
        CONTROL,
        ALWAYS;

        public boolean matches(InputWithModifiers modifiers) {
            return switch (this) {
                case NEVER -> false;
                case SHIFT -> modifiers.hasShiftDown();
                case ALT -> modifiers.hasAltDown();
                case CONTROL -> modifiers.hasControlDown();
                case ALWAYS -> true;
            };
        }
    }

    public enum ButtonBinding {
        UNBOUND(false),
        LEFT(true),
        SHIFT_LEFT(true),
        ALT_LEFT(true),
        CONTROL_LEFT(true),
        RIGHT(false),
        SHIFT_RIGHT(false),
        ALT_RIGHT(false),
        CONTROL_RIGHT(false);

        private final boolean left;
        ButtonBinding(boolean left) {
            this.left = left;
        }

        public Modifier getModifier() {
            return switch (this) {
                case UNBOUND -> Modifier.NEVER;
                case SHIFT_LEFT, SHIFT_RIGHT -> Modifier.SHIFT;
                case ALT_LEFT, ALT_RIGHT -> Modifier.ALT;
                case CONTROL_LEFT, CONTROL_RIGHT -> Modifier.CONTROL;
                case LEFT, RIGHT -> Modifier.ALWAYS;
            };
        }

        public boolean isLeft() {
            return left;
        }

        public boolean matches(InputWithModifiers event) {
            return (isLeft() ? event.input() == 0 : event.input() == 1) && getModifier().matches(event);
        }
    }

    @SuppressWarnings("unused")
    public static void reloadCommonComponents() {
        CommonComponents.OPTION_ON = CommonComponentsUtil.getOn();
        CommonComponents.OPTION_OFF = CommonComponentsUtil.getOff();
        reloadMessages();
    }

    @SuppressWarnings("unused")
    public static void reloadMessages() {
        Globals.RELOAD_MESSAGES = true;
    }
}
