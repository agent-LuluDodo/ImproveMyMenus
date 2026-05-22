package de.luludodo.improvemymenus.config;

import com.google.common.io.Files;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/// # Info
/// An extremely minimalist config
///
/// ## Features
///
/// This also means, that you don't get any customization.
/// Your config should include several subclasses, the name of the subclass is the name of that category.
/// Inside of these subclasses are the config values, represented by fields.
/// This config supports `boolean`, `int` and `enum` values.
///
/// ### Loading/Saving
///
/// This is the only thing which is slightly more complex than it has to be.
/// Before saving a backup of the config is created as to prevent any crashes during saving from breaking the config.
/// Loading first checks if that backup is present and loads it with priority over the normal config file.
///
/// ## Annotations
///
/// Your config is required to extends this class and to be annotated with [Mod].
/// `int` values are required to be annotated with [IntSlider].
/// Every value can optionally be annotated with [OnChange].
///
/// # Usage
///
/// ## Class
///
/// ```java
/// @MinimalConfig.Mod(name = "My Mod", id = "my-id")
/// public class Config extends MinimalConfig {
///     public static final Config INSTANCE = new Config();
///
///     public class MyCategory {
///         public static boolean BOOLEAN_OPTION = true;
///
///         @OnChange("reloadEnum")
///         public static MyEnum ENUM_OPTION = MyEnum.FOO;
///         public enum MyEnum {
///             FOO,
///             BAR,
///             FOO_BAR
///         }
///
///         @IntSlider(min = 0, max = 100)
///         public static int MY_INT = 10;
///     }
///
///     public static void reloadEnum() {
///         // do stuff
///     }
/// }
/// ```
///
/// ## Translations
///
/// ```json
/// {
///   "my-id.options": "My Config",
///   "my-id.options.my_category": "My Category",
///   "my-id.options.my_category.boolean_option": "Boolean Option",
///   "my-id.options.my_category.boolean_option.off": "Custom Off Translation",
///   "my-id.options.my_category.enum_option": "Enum Option",
///   "my-id.options.my_category.enum_option.tooltip": "This is a enum option",
///   "my-id.options.my_category.enum_option.foo": "Foo",
///   "my-id.options.my_category.enum_option.bar": "Bar",
///   "my-id.options.my_category.enum_option.bar.tooltip": "This will override the enum_option tooltip",
///   "my-id.options.my_category.enum_option.foo_bar": "Foo & Bar",
///   "my-id.options.my_category.my_int": "My Int Slider"
/// }
/// ```
///
/// ## Config File
///
/// ```json
/// {
///   "my_category": {
///     "boolean_option": true,
///     "enum_option": "foo",
///     "my_int": 10
///   }
/// }
/// ```
public class MinimalConfig {
    private static class Screen extends OptionsSubScreen {
        private final Category[] categories;
        private final Runnable save;
        private Screen(net.minecraft.client.gui.screens.Screen parent, Category[] categories, Component title, Runnable save) {
            super(parent, Minecraft.getInstance().options, title);
            this.categories = categories;
            this.save = save;
        }

        @Override
        protected void addOptions() {
            assert this.list != null;
            for (Category category : categories) {
                this.list.addHeader(category.header());

                AbstractWidget[] widgets = new AbstractWidget[category.actions.length + category.options.length];

                for (int i = 0; i < category.actions.length; i++) {
                    Action action = category.actions[i];
                    Button button = action.button;
                    button.setFocused(false);
                    if (I18n.exists(action.tooltipId)) {
                        button.setTooltip(Tooltip.create(Component.translatable(action.tooltipId)));
                    }
                    widgets[i] = button;
                }

                for (int i = 0; i < category.options.length; i++) {
                    widgets[i + category.actions.length] = category.options[i].instance.createButton(this.options);
                }

                this.list.addSmall(List.of(widgets));
            }
        }

        public void refresh() {
            minecraft.setScreen(new Screen(lastScreen, categories, title, save));
        }

        @Override
        public void onClose() {
            super.onClose();
            save.run();
        }
    }

    private record Category(String name, Component header, Action[] actions, Option[] options) {}

    private sealed interface Entry permits Action, Option {}

    private record Action(String name, Button button, String tooltipId, Method method) implements Entry { }

    private record Option(String name, Class<?> type, OptionInstance<?> instance, Object defaultValue) implements Entry {
        private void set(Object value) {
            OptionInstance<Object> objectInstance = uncheckedCast(instance);
            objectInstance.set(value);
            if (!Minecraft.getInstance().isRunning())
                objectInstance.onValueUpdate.accept(value);
        }

        private void reset() {
            set(defaultValue);
        }
    }

    private final Category[] categories;

    private  final Logger logger;
    private final File old;
    private final File file;
    private final String title;

