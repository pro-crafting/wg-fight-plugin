package de.hrc_gaming.kit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitAPI 
{
	private List<KitPlugin> kitPlugins;
	
	public KitAPI()
	{
		this.kitPlugins = new ArrayList<KitPlugin>();
		this.loadKitPlugins();
	}
	
	private void loadKitPlugins()
	{
		/*ServiceLoader<IKitPlugin> loader = ServiceLoader.load(IKitPlugin.class);
		for (IKitPlugin foundImpl : loader)
		{
			foundImpl.setServer(this.server);
			this.kitPlugins.add(foundImpl);
		}*/ //TODO:Reflection benutzen für das auslesen der KitPlugins
		try
		{
			KitPlugin toAdd = new AdminCmdKitPlugin();
			this.kitPlugins.add(toAdd);
		}
		catch (NoClassDefFoundError ex)
		{
		}
		try
		{
			KitPlugin toAdd = new EssentialsKitPlugin();
			this.kitPlugins.add(toAdd);
		}
		catch (NoClassDefFoundError ex)
		{
			
		}
	}
	
	public boolean existsKit(String kitName)
	{
		for (KitPlugin curr : this.kitPlugins)
		{
			try
			{
				return curr.existsKit(kitName);
			}
			catch(NoClassDefFoundError ex)
			{
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return false;
	}
	
	public void giveKit(String kitName, Player p)
	{
		for (KitPlugin curr : this.kitPlugins)
		{
			try
			{
				curr.giveKit(kitName, p);
				return;
			}
			catch(NoClassDefFoundError ex)
			{
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public ItemStack[] getKitItems(String kitName)
	{
		for (KitPlugin curr : this.kitPlugins)
		{
			try
			{
				return curr.getKitItems(kitName);
			}
			catch(NoClassDefFoundError ex)
			{
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new ItemStack[0];
	}
}
