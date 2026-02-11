package org.jokbit.entityreplacer.command;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.jokbit.entityreplacer.EntityReplacer;
import org.jokbit.entityreplacer.util.I18n;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Set;

import static org.jokbit.entityreplacer.util.I18n.t;

@Mod.EventBusSubscriber(modid = EntityReplacer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReloadCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String ENTITY_REPLACER = "entityreplacer";

    private static final String RELOAD = "reload";

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(ENTITY_REPLACER)
                        .then(
                                Commands.literal(RELOAD)
                                        .executes(context -> execute(context.getSource()))
                        )
        );
    }

    public static int execute(CommandSourceStack source) {
        source.sendSystemMessage(Component.literal("entityreplacer reload..."));
        Pair<Set<Path>, Set<Path>> res = EntityReplacer.reload();
        Set<Path> allJson = res.getLeft();
        Set<Path> successSet = res.getRight();
        String message;
        if (allJson.isEmpty()) {
            message = t(I18n.NO_CONFIG_FILE);
        } else if (allJson.size() == successSet.size()) {
            message = t(I18n.SUCC_TO_CONFIG).formatted(successSet.size());
        } else {
            message = t(I18n.FAIL_TO_CONFIG).formatted(Sets.intersection(allJson, successSet));
        }
        source.sendSystemMessage(Component.literal(message));
        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

}
