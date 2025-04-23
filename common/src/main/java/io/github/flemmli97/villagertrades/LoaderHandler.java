package io.github.flemmli97.villagertrades;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public interface LoaderHandler {

    default boolean hasPerm(CommandSourceStack src, String perm) {
        return this.hasPerm(src, perm, false);
    }

    default boolean hasPerm(CommandSourceStack src, String perm, boolean adminCmd) {
        if (VillagerTrades.FTB_RANKS && src.getEntity() instanceof ServerPlayer player) {
            return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(src.hasPermission(!adminCmd ? 0 : 2));
        }
        return src.hasPermission(!adminCmd ? 0 : 2);
    }

    default boolean hasPerm(ServerPlayer player, String perm, boolean adminCmd) {
        if (VillagerTrades.FTB_RANKS) {
            return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(player.hasPermissions(!adminCmd ? 0 : 2));
        }
        return player.hasPermissions(!adminCmd ? 0 : 2);
    }
}
