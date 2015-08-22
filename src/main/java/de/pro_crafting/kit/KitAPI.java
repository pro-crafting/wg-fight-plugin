package de.pro_crafting.kit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import de.pro_crafting.kit.plugins.AdminCmdProvider;
import de.pro_crafting.kit.plugins.CommandBookProvider;
import de.pro_crafting.kit.plugins.EssentialsProvider;

public class KitAPI 
{
	private List<KitProvider> kitProviders;
	private ServicesManager sm;
	
	public KitAPI()
	{
		this.kitProviders = new ArrayList<KitProvider>();
		this.sm = Bukkit.getServicesManager();
		this.loadKitPlugins();
	}
	
	private void loadKitPlugins()
	{
		hookKitPlugin("AdminCmd", AdminCmdProvider.class);
		hookKitPlugin("Essentials", EssentialsProvider.class);
		hookKitPlugin("CommandBook", CommandBookProvider.class);
	}
	
	private void hookKitPlugin(String name, Class<? extends KitProvider> hookClass)
	{
		Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
		if (Bukkit.getPluginManager().getPlugin(name) != null)
		{
			try {
				KitProvider instance = hookClass.getConstructor().newInstance();
				this.kitProviders.add(instance);
				this.sm.register(KitProvider.class, instance, plugin, ServicePriority.Normal);
			} catch (Exception ex) {
				
			}
		}
	}
}
