package me.Postremus.WarGear;

import java.io.File;

import me.Postremus.CommandFramework.CommandFramework;
import me.Postremus.Generator.BlockGenerator;
import me.Postremus.WarGear.Arena.ArenaManager;
import me.Postremus.WarGear.Commands.*;
import me.Postremus.KitApi.KitAPI;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class WarGear extends JavaPlugin {

	private WgkRepository repo;
	private BlockGenerator generator;
	private ArenaManager arenaManager;
	private KitAPI kitApi;
	private CommandFramework cmdFramework;
	private WarGearCommands wgCommands;
	private TeamCommands teamCommands;
	private ArenaCommands arenaCommands;
	
	@Override
	public void onEnable() {
		this.loadConfig();
		this.repo = new WgkRepository(this);
		this.generator = new BlockGenerator(this);
		this.arenaManager = new ArenaManager(this);
		this.kitApi = new KitAPI(this.getServer());
		this.cmdFramework = new CommandFramework(this);
		this.wgCommands = new WarGearCommands(this);
		this.teamCommands = new TeamCommands(this);
		this.arenaCommands = new ArenaCommands(this);
		this.cmdFramework.registerCommands(this.wgCommands);
		this.cmdFramework.registerCommands(this.teamCommands);
		this.cmdFramework.registerCommands(this.arenaCommands);
		this.cmdFramework.registerHelp();
		System.out.println("[WarGear] Plugin erfolgreich geladen!");
	}
	
	@Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return this.cmdFramework.handleCommand(sender, label, command, args);
    }
	
	@Override
	public void onDisable() {
		this.arenaManager.unloadArenas();
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
	
	public KitAPI getKitApi()
	{
		return this.kitApi;
	}
	
	public CommandFramework GetCmdFramework()
	{
		return this.cmdFramework;
	}
}
