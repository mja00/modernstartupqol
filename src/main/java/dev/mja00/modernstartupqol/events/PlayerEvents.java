package dev.mja00.modernstartupqol.events;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mja00.modernstartupqol.MSQConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;

import java.awt.*;
import java.lang.management.ManagementFactory;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerEvents {
    static final Logger LOGGER = LogManager.getLogger("ModernStartupQOL/PlayerEvents");
    // Startup time stuff
    public static long expectedTime = 0;
    public static long doneTime = 0;

    static boolean triggered = false;
    public static boolean trueFullscreen;

    static long startupTime;
    static boolean hasBeenMainMenu = false;
    static boolean hasLeftMainMenu = false;

    @SubscribeEvent
    public static void onGuiDraw(ScreenEvent.Render event) {
        if (!hasLeftMainMenu && event.getScreen() instanceof TitleScreen) {
            hasBeenMainMenu = true;
            long minutes = (startupTime / 1000) / 60;
            long seconds = (startupTime / 1000) % 60;
            long ex_mins = (expectedTime / 1000) / 60;
            long ex_secs = (expectedTime / 1000) % 60;

            float guiScale = (float) Minecraft.getInstance().getWindow().getGuiScale();
            if (guiScale <= 0) guiScale = 1; // Just in case

            String txt = "Startup took " + minutes + "m " + seconds + "s | Last 3 avg: " + ex_mins + "m " + ex_secs + "s";
            float pX = (float) Minecraft.getInstance().getWindow().getWidth() / 2 / guiScale - (float) Minecraft.getInstance().font.width(txt) /2;
            float pY = (float) Minecraft.getInstance().getWindow().getHeight() / guiScale - 20;
            int color;
            if (startupTime < 90_000) {
                // This is "good"
                color = Color.GREEN.getRGB();
            } else if (startupTime < 120_000) {
                // This is "ok"
                color = Color.YELLOW.getRGB();
            } else {
                // This is "bad"
                color = Color.RED.getRGB();
            }
            // Mojang removed the drawShadow method, so we have to use drawInBatch instead. Why? I wish I knew but fuck me it's annoying now
            // The below shit is to make the line readable
            Matrix4f matrix = new PoseStack().last().pose();
            var buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            Minecraft.getInstance().font.drawInBatch(txt, pX, pY, color, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 15728880, false);
        } else if (hasBeenMainMenu) {
            hasLeftMainMenu = true;
        }
    }

    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent.Init event) {
        if (!triggered && event.getScreen() instanceof TitleScreen) {
            triggered = true;

            Minecraft.getInstance().options.fullscreen().set(trueFullscreen);
            if (Minecraft.getInstance().options.fullscreen().get() && !Minecraft.getInstance().getWindow().isFullscreen()) {
                Minecraft.getInstance().getWindow().toggleFullScreen();
                Minecraft.getInstance().options.fullscreen().set(Minecraft.getInstance().getWindow().isFullscreen());
            }

            startupTime = ManagementFactory.getRuntimeMXBean().getUptime();
            LOGGER.info("Startup took " + startupTime + "ms");

            doneTime = startupTime;
            MSQConfig.addStartupTime(startupTime);
        }
    }
}
