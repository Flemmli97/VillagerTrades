package io.github.flemmli97.simplequests.forge.data;

import io.github.flemmli97.linguabib.api.ServerLangGen;
import io.github.flemmli97.villagertrades.VillagerTrades;
import net.minecraft.data.PackOutput;

import java.util.HashSet;
import java.util.Set;

/**
 * Lang gen for other language using en as verification
 * To use
 * 1. Extend this class and implement addTranslationsFor
 * 2. Add all translations
 * 3. Add the instance to {@link DataEvent#data(net.neoforged.neoforge.data.event.GatherDataEvent)}
 */
public abstract class TranslationLangGen extends ServerLangGen {

    private final Set<String> keys = new HashSet<>();

    private final ENLangGen enLang;

    public TranslationLangGen(PackOutput output, String locale, ENLangGen enLang) {
        super(output, VillagerTrades.MODID, locale);
        this.enLang = enLang;
    }

    protected abstract void addTranslationsFor();

    @Override
    protected final void addTranslations() {
        this.addTranslationsFor();
        this.verify();
    }

    @Override
    public void add(String key, String value) {
        super.add(key, value);
        this.keys.add(key);
    }

    @Override
    public void add(String key, String... lines) {
        super.add(key, lines);
        this.keys.add(key);
    }

    protected void verify() {
        Set<String> missing = new HashSet<>();
        this.enLang.allKeys().forEach(key -> {
            if (!this.keys.contains(key))
                missing.add(key);
        });
        if (!missing.isEmpty()) {
            VillagerTrades.LOGGER.error("Missing translation for {}", missing);
        }
    }
}
