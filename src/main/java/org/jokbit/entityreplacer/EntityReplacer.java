package org.jokbit.entityreplacer;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jokbit.entityreplacer.listener.EntityLoginListener;
import org.jokbit.entityreplacer.listener.ServerTickListener;
import org.jokbit.entityreplacer.manager.EntitySpawnManager;
import org.slf4j.Logger;

@Mod(org.jokbit.entityreplacer.EntityReplacer.MODID)
public class EntityReplacer {
    public static final String MODID = "entityreplacer";

    private static final Logger LOGGER = LogUtils.getLogger();

    public EntityReplacer(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 初始化配置
        Config.mkConfigDir();

        if (Config.readReplaceData()) {
            EntitySpawnManager.getInstance();
        }

        if (EntitySpawnManager.getInstance().needReplaceEntity()) {
            EntityLoginListener.register();
            ServerTickListener.register();
        }
    }
}
