package io.itch.aktomik.deadcorpses.pdc;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemArrayDataType implements PersistentDataType<byte[], ItemStack[]> {

	public static final ItemArrayDataType INSTANCE = new ItemArrayDataType();

	private ItemArrayDataType() {}// singleton

	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	public Class<ItemStack[]> getComplexType() {
		return ItemStack[].class;
	}

	@Override
	public byte[] toPrimitive(ItemStack[] complex, PersistentDataAdapterContext context) {
		return ItemStack.serializeItemsAsBytes(complex);
	}

	@Override
	public ItemStack[] fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
		return ItemStack.deserializeItemsFromBytes(primitive);
	}
}
