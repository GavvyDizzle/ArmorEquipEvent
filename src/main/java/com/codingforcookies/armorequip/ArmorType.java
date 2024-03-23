package com.codingforcookies.armorequip;

import org.bukkit.inventory.ItemStack;

public enum ArmorType {
	HELMET(5),
	CHESTPLATE(6),
	LEGGINGS(7),
	BOOTS(8);

	private final int slot;

	ArmorType(int slot) {
		this.slot = slot;
	}

	/**
	 * Attempts to match the ArmorType for the given item.<p>
	 * Player skulls are considered HELMET.
	 * Elytra are considered CHESTPLATE.
	 *
	 * @param itemStack The ItemStack to parse the type of.
	 * @return The parsed ArmorType, or null if the given item was null/air.
	 */
	public static ArmorType matchType(final ItemStack itemStack) {
		if (ArmorListener.isAirOrNull(itemStack)) return null;

		String type = itemStack.getType().name();
		if (type.endsWith("_HELMET") || type.endsWith("_SKULL") || type.endsWith("_HEAD")) return HELMET;
		else if (type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
		else if (type.endsWith("_LEGGINGS")) return LEGGINGS;
		else if (type.endsWith("_BOOTS")) return BOOTS;
		else return null;
	}

	/**
	 * Attempts to match the ArmorType for the given item.<p>
	 * ONLY helmets are considered HELMET.
	 * Elytra are considered CHESTPLATE.
	 *
	 * @param itemStack The ItemStack to parse the type of.
	 * @return The parsed ArmorType, or null if the given item was null/air.
	 */
	public static ArmorType matchArmorType(final ItemStack itemStack) {
		if (ArmorListener.isAirOrNull(itemStack)) return null;

		String type = itemStack.getType().name();
		if (type.endsWith("_HELMET")) return HELMET;
		else if (type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
		else if (type.endsWith("_LEGGINGS")) return LEGGINGS;
		else if (type.endsWith("_BOOTS")) return BOOTS;
		else return null;
	}

	/**
	 * Attempts to match the ArmorType for the given item.
	 * <p></p>
	 * Elytra are considered CHESTPLATE.
	 * Any other item is considered a HELMET (which allows for custom hats).
	 *
	 * @param itemStack The ItemStack to parse
	 * @return The parsed ArmorType, or null if the given item was null/air.
	 */
	public static ArmorType parseArmorType(final ItemStack itemStack) {
		if (ArmorListener.isAirOrNull(itemStack)) return null;

		String type = itemStack.getType().name();
		if (type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
		else if (type.endsWith("_LEGGINGS")) return LEGGINGS;
		else if (type.endsWith("_BOOTS")) return BOOTS;
		else return HELMET;
	}

	public int getSlot() {
		return slot;
	}
}