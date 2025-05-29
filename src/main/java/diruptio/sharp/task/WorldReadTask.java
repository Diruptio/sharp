package diruptio.sharp.task;

import diruptio.sharp.task.blockpalette.BlockEntityPaletteReadTask;
import diruptio.sharp.task.blockpalette.BlockPaletteReadTask;
import diruptio.sharp.task.chunk.ChunksReadTask;
import diruptio.sharp.util.BitBuffer;
import diruptio.sharp.util.EmptyWorldGenerator;
import diruptio.sharp.SharpPlugin;
import diruptio.sharp.data.Palette;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.concurrent.*;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record WorldReadTask(@NotNull String name,
                            @NotNull CompletableFuture<World> future,
                            @NotNull ObjectInputStream in) implements Runnable {
    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16, Thread.ofVirtual().factory());

            int version = in.readInt();
            if (version != 1) {
                throw new RuntimeException("Unsupported world version: " + version);
            }

            WorldCreator creator = new WorldCreator(name).generator(new EmptyWorldGenerator());
            creator.environment(Objects.requireNonNull(World.Environment.getEnvironment(in.readInt())));
            Future<World> worldFuture = Bukkit.getScheduler().callSyncMethod(SharpPlugin.getInstance(), creator::createWorld);

            BitBuffer blockPaletteBuffer = BitBuffer.fromObjectInputStream(in, in.readInt());
            Future<Palette<BlockData>> blockPaletteFuture = executor.submit(new BlockPaletteReadTask(blockPaletteBuffer));

            BitBuffer blockEntityPaletteBuffer = BitBuffer.fromObjectInputStream(in, in.readInt());
            Future<Palette<CompoundTag>> blockEntityPaletteFuture = executor.submit(new BlockEntityPaletteReadTask(blockEntityPaletteBuffer));

            BitBuffer chunksBuffer = BitBuffer.fromObjectInputStream(in, in.readInt());
            World world = worldFuture.get();
            Palette<BlockData> blockPalette = blockPaletteFuture.get();
            Palette<CompoundTag> blockEntityPalette = blockEntityPaletteFuture.get();
            new ChunksReadTask(executor::submit, world, blockPalette, blockEntityPalette, chunksBuffer).run();

            executor.close();
            debugPerformance("World read", start);
            future.complete(world);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
