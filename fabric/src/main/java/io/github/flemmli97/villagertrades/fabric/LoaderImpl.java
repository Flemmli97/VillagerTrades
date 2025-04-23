package io.github.flemmli97.villagertrades.fabric;

import io.github.flemmli97.villagertrades.LoaderHandler;
import io.github.flemmli97.villagertrades.VillagerTrades;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class LoaderImpl implements LoaderHandler {

    @Override
    public boolean hasPerm(CommandSourceStack src, String perm, boolean adminCmd) {
        if (VillagerTrades.PERMISSION_API) {
            return Permissions.check(src, perm, !adminCmd ? 0 : 2);
        }
        return LoaderHandler.super.hasPerm(src, perm, adminCmd);
    }

    @Override
    public boolean hasPerm(ServerPlayer player, String perm, boolean adminCmd) {
        if (VillagerTrades.PERMISSION_API) {
            return Permissions.check(player, perm, !adminCmd ? 0 : 2);
        }
        return LoaderHandler.super.hasPerm(player, perm, adminCmd);
    }
}
