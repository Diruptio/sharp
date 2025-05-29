package diruptio.sharp.task.chunk;

import diruptio.sharp.data.Palette;
import diruptio.sharp.task.TaskExecutor;
import diruptio.sharp.task.block.BlocksWriteTask;
import diruptio.sharp.util.BitBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Chunk;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record ChunksWriteTask(@NotNull TaskExecutor executor,
                              @NotNull List<Chunk> chunks,
                              @NotNull Palette<BlockData> blockPalette,
                              @NotNull Palette<CompoundTag> blockEntityPalette) implements Callable<BitBuffer> {
    @Override
    public BitBuffer call() throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();

        List<Future<BitBuffer>> futures = new ArrayList<>();
        for (Chunk chunk : chunks) {
            futures.add(executor.execute(new BlocksWriteTask(chunk, blockPalette, blockEntityPalette)));
        }

        BitBuffer buffer = new BitBuffer();
        buffer.writeInt(chunks.size());
        for (Future<BitBuffer> future : futures) {
            BitBuffer chunkBuffer = future.get();
            chunkBuffer.flip();
            buffer.writeInt(chunkBuffer.getSize());
            buffer.writeBits(chunkBuffer);
        }

        debugPerformance("Wrote chunks", start);
        return buffer;
    }
}
