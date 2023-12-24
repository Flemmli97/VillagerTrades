package io.github.flemmli97.simplequests.forge;

import io.github.flemmli97.villagertrades.TraderCommand;
import io.github.flemmli97.villagertrades.VillagerTrades;
import io.github.flemmli97.villagertrades.config.ConfigHandler;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(value = VillagerTrades.MODID)
public class SimpleQuestForge {

    public SimpleQuestForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "*", (s1, s2) -> true));
        VillagerTrades.updateLoaderImpl(new LoaderImpl());
        NeoForge.EVENT_BUS.addListener(SimpleQuestForge::command);
        ConfigHandler.init();
        VillagerTrades.FTB_RANKS = ModList.get().isLoaded("ftbranks");
    }

    public static void command(RegisterCommandsEvent event) {
        TraderCommand.register(event.getDispatcher());
    }
}
