package de.pro_crafting.wg;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.pro_crafting.kit.KitProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Repository {

  private WarGear plugin;
  private Economy eco;

  public Repository(WarGear plugin) {
    this.plugin = plugin;
    this.eco = loadEco();
  }

  public String getDefaultKitName() {
    return this.plugin.getConfig().getString("kit");
  }

  public boolean isEconomyEnabled() {
    return this.plugin.getConfig().getBoolean("economy.enabled", false);
  }

  public double getWinAmount() {
    return this.plugin.getConfig().getDouble("economy.win", 2.5);
  }

  public double getLoseAmount() {
    return this.plugin.getConfig().getDouble("economy.lose", -2.5);
  }

  public double getDrawAmount() {
    return this.plugin.getConfig().getDouble("economy.draw", 1);
  }

  public boolean isPrefixEnabled() {
    return this.plugin.getConfig().getBoolean("prefix-enabled", true);
  }

  public boolean areMetricsEnabled() {
    return this.plugin.getConfig().getBoolean("metrics", false);
  }

  public boolean isUpdateCheckEnabled() {
    return this.plugin.getConfig().getBoolean("update-check", false);
  }

  public int getOfflineKickTime() {
    return this.plugin.getConfig().getInt("offline-kick-time", 30);
  }

  public boolean isGroupChatEnabled() {
    return this.plugin.getConfig().getBoolean("group-chat.enabled", true);
  }

  public String getGroupChatSign() {
    return this.plugin.getConfig().getString("group-chat.sign", "#");
  }

  public String getGroupChatFormat() {
    return this.plugin.getConfig().getString("group-chat.format", "%gc>§7%displayname: %message");
  }

  public WorldEditPlugin getWorldEdit() {
    return (WorldEditPlugin) this.plugin.getServer().getPluginManager().getPlugin("WorldEdit");
  }

  public Economy getEco() {
    return this.eco;
  }

  private Economy loadEco() {
    if (this.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
      return null;
    }
    RegisteredServiceProvider<Economy> economyProvider = this.plugin.getServer()
        .getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
      return economyProvider.getProvider();
    }
    return null;
  }

  public KitProvider getKit() {
    return this.getKitProvider();
  }

  private KitProvider getKitProvider() {
    RegisteredServiceProvider<KitProvider> provider = this.plugin.getServer().getServicesManager()
        .getRegistration(KitProvider.class);
    if (provider != null) {
      return provider.getProvider();
    }
    return null;
  }

  public List<String> getArenaNames() {
    List<String> ret = new ArrayList<String>();
    for (File fileEntry : this.plugin.getArenaFolder().listFiles()) {
      if (fileEntry.isFile()) {
        String arenaName = fileEntry.getName().substring(0, fileEntry.getName().indexOf("."));
        ret.add(arenaName);
      }
    }
    return ret;
  }
}
