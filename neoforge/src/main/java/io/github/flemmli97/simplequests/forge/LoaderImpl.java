package io.github.flemmli97.simplequests.forge;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import io.github.flemmli97.villagertrades.LoaderHandler;
import io.github.flemmli97.villagertrades.VillagerTrades;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class LoaderImpl implements LoaderHandler {

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean hasPerm(CommandSourceStack src, String perm, boolean adminCmd) {
        if (VillagerTrades.FTB_RANKS && src.getEntity() instanceof ServerPlayer player) {
            return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(src.hasPermission(!adminCmd ? 0 : 2));
        }
        return src.hasPermission(!adminCmd ? 0 : 2);
    }

    @Override
    public boolean hasPerm(ServerPlayer player, String perm, boolean adminCmd) {
        if (VillagerTrades.FTB_RANKS) {
            return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(player.hasPermissions(!adminCmd ? 0 : 2));
        }
        return player.hasPermissions(!adminCmd ? 0 : 2);
    }
}
