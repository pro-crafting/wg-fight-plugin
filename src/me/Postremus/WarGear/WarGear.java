package me.Postremus.WarGear;

import java.io.File;

import me.Postremus.Generator.BlockGenerator;
import me.Postremus.WarGear.Arena.ArenaManager;

import org.bukkit.plugin.java.JavaPlugin;

public class WarGear extends JavaPlugin {

	private WgkRepository repo;
	private BlockGenerator generator;
	private ArenaManager arenaManager;
	
	@Override
	public void onEnable() {
		this.loadConfig();
		this.repo = new WgkRepository(this);
		this.generator = new BlockGenerator(this);
		this.arenaManager = new ArenaManager(this);
		this.getCommand("wgk").setExecutor(new WgkCommand(this));
		System.out.println("[WarGear] Plugin erfolgreich geladen!");
	}
	@Override
	public void onDisable() {
		System.out.println("[WarGear] Plugin erfolgreich deaktiviert!");
	}
	
	public void loadConfig()
	{
		if(!new File("plugins/WarGear/config.yml").exists()){			
			saveDefaultConfig();
			System.out.println("[WarGear] config.yml erstellt und geladen.");
		}
	}
	
	public WgkRepository getRepo()
	{
		return this.repo;
	}
	
	public BlockGenerator getGenerator()
	{
		return this.generator;
	}
	
	public ArenaManager getArenaManager()
	{
		return this.arenaManager;
	}
}
