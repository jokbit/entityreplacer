package org.jokbit.entityreplacer;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jokbit.entityreplacer.datarecords.ReplaceData;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = EntityReplacer.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String ENTITY_REPLACER = "entityreplacer";

    public static final String REPLACE = "replace.json";

    public static final Path ENTITY_REPLACER_CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(ENTITY_REPLACER);

    public static final Path REPLACE_PATH = FMLPaths.CONFIGDIR.get().resolve(ENTITY_REPLACER).resolve(REPLACE);

    private static final Set<ReplaceData> REPLACE_DATA = Sets.newHashSet();

    public static Set<ReplaceData> getReplaceData() {
        return REPLACE_DATA;
    }

    public static void mkConfigDir() {
        if (Files.notExists(ENTITY_REPLACER_CONFIG_PATH) || !Files.isDirectory(ENTITY_REPLACER_CONFIG_PATH)) {
            try {
                Files.createDirectory(ENTITY_REPLACER_CONFIG_PATH);
            } catch (IOException e) {
                LOGGER.error("mkdir entityreplacer failed err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            }
        }

        if (Files.notExists(REPLACE_PATH)) {
            try {
                Files.createFile(REPLACE_PATH);
                Files.writeString(REPLACE_PATH, "[{}]", StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            } catch (IOException e) {
                LOGGER.error("mkf replace failed err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    public static boolean readReplaceData() {
        REPLACE_DATA.clear();
        try {
            String content = Files.readString(REPLACE_PATH);
            REPLACE_DATA.addAll(
                    new Gson().fromJson(content, new TypeToken<Set<ReplaceData>>() {
                            })
                            .stream()
                            .filter(replaceData -> !replaceData.replaceds().isEmpty() &&
                                    !replaceData.replacers().isEmpty())
                            .collect(Collectors.toUnmodifiableSet())
            );
            return true;
        } catch (IOException e) {
            LOGGER.error("read replace json err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            return false;
        } catch (JsonSyntaxException e) {
            LOGGER.error("format json err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            return false;
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

    }
}
