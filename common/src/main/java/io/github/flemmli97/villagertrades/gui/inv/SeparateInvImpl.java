package io.github.flemmli97.villagertrades.gui.inv;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

public class SeparateInvImpl extends SimpleContainer implements SeparateInv {

    private final IntPredicate allowedSlots;

    public SeparateInvImpl(int size, IntPredicate allowedSlots) {
        super(size);
        this.allowedSlots = allowedSlots;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (this.allowedSlots.test(slot))
            return super.getItem(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> removeAllItems() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (this.allowedSlots.test(slot))
            return super.removeItem(slot, amount);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemType(Item item, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack addItem(ItemStack stack) {
        return super.addItem(stack);
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (this.allowedSlots.test(slot))
            return super.removeItemNoUpdate(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.allowedSlots.test(slot))
            super.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void updateStack(int slot, ItemStack stack) {
        super.setItem(slot, stack);
    }

    @Override
    public ItemStack getActualStack(int slot) {
        return super.getItem(slot);
    }
}
