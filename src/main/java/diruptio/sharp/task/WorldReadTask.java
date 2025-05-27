package diruptio.sharp.task;

import diruptio.sharp.util.BitBuffer;
import diruptio.sharp.util.EmptyWorldGenerator;
import diruptio.sharp.SharpPlugin;
import diruptio.sharp.data.BlockPalette;
import diruptio.sharp.data.BlockType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debug;

public record WorldReadTask(@NotNull String name,
                            @NotNull CompletableFuture<World> future,
                            @NotNull ObjectInputStream in) implements Runnable {
    @Override
    public void run() {
        debug("Starting to read world: " + name);

        WorldCreator creator = new WorldCreator(name).generator(new EmptyWorldGenerator());
        try {
            int version = in.readInt();
            if (version != 1) {
                throw new IOException("Unsupported world version: " + version);
            }
            creator.environment(Objects.requireNonNull(World.Environment.getEnvironment(in.readInt())));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read metadata for world: " + name, e);
        }

        long start = System.currentTimeMillis();
        World world;
        try {
            world = Bukkit.getScheduler().callSyncMethod(SharpPlugin.getInstance(), creator::createWorld).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create world: " + name, e);
        }
        debug("World " + name + " created in " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        BlockPalette blockPalette = readBlockPalette();
        debug("Block palette for world " + name + " read in " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        readChunks(Objects.requireNonNull(world), blockPalette);
        debug("Chunks for world " + name + " read in " + (System.currentTimeMillis() - start) + "ms");

        future.complete(world);
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
            long start = System.currentTimeMillis();
            List<Future<?>> chunkFutures = new ArrayList<>();
            int chunkCount = in.readInt();
            for (int i = 0; i < chunkCount; i++) {
                BitBuffer buffer = new BitBuffer(in.readInt());
                in.readFully(buffer.getBytes());
                buffer.setSize(buffer.getCapacity());

                CompletableFuture<Void> future = new CompletableFuture<>();
                chunkFutures.add(future);
                Thread.startVirtualThread(new ChunkBlocksReadTask(world, blockPalette, buffer, future));
            }
            debug("Read chunk data for world " + name + " in " + (System.currentTimeMillis() - start) + "ms");
            for (Future<?> future : chunkFutures) {
                future.get();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read chunks for world: " + name, e);
        }
    }
}
