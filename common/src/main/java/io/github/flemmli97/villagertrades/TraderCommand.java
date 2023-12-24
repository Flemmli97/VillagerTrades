package io.github.flemmli97.villagertrades;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.villagertrades.config.ConfigHandler;
import io.github.flemmli97.villagertrades.gui.TradeEditor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;

public class TraderCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("villagertrades")
                .then(Commands.literal("edit").requires(ctx -> VillagerTrades.getHandler().hasPerm(ctx, TraderCommandPerms.TRADE_COMMAND, true))
                        .then(Commands.argument("villager", EntityArgument.entities())
                                .executes(TraderCommand::editVillager))));
    }

    private static int editVillager(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Entity entity = EntityArgument.getEntity(ctx, "villager");
        if (!(entity instanceof AbstractVillager villager)) {
            ctx.getSource().sendFailure(Component.translatable(ConfigHandler.LANG.get("villagertrades.command.not.villager")).withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
        TradeEditor.openGui(player, villager);
        return Command.SINGLE_SUCCESS;
    }

}
