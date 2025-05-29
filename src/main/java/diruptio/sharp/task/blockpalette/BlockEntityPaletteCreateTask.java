package diruptio.sharp.task.blockpalette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import diruptio.sharp.data.Palette;
import diruptio.sharp.task.TaskExecutor;
import diruptio.sharp.task.block.BlockEntityCountTask;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import static diruptio.sharp.SharpPlugin.debugPerformance;

public record BlockEntityPaletteCreateTask(@NotNull TaskExecutor executor,
                                           @NotNull List<Chunk> chunks) implements Callable<Palette<CompoundTag>> {
    @Override
    public Palette<CompoundTag> call() throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();

        List<Future<Map<CompoundTag, Integer>>> blockEntityCountFutures = new ArrayList<>();
        for (Chunk chunk : chunks) {
            blockEntityCountFutures.add(executor.execute(new BlockEntityCountTask(chunk)));
        }

        Map<CompoundTag, Integer> blockEntityCount = new HashMap<>();
        for (Future<Map<CompoundTag, Integer>> future : blockEntityCountFutures) {
            for (Map.Entry<CompoundTag, Integer> entry : future.get().entrySet()) {
                int count = blockEntityCount.getOrDefault(entry.getKey(), 0);
                blockEntityCount.put(entry.getKey(), count + entry.getValue());
            }
        }

        List<Map.Entry<CompoundTag, Integer>> entries = new ArrayList<>(blockEntityCount.entrySet());
        entries.sort((v1, v2) -> Integer.compare(v2.getValue(), v1.getValue()));

        CompoundTag[] palette = new CompoundTag[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            palette[i] = entries.get(i).getKey();
        }

        debugPerformance("Created block-entity palette with size " + palette.length, start);
        return new Palette<>(palette);
    }
}
