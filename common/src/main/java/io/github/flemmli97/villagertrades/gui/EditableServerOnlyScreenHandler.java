package io.github.flemmli97.villagertrades.gui;

import io.github.flemmli97.villagertrades.gui.inv.SeparateInv;
import io.github.flemmli97.villagertrades.gui.inv.SeparateInvImpl;
import io.github.flemmli97.villagertrades.gui.inv.SlotDelegate;
import io.github.flemmli97.villagertrades.mixin.AbstractContainerAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntPredicate;

public abstract class EditableServerOnlyScreenHandler<T> extends AbstractContainerMenu {

    protected final int size;
    private final SeparateInvImpl inventory;
    private final IntPredicate allowed;

    protected EditableServerOnlyScreenHandler(int syncId, Inventory playerInventory, int rows, boolean canMoveItems, IntPredicate allowedSlots, T additionalData) {
        super(fromRows(rows), syncId);
        int i = (rows - 4) * 18;
        this.allowed = allowedSlots;
        this.inventory = new SeparateInvImpl(rows * 9, allowedSlots);
        this.size = this.inventory.getContainerSize();
        this.fillInventoryWith(playerInventory.player, this.inventory, additionalData);
        int n;
        int m;
        for (n = 0; n < rows; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new SlotDelegate(this.inventory, m + n * 9, 8 + m * 18, 18 + n * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return canMoveItems;
                    }

                    @Override
                    public boolean mayPickup(Player playerEntity) {
                        return canMoveItems;
                    }
                });
            }
        }

        for (n = 0; n < 3; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));
            }
        }

        for (n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i));
        }
    }

    private static MenuType<ChestMenu> fromRows(int rows) {
        return switch (rows) {
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_9x1;
        };
    }

    protected abstract void fillInventoryWith(Player player, SeparateInv inv, T additionalData);

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int i, int j, ClickType actionType, Player playerEntity) {
        if (i == -999) {
            super.clicked(i, j, actionType, playerEntity);
            return;
        }
        if (i < 0)
            return;
        if (i > 54 || this.allowed.test(i))
            super.clicked(i, j, actionType, playerEntity);
        Slot slot = this.slots.get(i);
        if (this.isRightSlot(i)) {
            if (((AbstractContainerAccessor) this).containerSync() != null)
                ((AbstractContainerAccessor) this).containerSync().sendCarriedChange(this, this.getCarried().copy());
            this.handleSlotClicked((ServerPlayer) playerEntity, i, slot, j);
        }
        ItemStack stack = slot.getItem().copy();
        for (ContainerListener listener : ((AbstractContainerAccessor) this).listeners())
            listener.slotChanged(this, i, stack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0)
            return ItemStack.EMPTY;
        if (index < 54 && !this.allowed.test(index)) {
            Slot slot = this.slots.get(index);
            if (this.isRightSlot(index))
                this.handleSlotClicked((ServerPlayer) player, index, slot, 0);
            return slot.getItem().copy();
        }
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < 54 ? !this.moveItemStackTo(itemStack2, 54, this.slots.size(), true)
                    : !this.moveToExceptAndUpdate((ServerPlayer) player, itemStack2, 18, 54, false, idx -> {
                int modI = idx % 9;
                return modI == 2 || modI == 4 || modI == 7;
            })) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        if (this.isRightSlot(index))
            this.handleSlotClicked((ServerPlayer) player, index, slot, 0);
        return itemStack;
    }

    private boolean moveToExceptAndUpdate(ServerPlayer player, ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, IntPredicate excluded) {
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                if (!excluded.test(i)) {
                    slot = this.slots.get(i);
                    itemStack = slot.getItem();
                    if (!itemStack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemStack)) {
                        int j = itemStack.getCount() + stack.getCount();
                        if (j <= stack.getMaxStackSize()) {
                            stack.setCount(0);
                            itemStack.setCount(j);
                            slot.setChanged();
                            this.handleSlotClicked(player, i, slot, 0);
                            bl = true;
                        } else if (itemStack.getCount() < stack.getMaxStackSize()) {
                            stack.shrink(stack.getMaxStackSize() - itemStack.getCount());
                            itemStack.setCount(stack.getMaxStackSize());
                            slot.setChanged();
                            this.handleSlotClicked(player, i, slot, 0);
                            bl = true;
                        }
                    }
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (reverseDirection ? i >= startIndex : i < endIndex) {
                if (!excluded.test(i)) {
                    slot = this.slots.get(i);
                    itemStack = slot.getItem();
                    if (itemStack.isEmpty() && slot.mayPlace(stack)) {
                        if (stack.getCount() > slot.getMaxStackSize()) {
                            slot.setByPlayer(stack.split(slot.getMaxStackSize()));
                        } else {
                            slot.setByPlayer(stack.split(stack.getCount()));
                        }
                        slot.setChanged();
                        this.handleSlotClicked(player, i, slot, 0);
                        bl = true;
                        break;
                    }
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return bl;
    }

    protected abstract boolean isRightSlot(int slot);

    /**
     * @param clickType 0 for left click, 1 for right click
     */
    protected abstract boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType);
}
