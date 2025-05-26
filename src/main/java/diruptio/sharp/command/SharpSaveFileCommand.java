package diruptio.sharp.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import diruptio.sharp.Sharp;
import diruptio.sharp.storage.FileStorage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class SharpSaveFileCommand {
    public static @NotNull LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("save-file")
                .requires(stack -> stack.getSender().hasPermission("sharp.save.file"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .requires(stack -> stack.getSender() instanceof Player)
                        .executes(SharpSaveFileCommand::executeWithoutWorldName)
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .executes(SharpSaveFileCommand::executeWithWorldName)))
                .build();
    }

    private static int executeWithoutWorldName(CommandContext<CommandSourceStack> stack) {
        save(stack, stack.getSource().getExecutor().getWorld());
        return Command.SINGLE_SUCCESS;
    }

    private static int executeWithWorldName(CommandContext<CommandSourceStack> stack) {
        save(stack, stack.getArgument("world", World.class));
        return Command.SINGLE_SUCCESS;
    }

    private static void save(CommandContext<CommandSourceStack> stack, World world) {
        CommandSender sender = stack.getSource().getSender();
        String name = stack.getArgument("name", String.class);
        List<Vector2i> chunkCoordinates = new ArrayList<>();
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                chunkCoordinates.add(new Vector2i(x, z));
            }
        }

        Component message = Sharp.getPrefix()
                .append(Component.text("Saving world ").color(NamedTextColor.GRAY))
                .append(Component.text(world.getName()).color(NamedTextColor.YELLOW));
        if (!name.equals(world.getName())) {
            message = message.append(Component.text(" as ").color(NamedTextColor.GRAY))
                    .append(Component.text(name).color(NamedTextColor.YELLOW));
        }
        message = message.append(Component.text(" to file storage").color(NamedTextColor.GRAY));
        sender.sendMessage(message);
        Sharp.writeWorld(name, world, chunkCoordinates, new FileStorage()).thenRun(() -> {
            sender.sendMessage(Sharp.getPrefix()
                    .append(Component.text("Successfully saved world ").color(NamedTextColor.GRAY))
                    .append(Component.text(world.getName()).color(NamedTextColor.YELLOW)));
        });
    }
}
