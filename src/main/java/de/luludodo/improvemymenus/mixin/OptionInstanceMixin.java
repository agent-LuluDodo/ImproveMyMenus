package de.luludodo.improvemymenus.mixin;

import de.luludodo.improvemymenus.mixinInterface.IdentifiableOptionInstance;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin implements IdentifiableOptionInstance {

    @Unique
    private Object improvemymenus$identifier = null;

    @Override
    public void improvemymenus$setIdentifier(Object identifier) {
        this.improvemymenus$identifier = identifier;
    }

    @Override
    public Object improvemymenus$getIdentifier() {
        return this.improvemymenus$identifier;
    }
}
