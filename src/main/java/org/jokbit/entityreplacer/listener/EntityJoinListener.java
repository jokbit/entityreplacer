package org.jokbit.entityreplacer.listener;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jokbit.entityreplacer.datarecords.SimpleEntityInfo;
import org.jokbit.entityreplacer.manager.ReplacerSpawnManager;
import org.slf4j.Logger;


public class EntityJoinListener {

    private static final EntityJoinListener INSTANCE = new EntityJoinListener();

    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean isRegistered = false;

    private EntityJoinListener() {

    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        try {
            filterEntityJoin(event);
        } catch (Exception e) {
            LOGGER.error("filterEntityJoin err: {}, strace: {}", e.getMessage(), e.getStackTrace());
        }
    }

    public void filterEntityJoin(EntityJoinLevelEvent event) {
        Entity replacedEntity = event.getEntity();
        if (!(replacedEntity instanceof LivingEntity)) {
            return;
        }

        if (event.loadedFromDisk()) {
            return;
        }

        String mobId = EntityType.getKey(replacedEntity.getType()).toString();
        if (!ReplacerSpawnManager.getInstance().testJoin(mobId)) {
            return;
        }

        event.setCanceled(true);

        SimpleEntityInfo replaced = new SimpleEntityInfo(
                mobId,
                replacedEntity.getX(),
                replacedEntity.getY(),
                replacedEntity.getZ(),
                replacedEntity.saveWithoutId(new CompoundTag()),
                replacedEntity.level().dimension()
        );

        ReplacerSpawnManager.getInstance().catchJoinedReplaced(replaced);

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
