package diruptio.sharp.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import diruptio.sharp.Sharp;
import diruptio.sharp.storage.FileStorage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SharpLoadFileCommand {
    public static @NotNull LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("load-file")
                .requires(stack -> stack.getSender().hasPermission("sharp.load.file"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(SharpLoadFileCommand::execute))
                .build();
    }

    private static int execute(CommandContext<CommandSourceStack> stack) {
        CommandSender sender = stack.getSource().getSender();
        String name = stack.getArgument("name", String.class);
        FileStorage storage = new FileStorage();

        if (Bukkit.getWorld(name) != null) {
            sender.sendMessage(Sharp.getPrefix()
                    .append(Component.text("The world ").color(NamedTextColor.RED))
                    .append(Component.text(name).color(NamedTextColor.YELLOW))
                    .append(Component.text(" is already loaded").color(NamedTextColor.RED)));
            return Command.SINGLE_SUCCESS;
        }

        if (!storage.exists(name)) {
            sender.sendMessage(Sharp.getPrefix()
                    .append(Component.text("The world ").color(NamedTextColor.RED))
                    .append(Component.text(name).color(NamedTextColor.YELLOW))
                    .append(Component.text(" does not exist").color(NamedTextColor.RED)));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(Sharp.getPrefix()
                .append(Component.text("Loading world ").color(NamedTextColor.GRAY))
                .append(Component.text(name).color(NamedTextColor.YELLOW))
                .append(Component.text(" from file storage").color(NamedTextColor.GRAY)));
        Sharp.readWorld(name, storage).thenAccept(world -> {
            sender.sendMessage(Sharp.getPrefix()
                    .append(Component.text("Successfully loaded world ").color(NamedTextColor.GRAY))
                    .append(Component.text(name).color(NamedTextColor.YELLOW)));
        });

        return Command.SINGLE_SUCCESS;
    }
}
