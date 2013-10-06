package me.Postremus.WarGear;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class WarGear extends JavaPlugin {

	FileConfiguration config;
	WgkRepository repo;
	
	@Override
	public void onEnable() {
		this.loadConfig();
		this.repo = new WgkRepository(this);
		this.getCommand("wgk").setExecutor(new WgkCommand(this));
		System.out.println("[WarGear] Plugin erfolgreich geladen!");
	}
	@Override
	public void onDisable() {
		System.out.println("[WarGear] Plugin erfolgreich deaktiviert!");
	}
	
	public void loadConfig()
	{
		config = getConfig();
		config.options().copyDefaults(false);
		if(new File("plugins/WarGear/config.yml").exists()){			
			System.out.println("[WarGear] config.yml geladen.");	
		}else{
			saveDefaultConfig();
			System.out.println("[WarGear] config.yml erstellt und geladen.");
		}
	}
	
	public WgkRepository getRepo()
	{
		return this.repo;
	}
}
