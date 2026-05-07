package de.luludodo.improvemymenus.config;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import de.luludodo.improvemymenus.util.IdentifierUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

// This uses a similar System to midnight config, just with a lot less features (and a lot more scuffed)
public class LuluConfig {
    public static class Screen extends OptionsSubScreen {
        private final Category[] categories;
        private final Runnable save;
        private Screen(net.minecraft.client.gui.screens.Screen parent, Category[] categories, String title, Runnable save) {
            super(parent, Minecraft.getInstance().options, Component.translatable(title));
            this.categories = categories;
            this.save = save;
        }

        @Override
        protected void addOptions() {
            assert this.list != null;
            for (Category category : categories) {
                this.list.addHeader(category.header());
                OptionInstance<?>[] instances = new OptionInstance<?>[category.options.length];
                for (int i = 0; i < instances.length; i++) {
                    instances[i] = category.options[i].instance;
                }
                this.list.addSmall(instances);
            }
        }

        @Override
        public void onClose() {
            super.onClose();
            save.run();
        }
    }

    private record Category(String name, Component header, Option[] options) {}

    private record Option(String name, OptionInstance<?> instance) {}

    private final Category[] categories;

    private final String title;

    public LuluConfig() {
        Identifier id = getClass().getAnnotation(Identifier.class);
        if (id == null)
            throw new IllegalArgumentException("Missing @Identifier annotation for '" + getClass().getSimpleName() + "'");

        this.title = id.value() + ".options";

        List<Category> categories = new ArrayList<>();
        for (Class<?> subClass : getClass().getDeclaredClasses()) {
            String category = category(subClass);
            String categoryId = this.title + "." + category;
            List<Option> options = new ArrayList<>();

            for (Field field : subClass.getDeclaredFields()) {
                String option = option(category, field);
                String optionId = categoryId + "." + option;

                Class<?> type = field.getType();
                try {
                    if (type == boolean.class) {
                        options.add(new Option(
                                option,
                                boolOption(
                                        optionId,
                                        field,
                                        subClass
                                )
                        ));
                    } else if (type == int.class) {
                        options.add(new Option(
                                option,
                                intOption(
                                        optionId,
                                        field,
                                        subClass
                                )
                        ));
                    } else if (type.isEnum()) {
                        options.add(new Option(
                                option,
                                enumOption(
                                        optionId,
                                        field,
                                        type,
                                        subClass
                                )
                        ));
                    } else {
                        throw new IllegalStateException("Unknown field type: " + type);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Couldn't get default value for '" + option + "'", e);
                }
            }

            categories.add(new Category(
                    category,
                    Component.translatable(categoryId),
                    options.toArray(Option[]::new)
            ));
        }
        this.categories = categories.reversed().toArray(Category[]::new);
    }

    public void save() {
        JsonObject result = new JsonObject();
        for (Category category : categories) {
            JsonObject categoryJson = new JsonObject();
            for (Option option : category.options) {
                Object value = option.instance.get();
                switch (value) {
                    case Boolean bool -> categoryJson.addProperty(option.name, bool);
                    case Integer integer -> categoryJson.addProperty(option.name, integer);
                    case Enum<?> enumValue -> categoryJson.addProperty(option.name, enumValue.name().toLowerCase(Locale.ROOT));
                    default -> throw new IllegalStateException("Unknown value type: " + value.getClass());
                }
            }
            result.add(category.name, categoryJson);
        }

        System.out.println(result);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Identifier {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IntSlider {
        int min();
        int max();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OnChange {
        String value();
    }

    public Screen getScreen(net.minecraft.client.gui.screens.Screen parent) {
        return new Screen(parent, this.categories, this.title, this::save);
    }

    private static OptionInstance<Boolean> boolOption(String option, Field field, Class<?> parent) throws IllegalAccessException {
        return OptionInstance.createBoolean(
                option,
                booleanTooltipSupplier(option),
                booleanStringifier(option),
                field.getBoolean(null),
                fieldSetter(option, field, parent)
        );
    }

    private static OptionInstance<Integer> intOption(String option, Field field, Class<?> parent) throws IllegalAccessException {
        IntSlider slider = field.getAnnotation(IntSlider.class);
        if (slider == null) throw new IllegalStateException("Missing @IntSlider annotation for int value!");
        String suffixId = option + ".suffix";
        return new OptionInstance<>(
                option,
                intTooltipSupplier(option),
                (caption, value) -> Options.genericValueLabel(caption, Component.literal(Integer.toString(value)).append(I18n.exists(suffixId) ? Component.translatable(suffixId) : Component.empty())),
                new OptionInstance.IntRange(slider.min(), slider.max()),
                field.getInt(null),
                fieldSetter(option, field, parent)
        );
    }

    private static <E extends Enum<E>> OptionInstance<?> enumOption(String option, Field field, Class<?> clazz, Class<?> parent) throws IllegalAccessException{
        //noinspection unchecked
        return new OptionInstance<>(
                option,
                enumTooltipSupplier(option),
                enumStringifier(option),
                enumValues(clazz),
                (E) field.get(null),
                fieldSetter(option, field, parent)
        );
    }

    private static <T> Consumer<T> fieldSetter(String option, Field field, Class<?> clazz) {
        OnChange onChange = field.getAnnotation(OnChange.class);
        Runnable callback;
        if (onChange != null) {
            final Method finalMethod = requireMethod(option, clazz, onChange.value());
            callback = () -> {
                try {
                    finalMethod.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Couldn't invoke on change callback for '" + option + "'", e);
                }
            };
        } else {
            callback = () -> {};
        }
        return value -> {
            try {
                field.set(null, value);
                callback.run();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't set default value for '" + option + "'", e);
            }
        };
    }

    private static @NonNull Method requireMethod(String option, Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredMethod(name);
        } catch (NoSuchMethodException _) {
            try {
                return clazz.getEnclosingClass().getDeclaredMethod(name);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Couldn't find on change callback for '" + option + "'", e);
            }
        }
    }

    private static <E extends Enum<E>> OptionInstance.Enum<E> enumValues(Class<?> clazz) {
        @SuppressWarnings("unchecked") Class<E> enumClass = (Class<E>) clazz;
        E[] constants = enumClass.getEnumConstants();
        List<E> values = Arrays.asList(constants);
        Codec<E> codec = ExtraCodecs.orCompressed(
                Codec.stringResolver(enumToString(), stringToEnum(enumClass)),
                ExtraCodecs.idResolverCodec(enumToInt(), intToEnum(constants), -1)
        );
        return new OptionInstance.Enum<>(values, codec);
    }

    private static <E extends Enum<E>> Function<E, String> enumToString() {
        return value -> value.name().toLowerCase(Locale.ROOT);
    }

    private static <E extends Enum<E>> Function<String, E> stringToEnum(Class<E> clazz) {
        return value -> Enum.valueOf(clazz, value.toUpperCase(Locale.ROOT));
    }

    private static <E extends Enum<E>> ToIntFunction<E> enumToInt() {
        return Enum::ordinal;
    }

    private static <E extends Enum<E>> IntFunction<E> intToEnum(E[] values) {
        return value -> value >= 0 && value < values.length ? values[value] : null;
    }

    private static String category(Class<?> category) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        PrimitiveIterator.OfInt codepoints = category.getSimpleName().chars().iterator();
        while (codepoints.hasNext()) {
            int codepoint = codepoints.nextInt();
            int lowercase = Character.toLowerCase(codepoint);
            if (first) {
                if (lowercase == codepoint)
                    throw new IllegalStateException("Expected first character of classname to be uppercase!");
                first = false;
            } else if (lowercase != codepoint) {
                result.append('_');
            }
            result.appendCodePoint(lowercase);
        }
        return result.toString();
    }

    private static String option(String category, Field field) {
        return field.getName().toLowerCase(Locale.ROOT);
    }

    private static <T> OptionInstance.TooltipSupplier<T> intTooltipSupplier(String option) {
        return (value) -> {
            String id = option + ".tooltip";
            if (I18n.exists(id)) {
                return Tooltip.create(Component.translatable(id));
            } else {
                return null;
            }
        };
    }

    private static <T> OptionInstance.CaptionBasedToString<T> enumStringifier(String option) {
        return (caption, value) -> Component.translatable(enumValue(option, value));
    }

    private static <T> OptionInstance.TooltipSupplier<T> enumTooltipSupplier(String option) {
        return (value) -> {
            String id = enumValue(option, value) + ".tooltip";
            if (I18n.exists(id)) {
                return Tooltip.create(Component.translatable(id));
            } else {
                id = option + ".tooltip";
                if (I18n.exists(id)) {
                    return Tooltip.create(Component.translatable(id));
                } else {
                    return null;
                }
            }
        };
    }

    private static OptionInstance.CaptionBasedToString<Boolean> booleanStringifier(String option) {
        return (caption, value) -> {
            String id = option + "." + (value ? "on" : "off");
            if (I18n.exists(id)) {
                return Component.translatable(id);
            } else {
                return value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
            }
        };
    }

    private static OptionInstance.TooltipSupplier<Boolean> booleanTooltipSupplier(String option) {
        return (value) -> {
            String id = option + "." + (value ? "on" : "off") + ".tooltip";
            if (I18n.exists(id)) {
                return Tooltip.create(Component.translatable(id));
            } else {
                id = option + ".tooltip";
                if (I18n.exists(id)) {
                    return Tooltip.create(Component.translatable(id));
                } else {
                    return null;
                }
            }
        };
    }

    private static String enumValue(String option, Object value) {
        return option + "." + ((Enum<?>) value).name().toLowerCase(Locale.ROOT);
    }
}
