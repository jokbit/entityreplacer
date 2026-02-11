package org.jokbit.entityreplacer;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jokbit.entityreplacer.listener.EntityJoinListener;
import org.jokbit.entityreplacer.listener.MobSpawnListener;
import org.jokbit.entityreplacer.listener.ServerTickListener;
import org.jokbit.entityreplacer.manager.ReplacerSpawnManager;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Set;

@Mod(org.jokbit.entityreplacer.EntityReplacer.MODID)
public class EntityReplacer {
    public static final String MODID = "entityreplacer";

    private static final Logger LOGGER = LogUtils.getLogger();

    public EntityReplacer(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    public static Pair<Set<Path>, Set<Path>> reload() {
        EntityJoinListener.unRegister();
        MobSpawnListener.unRegister();
        ServerTickListener.unRegister();
        Pair<Set<Path>, Set<Path>> res = load();
        if (res.getRight().isEmpty()) {
            return res;
        }
        ReplacerSpawnManager.getInstance().reload();
        return res;
    }

    public static Pair<Set<Path>, Set<Path>> load() {
        Pair<Set<Path>, Set<Path>> res = Config.readReplaceData();
        Set<Path> successSet = res.getRight();
        if (successSet.isEmpty()) {
            return res;
        }

        if (ReplacerSpawnManager.getInstance().hasSpawnReplaced()
                || ReplacerSpawnManager.getInstance().hasJoinedReplaced()) {
            ServerTickListener.register();
        }

        if (ReplacerSpawnManager.getInstance().hasSpawnReplaced()) {
            MobSpawnListener.register();
        }

        if (ReplacerSpawnManager.getInstance().hasJoinedReplaced()) {
            EntityJoinListener.register();
        }

        return res;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        Config.mkConfigDir();

        load();
    }
}
