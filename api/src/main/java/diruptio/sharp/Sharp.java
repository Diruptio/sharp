package diruptio.sharp;

import diruptio.sharp.storage.SharpStorage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public abstract class Sharp {
    private static Sharp instance;

    public abstract @NotNull CompletableFuture<World> read(@NotNull String name, @NotNull SharpStorage storage);
    public abstract @NotNull CompletableFuture<Void> write(@NotNull String name,
                                                           @NotNull World world,
                                                           @NotNull List<Vector2i> chunkCoordinates,
                                                           @NotNull SharpStorage storage);

    public static @NotNull CompletableFuture<World> readWorld(@NotNull String name, @NotNull SharpStorage storage) {
        return getInstance().read(name, storage);
    }

    public static @NotNull CompletableFuture<Void> writeWorld(@NotNull String name,
                                                              @NotNull World world,
                                                              @NotNull List<Vector2i> chunkCoordinates,
                                                              @NotNull SharpStorage storage) {
        return getInstance().write(name, world, chunkCoordinates, storage);
    }

    public static @NotNull Component getPrefix() {
        return Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("🔪").color(NamedTextColor.YELLOW))
                .append(Component.text("]").color(NamedTextColor.DARK_GRAY))
                .appendSpace();
    }

    private static @NotNull Sharp getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Sharp instance is not initialized");
        }
        return instance;
    }

    public static void setInstance(@NotNull Sharp sharp) {
        if (instance != null) {
            throw new IllegalStateException("Sharp instance is already initialized");
        }
        instance = sharp;
    }
}
