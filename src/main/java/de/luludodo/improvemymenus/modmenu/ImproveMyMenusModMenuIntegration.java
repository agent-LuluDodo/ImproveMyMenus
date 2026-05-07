package de.luludodo.improvemymenus.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.luludodo.improvemymenus.config.Config;

public class ImproveMyMenusModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Config.INSTANCE::getScreen;
    }
}
