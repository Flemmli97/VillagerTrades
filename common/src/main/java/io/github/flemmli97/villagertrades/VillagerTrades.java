package io.github.flemmli97.villagertrades;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VillagerTrades {

    public static final String MODID = "villagertrades";

    public static final Logger LOGGER = LogManager.getLogger("villagertrades");

    private static LoaderHandler HANDLER;

    public static boolean FTB_RANKS;
    public static boolean PERMISSION_API;

    public static void updateLoaderImpl(LoaderHandler impl) {
        HANDLER = impl;
    }

    public static LoaderHandler getHandler() {
        return HANDLER;
    }
}
