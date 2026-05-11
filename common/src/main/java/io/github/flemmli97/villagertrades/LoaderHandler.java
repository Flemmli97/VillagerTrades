package io.github.flemmli97.villagertrades;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.Mth;

public interface LoaderHandler {

    static PermissionLevel permissionOfLevel(int level) {
        return PermissionLevel.byId(Mth.clamp(level, 0, PermissionLevel.OWNERS.id()));
    }

    static boolean hasPermissionOfLevel(PermissionSet set, int level) {
        return set.hasPermission(new Permission.HasCommandLevel(permissionOfLevel(level)));
    }

    default boolean hasPerm(CommandSourceStack src, String perm) {
        return this.hasPerm(src, perm, false);
    }

    default boolean hasPerm(CommandSourceStack src, String perm, boolean adminCmd) {
        if (VillagerTrades.FTB_RANKS && src.getEntity() instanceof ServerPlayer player) {
            return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(hasPermissionOfLevel(src.permissions(), !adminCmd ? 0 : 2));
        }
        return hasPermissionOfLevel(src.permissions(), !adminCmd ? 0 : 2);
    }

    default boolean hasPerm(ServerPlayer player, String perm, boolean adminCmd) {
        if (VillagerTrades.FTB_RANKS) {
            return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(hasPermissionOfLevel(player.permissions(), !adminCmd ? 0 : 2));
        }
        return hasPermissionOfLevel(player.permissions(), !adminCmd ? 0 : 2);
    }
}
