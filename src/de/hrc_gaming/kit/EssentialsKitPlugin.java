package de.hrc_gaming.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.User;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.SimpleTextInput;

public class EssentialsKitPlugin implements KitPlugin
{
	@Override
	public boolean existsKit(String kitName) {
		kitName = kitName.toLowerCase();
		return this.getPlugin().getSettings().getKit(kitName) != null;
	}

	@Override
	public void giveKit(String kitName, Player p) 
	{
		kitName = kitName.toLowerCase();
		Essentials plugin =  this.getPlugin();
		User to = plugin.getUser(p);
		Map<String, Object> kit = plugin.getSettings().getKit(kitName);
		List<String> items;
		try {
			items = Kit.getItems(plugin, to, kitName, kit);
			Kit.expandItems(plugin, to, items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Essentials getPlugin()
	{
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
		 
	    if (plugin == null || !(plugin instanceof IEssentials)) {
	        return null; 
	    }
	    
	    return (Essentials) plugin;
	}

	@Override
	public ItemStack[] getKitItems(String kitName) {
		Essentials plugin =  this.getPlugin();
		
		Map<String, Object> kit = plugin.getSettings().getKit(kitName);
		List<String> items = null;
		User tmp = plugin.getOfflineUser("tmp");
		try {
			items = Kit.getItems(plugin, tmp, kitName, kit);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		IText input = new SimpleTextInput(items);
        IText output = new KeywordReplacer(input, null, plugin);

        List<ItemStack> ret = new ArrayList<ItemStack>();
        for (String kitItem : output.getLines())
        {
        	final String[] parts = kitItem.split(" +");
            try {
				final ItemStack parseStack = plugin.getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1);
				ret.add(parseStack);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
		return ret.toArray(new ItemStack[0]);
	}
}
