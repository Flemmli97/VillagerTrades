package io.github.flemmli97.villagertrades.neoforge.data;

import com.google.common.collect.ImmutableSet;
import io.github.flemmli97.linguabib.api.ServerLangGen;
import io.github.flemmli97.villagertrades.VillagerTrades;
import net.minecraft.data.PackOutput;

import java.util.HashSet;
import java.util.Set;

public class ENLangGen extends ServerLangGen {

    private final Set<String> keys = new HashSet<>();

    public ENLangGen(PackOutput output) {
        super(output, VillagerTrades.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("villagertrades.command.not.villager", "Entity is not a villager!");
        this.add("villagertrades.gui.trade.edit", "Click to edit Offer");
        this.add("villagertrades.gui.close", "Close");
        this.add("villagertrades.gui.next", "Next");
        this.add("villagertrades.gui.back", "Back");
        this.add("villagertrades.gui.previous", "Previous");
        this.add("villagertrades.gui.trade.edit.infinite", "Infinite Trades");
        this.add("villagertrades.gui.trade.edit.uses", "Uses Left: %s - Max Uses: %s");
        this.add("villagertrades.gui.trade.edit.xp", "Reward XP: %s - Amount: %s");
        this.add("villagertrades.gui.trade.edit.demand", "Demand: %s");
        this.add("villagertrades.gui.trade.edit.price", "Price Multiplier: %s - Special Multiplier: %s");

        this.add("villagertrades.gui.villager.edit.data", "Edit villager data");
        this.add("villagertrades.gui.villager.edit.profession", "Edit villager profession");
        this.add("villagertrades.gui.villager.edit.reset", "Reset villager trades");

        this.add("villagertrades.gui.offer.edit", "Edit offer");
        this.add("villagertrades.gui.offer.edit.uses", "Edit uses");
        this.add("villagertrades.gui.offer.edit.maxUses", "Edit max uses");
        this.add("villagertrades.gui.offer.edit.infinite", "Toggle infinite uses");
        this.add("villagertrades.gui.offer.edit.rewardExp", "Reward XP");
        this.add("villagertrades.gui.offer.edit.xp", "Edit xp amount");
        this.add("villagertrades.gui.offer.edit.specialPriceDiff", "Edit special price");
        this.add("villagertrades.gui.offer.edit.demand", "Edit current demand");
        this.add("villagertrades.gui.offer.edit.priceMultiplier", "Edit price multiplier");

        this.add("villagertrades.gui.offer.tooltip.invalid", "Invalid Offer. Price and Result item cannot be empty!");
        this.add("villagertrades.gui.offer.tooltip.uses", "Uses: %s");
        this.add("villagertrades.gui.offer.tooltip.maxUses", "Max Uses: %s");
        this.add("villagertrades.gui.offer.tooltip.infinite", "Infinite: %s");
        this.add("villagertrades.gui.offer.tooltip.rewardExp", "Should reward XP: %s");
        this.add("villagertrades.gui.offer.tooltip.xp", "XP amount: %s");
        this.add("villagertrades.gui.offer.tooltip.specialPriceDiff", "Special price diff: %s");
        this.add("villagertrades.gui.offer.tooltip.demand", "Demand: %s");
        this.add("villagertrades.gui.offer.tooltip.priceMultiplier", "Price Multiplier: %s");

        this.add("villagertrades.gui.string.result", "Edit price multiplier");
        this.add("villagertrades.gui.true", "True");
        this.add("villagertrades.gui.false", "False");
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

    public Set<String> allKeys() {
        return ImmutableSet.copyOf(this.keys);
    }
}
