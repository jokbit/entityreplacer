package org.jokbit.entityreplacer.listener;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jokbit.entityreplacer.datarecords.ReplaceInfo;
import org.jokbit.entityreplacer.datarecords.SimpleEntityInfo;
import org.jokbit.entityreplacer.manager.EntitySpawnManager;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class ServerTickListener {

    private static final ServerTickListener SERVER_TICK_LISTENER = new ServerTickListener();

    private ServerTickListener() {

    }

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!EntitySpawnManager.getInstance().hasSpawnInfo()) {
            return;
        }

        try {
            doSpawn(event);
        } catch (Exception e) {
            LOGGER.error("doSpawn err:{}, {}", e.getMessage(), e.getStackTrace());
        }

    }

    private void doSpawn(TickEvent.ServerTickEvent event) {
        EntitySpawnManager.getInstance()
                .poll()
                .ifPresent((replacerSpawnInfo -> {
                    SimpleEntityInfo originEntityInfo = replacerSpawnInfo.originEntityInfo();
                    String mobId = originEntityInfo.mobId();
                    ServerLevel level = event.getServer().getLevel(originEntityInfo.levelKey());
                    if (level == null) {
                        return;
                    }
                    ReplaceInfo replaceInfo = replacerSpawnInfo.replaceInfo();
                    int rolls = replaceInfo.rolls();
                    for (int i = 0; i < rolls; i++) {
                        EntitySpawnManager.getInstance().chooseReplacer(mobId)
                                .ifPresent((replacer -> {
                                    String replacerId = replacer.mobId();
                                    EntityType.byString(replacerId)
                                            .map(entityType -> entityType.create(level))
                                            .ifPresent(entity -> {
                                                double offsetX = ThreadLocalRandom.current()
                                                        .nextDouble(-rolls, rolls);
                                                double offsetZ = ThreadLocalRandom.current()
                                                        .nextDouble(-rolls, rolls);
                                                entity.setPos(originEntityInfo.x() + offsetX,
                                                        originEntityInfo.y(),
                                                        originEntityInfo.z() + offsetZ);
                                                CompoundTag compoundTag = new CompoundTag();
                                                entity.saveWithoutId(compoundTag);
                                                CompoundTag nbt = replacer.nbt();
                                                if (nbt != null) {
                                                    for (String key : nbt.getAllKeys()) {
                                                        Tag tag = nbt.get(key);
                                                        if (tag != null) {
                                                            compoundTag.put(key, tag);
                                                        }
                                                    }
                                                }
                                                entity.load(compoundTag);
                                                level.addFreshEntity(entity);
                                            });
                                }));
                    }
                }));
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(SERVER_TICK_LISTENER);
    }

}
