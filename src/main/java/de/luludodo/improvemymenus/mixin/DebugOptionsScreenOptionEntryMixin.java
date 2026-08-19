package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.mixinInterface.AbstractButtonWithType;
import de.luludodo.improvemymenus.util.DebugOptionsScreenUtil;
import de.luludodo.improvemymenus.util.IdentifierUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.screens.debug.DebugOptionsScreen$OptionEntry")
public abstract class DebugOptionsScreenOptionEntryMixin {
    @Shadow
    @Final
    private CycleButton<Boolean> always;

    @Shadow
    @Final
    private CycleButton<Boolean> overlay;

    @Shadow
    @Final
    private CycleButton<Boolean> never;

    @Shadow
    @Final
    private boolean isAllowed;

    @Unique
    private Component improvemymenus$name;

    @Unique
    private List<Component> improvemymenus$tooltip;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void improvemymenus$noIndicators(DebugOptionsScreen parent, Identifier location, CallbackInfo ci, @Local(name = "name") String name) {
        AbstractButtonWithType.setType(this.always, AbstractButtonWithType.Type.DEBUG_OPTION_RIGHT);
        AbstractButtonWithType.setType(this.overlay, AbstractButtonWithType.Type.DEBUG_OPTION_CENTER);
        AbstractButtonWithType.setType(this.never, AbstractButtonWithType.Type.DEBUG_OPTION_LEFT);

        if (!Config.Other.IMPROVE_DEBUG_OPTIONS) return;

        String translationKey = DebugOptionsScreenUtil.getTranslationKey(location);
        MutableComponent newName = Component.translatableWithFallback(translationKey, name);

        if (!this.isAllowed) newName.withStyle(ChatFormatting.ITALIC);

        improvemymenus$name = newName;

        List<Component> newTooltip = new ArrayList<>();
        newTooltip.add(Component.literal(name).withStyle(ChatFormatting.YELLOW));

        String tooltipTranslationKey = translationKey + ".tooltip";
        if (Language.getInstance().has(tooltipTranslationKey))
            newTooltip.add(Component.translatable(tooltipTranslationKey).withStyle(ChatFormatting.WHITE));

        java.util.Map<Identifier, DebugScreenEntryStatus> defaults = DebugScreenEntries.PROFILES.get(DebugScreenProfile.DEFAULT);
        DebugScreenEntryStatus defaultValue = defaults.getOrDefault(location, DebugScreenEntryStatus.NEVER);
        newTooltip.add(Component.translatable("improvemymenus.debug.options.tooltip.default", (switch (defaultValue) {
            case ALWAYS_ON -> DebugOptionsScreen.ENABLED_TEXT;
            case IN_OVERLAY -> DebugOptionsScreen.IN_OVERLAY_TEXT;
            case NEVER -> DebugOptionsScreen.DISABLED_TEXT;
        }).copy().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY));

        improvemymenus$tooltip = newTooltip;
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CycleButton;booleanBuilder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)Lnet/minecraft/client/gui/components/CycleButton$Builder;"
            ),
            index = 0
    )
    private Component improvemymenus$buttonText(Component trueText) {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS)
            return trueText.copy().withColor(0xFFFFFFFF);
        return trueText;
    }

    @Inject(
            method = "extractContent",
            at = @At("HEAD")
    )
    private void improvemymenus$tooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (hovered && Config.Other.IMPROVE_DEBUG_OPTIONS) graphics.setComponentTooltipForNextFrame(
                Minecraft.getInstance().font,
                improvemymenus$tooltip,
                mouseX, mouseY
        );
    }

    @ModifyArg(
            method = "extractContent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            index = 1
    )
    private String improvemymenus$name(@Nullable String str) {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS) {
            return improvemymenus$name.getString();
        }
        return str;
    }

    @Unique private static final Identifier IMPROVEMYMENUS$BORDER =
            IdentifierUtil.id("debug_option/border");
    @Unique private static final Identifier IMPROVEMYMENUS$BORDER_PRESSED =
            IdentifierUtil.id("debug_option/border_pressed");
    @Unique private static final Identifier IMPROVEMYMENUS$BORDER_HIGHLIGHTED =
            IdentifierUtil.id("debug_option/border_highlighted");
    @Unique private static final Identifier IMPROVEMYMENUS$BORDER_PRESSED_HIGHLIGHTED =
            IdentifierUtil.id("debug_option/border_pressed_highlighted");

    @Inject(
            method = "extractContent",
            at = @At("TAIL")
    )
    private void improvemymenus$extractBorder(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (Config.Other.IMPROVE_DEBUG_OPTIONS) {
            boolean leftBorderHighlighted = this.never.isHoveredOrFocused() || this.overlay.isHoveredOrFocused();
            boolean leftBorderPressed = this.never.getValue() || this.overlay.getValue();
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    leftBorderPressed ?
                            leftBorderHighlighted ? IMPROVEMYMENUS$BORDER_PRESSED_HIGHLIGHTED : IMPROVEMYMENUS$BORDER_PRESSED :
                            leftBorderHighlighted ? IMPROVEMYMENUS$BORDER_HIGHLIGHTED : IMPROVEMYMENUS$BORDER,
                    this.overlay.getX(),
                    this.overlay.getY(),
                    1,
                    this.overlay.getHeight()
            );

            boolean rightBorderHighlighted = this.overlay.isHoveredOrFocused() || this.always.isHoveredOrFocused();
            boolean rightBorderPressed = this.overlay.getValue() || this.always.getValue();
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    rightBorderPressed ?
                            rightBorderHighlighted ? IMPROVEMYMENUS$BORDER_PRESSED_HIGHLIGHTED : IMPROVEMYMENUS$BORDER_PRESSED :
                            rightBorderHighlighted ? IMPROVEMYMENUS$BORDER_HIGHLIGHTED : IMPROVEMYMENUS$BORDER,
                    this.overlay.getX() + this.overlay.getWidth() - 1,
                    this.overlay.getY(),
                    1,
                    this.overlay.getHeight()
            );
        }
    }
}
