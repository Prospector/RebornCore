/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore.client.containerBuilder.builder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerListener;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.util.ItemUtils;
import reborncore.mixin.extensions.ContainerExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class BuiltContainer extends Container implements IExtendedContainerListener {

	private final String name;

	private final Predicate<PlayerEntity> canInteract;
	private final List<Range<Integer>> playerSlotRanges;
	private final List<Range<Integer>> blockEntitySlotRanges;

	private final ArrayList<MutableTriple<IntSupplier, IntConsumer, Short>> shortValues;
	private final ArrayList<MutableTriple<IntSupplier, IntConsumer, Integer>> integerValues;
	private final ArrayList<MutableTriple<Supplier, Consumer, Object>> objectValues;
	private List<Consumer<CraftingInventory>> craftEvents;
	private Integer[] integerParts;

	private final MachineBaseBlockEntity blockEntity;

	public BuiltContainer(int syncID, final String name, final Predicate<PlayerEntity> canInteract,
	                      final List<Range<Integer>> playerSlotRange,
	                      final List<Range<Integer>> blockEntitySlotRange, MachineBaseBlockEntity blockEntity) {
		super(null, syncID);
		this.name = name;

		this.canInteract = canInteract;

		this.playerSlotRanges = playerSlotRange;
		this.blockEntitySlotRanges = blockEntitySlotRange;

		this.shortValues = new ArrayList<>();
		this.integerValues = new ArrayList<>();
		this.objectValues = new ArrayList<>();

		this.blockEntity = blockEntity;
	}

	public void addShortSync(final List<Pair<IntSupplier, IntConsumer>> syncables) {

		for (final Pair<IntSupplier, IntConsumer> syncable : syncables) {
			this.shortValues.add(MutableTriple.of(syncable.getLeft(), syncable.getRight(), (short) 0));
		}
		this.shortValues.trimToSize();
	}

	public void addIntegerSync(final List<Pair<IntSupplier, IntConsumer>> syncables) {

		for (final Pair<IntSupplier, IntConsumer> syncable : syncables) {
			this.integerValues.add(MutableTriple.of(syncable.getLeft(), syncable.getRight(), 0));
		}
		this.integerValues.trimToSize();
		this.integerParts = new Integer[this.integerValues.size()];
	}

	public void addObjectSync(final List<Pair<Supplier, Consumer>> syncables) {

		for (final Pair<Supplier, Consumer> syncable : syncables) {
			this.objectValues.add(MutableTriple.of(syncable.getLeft(), syncable.getRight(), null));
		}
		this.objectValues.trimToSize();
	}

	public void addCraftEvents(final List<Consumer<CraftingInventory>> craftEvents) {
		this.craftEvents = craftEvents;
	}

	@Override
	public boolean canUse(final PlayerEntity playerIn) {
		return this.canInteract.test(playerIn);
	}

	@Override
	public final void onContentChanged(final Inventory inv) {
		if (!this.craftEvents.isEmpty()) {
			this.craftEvents.forEach(consumer -> consumer.accept((CraftingInventory) inv));
		}
	}

	@Override
	public void sendContentUpdates() {
		super.sendContentUpdates();

		for (final ContainerListener listener : ContainerExtensions.get(this).getListeners()) {

			int i = 0;
			if (!this.shortValues.isEmpty()) {
				for (final MutableTriple<IntSupplier, IntConsumer, Short> value : this.shortValues) {
					final short supplied = (short) value.getLeft().getAsInt();
					if (supplied != value.getRight()) {

						listener.onContainerPropertyUpdate(this, i, supplied);
						value.setRight(supplied);
					}
					i++;
				}
			}

			if (!this.integerValues.isEmpty()) {
				for (final MutableTriple<IntSupplier, IntConsumer, Integer> value : this.integerValues) {
					final int supplied = value.getLeft().getAsInt();
					if (supplied != value.getRight()) {

						listener.onContainerPropertyUpdate(this, i, supplied >> 16);
						listener.onContainerPropertyUpdate(this, i + 1, (short) (supplied & 0xFFFF));
						value.setRight(supplied);
					}
					i += 2;
				}
			}

			if (!this.objectValues.isEmpty()) {
				int objects = 0;
				for (final MutableTriple<Supplier, Consumer, Object> value : this.objectValues) {
					final Object supplied = value.getLeft().get();
					if(supplied != value.getRight()){
						sendObject(listener,this, objects, supplied);
						value.setRight(supplied);
					}
					objects++;
				}
			}
		}
	}

	@Override
	public void addListener(final ContainerListener listener) {
		super.addListener(listener);

		int i = 0;
		if (!this.shortValues.isEmpty()) {
			for (final MutableTriple<IntSupplier, IntConsumer, Short> value : this.shortValues) {
				final short supplied = (short) value.getLeft().getAsInt();

				listener.onContainerPropertyUpdate(this, i, supplied);
				value.setRight(supplied);
				i++;
			}
		}

		if (!this.integerValues.isEmpty()) {
			for (final MutableTriple<IntSupplier, IntConsumer, Integer> value : this.integerValues) {
				final int supplied = value.getLeft().getAsInt();

				listener.onContainerPropertyUpdate(this, i, supplied >> 16);
				listener.onContainerPropertyUpdate(this, i + 1, (short) (supplied & 0xFFFF));
				value.setRight(supplied);
				i += 2;
			}
		}

		if (!this.objectValues.isEmpty()) {
			int objects = 0;
			for (final MutableTriple<Supplier, Consumer, Object> value : this.objectValues) {
				final Object supplied = value.getLeft();
				sendObject(listener, this, objects, ((Supplier) supplied).get());
				value.setRight(supplied);
				objects++;
			}
		}
	}

	@Override
	public void handleObject(int var, Object value) {
		this.objectValues.get(var).getMiddle().accept(value);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void setProperties(final int id, final int value) {

		if (id < this.shortValues.size()) {
			this.shortValues.get(id).getMiddle().accept((short) value);
			this.shortValues.get(id).setRight((short) value);
		} else if (id - this.shortValues.size() < this.integerValues.size() * 2) {

			if ((id - this.shortValues.size()) % 2 == 0) {
				this.integerParts[(id - this.shortValues.size()) / 2] = value;
			} else {
				this.integerValues.get((id - this.shortValues.size()) / 2).getMiddle().accept(
					(this.integerParts[(id - this.shortValues.size()) / 2] & 0xFFFF) << 16 | value & 0xFFFF);
			}
		}
	}

	@Override
	public ItemStack transferSlot(final PlayerEntity player, final int index) {

		ItemStack originalStack = ItemStack.EMPTY;

		final Slot slot = this.slotList.get(index);

		if (slot != null && slot.hasStack()) {

			final ItemStack stackInSlot = slot.getStack();
			originalStack = stackInSlot.copy();

			boolean shifted = false;

			for (final Range<Integer> range : this.playerSlotRanges) {
				if (range.contains(index)) {

					if (this.shiftToBlockEntity(stackInSlot)) {
						shifted = true;
					}
					break;
				}
			}

			if (!shifted) {
				for (final Range<Integer> range : this.blockEntitySlotRanges) {
					if (range.contains(index)) {
						if (this.shiftToPlayer(stackInSlot)) {
							shifted = true;
						}
						break;
					}
				}
			}

			slot.onStackChanged(stackInSlot, originalStack);
			if (stackInSlot.getCount() <= 0) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
			if (stackInSlot.getCount() == originalStack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTakeItem(player, stackInSlot);
		}
		return originalStack;

	}

	protected boolean shiftItemStack(final ItemStack stackToShift, final int start, final int end) {
		boolean changed = false;
		if (stackToShift.isStackable()) {
			for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++) {
				final Slot slot = this.slotList.get(slotIndex);
				final ItemStack stackInSlot = slot.getStack();
				if (!stackInSlot.isEmpty() && ItemUtils.isItemEqual(stackInSlot, stackToShift, true, true)
					&& slot.canInsert(stackToShift)) {
					final int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
					final int max = Math.min(stackToShift.getMaxCount(), slot.getMaxStackAmount());
					if (resultingStackSize <= max) {
						stackToShift.setCount(0);
						stackInSlot.setCount(resultingStackSize);
						slot.markDirty();
						changed = true;
					} else if (stackInSlot.getCount() < max) {
						stackToShift.decrement(max - stackInSlot.getCount());
						stackInSlot.setCount(max);
						slot.markDirty();
						changed = true;
					}
				}
			}
		}
		if (stackToShift.getCount() > 0) {
			for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++) {
				final Slot slot = this.slotList.get(slotIndex);
				ItemStack stackInSlot = slot.getStack();
				if (stackInSlot.isEmpty() && slot.canInsert(stackToShift)) {
					final int max = Math.min(stackToShift.getMaxCount(), slot.getMaxStackAmount());
					stackInSlot = stackToShift.copy();
					stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
					stackToShift.decrement(stackInSlot.getCount());
					slot.setStack(stackInSlot);
					slot.markDirty();
					changed = true;
				}
			}
		}
		return changed;
	}

	private boolean shiftToBlockEntity(final ItemStack stackToShift) {
		if(!blockEntity.getOptionalInventory().isPresent()){
			return false;
		}
		for (final Range<Integer> range : this.blockEntitySlotRanges) {
			if (this.shiftItemStack(stackToShift, range.getMinimum(), range.getMaximum() + 1)) {
				return true;
			}
		}
		return false;
	}

	private boolean shiftToPlayer(final ItemStack stackToShift) {
		for (final Range<Integer> range : this.playerSlotRanges) {
			if (this.shiftItemStack(stackToShift, range.getMinimum(), range.getMaximum() + 1)) {
				return true;
			}
		}
		return false;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Slot addSlot(Slot slotIn) {
		return super.addSlot(slotIn);
	}
}
