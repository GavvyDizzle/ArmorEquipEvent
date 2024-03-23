package com.codingforcookies.armorequip;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ArmorListener implements Listener {

	private final JavaPlugin instance;
	private final Set<Material> interactBlocks;
	private final Set<String> hatCommands;

	public ArmorListener(JavaPlugin instance) {
        this.instance = instance;

        interactBlocks = new HashSet<>();

		hatCommands = new HashSet<>(4);
		hatCommands.add("hat");
		hatCommands.add("ehat");
		hatCommands.add("head");
		hatCommands.add("ehead");

		reload();
	}

	public void reload() {
		loadInteractBlocks();
	}

	private void loadInteractBlocks() {
		interactBlocks.clear();

		interactBlocks.addAll(Tag.ALL_SIGNS.getValues());
		interactBlocks.addAll(Tag.ALL_HANGING_SIGNS.getValues());
		interactBlocks.addAll(Tag.DOORS.getValues());
		interactBlocks.addAll(Tag.TRAPDOORS.getValues());
		interactBlocks.addAll(Tag.BUTTONS.getValues());
		interactBlocks.addAll(Tag.FENCE_GATES.getValues());
		interactBlocks.addAll(Tag.BEDS.getValues());
		interactBlocks.addAll(Tag.FLOWER_POTS.getValues());
		interactBlocks.addAll(Tag.ANVIL.getValues());
		interactBlocks.addAll(Tag.CANDLE_CAKES.getValues());

		FileConfiguration config = instance.getConfig();
		config.options().copyDefaults(true);

		config.addDefault("interactBlocks", new ArrayList<>());
		for (String str : config.getStringList("interactBlocks")) {
			try {
				Material material = Material.getMaterial(str);
				interactBlocks.add(material);
			} catch (Exception e) {
				instance.getLogger().warning("Invalid material '" + str + "' at interactBlocks in config.yml. This entry will be ignored");
			}
		}

		instance.saveConfig();
	}

	//Event Priority is highest because other plugins might cancel the events before we check.
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void inventoryClick(InventoryClickEvent e) {
		// If the click is invalid or not done in the default player inventory
		if (e.getClickedInventory() == null || e.getAction() == InventoryAction.NOTHING || e.getView().getTopInventory().getSize() != 5) return;

		// If the click is not a valid type. These 3 slot types are the 3 possible ones to come from a default player inventory
		if (e.getSlotType() != InventoryType.SlotType.ARMOR && e.getSlotType() != InventoryType.SlotType.QUICKBAR && e.getSlotType() != InventoryType.SlotType.CONTAINER) return;

		if (!(e.getWhoClicked() instanceof Player player)) return;

		boolean shift = e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT);
		boolean numberkey = e.getClick().equals(ClickType.NUMBER_KEY);

		ArmorType newArmorType = ArmorType.matchType(shift ? e.getCurrentItem() : e.getCursor());

		// Used for drag and drop checking to make sure you aren't trying to place armor in the wrong slot.
		if (!shift && newArmorType != null && e.getRawSlot() != newArmorType.getSlot()) {
			return;
		}

		if (shift) {
			if (newArmorType == null) return;

			boolean equipping = e.getRawSlot() != newArmorType.getSlot();
			if (newArmorType.equals(ArmorType.HELMET) && (equipping == isAirOrNull(player.getInventory().getHelmet())) ||
					newArmorType.equals(ArmorType.CHESTPLATE) && (equipping == isAirOrNull(player.getInventory().getChestplate())) ||
					newArmorType.equals(ArmorType.LEGGINGS) && (equipping == isAirOrNull(player.getInventory().getLeggings())) ||
					newArmorType.equals(ArmorType.BOOTS) && (equipping == isAirOrNull(player.getInventory().getBoots()))) {
				ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
				Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
				if (armorEquipEvent.isCancelled()) {
					e.setCancelled(true);
				}
			}
		}
		else {
			ItemStack newArmorPiece = e.getCursor();
			ItemStack oldArmorPiece = e.getCurrentItem();
			if (numberkey) {
				if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) { // Prevents shit in the 2by2 crafting
					// e.getClickedInventory() == The players inventory
					// e.getHotBarButton() == key people are pressing to equip or unequip the item to or from.
					// e.getRawSlot() == The slot the item is going to.
					// e.getSlot() == Armor slot, can't use e.getRawSlot() as that gives a hotbar slot ;-;
					ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
					if (!isAirOrNull(hotbarItem)) { // Equipping
						newArmorType = ArmorType.matchType(hotbarItem);
						newArmorPiece = hotbarItem;
						oldArmorPiece = e.getClickedInventory().getItem(e.getSlot());
					}
					else { // Unequipping
						newArmorType = ArmorType.parseArmorType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
					}
				}
			}
			else {
				if (isAirOrNull(e.getCursor()) && !isAirOrNull(e.getCurrentItem())) { // unequip with no new item going into the slot.
					newArmorType = ArmorType.parseArmorType(e.getCurrentItem());
				}
				// e.getCurrentItem() == Unequip
				// e.getCursor() == Equip
				// newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
			}

			if (newArmorType != null && e.getRawSlot() == newArmorType.getSlot()) {
				ArmorEquipEvent.EquipMethod method = ArmorEquipEvent.EquipMethod.PICK_DROP;
				if (e.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey) method = ArmorEquipEvent.EquipMethod.HOTBAR_SWAP;

				ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, method, newArmorType, oldArmorPiece, newArmorPiece);
				Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
				if (armorEquipEvent.isCancelled()) {
					e.setCancelled(true);
				}
			}

		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void playerInteractEvent(PlayerInteractEvent e) {
		if (e.getAction() == Action.PHYSICAL) return;

		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			// If the clicked block cancels the hotbar armor equip
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block clickedBlock = e.getClickedBlock();

				// Armor equips normally if the player is crouching
				if (clickedBlock != null && !e.getPlayer().isSneaking() && hasInteraction(clickedBlock)) return;
			}

			// This is the same as checking if is cancelled because it is deprecated for this event
			if (e.useItemInHand().equals(Event.Result.DENY)) return;

			ArmorType newArmorType = ArmorType.matchArmorType(e.getItem());
			if (newArmorType == null) return;

			ItemStack oldArmorPiece = null;
			switch (newArmorType) {
				case HELMET -> oldArmorPiece = e.getPlayer().getInventory().getHelmet();
				case CHESTPLATE -> oldArmorPiece = e.getPlayer().getInventory().getChestplate();
				case LEGGINGS -> oldArmorPiece = e.getPlayer().getInventory().getLeggings();
				case BOOTS -> oldArmorPiece = e.getPlayer().getInventory().getBoots();
			}

			ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), ArmorEquipEvent.EquipMethod.HOTBAR, ArmorType.matchType(e.getItem()), oldArmorPiece, e.getItem());
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if (armorEquipEvent.isCancelled()) {
				e.setCancelled(true);
			}
		}
	}

	/**
	 * Determines if this block has an interaction tied to it.
	 * These are the types of blocks that stop armor equipping when right-clicked.
	 * @param block The clicked block
	 * @return If this block should cause the armor equip to be ignored
	 */
	private boolean hasInteraction(Block block) {
		Material material = block.getType();
		if (material.isAir()) return false;

		// If the block will open an inventory
		if (block.getState() instanceof InventoryHolder inventoryHolder) {
			if (!(inventoryHolder instanceof Jukebox)) return true;
		}

		// If the block is any number of blocks with interactions
		return interactBlocks.contains(material);
	}


	@EventHandler(priority =  EventPriority.HIGHEST, ignoreCancelled = true)
	private void inventoryDrag(InventoryDragEvent event) {
		// getType() seems to always be even.
		// Old Cursor gives the item you are equipping
		// Raw slot is the ArmorType slot
		// Can't replace armor using this method making getCursor() useless.

		// If the click is not done in the default player inventory
		if (event.getView().getTopInventory().getSize() != 5) return;

		if(event.getRawSlots().isEmpty()) return; // This may ever happen

		ArmorType type = ArmorType.matchType(event.getOldCursor());
		if (type != null && type.getSlot() == event.getRawSlots().stream().findFirst().orElse(0)) {
			ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) event.getWhoClicked(), ArmorEquipEvent.EquipMethod.DRAG, type, null, event.getOldCursor());
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if (armorEquipEvent.isCancelled()) {
				event.setResult(Event.Result.DENY);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	private void itemBreakEvent(PlayerItemBreakEvent e) {
		ArmorType type = ArmorType.matchType(e.getBrokenItem());
		if (type == null) return;

		Player p = e.getPlayer();
		ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, ArmorEquipEvent.EquipMethod.BROKE, type, e.getBrokenItem(), null);
		Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);

		if (armorEquipEvent.isCancelled()) {
			ItemStack armor = e.getBrokenItem().clone();
			Damageable damageable = (Damageable) armor.getItemMeta();
			assert damageable != null;
			damageable.setDamage(damageable.getDamage() - 1);
			armor.setItemMeta(damageable);

			if (type.equals(ArmorType.HELMET)) {
				p.getInventory().setHelmet(armor);
			}
			else if (type.equals(ArmorType.CHESTPLATE)) {
				p.getInventory().setChestplate(armor);
			}
			else if (type.equals(ArmorType.LEGGINGS)) {
				p.getInventory().setLeggings(armor);
			}
			else if (type.equals(ArmorType.BOOTS)) {
				p.getInventory().setBoots(armor);
			}
		}
	}

	@EventHandler
	private void playerDeathEvent(PlayerDeathEvent e) {
		if (e.getKeepInventory()) return;

		Player p = e.getEntity();
		for (ItemStack i : p.getInventory().getArmorContents()) {
			if(!isAirOrNull(i)) {
				Bukkit.getServer().getPluginManager().callEvent(new ArmorEquipEvent(p, ArmorEquipEvent.EquipMethod.DEATH, ArmorType.parseArmorType(i), i, null));
				// No way to cancel a death event.
			}
		}
	}

	@EventHandler
	private void onHatCommand(PlayerCommandPreprocessEvent e) {
		String cmd;
		if (e.getMessage().contains(" ")) {
			cmd = e.getMessage().substring(1, e.getMessage().indexOf(" "));
		}
		else {
			cmd = e.getMessage().substring(1);
		}

		ItemStack oldHat = e.getPlayer().getInventory().getHelmet();
		if (hatCommands.contains(cmd.toLowerCase())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> {
				ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), ArmorEquipEvent.EquipMethod.HAT_COMMAND, ArmorType.HELMET, oldHat, e.getPlayer().getInventory().getHelmet());
				Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
				if (armorEquipEvent.isCancelled()) {
					e.getPlayer().getInventory().addItem(e.getPlayer().getInventory().getHelmet());
					e.getPlayer().getInventory().setHelmet(null);
				}
			});
		}
	}

	@EventHandler
	private void dispenseArmorEvent(BlockDispenseArmorEvent event) {
		ArmorType type = ArmorType.matchType(event.getItem());
		if (type != null) {
			Player p = (Player) event.getTargetEntity();
			ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, ArmorEquipEvent.EquipMethod.DISPENSER, type, null, event.getItem());
			Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
			if(armorEquipEvent.isCancelled()){
				event.setCancelled(true);
			}
		}
	}

	/**
	 * A utility method to support versions that use null or air ItemStacks.
	 */
	public static boolean isAirOrNull(ItemStack item) {
		return item == null || item.getType().isAir();
	}
}
