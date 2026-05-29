package de.luludodo.improvemymenus.compatibility;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luludodo.improvemymenus.config.Config;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class ModMenuHelper {
    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("modmenu")) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
                dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("improvemymenus-config").executes((_ -> {
                    Minecraft.getInstance().schedule(() -> {
                        Minecraft.getInstance().setScreenAndShow(Config.INSTANCE.getScreen(null));
                    });
                    return 1;
                })));
            });
        }
    }
}
