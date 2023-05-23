package dev.mja00.modernstartupqol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MSQConfig {

    static final Logger LOGGER = LogManager.getLogger("ModernStartupQOL/Config");

    public static final File DOT_MINECRAFT = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).toFile().getParentFile();
    public static final File TIMES_FILE = new File(DOT_MINECRAFT, "config/modernstartupqol/startup_times.json");

    public static class Client {
        // Client config options
        public final ForgeConfigSpec.IntValue fadeOutTime;
        public final ForgeConfigSpec.IntValue fadeInTime;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Startup Settings").push("startup");

            fadeOutTime = builder
                    .comment("How long should the timer take to fade out? (in ticks)")
                    .defineInRange("fadeOutTime", 1000, 0, 10000);

            fadeInTime = builder
                    .comment("How long should the timer take to fade in? (in ticks)")
                    .defineInRange("fadeInTime", 500, 0, 10000);
        }
    }

    public static final ForgeConfigSpec clientSpec;
    public static final MSQConfig.Client CLIENT;

    static {
        final Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(MSQConfig.Client::new);
        clientSpec = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        LOGGER.debug("Config has been loaded: {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        LOGGER.debug("Config has been reloaded: {}", configEvent.getConfig().getFileName());
    }

    public static long getTimeEstimates() {
        try {
            TIMES_FILE.getParentFile().mkdirs();
            if (!TIMES_FILE.exists()) {
                TIMES_FILE.createNewFile();
            }

            JsonReader jr = new JsonReader(new FileReader(TIMES_FILE));
            JsonElement jp = JsonParser.parseReader(jr);
            if (jp.isJsonObject()) {
                JsonObject jo = jp.getAsJsonObject();
                if (jo.has("times") && jo.get("times").isJsonArray()) {
                    JsonArray ja = jo.get("times").getAsJsonArray();
                    if (ja.size() > 0) {
                        long sum = 0;
                        for (int i = 0; i < ja.size(); i++) {
                            sum += ja.get(i).getAsLong();
                        }
                        sum /= ja.size();

                        return sum;
                    }
                }
            }
            jr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void addStartupTime(long startupTime) {
        try {
            TIMES_FILE.getParentFile().mkdirs();
            if (!TIMES_FILE.exists()) {
                TIMES_FILE.createNewFile();
            }

            long[] times = new long[0];
            JsonReader jr = new JsonReader(new FileReader(TIMES_FILE));
            JsonElement jp = JsonParser.parseReader(jr);

            if (jp.isJsonObject()) {
                JsonObject jo = jp.getAsJsonObject();
                if (jo.has("times") && jo.get("times").isJsonArray()) {
                    JsonArray ja = jo.get("times").getAsJsonArray();
                    times = new long[ja.size()];
                    for (int i = 0; i < ja.size(); i++) {
                        times[i] = ja.get(i).getAsLong();
                    }
                }
            }

            jr.close();

            // Write the times
            JsonWriter jw = new JsonWriter(new FileWriter(TIMES_FILE));
            jw.setIndent("  ");
            jw.beginObject();

            jw.name("times");
            jw.beginArray();
            // Only keep 3 times
            if (times.length > 2) {
                for (int i = times.length - 2; i < times.length; i++) {
                    jw.value(times[i]);
                }
            } else {
                for (long time : times) {
                    jw.value(time);
                }
            }
            jw.value(startupTime);
            jw.endArray();

            jw.endObject();
            jw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
