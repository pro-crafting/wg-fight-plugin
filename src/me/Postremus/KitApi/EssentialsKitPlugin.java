package me.Postremus.KitApi;

import java.util.List;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.User;

public class EssentialsKitPlugin implements IKitPlugin
{

	private Server server;
	@Override
	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public boolean existsKit(String kitName) {
		kitName = kitName.toLowerCase();
		User u = this.getPlugin().getOfflineUser("eskit");
		String kits;
		try {
			kits = Kit.listKits(this.getPlugin(), u);
			return kits.contains(kitName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
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
		Plugin wgPlugin = server.getPluginManager().getPlugin("Essentials");
		 
	    if (wgPlugin == null || !(wgPlugin instanceof IEssentials)) {
	        return null; 
	    }
	 
	    return (Essentials) wgPlugin;
	}
}
