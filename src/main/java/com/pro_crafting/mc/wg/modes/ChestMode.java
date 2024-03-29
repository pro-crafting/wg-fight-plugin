package com.pro_crafting.mc.wg.modes;

import com.pro_crafting.mc.wg.Util;
import com.pro_crafting.mc.wg.WarGear;
import com.pro_crafting.mc.wg.arena.Arena;
import com.pro_crafting.mc.wg.arena.State;

import java.util.ArrayList;
import java.util.List;

import com.pro_crafting.mc.wg.event.ArenaStateChangeEvent;
import com.pro_crafting.mc.wg.group.Group;
import com.pro_crafting.mc.wg.group.GroupMember;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ChestMode extends FightBase implements FightMode, Listener {

  private BukkitTask task;
  private int counter;
  private boolean areChestsOpen;

  public ChestMode(WarGear plugin, Arena arena) {
    super(plugin, arena);

    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

    starter = () -> task = ChestMode.this.plugin.getServer().getScheduler()
        .runTaskTimer(ChestMode.this.plugin, new Runnable() {
          public void run() {
            chestOpenCountdown();
          }
        }, 0, 20);
  }

  @Override
  public void start() {
    this.fillChest(this.arena.getRepo().getTeam1Warp(), BlockFace.WEST);
    this.fillChest(this.arena.getRepo().getTeam2Warp(), BlockFace.EAST);
    counter = 0;
    super.start();
  }

  private void fillChest(Location loc, BlockFace facing) {
    Location l = loc.clone();
    l.setY(l.getY() - 1);
    Location chestOne = Util.move(l, 2);
    Location chestTwo = Util.move(chestOne, 1);
    setChests(chestOne, org.bukkit.block.data.type.Chest.Type.LEFT, facing);
    setChests(chestTwo, org.bukkit.block.data.type.Chest.Type.RIGHT, facing);

    ItemStack[] items = removeTNTStacks(
        this.plugin.getRepo().getKit().getItems(this.arena.getKit()));
    Inventory chestOneInv = ((Chest) chestOne.getBlock().getState()).getBlockInventory();
    chestOneInv.setContents(items);

    fillChestWithTnt(chestOne);
    fillChestWithTnt(chestTwo);
  }

  private ItemStack[] removeTNTStacks(ItemStack[] withtnt) {
    List<ItemStack> ret = new ArrayList<ItemStack>();
    for (ItemStack stack : withtnt) {
      if (stack.getType() != Material.TNT) {
        ret.add(stack);
      }
    }
    return ret.toArray(new ItemStack[0]);
  }

  private void fillChestWithTnt(Location loc) {
    if (loc.getBlock().getType() != Material.CHEST) {
      return;
    }
    Inventory inv = ((Chest) loc.getBlock().getState()).getBlockInventory();
    for (int i = 0; i < inv.getSize(); i++) {
      if (inv.getItem(i) == null) {
        inv.setItem(i, new ItemStack(Material.TNT, 64));
      }
    }
  }

  private void setChests(Location chestLoc, org.bukkit.block.data.type.Chest.Type type, BlockFace facing) {

    Block block = chestLoc.getBlock();

    block.setType(Material.CHEST);
    org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) block.getBlockData();
    chestData.setType(type);
    chestData.setFacing(facing);
    block.setBlockData(chestData);

  }

  private void chestOpenCountdown() {
    if (counter == 0) {
      this.arena.broadcastMessage(ChatColor.GOLD + "Kisten werden geöffnet in:");
      this.arena.broadcastMessage(ChatColor.GOLD + "5 Sekunden");
    } else if (counter > 0 && counter < 2) {
      int diff = 5 - counter;
      this.arena.broadcastMessage(ChatColor.GOLD + "" + diff + " Sekunden");
    } else if (counter > 1 && counter < 5) {
      int diff = 5 - counter;
      this.arena.broadcastMessage(ChatColor.AQUA + "" + diff + " Sekunden");
    } else if (counter == 5) {
      counter = 0;
      areChestsOpen = true;
      this.arena.broadcastMessage(ChatColor.AQUA + "Kisten geöffnet!");
      task.cancel();
      task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable() {
        public void run() {
          fightPreStartCountdown();
        }
      }, 0, 20);
      return;
    }
    counter++;
  }

  private void fightPreStartCountdown() {
    if (counter == 0) {
      this.arena.broadcastMessage(ChatColor.GOLD + "Kisten werden geschlossen in:");
      this.arena.broadcastMessage(ChatColor.GOLD + "30 Sekunden");
    } else if (counter == 10) {
      this.arena.broadcastMessage(ChatColor.GOLD + "20 Sekunden");
    } else if (counter == 15) {
      this.arena.broadcastMessage(ChatColor.GOLD + "15 Sekunden");
    } else if (counter == 20) {
      this.arena.broadcastMessage(ChatColor.GOLD + "10 Sekunden");
    } else if (counter > 24 && counter < 27) {
      int diff = 30 - counter;
      this.arena.broadcastMessage(ChatColor.GOLD + "" + diff + " Sekunden");
    } else if (counter > 26 && counter < 30) {
      int diff = 30 - counter;
      this.arena.broadcastMessage(ChatColor.AQUA + "" + diff + " Sekunden");
    } else if (counter == 30) {
      closeInventories(arena.getGroupManager().getGroup1());
      closeInventories(arena.getGroupManager().getGroup2());

      counter = 0;
      areChestsOpen = false;
      this.arena.broadcastMessage(ChatColor.AQUA + "Kisten geschlossen!");
      this.arena.broadcastMessage(ChatColor.AQUA + "Wargear betreten!");
      task.cancel();
      task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this::fightStartCountdown, 0, 20);
      return;
    }
    counter++;
  }

  private void fightStartCountdown() {
    if (counter == 0) {
      this.arena.broadcastMessage(ChatColor.GOLD + "Fight beginnt in:");
      this.arena.broadcastMessage(ChatColor.GOLD + "30 Sekunden");
    } else if (counter == 10) {
      this.arena.broadcastMessage(ChatColor.GOLD + "20 Sekunden");
    } else if (counter == 15) {
      this.arena.broadcastMessage(ChatColor.GOLD + "15 Sekunden");
    } else if (counter == 20) {
      this.arena.broadcastMessage(ChatColor.GOLD + "10 Sekunden");
    } else if (counter > 24 && counter < 27) {
      int diff = 30 - counter;
      this.arena.broadcastMessage(ChatColor.GOLD + "" + diff + " Sekunden");
    } else if (counter > 26 && counter < 30) {
      int diff = 30 - counter;
      this.arena.broadcastMessage(ChatColor.AQUA + "" + diff + " Sekunden");
    } else if (counter == 30) {
      task.cancel();
      this.arena.updateState(State.Running);
      arena.open();
      return;
    }
    counter++;
  }

  @Override
  public void stop() {
    super.stop();
    task.cancel();
    PlayerInteractEvent.getHandlerList().unregister(this);
  }

  @Override
  public String getName() {
    return "chest";
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void playerInteractHandler(PlayerInteractEvent event) {
    if (areChestsOpen) {
      return;
    }
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    if (event.getClickedBlock().getType() != Material.CHEST) {
      return;
    }
    if (event.getPlayer().hasPermission("wargear.chest.open")) {
      return;
    }
    if (this.arena.getGroupManager().getGroupOfPlayer(event.getPlayer()) == null) {
      event.setCancelled(true);
    }
    Location clicked = event.getClickedBlock().getLocation();
    if (isItemChestLocation(clicked, this.arena.getRepo().getTeam1Warp().clone()) ||
        isItemChestLocation(clicked, this.arena.getRepo().getTeam2Warp().clone())) {
      event.setCancelled(!areChestsOpen);
    }
  }

  private void closeInventories(Group group) {
    for (GroupMember member : group.getMembers()) {
      if (member.isOnline()) {
        member.getPlayer().closeInventory();
      }
    }
  }

  private boolean isItemChestLocation(Location value, Location checkAgainst) {
    Location l = checkAgainst.clone();
    l.setY(l.getY() - 1);
    Location chestOne = Util.move(l, 2);
    Location chestTwo = Util.move(chestOne, 1);
    return equalsLocation(chestOne, value) || equalsLocation(chestTwo, value);
  }

  private Boolean equalsLocation(Location one, Location two) {
    return one.getBlockX() == two.getBlockX() && one.getBlockY() == two.getBlockY()
        && one.getBlockZ() == two.getBlockZ();
  }
}
