package dev.mja00.modernstartupqol.mixin;

import dev.mja00.modernstartupqol.MSQConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 1000.0f, ordinal = 0))
    private float modifyFade(float f) {
        return MSQConfig.CLIENT.fadeOutTime.get();
    }
}