    /// Creates a new instance of this config.
    protected MinimalConfig() {
        Mod id = getClass().getAnnotation(Mod.class);
        if (id == null)
            throw new IllegalArgumentException("Missing @Identifier annotation for '" + getClass().getName() + "'");

        String modId = id.id();
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.old = configDir.resolve(modId + ".json.old").toFile();
        this.file = configDir.resolve(modId + ".json").toFile();

        this.logger = LoggerFactory.getLogger(id.name() + "/MinimalConfig");
        this.title = modId + ".options";

        List<Category> categories = new ArrayList<>();
        for (Class<?> subClass : getClass().getDeclaredClasses()) {
            if (subClass.isEnum()) continue;

            String category = category(subClass);
            String categoryId = this.title + "." + category;

            List<Action> actions = new ArrayList<>();

            for (Method method : subClass.getDeclaredMethods()) {
                String action = action(method);
                String actionId = categoryId + "." + action;

                actions.add(new Action(
                        action,
                        actionButton(
                                actionId,
                                method
                        ),
                        actionId + ".tooltip",
                        method
                ));
            }

            List<Option> options = new ArrayList<>();

            for (Field field : subClass.getDeclaredFields()) {
                String option = option(field);
                String optionId = categoryId + "." + option;

                Class<?> type = field.getType();
                try {
                    Object value = field.get(null);
                    if (type == boolean.class) {
                        options.add(new Option(
                                option,
                                boolean.class,
                                boolOption(
                                        optionId,
                                        field,
                                        subClass
                                ),
                                value
                        ));
                    } else if (type == int.class) {
                        options.add(new Option(
                                option,
                                int.class,
                                intOption(
                                        optionId,
                                        field,
                                        subClass
                                ),
                                value
                        ));
                    } else if (type.isEnum()) {
                        options.add(new Option(
                                option,
                                type,
                                enumOption(
                                        type.getEnclosingClass() == subClass ? categoryId : title,
                                        optionId,
                                        field,
                                        type,
                                        subClass
                                ),
                                value
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
                    actions.toArray(Action[]::new),
                    options.toArray(Option[]::new)
            ));
        }
        this.categories = categories.reversed().toArray(Category[]::new);

        reload();
    }

    /// Reloads this config, gets automatically called upon initializing.
    public void reload() {
        File load;
        if (old.exists()) {
            try {
                Files.move(old, file);
                load = file;
            } catch (IOException _) {
                load = old;
            }
        } else if (file.exists()) {
            load = file;
        } else {
            return;
        }

        String json;
        try (BufferedReader reader = Files.newReader(load, StandardCharsets.UTF_8)) {
            json = reader.readAllAsString();
        } catch (IOException e) {
            this.logger.error("Could not load config", e);
            return;
        }

        if (setJson(json)) save();
    }

    /// Saves this config, gets automatically called upon closing the config screen.
    public void save() {
        String json = getJson();

        try {
            if (!old.exists() && file.exists()) {
                Files.copy(file, old);
            }

            try (BufferedWriter writer = Files.newWriter(file, StandardCharsets.UTF_8)) {
                writer.write(json);
            }

            old.delete();
        } catch (IOException e) {
            this.logger.error("Could not save config", e);

            try {
                Files.move(old, file);
            } catch (IOException _) {}
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String getJson() {
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
            if (!categoryJson.isEmpty())
                result.add(category.name, categoryJson);
        }
        return GSON.toJson(result);
    }

    private boolean setJson(String content) {
        try {
            boolean hasMissingConfigValue = false;
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            for (Category category : categories) {
                JsonElement categoryJsonElement = json.get(category.name);
                if (!(categoryJsonElement instanceof JsonObject categoryJson)) {
                    this.logger.warn("Config is missing value for category '{}'", category.name);
                    continue;
                }

                for (Option option : category.options) {
                    JsonElement optionJsonElement = categoryJson.get(option.name);
                    if (!(optionJsonElement instanceof JsonPrimitive optionJson)) {
                        hasMissingConfigValue = missingConfigValue(option);
                        continue;
                    }

                    if (option.type == boolean.class) {
                        if (!optionJson.isBoolean()) {
                            hasMissingConfigValue = missingConfigValue(option);
                            continue;
                        }

                        option.set(optionJson.getAsBoolean());
                    } else if (option.type == int.class) {
                        if (!optionJson.isNumber()) {
                            hasMissingConfigValue = missingConfigValue(option);
                            continue;
                        }

                        option.set(optionJson.getAsInt());
                    } else if (option.type.isEnum()) {
                        if (!optionJson.isString()) {
                            hasMissingConfigValue = missingConfigValue(option);
                            continue;
                        }

                        try {
                            option.set(Enum.valueOf(
                                    uncheckedCast(option.type),
                                    optionJson.getAsString().toUpperCase(Locale.ROOT)
                            ));
                        } catch (IllegalArgumentException e) {
                            hasMissingConfigValue = missingConfigValue(option);
                        }
                    } else {
                        throw new IllegalStateException("Unknown value type: " + option.type);
                    }
                }
            }
            return hasMissingConfigValue;
        } catch (RuntimeException e) {
            this.logger.error("Could not parse config", e);
            return true;
        }
    }

    private static <T> T uncheckedCast(Object obj) {
        //noinspection unchecked
        return (T) obj;
    }

    private boolean missingConfigValue(Option option) {
        this.logger.warn("Config is missing value for option '{}'", option.name);
        return true;
    }

    /// Required for the class extending [MinimalConfig].
    /// Specifies the identifier used to create translations and save the config.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Mod {
        /// the mod name
        String name();

        // the mod id
        String id();
    }

    /// Required for a field of type `int`.
    /// Provides information on the range of the slider.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IntSlider {
        /// The smallest allowed value. (inclusive)
        int min();

        /// The largest allowed value. (inclusive)
        int max();
    }

    /// Calls the specified function when the value of this field changes.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OnChange {
        /// The name of the function to call.
        String value();
    }

    /// Creates an [OptionsSubScreen] for this config.
    ///
    /// @param parent The parent screen
    ///
    /// @return The newly instantiated {@link OptionsSubScreen}
    public @NotNull OptionsSubScreen getScreen(net.minecraft.client.gui.screens.Screen parent) {
        return new Screen(parent, this.categories, Component.translatable(this.title), this::save);
    }

    /// Resets the config to its default values
    public void reset() {
        for (Category category : categories) {
            for (Option option : category.options) {
                option.reset();
            }
        }

        if (Minecraft.getInstance().screen instanceof Screen configScreen) {
            configScreen.refresh();
        }

        save();
    }

    /// Refreshes the config
    ///
    /// Updates the config based on the fields,
    /// use this if you manually changed config options,
    /// by assigning to a field.
    public void refresh() {
        Map<String, Map<String, Field>> classToFieldMap = new HashMap<>();
        for (Class<?> subClass : getClass().getDeclaredClasses()) {
            if (subClass.isEnum()) continue;

            Map<String, Field> fieldMap = new HashMap<>();
            classToFieldMap.put(category(subClass), fieldMap);

            for (Field field : subClass.getDeclaredFields()) {
                fieldMap.put(option(field), field);
            }
        }

        for (Category category : categories) {
            Map<String, Field> fieldMap = classToFieldMap.get(category.name);
            for (Option option : category.options) {
                Field field = fieldMap.get(option.name);
                try {
                    option.set(field.get(null));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not get option '" + option.name + "' in category '" + category.name + "'", e);
                }
            }
        }

        if (Minecraft.getInstance().screen instanceof Screen configScreen) {
            configScreen.refresh();
        }

        save();
    }

    private static Button actionButton(String action, Method method) {
        return Button.builder(
                Component.translatable(action),
                _ -> {
                    try {
                        method.invoke(null);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException("Could not run method '" + method.getName() + "'", e);
                    }
                }
        ).build();
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

    private static <E extends Enum<E>> OptionInstance<?> enumOption(String prefix, String option, Field field, Class<?> clazz, Class<?> parent) throws IllegalAccessException{
        return new OptionInstance<E>(
                option,
                enumTooltipSupplier(option),
                enumStringifier(prefix + "." + category(clazz)),
                enumValues(clazz),
                uncheckedCast(field.get(null)),
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
        Class<E> enumClass = uncheckedCast(clazz);
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
        return classOrFieldName(category.getSimpleName(), false);
    }

    private static String action(Method method) {
        return classOrFieldName(method.getName(), true);
    }

    private static String classOrFieldName(String name, boolean fieldName) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        PrimitiveIterator.OfInt codepoints = name.chars().iterator();
        while (codepoints.hasNext()) {
            int codepoint = codepoints.nextInt();
            int lowercase = Character.toLowerCase(codepoint);
            if (first) {
                if (!fieldName && lowercase == codepoint)
                    throw new IllegalStateException("Expected first character of classname to be uppercase!");
                if (fieldName && lowercase != codepoint)
                    throw new IllegalStateException("Expected first character of fieldname to be lowercase!");
                first = false;
            } else if (lowercase != codepoint) {
                result.append('_');
            }
            result.appendCodePoint(lowercase);
        }
        return result.toString();
    }

    private static String option(Field field) {
        return field.getName().toLowerCase(Locale.ROOT);
    }

    private static <T> OptionInstance.TooltipSupplier<T> intTooltipSupplier(String option) {
        return _ -> {
            String id = option + ".tooltip";
            if (I18n.exists(id)) {
                return Tooltip.create(Component.translatable(id));
            } else {
                return null;
            }
        };
    }

    private static <T> OptionInstance.CaptionBasedToString<T> enumStringifier(String option) {
        return (_, value) -> Component.translatable(enumValue(option, value));
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
        return (_, value) -> {
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
