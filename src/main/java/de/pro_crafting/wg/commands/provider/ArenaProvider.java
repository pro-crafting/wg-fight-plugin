package de.pro_crafting.wg.commands.provider;

import com.google.common.collect.ImmutableList;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.commands.WarGearException;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.Nullable;


public class ArenaProvider implements Provider<Arena> {
    private WarGear plugin;

    public ArenaProvider(WarGear plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public Arena get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        CommandSender sender = arguments.getNamespace().get(CommandSender.class);
        String arenaName = arguments.getFlags().get('a');
        Arena ret = null;
        if (arenaName != null) {
            ret = plugin.getArenaManager().getArena(arenaName);
            if (ret != null) {
                return ret;
            }
        }
        if (!(sender instanceof ConsoleCommandSender)) {
            if (sender instanceof Player) {
                ret = plugin.getArenaManager().getArenaAt(((Player) sender).getLocation());
            } else if (sender instanceof BlockCommandSender) {
                ret = plugin.getArenaManager().getArenaAt(((BlockCommandSender) sender).getBlock().getLocation());
            }
        }

        if (ret == null) {
            throw new WarGearException("§cDu stehst in keiner Arena, oder Sie existiert nicht.");
        }

        return ret;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}