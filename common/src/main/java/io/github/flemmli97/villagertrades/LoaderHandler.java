package io.github.flemmli97.villagertrades;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface LoaderHandler {

    Path getConfigPath();

    default boolean hasPerm(CommandSourceStack src, String perm) {
        return this.hasPerm(src, perm, false);
    }

    boolean hasPerm(CommandSourceStack src, String perm, boolean adminCmd);

    boolean hasPerm(ServerPlayer src, String perm, boolean adminCmd);
}
