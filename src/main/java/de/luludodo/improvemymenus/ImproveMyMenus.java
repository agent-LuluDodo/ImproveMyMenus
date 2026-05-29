package de.luludodo.improvemymenus;

import de.luludodo.improvemymenus.compatibility.ModMenuHelper;
import de.luludodo.improvemymenus.util.ScheduleUtil;
import de.luludodo.improvemymenus.util.TextAdjustments;
import net.fabricmc.api.ClientModInitializer;

public class ImproveMyMenus implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModMenuHelper.init();

        TextAdjustments.init();
        ScheduleUtil.init();
    }
}
