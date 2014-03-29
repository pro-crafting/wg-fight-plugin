package me.Postremus.WarGear;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.Postremus.CommandFramework.CommandArgs;
import me.Postremus.CommandFramework.CommandFramework;
import me.Postremus.CommandFramework.Completer;
import me.Postremus.Generator.BlockGenerator;
import me.Postremus.WarGear.Arena.ArenaManager;
import me.Postremus.WarGear.Commands.*;
import me.Postremus.KitApi.KitAPI;

import org.apache.commons.lang.StringUtils;
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
