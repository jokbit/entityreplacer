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
import org.apache.commons.lang3.tuple.Pair;
import org.jokbit.entityreplacer.datarecords.ReplaceData;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = EntityReplacer.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String ENTITY_REPLACER = "entityreplacer";

    public static final String REPLACE = "replace.json";

    public static final Path ENTITY_REPLACER_CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(ENTITY_REPLACER);

    public static final Path JSON_FILES_Path = FMLPaths.CONFIGDIR.get().resolve(ENTITY_REPLACER).resolve(REPLACE);

    private static final Set<ReplaceData> REPLACE_DATA = Sets.newHashSet();

    private static final Gson GSON = new Gson();

    private static final TypeToken<Set<ReplaceData>> REPLACER_DATA_TYPE_TOKEN = new TypeToken<>() {
    };

    public static Set<ReplaceData> getReplaceData() {
        return REPLACE_DATA;
    }

    public static void mkConfigDir() {
        if (Files.notExists(ENTITY_REPLACER_CONFIG_PATH) || !Files.isDirectory(ENTITY_REPLACER_CONFIG_PATH)) {
            try {
                Files.createDirectory(ENTITY_REPLACER_CONFIG_PATH);
                Files.createFile(JSON_FILES_Path);
                Files.writeString(JSON_FILES_Path, "[{}]");
            } catch (IOException e) {
                LOGGER.error("mkdir entityreplacer failed err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    public static Pair<Set<Path>, Set<Path>> readReplaceData() {
        REPLACE_DATA.clear();
        Set<Path> allJson = Sets.newHashSet();
        Set<Path> successSet = Sets.newHashSet();
        try (Stream<Path> pathStream = Files.walk(ENTITY_REPLACER_CONFIG_PATH)) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        allJson.add(path);
                        try {
                            String content = Files.readString(path);
                            Set<ReplaceData> replaceDataSet = GSON.fromJson(content, REPLACER_DATA_TYPE_TOKEN)
                                    .stream()
                                    .filter(replaceData -> !replaceData.replaceds().isEmpty())
                                    .filter(replaceData -> !replaceData.replacers().isEmpty())
                                    .collect(Collectors.toUnmodifiableSet());
                            REPLACE_DATA.addAll(replaceDataSet);
                            successSet.add(path);
                        } catch (IOException e) {
                            LOGGER.error("read replace json err: {}, strace: {}", e.getMessage(), e.getStackTrace());
                        } catch (JsonSyntaxException e) {
                            LOGGER.error("format json err: {}, strace: {}", e.getMessage(), e.getStackTrace());
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("read json files err: {}, strace: {}", e.getMessage(), e.getStackTrace());
        }

        return Pair.of(allJson, successSet);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

    }
}
