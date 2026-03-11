package org.jokbit.entityreplacer.listener;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jokbit.entityreplacer.datarecords.SimpleEntityInfo;
import org.jokbit.entityreplacer.manager.ReplacerSpawnManager;
import org.slf4j.Logger;


public class MobSpawnListener {

    private static final MobSpawnListener INSTANCE = new MobSpawnListener();

    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean isRegistered = false;

    private MobSpawnListener() {

    }

    @SubscribeEvent
    public void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        try {
            filterMobSpawn(event);
        } catch (Exception e) {
            LOGGER.error("filterMobSpawn err: {}, strace: {}", e.getMessage(), e.getStackTrace());
        }
    }

    public void filterMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob replacedEntity = event.getEntity();

        if (replacedEntity.getTags().contains(ReplacerSpawnManager.KEY_REPLACED_TAG)) {
            return;
        }

        String mobId = EntityType.getKey(replacedEntity.getType()).toString();
        if (!ReplacerSpawnManager.getInstance().testSpawn(mobId)) {
            return;
        }

        event.setCanceled(true);
        event.setSpawnCancelled(true);

        SimpleEntityInfo replaced = new SimpleEntityInfo(
                mobId,
                replacedEntity.getX(),
                replacedEntity.getY(),
                replacedEntity.getZ(),
                replacedEntity.saveWithoutId(new CompoundTag()),
                replacedEntity.level().dimension()
        );

        ReplacerSpawnManager.getInstance().catchSpawnedReplaced(replaced);
    }


    public static void register() {
        if (isRegistered) {
            return;
        }
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        isRegistered = true;
    }

    public static void unRegister() {
        if (!isRegistered) {
            return;
        }
        MinecraftForge.EVENT_BUS.unregister(INSTANCE);
        isRegistered = false;
    }
}
