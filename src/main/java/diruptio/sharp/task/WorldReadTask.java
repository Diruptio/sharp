package diruptio.sharp.task;

import diruptio.sharp.util.EmptyWorldGenerator;
import diruptio.sharp.SharpPlugin;
import diruptio.sharp.data.BlockHeap;
import diruptio.sharp.data.BlockPalette;
import diruptio.sharp.data.BlockType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

public record WorldReadTask(@NotNull String name,
                            @NotNull CompletableFuture<World> future,
                            @NotNull ObjectInputStream in) implements Runnable {
    @Override
    public void run() {
        WorldCreator creator = new WorldCreator(name).generator(new EmptyWorldGenerator());
        World world;
        try {
            world = Bukkit.getScheduler().callSyncMethod(SharpPlugin.getInstance(), creator::createWorld).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create world: " + name, e);
        }

        BlockPalette blockPalette = readBlockPalette();
        readChunks(/*executor, */Objects.requireNonNull(world), blockPalette);

        //executor.close();
        future().complete(world);
    }

    private @NotNull BlockPalette readBlockPalette() {
        try {
            int size = in.readInt();
            BlockType[] blockTypes = new BlockType[size];
            for (int i = 0; i < size; i++) {
                blockTypes[i] = new BlockType(Key.key(in.readUTF()));
            }
            return new BlockPalette(blockTypes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read block palette for world: " + name, e);
        }
    }

    private void readChunks(@NotNull World world, @NotNull BlockPalette blockPalette) {
        try {
            List<Future<?>> chunkFutures = new ArrayList<>();
            int chunkCount = in.readInt();
            for (int i = 0; i < chunkCount; i++) {
                Chunk chunk = world.getChunkAt(in.readInt(), in.readInt());
                BlockHeap[] heaps = new BlockHeap[in.readInt()];
                for (int j = 0; j < heaps.length; j++) {
                    int count = in.readInt();
                    int blockId = in.readInt();
                    heaps[j] = new BlockHeap(count, blockId);
                }
                CompletableFuture<Void> future = new CompletableFuture<>();
                chunkFutures.add(future);
                Thread.startVirtualThread(new ChunkBlocksReadTask(chunk, blockPalette, heaps, future));
            }
            for (Future<?> future : chunkFutures) {
                future.get();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read chunks for world: " + name, e);
        }
    }
}
