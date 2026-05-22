package de.luludodo.improvemymenus.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextAdjustments {
    public static final Identifier IDENTIFIER = IdentifierUtil.id("text_adjustments");

    private static Identifier id(String name) {
        return IdentifierUtil.id("text_adjustments/" + name + ".json");
    }

    public static final Identifier DEBUG_OPTION = id("debug_option");
    public static final Identifier SOCIAL_INTERACTIONS = id("social_interactions");

    public static DebugOptionAdjustment DEBUG_OPTION_ADJUSTMENT;
    public static SocialInteractionsAdjustment SOCIAL_INTERACTIONS_ADJUSTMENT;

    private static final Logger LOG = LoggerFactory.getLogger(Globals.MOD_NAME + "/TextAdjustments");

    public record DebugOptionAdjustment(int left, int center, int right) {}
    public record SocialInteractionsAdjustment(int down) {}

    private static class ReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NonNull ResourceManager resourceManager) {
            DEBUG_OPTION_ADJUSTMENT = parseJson(resourceManager, DEBUG_OPTION, jsonElement -> {
                JsonObject json = jsonElement.getAsJsonObject();
                int left = json.get("left").getAsInt();
                int center = json.get("center").getAsInt();
                int right = json.get("right").getAsInt();
                return new DebugOptionAdjustment(left, center, right);
            }, () -> new DebugOptionAdjustment(0, 0, 0));
            SOCIAL_INTERACTIONS_ADJUSTMENT = parseJson(resourceManager, SOCIAL_INTERACTIONS, jsonElement -> {
                JsonObject json = jsonElement.getAsJsonObject();
                int down = json.get("down").getAsInt();
                return new SocialInteractionsAdjustment(down);
            }, () -> new SocialInteractionsAdjustment(0));
        }

        private static <T> T parseJson(ResourceManager resourceManager, Identifier id, Function<JsonElement, T> consumer, Supplier<T> fallback) {
            Optional<Resource> resource = resourceManager.getResource(id);
            if (resource.isPresent()) {
                try (BufferedReader reader = resource.get().openAsReader()) {
                    return consumer.apply(JsonParser.parseReader(reader));
                } catch (IOException | RuntimeException e) {
                    LOG.error("Error reading text adjustment '{}'", DEBUG_OPTION, e);
                    return fallback.get();
                }
            } else {
                LOG.error("Couldn't find text adjustment '{}'", id);
                return fallback.get();
            }
        }
    }

    public static void registerReloadListener() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(IDENTIFIER, new ReloadListener());
    }
}
