package org.jokbit.entityreplacer.util;

import net.minecraft.network.chat.Component;

public class I18n {

    public static String t(String message){
        return Component.translatable(message).getString();
    }

    public static final String NO_CONFIG_FILE = "tip.insconfig.no_config_file";

    public static final String SUCC_TO_CONFIG = "tip.insconfig.succ_to_config_files";

    public static final String FAIL_TO_CONFIG = "tip.insconfig.fail_to_config_files";

}