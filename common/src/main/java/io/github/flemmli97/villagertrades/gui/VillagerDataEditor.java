package io.github.flemmli97.villagertrades.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.MerchantOffer;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;

public class VillagerDataEditor extends ServerOnlyScreenHandler implements TradeEditor.MerchantDataBacktrack {

    private static final Comparator<ResourceLocation> RESOURCE_LOCATION_COMPARATOR = (f, s) -> {
        if (f.getNamespace().equals(s.getNamespace()))
            return f.getPath().compareTo(s.getPath());
        if (f.getNamespace().equals("minecraft"))
            return -1;
        return f.getNamespace().compareTo(s.getNamespace());
    };

    private final AbstractVillager villager;
    private final int page;
    private final List<MerchantOffer> currentOffers;
    private final boolean changed;

    private int professionIdx = -1;
    private final List<VillagerProfession> professions;

    protected VillagerDataEditor(int syncId, Inventory playerInventory, AbstractVillager villager, int page, List<MerchantOffer> currentOffers, boolean changed) {
        super(syncId, playerInventory, 1);
        this.villager = villager;
        this.page = page;
        this.currentOffers = currentOffers;
        this.changed = changed;
        this.professions = BuiltInRegistries.VILLAGER_PROFESSION.holders()
                .sorted(Comparator.comparing(h -> h.key().location(), RESOURCE_LOCATION_COMPARATOR))
                .map(Holder.Reference::value)
                .filter(profession -> profession != VillagerProfession.NONE && profession != VillagerProfession.NITWIT).toList();
        if (this.villager instanceof Villager v) {
            for (int i = 0; i < this.professions.size(); i++) {
                if (this.professions.get(i) == v.getVillagerData().getProfession()) {
                    this.professionIdx = i;
                    break;
                }
            }
        }
        this.update();
    }

    public static void openGui(ServerPlayer player, AbstractVillager villager, int page, List<MerchantOffer> currentOffers, boolean changed) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new VillagerDataEditor(syncId, inv, villager, page, currentOffers, changed);
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("villagertrades.gui.villager.edit.data");
            }
        };
        player.openMenu(fac);
    }

    private void update() {
        ItemStack stack = new ItemStack(Items.RED_TERRACOTTA);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.back").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        this.slots.get(0).set(stack);
        if (this.villager instanceof Villager v) {
            stack = new ItemStack(Items.LECTERN);
            stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.villager.edit.profession").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GOLD)));
            stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatableWithFallback(v.getVillagerData().getProfession().name(), StringUtils.capitalize(v.getVillagerData().getProfession().name()))
                    .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
            this.slots.get(3).set(stack);
        }
        stack = new ItemStack(Items.REDSTONE_BLOCK);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.villager.edit.reset").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.DARK_RED)));
        this.slots.get(5).set(stack);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int mouse) {
        if (index == 0) {
            TradeEditor.openFrom(player, this);
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        switch (index) {
            case 3 -> {
                if (this.villager instanceof Villager villager) {
                    TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_YES, 1, 1f);
                    this.professionIdx = (this.professionIdx + 1) % this.professions.size();
                    villager.setVillagerData(villager.getVillagerData().setProfession(this.professions.get(this.professionIdx)));
                    villager.setVillagerXp(1);
                    this.update();
                    return true;
                }
            }
            case 5 -> {
                this.villager.getOffers().clear();
                if (this.villager instanceof Villager villager) {
                    villager.releasePoi(MemoryModuleType.JOB_SITE);
                    villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NONE)
                            .setLevel(0));
                    villager.setVillagerXp(0);
                    villager.refreshBrain((ServerLevel) villager.level());
                }
                player.closeContainer();
                TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_YES, 1, 1f);
                return true;
            }
        }
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 3 || slot == 5;
    }

    @Override
    public TradeEditor.MerchantData getMerchantData() {
        return new TradeEditor.MerchantData(this.villager, this.currentOffers, this.page, this.changed);
    }
}
