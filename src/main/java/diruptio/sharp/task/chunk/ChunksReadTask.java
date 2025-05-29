package diruptio.sharp.task.chunk;

import diruptio.sharp.data.Palette;
import diruptio.sharp.task.TaskExecutor;
import diruptio.sharp.task.block.BlocksReadTask;
import diruptio.sharp.util.BitBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record ChunksReadTask(@NotNull TaskExecutor executor,
                             @NotNull World world,
                             @NotNull Palette<BlockData> blockPalette,
                             @NotNull Palette<CompoundTag> blockEntityPalette,
                             @NotNull BitBuffer buffer) implements Runnable {
    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();

            List<Future<Void>> chunkFutures = new ArrayList<>();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                BitBuffer chunkBuffer = buffer.readBits(buffer.readInt());
                chunkFutures.add(executor.execute(new BlocksReadTask(world, blockPalette, blockEntityPalette, chunkBuffer)));
            }
            for (Future<?> future : chunkFutures) {
                future.get();
            }

            debugPerformance("Read chunks", start);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read chunks", e);
        }
    }
}
