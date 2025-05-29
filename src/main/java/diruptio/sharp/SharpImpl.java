package diruptio.sharp;

import diruptio.sharp.task.WorldReadTask;
import diruptio.sharp.task.WorldWriteTask;
import diruptio.sharp.storage.SharpStorage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class SharpImpl extends Sharp {
    public @NotNull CompletableFuture<World> read(@NotNull String name, @NotNull SharpStorage storage) {
        if (Bukkit.getWorld(name) != null) {
            throw new IllegalStateException("The world \"" + name + "\" is already loaded");
        }
        CompletableFuture<World> future = new CompletableFuture<>();
        ObjectInputStream in = storage.openInputStream(name);
        Thread.ofVirtual().name("SharpReader-" + name).start(new WorldReadTask(name, future, in));
        return future;
    }

    public @NotNull CompletableFuture<Void> write(@NotNull String name,
                                                  @NotNull World world,
                                                  @NotNull List<Vector2i> chunkCoordinates,
                                                  @NotNull SharpStorage storage) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ObjectOutputStream out = storage.openOutputStream(name);
        Thread.ofVirtual().name("SharpWriter-" + world.getName()).start(new WorldWriteTask(world, chunkCoordinates, out, future));
        return future;
    }
}
