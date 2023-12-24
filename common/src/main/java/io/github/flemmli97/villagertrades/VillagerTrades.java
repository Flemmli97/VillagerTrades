package io.github.flemmli97.villagertrades;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class VillagerTrades {

    public static final String MODID = "villagertrades";

    public static final Logger LOGGER = LogManager.getLogger("villagertrades");

    private static LoaderHandler HANDLER;

    public static boolean FTB_RANKS;
    public static boolean PERMISSION_API;

    public static void updateLoaderImpl(LoaderHandler impl) {
        HANDLER = impl;
    }

    public static LoaderHandler getHandler() {
        return HANDLER;
    }

    public static void addLore(ItemStack stack, List<Component> components) {
        ListTag lore = new ListTag();
        components.forEach(c -> lore.add(StringTag.valueOf(Component.Serializer.toJson(c))));
        stack.getOrCreateTagElement("display").put("Lore", lore);
    }
}
