package com.pro_crafting.mc.wg;

import com.pro_crafting.mc.blockgenerator.BlockGenerator;
import com.pro_crafting.mc.commandframework.CommandArgs;
import com.pro_crafting.mc.commandframework.CommandFramework;
import com.pro_crafting.mc.commandframework.Completer;
import com.pro_crafting.mc.common.scoreboard.ScoreboardManager;
import com.pro_crafting.mc.kit.KitAPI;
import com.pro_crafting.mc.wg.arena.ArenaManager;
import com.pro_crafting.mc.wg.group.PlayerGroupKey;
import com.pro_crafting.mc.wg.group.invitation.InvitationManager;
import com.pro_crafting.mc.wg.modes.ModeManager;
import com.pro_crafting.mc.wg.ui.ScoreboardDisplay;
import com.pro_crafting.mc.wg.commands.ArenaCommands;
import com.pro_crafting.mc.wg.commands.TeamCommands;
import com.pro_crafting.mc.wg.commands.WarGearCommands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class WarGear extends JavaPlugin {

  private Repository repo;
  private BlockGenerator generator;
  private ArenaManager arenaManager;
  private CommandFramework cmdFramework;
  private WgEconomy eco;
  private Metrics metrics;
  private WgListener wgListener;
  private File arenaFolder;
  private OfflineManager offlineManager;
  private ScoreboardManager<PlayerGroupKey> scoreboardManager;
  private ScoreboardDisplay scoreboard;
  private InvitationManager inviteManager;
  private ModeManager modes;
  private WgRegionListener wgRegionListener;

  @Override
  public void onEnable() {
    KitAPI.getInstance().load();
    this.loadConfig();
    this.repo = new Repository(this);
    this.generator = new BlockGenerator(this, 50000);
    this.modes = new ModeManager(this);
    this.arenaManager = new ArenaManager(this);

    if (this.repo.isEconomyEnabled()) {
      this.eco = new WgEconomy(this);
    }
    startMetrics();
    registerCommands();
    this.wgListener = new WgListener(this);
    this.wgRegionListener = new WgRegionListener(this);
    this.offlineManager = new OfflineManager(this);
    this.scoreboardManager = new ScoreboardManager<>();
    this.scoreboard = new ScoreboardDisplay(this);

    this.inviteManager = new InvitationManager(this);

    if (this.repo.getKit() == null) {
      this.getLogger().warning("Kein Kit Provider gefunden!");
    } else {
      this.getLogger().info(this.repo.getKit().getName() + " stellt die Kits bereit.");
    }

    this.getLogger().info("Plugin erfolgreich geladen!");
  }

  private void registerCommands() {
    this.cmdFramework = new CommandFramework(this);
    this.cmdFramework.registerCommands(new WarGearCommands(this));
    this.cmdFramework.registerCommands(new TeamCommands(this));
    this.cmdFramework.registerCommands(new ArenaCommands(this));
    this.cmdFramework.registerCommands(this);
    this.cmdFramework.registerHelp();

    this.cmdFramework.setInGameOnlyMessage("Der Command muss von einem Spieler ausgeführt werden.");
  }

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this.eco);
    HandlerList.unregisterAll(this.wgListener);
    HandlerList.unregisterAll(this.wgRegionListener);
    this.arenaManager.unloadArenas();
    this.metrics.shutdown();
    this.getLogger().info("Plugin erfolgreich deaktiviert!");
  }

  @Completer(name = "wgk")
  public List<String> completeCommands(CommandArgs args) {
    List<String> ret = new ArrayList<>();
    String label = args.getCommand().getLabel();
    for (String arg : args.getArgs()) {
      label += " " + arg;
    }
    for (String currentLabel : this.cmdFramework.getCommandLabels()) {
      String current = currentLabel.replace('.', ' ');
      if (current.contains(label)) {
        current = current.substring(label.lastIndexOf(' ')).trim();
        current = current
            .substring(0, current.indexOf(' ') != -1 ? current.indexOf(' ') : current.length())
            .trim();
        if (!ret.contains(current)) {
          ret.add(current);
        }
      }
    }
    return ret;
  }

  private void loadConfig() {
    saveDefaultConfig();
    this.getLogger().info("config.yml geladen.");

    arenaFolder = new File(this.getDataFolder(), "arenas/");
    if (!arenaFolder.exists()) {
      this.saveResource("arenas/arena.yml", false);
    }
  }

  private void startMetrics() {
    if (repo.areMetricsEnabled()) {
        metrics = new Metrics(this, 20639);
        this.getLogger().info("Metrics gestartet!");
    }
  }

  public Repository getRepo() {
    return this.repo;
  }

  public BlockGenerator getGenerator() {
    return this.generator;
  }

  public ArenaManager getArenaManager() {
    return this.arenaManager;
  }

  public CommandFramework GetCmdFramework() {
    return this.cmdFramework;
  }

  public File getArenaFolder() {
    return this.arenaFolder;
  }

  public OfflineManager getOfflineManager() {
    return this.offlineManager;
  }

  public ScoreboardManager<PlayerGroupKey> getScoreboardManager() {
    return this.scoreboardManager;
  }

  public ScoreboardDisplay getScoreboard() {
    return this.scoreboard;
  }

  public InvitationManager getInviteManager() {
    return this.inviteManager;
  }

  public ModeManager getModes() {
    return modes;
  }
}
