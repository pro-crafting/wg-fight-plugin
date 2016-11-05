package de.pro_crafting.wg.commands;

import com.google.common.base.Joiner;

import com.sk89q.intake.CommandException;
import com.sk89q.intake.CommandMapping;
import com.sk89q.intake.Description;
import com.sk89q.intake.Intake;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.fluent.CommandGraph;
import com.sk89q.intake.parametric.Injector;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;

import de.pro_crafting.wg.WarGear;
import de.pro_crafting.wg.commands.util.CommandInfo;
import de.pro_crafting.wg.commands.util.CommandRegistration;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager {
    private WarGear plugin;
    private Dispatcher dispatcher;
    private CommandRegistration commandRegistration;

    public CommandManager(WarGear plugin) {
        this.plugin = plugin;
        this.commandRegistration = new CommandRegistration(this.plugin);

        Injector injector = Intake.createInjector();
        injector.install(new WarGearModule(this.plugin));

        ParametricBuilder builder = new ParametricBuilder(injector);
        builder.setAuthorizer(new PlayerAuthorizer());

        dispatcher = new CommandGraph().builder(builder).commands()
                .group("wgk")
                .registerMethods(new WarGearCommands(this.plugin))
                .group("arena")
                .registerMethods(new ArenaCommands(this.plugin))
                .parent()
                .group("team")
                .registerMethods(new TeamCommands(this.plugin))
                .parent()
                .parent()
                .graph()
                .getDispatcher();

        registerCommands(dispatcher);
    }

    public void executeCommand(CommandSender sender, String[] args) {
        Namespace namespace = new Namespace();
        namespace.put(CommandSender.class, sender);
        try {
            dispatcher.call(Joiner.on(' ').join(args), namespace, Collections.emptyList());
        } catch (CommandException e) {
            if (e.getCause() instanceof WarGearException) {
                sender.sendMessage(e.getCause().getMessage());
            }
        } catch (InvocationCommandException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }
    }

    public void registerCommands(Dispatcher dispatcher) {
        List<CommandInfo> toRegister = new ArrayList<CommandInfo>();
        for (CommandMapping command : dispatcher.getCommands()) {
            Description description = command.getDescription();
            List<String> permissions = description.getPermissions();
            String[] permissionsArray = new String[permissions.size()];
            permissions.toArray(permissionsArray);

            toRegister.add(new CommandInfo(description.getUsage(), description.getHelp(), command.getAllAliases(), this.dispatcher, permissionsArray));
        }

        commandRegistration.register(toRegister);
    }
}
