package com.codingforcookies.armorequip;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();

		ArmorListener armorListener = new ArmorListener(this);
		getServer().getPluginManager().registerEvents(armorListener, this);

		Objects.requireNonNull(getCommand("armorEquipEvent")).setExecutor(new AdminCommandManager(this, armorListener));
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}
}