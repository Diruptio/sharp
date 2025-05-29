package diruptio.sharp.task;

import diruptio.sharp.data.Palette;
import diruptio.sharp.task.blockpalette.BlockEntityPaletteCreateTask;
import diruptio.sharp.task.blockpalette.BlockEntityPaletteWriteTask;
import diruptio.sharp.task.blockpalette.BlockPaletteCreateTask;
import diruptio.sharp.task.blockpalette.BlockPaletteWriteTask;
import diruptio.sharp.task.chunk.ChunksWriteTask;
import diruptio.sharp.util.BitBuffer;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record WorldWriteTask(@NotNull World world,
                             @NotNull List<Vector2i> chunkCoordinates,
                             @NotNull ObjectOutputStream out,
                             @NotNull CompletableFuture<Void> future) implements Runnable {
    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16, Thread.ofVirtual().factory());

            long chunkLoadStart = System.currentTimeMillis();
            List<Chunk> chunks = new ArrayList<>();
            for (Vector2i coordinate : chunkCoordinates) {
                chunks.add(world.getChunkAt(coordinate.x, coordinate.y));
            }
            debugPerformance("Loaded chunks", chunkLoadStart);

            Future<Palette<BlockData>> blockPaletteFuture = executor.submit(new BlockPaletteCreateTask(
                    executor::submit,
                    chunks));
            Future<Palette<CompoundTag>> blockEntityPaletteFuture = executor.submit(new BlockEntityPaletteCreateTask(
                    executor::submit,
                    chunks));

            Palette<BlockData> blockPalette = blockPaletteFuture.get();
            Future<BitBuffer> writtenBlockPaletteFuture = executor.submit(new BlockPaletteWriteTask(
                    blockPalette));
            Palette<CompoundTag> blockEntityPalette = blockEntityPaletteFuture.get();
            Future<BitBuffer> writtenBlockEntityPaletteFuture = executor.submit(new BlockEntityPaletteWriteTask(
                    blockEntityPalette));
            Future<BitBuffer> writtenChunksFuture = executor.submit(new ChunksWriteTask(
                    executor::submit,
                    chunks,
                    blockPalette,
                    blockEntityPalette));

            out.writeInt(1); // Version
            out.writeInt(world.getEnvironment().getId()); // Environment ID

            BitBuffer writtenBlockPalette = writtenBlockPaletteFuture.get();
            writtenBlockPalette.flip();
            out.writeInt(writtenBlockPalette.getSize());
            out.write(writtenBlockPalette.getBytes(), 0, writtenBlockPalette.getBytes().length);

            BitBuffer writtenBlockEntityPalette = writtenBlockEntityPaletteFuture.get();
            writtenBlockEntityPalette.flip();
            out.writeInt(writtenBlockEntityPalette.getSize());
            out.write(writtenBlockEntityPalette.getBytes(), 0, writtenBlockEntityPalette.getBytes().length);

            BitBuffer writtenChunks = writtenChunksFuture.get();
            writtenChunks.flip();
            out.writeInt(writtenChunks.getSize());
            out.write(writtenChunks.getBytes(), 0, writtenChunks.getBytes().length);

            out.flush();
            out.close();
            executor.close();
            debugPerformance("Wrote world", start);
            future.complete(null);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to write world", e);
        }
    }
}
