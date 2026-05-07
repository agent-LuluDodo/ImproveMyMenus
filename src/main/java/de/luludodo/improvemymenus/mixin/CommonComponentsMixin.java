package de.luludodo.improvemymenus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.luludodo.improvemymenus.config.Config;
import de.luludodo.improvemymenus.util.CommonComponentsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommonComponents.class)
public abstract class CommonComponentsMixin {
    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0
            )
    )
    private static MutableComponent improvemymenus$onColor(MutableComponent original) {
        CommonComponentsUtil.setOriginalOn(original);
        return CommonComponentsUtil.getOn();
    }

    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 1
            )
    )
    private static MutableComponent improvemymenus$offColor(MutableComponent original) {
        CommonComponentsUtil.setOriginalOff(original);
        return CommonComponentsUtil.getOff();
    }
}
