package com.pro_crafting.mc.wg.arena;

import com.pro_crafting.mc.blockgenerator.JobState;
import com.pro_crafting.mc.blockgenerator.JobStateChangedCallback;
import com.pro_crafting.mc.blockgenerator.criteria.Criteria;
import com.pro_crafting.mc.blockgenerator.criteria.SingleBlockCriteria;
import com.pro_crafting.mc.blockgenerator.job.Job;
import com.pro_crafting.mc.blockgenerator.job.SimpleJob;
import com.pro_crafting.mc.blockgenerator.provider.BlockSearchProvider;
import com.pro_crafting.mc.blockgenerator.provider.SingleBlockProvider;
import com.pro_crafting.mc.common.Point;
import com.pro_crafting.mc.common.Size;
import com.pro_crafting.mc.wg.event.ArenaStateChangeEvent;
import com.pro_crafting.mc.wg.group.Group;
import com.pro_crafting.mc.wg.group.GroupManager;
import com.pro_crafting.mc.wg.group.PlayerGroupKey;
import com.pro_crafting.mc.wg.group.PlayerRole;
import com.pro_crafting.mc.wg.modes.FightMode;
import com.pro_crafting.mc.wg.modes.KitMode;
import com.pro_crafting.mc.wg.ErrorMessages;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.model.WgRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Arena {

  private WarGear plugin;
  private String name;
  private GroupManager team;
  private String kitname;
  private FightMode fightMode;
  private Reseter reseter;
  private WaterRemover remover;
  private State state;
  private List<UUID> players;
  private Repository repo;
  private SpectatorMode spectator;
  private boolean isOpen;

  public Arena(WarGear plugin, String arenaName) {
    this.plugin = plugin;
    this.name = arenaName;

    this.state = State.Idle;
    this.kitname = "";
    this.players = new ArrayList<>();
    this.repo = new Repository(this.plugin, this);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public GroupManager getGroupManager() {
    return team;
  }

  public Reseter getReseter() {
    return this.reseter;
  }

  public WaterRemover getRemover() {
    return this.remover;
  }

  public State getState() {
    return this.state;
  }

  public String getKit() {
    return kitname;
  }

  public void setKit(String kitname) {
    this.kitname = kitname;
  }

  public FightMode getFightMode() {
    return fightMode;
  }

  public void setFightMode(FightMode fightMode) {
    this.fightMode = fightMode;
  }

  public List<UUID> getPlayers() {
    return this.players;
  }

  public void join(Player p) {
    if (!this.players.contains(p.getUniqueId())) {
      this.players.add(p.getUniqueId());
      this.plugin.getScoreboard().addViewer(this.getGroupManager().getGroupKey(p), p);
    }
  }

  public void leave(Player p) {
    if (this.players.contains(p.getUniqueId())) {
      this.players.remove(p.getUniqueId());
      this.plugin.getScoreboard().removeViewer(this, p);
    }
  }

  public Repository getRepo() {
    return this.repo;
  }

  public void open() {
    this.setOpen(true);
    this.remover.start();
    this.broadcastMessage(ChatColor.GREEN + "Arena Freigegeben!");
  }

  public void close() {
    this.setOpen(false);
    this.remover.stop();
    this.broadcastMessage(ChatColor.GREEN + "Arena gesperrt!");
  }

  public ErrorMessages load() {
    ErrorMessages errors = this.repo.load();
    if (!errors.hasErrors()) {
      this.team = new GroupManager(plugin, this);
      this.setFightMode(new KitMode(this.plugin, this));
      this.reseter = new Reseter(this.plugin, this);
      this.remover = new WaterRemover(this.plugin, this);
      this.spectator = new SpectatorMode(this.plugin, this);
      this.setOpen(false);
    }
    return errors;
  }

  public void unload() {
    HandlerList.unregisterAll(this.reseter);
    this.remover.stop();
    this.players.clear();
    this.setOpen(false);
    this.state = State.Idle;
    this.plugin.getScoreboard().clearScoreboard(this);
  }

  public void broadcastMessage(String message) {
    for (UUID id : getPlayers()) {
      Player player = Bukkit.getPlayer(id);
      if (player != null) {
        player.sendMessage(message);
      }
    }
  }

  public void broadcastOutside(String message) {
    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
      if (!this.contains(player.getLocation())) {
        player.sendMessage(message);
      }
    }
  }

  public void updateState(State to) {
    to = processStateChange(to);
    ArenaStateChangeEvent arenaStateEvent = new ArenaStateChangeEvent(this, this.state, to);
    this.plugin.getServer().getPluginManager().callEvent(arenaStateEvent);
    if (!arenaStateEvent.isCancelled()) {
      this.state = arenaStateEvent.getTo();
    }
  }

  private State processStateChange(State to) {
    if (to == State.Spectate && !this.repo.isScoreboardEnabled()) {
      to = State.Resetting;
    }
    if (to == State.Resetting && !this.repo.isAutoReset()) {
      to = State.Idle;
    }
    return to;
  }

  public boolean contains(Location loc) {
    return this.repo.getArenaRegion().contains(loc);
  }

  public void teleport(Player player) {
    player.teleport(this.getSpawnLocation(player), TeleportCause.PLUGIN);
  }

  public void startFight(CommandSender sender) {
    if (this.getKit() == null || this.getKit().isEmpty()) {
      if (this.plugin.getRepo().getDefaultKitName() == null || this.plugin.getRepo()
          .getDefaultKitName().isEmpty()) {
        sender.sendMessage("§cEs wurde kein Kit ausgewählt oder ein Standard Kit angegeben.");
        return;
      } else {
        this.setKit(this.plugin.getRepo().getDefaultKitName());
      }
    }
    this.setFightMode(plugin.getModes().get(this.getRepo().getFightMode(), this));
    if (this.getFightMode() == null) {
      Bukkit.getLogger().warning(
          "Fightmode " + this.getRepo().getFightMode() + " unknown in arena " + this.getName()
              + "!");
      Bukkit.getLogger().info("Falling back to kit mode");
      this.setFightMode(new KitMode(this.plugin, this));
    }
    this.setOpen(false);
    this.getFightMode().start();
    this.updateState(State.PreRunning);
    countCannons();
  }

  private void countCannons() {
    startCannonCounterJob(this.getRepo().getTeam1Region(),
        this.getGroupManager().getGroupKey(PlayerRole.Team1));
    startCannonCounterJob(this.getRepo().getTeam2Region(),
        this.getGroupManager().getGroupKey(PlayerRole.Team2));
  }

  private void startCannonCounterJob(WgRegion rg, final PlayerGroupKey groupKey) {
    Point origin = new Point(rg.getMin().getX(), rg.getMin().getY(), rg.getMin().getZ());
    Point max = new Point(rg.getMax().getX(), rg.getMax().getY(), rg.getMax().getZ());
    Size size = new Size(max.getX() - origin.getX(), max.getY() - origin.getY(),
        max.getZ() - origin.getZ());
    this.plugin.getGenerator()
        .addJob(new SimpleJob(origin, size, getRepo().getWorld(), new JobStateChangedCallback() {

          public void jobStateChanged(Job job, JobState state) {
            if (job.getState() == JobState.Finished) {
              plugin.getScoreboard().updateCannons(groupKey.getArena(), groupKey.getRole(),
                  groupKey.getGroup().getCannons());
            }
          }
        }, new BlockSearchProvider(new Criteria() {
          Group group = groupKey.getGroup();

          public void wrap(Criteria arg0) {
            // TODO Auto-generated method stub

          }

          public boolean matches(Point point, BlockData data) {
            if (data.getMaterial() == Material.JACK_O_LANTERN) {
              group.setCannons(group.getCannons() + 1);
            }
            return true;
          }
        })));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Arena other = (Arena) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  public Location getSpawnLocation(Player p) {
    if (this.state == State.Running || this.state == State.PreRunning
        || this.state == State.Setup || this.state == State.Spectate) {
      Group playerTeam = this.team.getGroupOfPlayer(p);
      if (playerTeam != null) {
        if (this.state == State.Running && !playerTeam.getMember(p).isAlive()) {
          return this.repo.getSpawnWarp();
        }
        return this.team.getGroupSpawn(playerTeam.getRole());
      }
    }
    return this.repo.getSpawnWarp();
  }

  public SpectatorMode getSpectatorMode() {
    return this.spectator;
  }

  public boolean isOpen() {
    return this.isOpen;
  }

  public void setOpen(boolean isOpen) {
    this.isOpen = isOpen;
  }

  public ArenaPosition getPosition(Location where) {
    WgRegion innerRegion = repo.getInnerRegion();

    if (!contains(where)) {
      return ArenaPosition.Outside;
    }
    if (!innerRegion.contains(where)) {
      return ArenaPosition.Platform;
    }

    WgRegion team1 = this.repo.getTeam1Region();
    if (this.repo.getTeam1Region().contains(where)) {
      return ArenaPosition.Team1WG;
    }

    WgRegion team2 = this.repo.getTeam2Region();
    if (team2.contains(where)) {
      return ArenaPosition.Team2WG;
    }

    double distanceTeam1Squared =
            where.distanceSquared(team1.getMin().toLocation(team1.getWorld()))
                    + where.distanceSquared(team1.getMax().toLocation(team1.getWorld()));

    double distanceTeam2Squared =
            where.distanceSquared(team2.getMin().toLocation(team2.getWorld()))
            + where.distanceSquared(team2.getMax().toLocation(team2.getWorld()));

    if ((distanceTeam1Squared - distanceTeam2Squared) > 0) {
      return ArenaPosition.Team2PlayField;
    } else {
      return ArenaPosition.Team1PlayField;
    }
  }

  public void replaceMG() {
    World world = this.repo.getWorld();
    WgRegion innerRegion = repo.getInnerRegion();
    Point origin = innerRegion.getMin();
    Size size = new Size(innerRegion.getWidth(), innerRegion.getHeight(), innerRegion.getLength());
    this.plugin.getGenerator().addJob(new SimpleJob(origin, size, world, null,
        new SingleBlockProvider(new SingleBlockCriteria(Material.OBSIDIAN.createBlockData()), Material.TNT.createBlockData()),
        true));
  }
}
