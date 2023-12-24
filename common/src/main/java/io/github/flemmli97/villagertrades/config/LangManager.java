package io.github.flemmli97.villagertrades.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.flemmli97.villagertrades.VillagerTrades;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LangManager {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    @SuppressWarnings({"UnstableApiUsage"})
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private static final Map<String, String> DEFAULT_TRANSLATION = new LinkedHashMap<>();

    static {
        DEFAULT_TRANSLATION.put("villagertrades.command.not.villager", "Entity is not a villager!");
        DEFAULT_TRANSLATION.put("villagertrades.gui.trade.edit", "Click to edit Offer");
        DEFAULT_TRANSLATION.put("villagertrades.gui.close", "Close");
        DEFAULT_TRANSLATION.put("villagertrades.gui.next", "Next");
        DEFAULT_TRANSLATION.put("villagertrades.gui.back", "Back");
        DEFAULT_TRANSLATION.put("villagertrades.gui.previous", "Previous");
        DEFAULT_TRANSLATION.put("villagertrades.gui.trade.edit.infinite", "Infinite Trades");
        DEFAULT_TRANSLATION.put("villagertrades.gui.trade.edit.uses", "Uses Left: %s - Max Uses: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.trade.edit.xp", "Reward XP: %s - Amount: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.trade.edit.demand", "Demand: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.trade.edit.price", "Price Multiplier: %s - Special Multiplier: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit", "Edit offer");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.uses", "Edit uses");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.maxUses", "Edit max uses");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.infinite", "Toggle infinite uses");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.rewardExp", "Reward XP");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.xp", "Edit xp amount");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.specialPriceDiff", "Edit special price");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.demand", "Edit current demand");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.edit.priceMultiplier", "Edit price multiplier");

        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.uses", "Uses: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.maxUses", "Max Uses: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.infinite", "Infinite: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.rewardExp", "Should reward XP: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.xp", "XP amount: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.specialPriceDiff", "Special price diff: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.demand", "Demand: %s");
        DEFAULT_TRANSLATION.put("villagertrades.gui.offer.tooltip.priceMultiplier", "Price Multiplier: %s");

        DEFAULT_TRANSLATION.put("villagertrades.gui.string.result", "Edit price multiplier");
    }

    private Map<String, String> translation = new HashMap<>();

    private final Path confDir;

    public LangManager() {
        Path configDir = VillagerTrades.getHandler().getConfigPath().resolve("villagertrades").resolve("lang");
        this.confDir = configDir;
        try {
            File dir = configDir.toFile();
            if (!dir.exists())
                dir.mkdirs();
            URL url = LangManager.class.getClassLoader().getResource("data/villagertrades/lang");
            if (url != null) {
                URI uri = LangManager.class.getClassLoader().getResource("data/villagertrades/lang").toURI();
                try {
                    FileSystems.newFileSystem(uri, Collections.emptyMap());
                } catch (FileSystemAlreadyExistsException | IllegalArgumentException ignored) {
                }
                Files.walk(Path.of(uri))
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                InputStream s = Files.newInputStream(p, StandardOpenOption.READ);
                                File target = configDir.resolve(p.getFileName().toString()).toFile();
                                if (!target.exists())
                                    target.createNewFile();
                                OutputStream o = new FileOutputStream(target);
                                s.transferTo(o);
                                s.close();
                                o.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            File def = configDir.resolve("en_us.json").toFile();
            if (!def.exists()) {
                def.createNewFile();
                saveTo(def, DEFAULT_TRANSLATION);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        this.reload();
    }

    public void reload() {
        try {
            FileReader reader = new FileReader(this.confDir.resolve("en_us.json").toFile());
            this.translation = GSON.fromJson(reader, MAP_TYPE);
            reader.close();
            //en_us is basically used as a default modifiable file
            Map<String, String> ordered = new LinkedHashMap<>();
            DEFAULT_TRANSLATION.forEach((key, t) -> ordered.put(key, this.translation.getOrDefault(key, t)));
            saveTo(this.confDir.resolve("en_us.json").toFile(), ordered);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return this.translation.getOrDefault(key, DEFAULT_TRANSLATION.getOrDefault(key, key));
    }

    private static void saveTo(File file, Map<String, String> translation) {
        try {
            FileWriter writer = new FileWriter(file);
            GSON.toJson(translation, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
