package diruptio.sharp;

import diruptio.sharp.command.SharpCommand;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SharpPlugin extends JavaPlugin {
    private static SharpPlugin instance;

    @Override
    public void onLoad() {
        instance = this;
        Sharp.setInstance(new SharpImpl());
    }

    @Override
    public void onEnable() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::registerCommands);
    }

    private void registerCommands(@NotNull ReloadableRegistrarEvent<@NotNull Commands> event) {
        event.registrar().register(SharpCommand.create(), "Command for managing sharp worlds");
    }

    public static @NotNull SharpPlugin getInstance() {
        return instance;
    }
}
