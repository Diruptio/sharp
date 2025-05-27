package diruptio.sharp.task;

import diruptio.sharp.data.BlockPalette;
import diruptio.sharp.data.BlockType;
import diruptio.sharp.util.BitBuffer;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.*;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import static diruptio.sharp.SharpPlugin.debug;

public record WorldWriteTask(@NotNull World world,
                             @NotNull List<Vector2i> chunkCoordinates,
                             @NotNull CompletableFuture<Void> future,
                             @NotNull ObjectOutputStream out) implements Runnable {
    @Override
    public void run() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(9, Thread.ofVirtual().factory());
        debug("Starting to write world: " + world.getName());

        long start = System.currentTimeMillis();
        List<Chunk> chunks = new ArrayList<>();
        for (Vector2i coordinate : chunkCoordinates) {
            chunks.add(world.getChunkAt(coordinate.x, coordinate.y));
        }
        debug("Loaded chunks for world: " + world.getName() + " in " + (System.currentTimeMillis() - start) + "ms");

        BlockPalette blockPalette = createBlockPalette(executor, chunks);
        debug("Created block palette for world: " + world.getName() + " in " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        writeBlockPalette(blockPalette);
        debug("Wrote block palette for world: " + world.getName() + " in " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        writeChunks(executor, chunks, blockPalette);
        debug("Wrote chunks for world: " + world.getName() + " in " + (System.currentTimeMillis() - start) + "ms");

        try {
            executor.close();
            out.flush();
            out.close();
            future.complete(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to close output stream of world: " + world.getName(), e);
        }
    }

    private @NotNull BlockPalette createBlockPalette(@NotNull ExecutorService executor, @NotNull List<Chunk> chunks) {
        long start = System.currentTimeMillis();
        List<Future<Map<BlockType, Integer>>> blockPaletteFutures = new ArrayList<>();
        for (Chunk chunk : chunks) {
            blockPaletteFutures.add(executor.submit(new ChunkCreateBlockPaletteTask(chunk)));
        }
        Map<BlockType, Integer> blockMap = new HashMap<>();
        for (Future<Map<BlockType, Integer>> future : blockPaletteFutures) {
            try {
                Map<BlockType, Integer> chunkBlockMap = future.get();
                for (Map.Entry<BlockType, Integer> entry : chunkBlockMap.entrySet()) {
                    blockMap.put(entry.getKey(), blockMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to create block palette for a chunk in world: " + world.getName(), e);
            }
        }
        debug("Created block map for world: " + world.getName() + " in " + (System.currentTimeMillis() - start) + "ms");

        List<Map.Entry<BlockType, Integer>> blockList = new ArrayList<>(blockMap.entrySet());
        blockList.sort((v1, v2) -> Integer.compare(v2.getValue(), v1.getValue()));

        BlockType[] palette = new BlockType[blockList.size()];
        for (int i = 0; i < blockList.size(); i++) {
            palette[i] = blockList.get(i).getKey();
        }
        return new BlockPalette(palette);
    }

    private void writeBlockPalette(@NotNull BlockPalette blockPalette) {
        try {
            out.writeInt(blockPalette.getSize());
            for (int i = 0; i < blockPalette.getSize(); i++) {
                out.writeUTF(blockPalette.getBlockType(i).key().asString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write block palette for world: " + world.getName(), e);
        }
    }

    private void writeChunks(@NotNull ExecutorService executor, @NotNull List<Chunk> chunks, @NotNull BlockPalette blockPalette) {
        List<Future<BitBuffer>> chunkFutures = new ArrayList<>();
        for (Chunk chunk : chunks) {
            chunkFutures.add(executor.submit(new ChunkWriteTask(chunk, blockPalette)));
        }

        try {
            out.writeInt(chunks.size());
            for (Future<BitBuffer> future : chunkFutures) {
                BitBuffer buffer = future.get();
                out.writeInt(buffer.getSize());
                out.write(buffer.getBytes());
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to write chunks of world: " + world.getName(), e);
        }
    }
}
