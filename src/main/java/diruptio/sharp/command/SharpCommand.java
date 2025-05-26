package diruptio.sharp.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;

public class SharpCommand {
    public static @NotNull LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("sharp")
                .then(SharpLoadFileCommand.create())
                .then(SharpSaveFileCommand.create())
                .build();
    }
}
