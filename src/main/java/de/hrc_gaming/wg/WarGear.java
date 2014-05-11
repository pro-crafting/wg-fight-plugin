package de.hrc_gaming.wg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

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
	private Repository repo;
	private BlockGenerator generator;
	private ArenaManager arenaManager;
	private KitAPI kitApi;
	private CommandFramework cmdFramework;
	private WarGearCommands wgCommands;
	private TeamCommands teamCommands;
	private ArenaCommands arenaCommands;
	private WgEconomy eco;
	private MetricsLite metrics;
	
	@Override
	public void onEnable() {
		this.loadConfig();
		this.repo = new Repository(this);
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
		if (this.repo.isEconomyEnabled())
		{
			this.eco = new WgEconomy(this);
		}
		startMetrics();
		this.getLogger().info("Plugin erfolgreich geladen!");
	}
	
	private void startMetrics()
	{
		if (repo.areMetricsEnabled())
		{
			try {
				metrics = new MetricsLite(this);
				if (metrics.start())
				{
					this.getLogger().info("Metrics gestartet!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
	
	public Repository getRepo()
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
