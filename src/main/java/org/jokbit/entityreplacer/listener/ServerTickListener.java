package org.jokbit.entityreplacer.listener;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jokbit.entityreplacer.manager.ReplacerSpawnManager;
import org.slf4j.Logger;

public class ServerTickListener {

    private static final ServerTickListener SERVER_TICK_LISTENER = new ServerTickListener();

    public static boolean isRegistered = false;

    private ServerTickListener() {

    }

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient()) {
            return;
        }
        if (!ReplacerSpawnManager.getInstance().hasReplacerWaitToSummon()) {
            return;
        }

        try {
            doSpawn(event);
        } catch (Exception e) {
            LOGGER.error("doSpawn err:{}, {}", e.getMessage(), e.getStackTrace());
        }

    }

    private void doSpawn(TickEvent.ServerTickEvent event) {
        ReplacerSpawnManager.getInstance().pollReplacer()
                .ifPresent(replacer -> {
                    ServerLevel serverLevel = event.getServer().getLevel(replacer.levelKey());
                    if (serverLevel == null) {
                        return;
                    }
                    EntityType.byString(replacer.mobId())
                            .map(entityType -> entityType.create(serverLevel))
                            .map(replacerEntity -> {
                                CompoundTag compoundTag = new CompoundTag();
                                replacerEntity.saveWithoutId(compoundTag);
                                compoundTag.merge(replacer.nbt());
                                replacerEntity.load(compoundTag);
                                replacerEntity.setPos(replacer.x(), replacer.y(), replacer.z());
                                return replacerEntity;
                            })
                            .ifPresent(serverLevel::addFreshEntity);
                });
    }

    public static void register() {
        if (isRegistered) {
            return;
        }
        MinecraftForge.EVENT_BUS.register(SERVER_TICK_LISTENER);
        isRegistered = true;
    }

    public static void unRegister() {
        if (!isRegistered) {
            return;
        }
        MinecraftForge.EVENT_BUS.unregister(SERVER_TICK_LISTENER);
        isRegistered = false;

    }

}
