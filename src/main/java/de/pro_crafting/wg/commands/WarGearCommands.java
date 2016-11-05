package de.pro_crafting.wg.commands;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.arena.Arena;
import de.pro_crafting.wg.arena.State;
import de.pro_crafting.wg.commands.provider.Named;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarGearCommands {
    private WarGear plugin;

    public WarGearCommands(WarGear plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"help", ""}, desc = "Zeigt die Hilfe an.")
    @Require("wargear.help")
    public void WarGear(CommandSender sender) {
        sender.sendMessage("§c§LKein passender Befehl gefunden!");
        sender.sendMessage("§B/wgk team ...");
        sender.sendMessage("§B/wgk arena ...");
        sender.sendMessage("§B/wgk kit <kitName>");
        sender.sendMessage("§B/wgk warp <arenaname> [playername]");
        sender.sendMessage("§B/wgk quit [team1|team2]");
        sender.sendMessage("§B/wgk reload");
        sender.sendMessage("§B/wgk start");
    }

    @Command(aliases = {"reload"}, desc = "Reloadet die Config.")
    @Require("wargear.reload")
    public void reload(CommandSender sender) {
        this.plugin.reloadConfig();
        this.plugin.getServer().getPluginManager().disablePlugin(plugin);
        this.plugin.getServer().getPluginManager().enablePlugin(plugin);
        sender.sendMessage("Plugin wurde gereloadet.");
    }

    @Command(aliases = {"warp"}, desc = "Teleport zu der Arena.", min = 1, max = 2)
    @Require("wargear.warp")
    public void warp(CommandSender sender, @Named Arena arena, @Optional Player player) {
        Player toWarp = sender instanceof Player ? (Player) sender : null;
        if (player != null) {
            if (!sender.hasPermission("wargear.warp.other")) {
                sender.sendMessage("§cDu hast keine Rechte dafür.");
                return;
            }
            toWarp = player;
        }

        if (toWarp == null) {
            sender.sendMessage("§cEs muss ein Spieler angegeben werden.");
            return;
        }

        arena.teleport(toWarp);
    }

    /*@Command(aliases = {"kit"}, desc = "Legt das Kit für den Fight fest.", min = 1, max = 1)
    @Require("wargear.kit")
    public void kit(CommandSender sender, Arena arena, @Kit String kitName) {
        if (arena.getState() != State.Setup) {
            sender.sendMessage("§cEs muss bereits mindestens ein Team geben.");
            return;
        }
        arena.setKit(kitName);
    }

    @Command(aliases = {"quit"}, desc = "Beendet einen Fight.", min = 1)
    @Require("wargear.quit")
    public void quit(CommandSender sender, Arena arena, @Winner String winner) {
        if (arena.getState() != State.PreRunning && arena.getState() != State.Running) {
            sender.sendMessage("§cIn dieser Arena läuft kein Fight.");
            return;
        }

        FightQuitEvent event = null;
        if (winner == null) {
            event = new DrawQuitEvent(arena, "Unentschieden", arena.getGroupManager().getGroup1(), arena.getGroupManager().getGroup2(), FightQuitReason.FightLeader);
            this.plugin.getServer().getPluginManager().callEvent(event);
        } else if (winner.equalsIgnoreCase("team1")) {
            event = new WinQuitEvent(arena, "", arena.getGroupManager().getGroup1(), arena.getGroupManager().getGroup2(), FightQuitReason.FightLeader);
            this.plugin.getServer().getPluginManager().callEvent(event);
        } else if (winner.equalsIgnoreCase("team2")) {
            event = new WinQuitEvent(arena, "", arena.getGroupManager().getGroup2(), arena.getGroupManager().getGroup1(), FightQuitReason.FightLeader);
        }
        if (event != null) {
            this.plugin.getServer().getPluginManager().callEvent(event);
        }
    }*/

    @Command(aliases = {"start"}, desc = "Startet einen Fight.")
    @Require("wargear.start")
    public void start(CommandSender sender, Arena arena) {
        if (arena.getState() != State.Setup) {
            sender.sendMessage("§cHier kann kein Fight gestartet werden.");
            return;
        }

        if (arena.getGroupManager().getGroup1().getMembers().size() == 0 || arena.getGroupManager().getGroup2().getMembers().size() == 0) {
            sender.sendMessage("§cBeide Teams müssen einen Spieler haben.");
            return;
        }

        arena.startFight(sender);
    }
}
