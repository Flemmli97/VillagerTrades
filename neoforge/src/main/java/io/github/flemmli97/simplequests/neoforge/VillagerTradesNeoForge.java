package io.github.flemmli97.simplequests.neoforge;

import io.github.flemmli97.villagertrades.TraderCommand;
import io.github.flemmli97.villagertrades.VillagerTrades;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(value = VillagerTrades.MODID)
public class VillagerTradesNeoForge {

    public VillagerTradesNeoForge() {
        VillagerTrades.updateLoaderImpl(new LoaderImpl());
        NeoForge.EVENT_BUS.addListener(VillagerTradesNeoForge::command);
        VillagerTrades.FTB_RANKS = ModList.get().isLoaded("ftbranks");
    }

    public static void command(RegisterCommandsEvent event) {
        TraderCommand.register(event.getDispatcher());
    }
}
