package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.AbstractButtonWithType;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SocialInteractionsScreen.class)
public abstract class SocialInteractionsScreenMixin {
    @Shadow
    private Button allButton;

    @Shadow
    private Button hiddenButton;

    @Shadow
    private Button blockedButton;

    @Inject(
            method = "showPage",
            at = @At("HEAD")
    )
    private void improvemymenus$updateButtons(SocialInteractionsScreen.Page page, CallbackInfo ci) {
        updateTab(allButton, page, SocialInteractionsScreen.Page.ALL);
        updateTab(hiddenButton, page, SocialInteractionsScreen.Page.HIDDEN);
        updateTab(blockedButton, page, SocialInteractionsScreen.Page.BLOCKED);
    }

    @Unique
    private void updateTab(AbstractButton button, SocialInteractionsScreen.Page page, SocialInteractionsScreen.Page buttonPage) {
        AbstractButtonWithType.setType(
                button,
                page == buttonPage ?
                        AbstractButtonWithType.Type.SOCIAL_INTERACTIONS_TAB_SELECTED :
                        AbstractButtonWithType.Type.SOCIAL_INTERACTIONS_TAB
        );
    }
}
