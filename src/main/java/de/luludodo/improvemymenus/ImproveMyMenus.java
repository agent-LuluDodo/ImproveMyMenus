package de.luludodo.improvemymenus;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luludodo.improvemymenus.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class ImproveMyMenus implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!FabricLoader.getInstance().isModLoaded("modmenu")) {
                dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("improvemymenus-config").executes((_ -> {
                    Minecraft.getInstance().schedule(() -> {
                        Minecraft.getInstance().setScreenAndShow(Config.INSTANCE.getScreen(null));
                    });
                    return 1;
                })));
            }
        });
    }
}
