package diruptio.sharp.task.blockpalette;

import diruptio.sharp.data.Palette;
import diruptio.sharp.task.TaskExecutor;
import diruptio.sharp.task.block.BlockCountTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bukkit.Chunk;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record BlockPaletteCreateTask(@NotNull TaskExecutor executor,
                                     @NotNull List<Chunk> chunks) implements Callable<Palette<BlockData>> {
    @Override
    public Palette<BlockData> call() throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();

        List<Future<Map<BlockData, Integer>>> blockCountFutures = new ArrayList<>();
        for (Chunk chunk : chunks) {
            blockCountFutures.add(executor.execute(new BlockCountTask(chunk)));
        }

        Map<BlockData, Integer> blockCount = new HashMap<>();
        for (Future<Map<BlockData, Integer>> future : blockCountFutures) {
            for (Map.Entry<BlockData, Integer> entry : future.get().entrySet()) {
                int count = blockCount.getOrDefault(entry.getKey(), 0);
                blockCount.put(entry.getKey(), count + entry.getValue());
            }
        }

        List<Map.Entry<BlockData, Integer>> entries = new ArrayList<>(blockCount.entrySet());
        entries.sort((v1, v2) -> Integer.compare(v2.getValue(), v1.getValue()));

        BlockData[] palette = new BlockData[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            palette[i] = entries.get(i).getKey();
        }

        debugPerformance("Created block palette with size " + palette.length, start);
        return new Palette<>(palette);
    }
}
