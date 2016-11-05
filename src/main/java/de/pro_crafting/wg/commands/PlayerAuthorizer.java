package de.pro_crafting.wg.commands;

import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.util.auth.Authorizer;

import org.bukkit.command.CommandSender;

public class PlayerAuthorizer implements Authorizer {
    @Override
    public boolean testPermission(Namespace namespace, String permission) {
        System.out.println("permission: " + permission);
        CommandSender commandSender = namespace.get(CommandSender.class);
        return commandSender.hasPermission(permission);
    }
}
