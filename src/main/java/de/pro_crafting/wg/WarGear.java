package de.pro_crafting.wg;

import de.pro_crafting.common.scoreboard.ScoreboardManager;
import de.pro_crafting.generator.BlockGenerator;
import de.pro_crafting.region.RegionManager;
import de.pro_crafting.wg.arena.ArenaManager;
import de.pro_crafting.wg.commands.CommandManager;
import de.pro_crafting.wg.group.PlayerGroupKey;
import de.pro_crafting.wg.group.invitation.InvitationManager;
import de.pro_crafting.wg.modes.ModeManager;
import de.pro_crafting.wg.ui.ScoreboardDisplay;

import net.gravitydevelopment.updater.Updater;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.File;
import java.io.IOException;

public class WarGear extends JavaPlugin {
    private Repository repo;
    private BlockGenerator generator;
    private ArenaManager arenaManager;
    private WgEconomy eco;
    private MetricsLite metrics;
    private Updater updater;
    private WgListener wgListener;
    private File arenaFolder;
    private OfflineManager offlineManager;
    private ScoreboardManager<PlayerGroupKey> scoreboardManager;
    private ScoreboardDisplay scoreboard;
    private InvitationManager inviteManager;
    private ModeManager modes;
    private File modeFolder;
    private RegionManager regionsManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        this.loadConfig();
        this.repo = new Repository(this);
        this.regionsManager = new RegionManager();
        this.generator = new BlockGenerator(this, 50000);
        this.modes = new ModeManager(this);
        this.arenaManager = new ArenaManager(this);

        if (this.repo.isEconomyEnabled()) {
            this.eco = new WgEconomy(this);
        }
        startMetrics();
        startUpdater();
        commandManager = new CommandManager(this);
        this.wgListener = new WgListener(this);
        this.offlineManager = new OfflineManager(this);
        this.scoreboardManager = new ScoreboardManager<PlayerGroupKey>();
        this.scoreboard = new ScoreboardDisplay(this);

        this.inviteManager = new InvitationManager(this);

        if (this.repo.getKit() == null) {
            this.getLogger().warning("Kein Kit Provider gefunden!");
        } else {
            this.getLogger().info(this.repo.getKit().getName() + " stellt die Kits bereit.");
        }

        this.getLogger().info("Plugin erfolgreich geladen!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String[] split = new String[args.length + 1];
        System.arraycopy(args, 0, split, 1, args.length);
        split[0] = command.getName();
        commandManager.executeCommand(sender, split);
        return true;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this.eco);
        HandlerList.unregisterAll(this.wgListener);
        this.arenaManager.unloadArenas();
        this.getLogger().info("Plugin erfolgreich deaktiviert!");
    }

	/*@Completer (name="wgk")
    public List<String> completeCommands(CommandArgs args) {
		List<String> ret = new ArrayList<String>();
		String label = args.getCommand().getLabel();
		for (String arg : args.getArgs()) {
			label += " " + arg;
		}
		for(String currentLabel : this.cmdFramework.getCommandLabels()) {
			String current = currentLabel.replace('.', ' ');
			if (current.contains(label)) {
				current = current.substring(label.lastIndexOf(' ')).trim();
				current = current.substring(0, current.indexOf(' ') != -1 ? current.indexOf(' ') : current.length()).trim();
				if (!ret.contains(current)) {
					ret.add(current);
				}
			}
		}
		return ret;
	}*/

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
            try {
                metrics = new MetricsLite(this);
                if (metrics.start()) {
                    this.getLogger().info("Metrics gestartet!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startUpdater() {
        if (repo.isUpdateCheckEnabled()) {
            updater = new Updater(this, 66631, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
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

    public Updater getUpdater() {
        return this.updater;
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

    public File getModeFolder() {
        return modeFolder;
    }

    public RegionManager getRegionsManager() {
        return this.regionsManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
