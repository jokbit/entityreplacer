package org.jokbit.entityreplacer.listener;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jokbit.entityreplacer.datarecords.ReplacerSpawnInfo;
import org.jokbit.entityreplacer.datarecords.SimpleEntityInfo;
import org.jokbit.entityreplacer.manager.EntitySpawnManager;


public class EntityLoginListener {

    private static final EntityLoginListener INSTANCE = new EntityLoginListener();

    private EntityLoginListener() {

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        String mobId = EntityType.getKey(entity.getType()).toString();
        if (!EntitySpawnManager.getInstance().containReplaced(mobId)) {
            return;
        }

        EntitySpawnManager.getInstance().findReplaceInfoByMobId(mobId)
                .ifPresent((replaceInfo -> {
                    if (!replaceInfo.existed() && event.loadedFromDisk()) {
                        return;
                    }
                    double chance = replaceInfo.chance();
                    if (Math.random() > chance) {
                        return;
                    }
                    if (!replaceInfo.additionMode()) {
                        if (event.isCancelable()) {
                            event.setCanceled(true);
                            event.getEntity().discard();
                        }
                    }
                    ReplacerSpawnInfo replacerSpawnInfo = new ReplacerSpawnInfo(
                            new SimpleEntityInfo(
                                    mobId,
                                    livingEntity.getX(),
                                    livingEntity.getY(),
                                    livingEntity.getZ(),
                                    livingEntity.level().dimension()),
                            replaceInfo);
                    EntitySpawnManager.getInstance().offer(replacerSpawnInfo);
                }));
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }
}
