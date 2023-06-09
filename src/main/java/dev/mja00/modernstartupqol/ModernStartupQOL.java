package dev.mja00.modernstartupqol;

import com.mojang.logging.LogUtils;
import dev.mja00.modernstartupqol.events.PlayerEvents;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("modernstartupqol")
public class ModernStartupQOL {

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    static {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            PlayerEvents.expectedTime = MSQConfig.getTimeEstimates();
        });
    }

    public ModernStartupQOL() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(MSQConfig.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            PlayerEvents.trueFullscreen = Minecraft.getInstance().options.fullscreen().get();
            Minecraft.getInstance().options.fullscreen().set(false);
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MSQConfig.clientSpec);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
