package de.hrc_gaming.wg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import de.hrc_gaming.commandframework.CommandArgs;
import de.hrc_gaming.commandframework.CommandFramework;
import de.hrc_gaming.commandframework.Completer;
import de.hrc_gaming.generator.BlockGenerator;
import de.hrc_gaming.kit.KitAPI;
import de.hrc_gaming.wg.arena.ArenaManager;
import de.hrc_gaming.wg.commands.ArenaCommands;
import de.hrc_gaming.wg.commands.TeamCommands;
import de.hrc_gaming.wg.commands.WarGearCommands;

public class WarGear extends JavaPlugin {
	private WarGearRepository repo;
	private BlockGenerator generator;
	private ArenaManager arenaManager;
	private KitAPI kitApi;
	private CommandFramework cmdFramework;
	private WarGearCommands wgCommands;
	private TeamCommands teamCommands;
	private ArenaCommands arenaCommands;
	private WgEconomy eco;
	
	@Override
	public void onEnable() {
		this.loadConfig();
		this.repo = new WarGearRepository(this);
		this.generator = new BlockGenerator(this, 10000);
		this.arenaManager = new ArenaManager(this);
		this.kitApi = new KitAPI();
		this.cmdFramework = new CommandFramework(this);
		this.wgCommands = new WarGearCommands(this);
		this.teamCommands = new TeamCommands(this);
		this.arenaCommands = new ArenaCommands(this);
		this.cmdFramework.registerCommands(this.wgCommands);
		this.cmdFramework.registerCommands(this.teamCommands);
		this.cmdFramework.registerCommands(this.arenaCommands);
		this.cmdFramework.registerCommands(this);
		this.cmdFramework.registerHelp();
		this.eco = new WgEconomy(this);
		this.getLogger().info("Plugin erfolgreich geladen!");
	}
	
	@Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return this.cmdFramework.handleCommand(sender, label, command, args);
    }
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this.eco);
		this.arenaManager.unloadArenas();
		this.getLogger().info("Plugin erfolgreich deaktiviert!");
	}
	
	@Completer (name="wgk")
	public List<String> completeCommands(CommandArgs args)
	{
		List<String> ret = new ArrayList<String>();
		String label = args.getCommand().getLabel();
		for (String arg : args.getArgs())
		{
			label += " " + arg;
		}
		for(String currentLabel : this.cmdFramework.getCommandLabels())
		{
			String current = currentLabel.replace('.', ' ');
			if (current.contains(label))
			{
				current = current.substring(label.lastIndexOf(' ')).trim();
				current = current.substring(0, current.indexOf(' ') != -1 ? current.indexOf(' ') : current.length()).trim();
				if (!ret.contains(current))
				{
					ret.add(current);
				}
			}
		}
		return ret;
	}
	
	public void loadConfig()
	{
		if(!new File(this.getDataFolder(), "config.yml").exists()){			
			saveDefaultConfig();
			this.getLogger().info("config.yml erstellt und geladen.");
		}
	}
	
	public WarGearRepository getRepo()
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
