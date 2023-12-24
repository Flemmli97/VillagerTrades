package io.github.flemmli97.villagertrades.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static LangManager LANG;

    public static void init() {
        LANG = new LangManager();
        reloadConfigs();
    }

    public static void reloadConfigs() {
        LANG.reload();
    }
}
